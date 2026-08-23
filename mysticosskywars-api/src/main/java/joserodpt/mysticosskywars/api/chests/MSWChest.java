package joserodpt.mysticosskywars.api.chests;

/*
 *   _____            _  _____ _
 *  |  __ \          | |/ ____| |
 *  | |__) |___  __ _| | (___ | | ___   ___      ____ _ _ __ ___
 *  |  _  // _ \/ _` | |\___ \| |/ / | | \ \ /\ / / _` | '__/ __|
 *  | | \ \  __/ (_| | |____) |   <| |_| |\ V  V / (_| | |  \__ \
 *  |_|  \_\___|\__,_|_|_____/|_|\_\\__, | \_/\_/ \__,_|_|  |___/
 *                                   __/ |
 *                                  |___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/MysticosSkywars
 */

import com.google.common.collect.ImmutableMap;
import dev.dejvokep.boostedyaml.YamlDocument;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.chests.BasicChestConfig;
import joserodpt.mysticosskywars.api.config.chests.EPICChestConfig;
import joserodpt.mysticosskywars.api.config.chests.NormalChestConfig;
import joserodpt.mysticosskywars.api.managers.holograms.MSWHologram;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.MSWMapEvent;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.CountdownTimer;
import joserodpt.mysticosskywars.api.utils.ItemStackSpringer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class MSWChest {
    private final int x, y, z;
    private final String worldName;
    private final Type type;
    private final BlockFace bf;
    private final MSWHologram hologram;
    private List<MSWChestItem> items = new ArrayList<>();
    private Boolean opened = false;
    private int maxItemsPerChest;
    private CountdownTimer chestCTD;

    public MSWChest(Type ct, Location l, BlockFace bf) {
        this.type = ct;
        this.x = l.getBlockX();
        this.y = l.getBlockY();
        this.z = l.getBlockZ();
        this.worldName = Objects.requireNonNull(l.getWorld()).getName();
        this.bf = bf;
        this.hologram = MysticosSkywarsAPI.getInstance().getHologramManagerAPI().getHologramInstance();
        this.clear();
    }

    public MSWChest(Type ct, String worldName, int x, int y, int z, BlockFace bf) {
        this.type = ct;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.bf = bf;
        this.hologram = MysticosSkywarsAPI.getInstance().getHologramManagerAPI().getHologramInstance();
        this.clear();
    }

    @Override
    public String toString() {
        return "SWChest{" + "x=" + x + ", y=" + y + ", z=" + z + ", worldName='" + worldName + '\'' + ", items=" + items + ", type=" + type + ", opened=" + opened + ", maxItemsPerChest=" + maxItemsPerChest + ", chestCTD=" + chestCTD + '}';
    }

    public void setLoot(List<MSWChestItem> chest, int maxItemsPerChest) {
        this.items.clear();
        this.items = chest;
        this.maxItemsPerChest = maxItemsPerChest;
    }

    public Type getType() {
        return this.type;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(this.worldName), this.x, this.y, this.z);
    }

    public void clear() {
        this.cancelTasks();
        if (this.hologram != null) {
            this.hologram.deleteHologram();
        }
        this.setChest();
        ((Chest) this.getChestBlock().getState()).getInventory().clear();
        this.opened = false;
    }

    public void setChest() {
        this.getLocation().getWorld().getBlockAt(this.getLocation()).setType(Material.CHEST);
        Block b = this.getLocation().getWorld().getBlockAt(this.getLocation());
        BlockData blockData = b.getBlockData();
        ((Directional) blockData).setFacing(this.bf);
        b.setBlockData(blockData);
    }

    public Block getChestBlock() {
        return this.getLocation().getBlock();
    }

    public Boolean isChest() {
        return this.getLocation().getBlock().getType().equals(Material.CHEST);
    }

    public void populate() {
        if (!isOpened() && isChest()) {
            this.opened = true;
            Inventory inv = ((Chest) getChestBlock().getState()).getInventory();

            List<ItemStack> tmp = new ArrayList<>();
            for (MSWChestItem item : this.items) {
                int chance = MysticosSkywarsAPI.getInstance().getRandom().nextInt(100);
                if (chance < item.getChance()) {
                    tmp.add(item.getItemStack());
                }
            }

            Collections.shuffle(tmp);
            if (tmp.size() > this.maxItemsPerChest) {
                tmp = tmp.subList(0, this.maxItemsPerChest);
            }

            if (MSWConfig.file().getBoolean("Config.Shuffle-Items-In-Chest")) {
                boolean[] chosen = new boolean[inv.getSize()];
                for (ItemStack itemStack : tmp) {
                    int slot;

                    do {
                        slot = MysticosSkywarsAPI.getInstance().getRandom().nextInt(inv.getSize());
                    } while (chosen[slot]);

                    chosen[slot] = true;
                    inv.setItem(MysticosSkywarsAPI.getInstance().getRandom().nextInt(inv.getSize()), itemStack);
                }
            } else {
                tmp.forEach(inv::addItem);
            }
        }
    }

    public void startTasks(MSWMap sgm) {
        if (this.chestCTD == null && this.isChest()) {
            int time = MSWConfig.file().getInt("Config.Default-Refill-Time");

            Optional<MSWMapEvent> e = getRefillTime(sgm);
            if (e.isPresent()) {
                time = e.get().getTimeLeft();
            }

            this.hologram.spawnHologram(this.getLocation());
            this.hologram.setTime(time);

            this.chestCTD = new CountdownTimer(MysticosSkywarsAPI.getInstance().getPlugin(), time, () -> {
                //
            }, () -> {
                this.getLocation().getWorld().spawnParticle(Particle.CLOUD, this.getLocation().add(0.5, 0, 0.5), 5);
                if (this.isChest() && !MSWConfig.file().getBoolean("Config.Disable-Chest-Animation")) {
                    MysticosSkywarsAPI.getInstance().getNMS().playChestAnimation(this.getChestBlock(), false);
                }
                this.clear();
                this.hologram.deleteHologram();
            }, (t) -> {
                this.hologram.setTime(t.getSecondsLeft());
                if (this.isChest() && !MSWConfig.file().getBoolean("Config.Disable-Chest-Animation")) {
                    MysticosSkywarsAPI.getInstance().getNMS().playChestAnimation(this.getChestBlock(), true);
                }
            });

            this.chestCTD.scheduleTimer();
        }
    }

    private Optional<MSWMapEvent> getRefillTime(MSWMap sgm) {
        return sgm.getEvents().stream().filter(c -> c.getEventType().equals(MSWMapEvent.EventType.REFILL)).findFirst();
    }

    public void cancelTasks() {
        if (this.chestCTD != null) {
            this.chestCTD.killTask();
            this.chestCTD = null;
        }
    }

    public boolean isOpened() {
        return this.opened;
    }

    public void clearHologram() {
        this.hologram.deleteHologram();
    }

    public enum Tier {
        BASIC, NORMAL, EPIC;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case BASIC:
                    return TranslatableLine.VOTE_CHEST_BASIC.get(p);
                case NORMAL:
                    return TranslatableLine.VOTE_CHEST_NORMAL.get(p);
                case EPIC:
                    return TranslatableLine.VOTE_CHEST_EPIC.get(p);
                default:
                    return "?";
            }
        }

        public YamlDocument getConfig() {
            switch (this) {
                case BASIC:
                    return BasicChestConfig.file();
                case NORMAL:
                    return NormalChestConfig.file();
                case EPIC:
                    return EPICChestConfig.file();
                default:
                    return null;
            }
        }

        public int getMaxItemsPerChest() {
            return this.getConfig() == null ? 0 : this.getConfig().getInt("Max-Itens-Per-Chest");
        }

        public List<MSWChestItem> getChest(MSWChest.Type type) {
            return Objects.requireNonNull(this.getConfig()).getMapList(type.getConfigName()).stream()
                    .map(this::createSWChestItemFromMap)
                    .collect(Collectors.toList());
        }

        private MSWChestItem createSWChestItemFromMap(Map<?, ?> map) {
            ItemStack itemStack = ItemStackSpringer.getItemDeSerialized(ImmutableMap.copyOf((Map<String, Object>) map.get("Item")));
            Integer chance = (Integer) map.get("Chance");
            return new MSWChestItem(itemStack, chance);
        }

        public void set2ChestRaw(MSWChest.Type type, List<MSWChestItem> itens) throws IOException {
            List<Map<String, Object>> map = itens.stream().map(swChestItem -> ImmutableMap.of("Chance", swChestItem.getChance(), "Item", ItemStackSpringer.getItemSerialized(swChestItem.getItemStack()))).collect(Collectors.toList());

            this.getConfig().set(type.getConfigName(), map);
            this.getConfig().save();
        }

        public void set2Chest(MSWChest.Type type, List<ItemStack> itens) throws IOException {
            List<Map<String, Object>> map = itens.stream().map(itemStack -> ImmutableMap.of("Chance", 50, "Item", ItemStackSpringer.getItemSerialized(itemStack))).collect(Collectors.toList());

            this.getConfig().set(type.getConfigName(), map);
            this.getConfig().save();
        }
    }

    public enum Type {
        NORMAL, MID;

        public String getConfigName() {
            return (this == MID) ? "Mid-Items" : "Items";
        }
    }
}
