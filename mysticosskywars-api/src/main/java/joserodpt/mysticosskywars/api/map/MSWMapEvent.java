package joserodpt.mysticosskywars.api.map;

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

import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.ItemStackSpringer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MSWMapEvent {

    private final EventType eventType;
    private final MSWMap room;
    private int time;

    public MSWMapEvent(MSWMap room, EventType eventType, int time) {
        this.room = room;
        this.eventType = eventType;
        this.time = time;
    }

    public MSWMapEvent(MSWMap map, EventType eventType) {
        this(map, eventType, 30);
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public String getName() {
        return Text.color(this.eventType.getName() + " " + Text.formatSeconds(this.getTimeLeft()));
    }

    public int getTimeLeft() {
        return (this.room.getMaxGameTime() - (this.room.getMaxGameTime() - this.getTime())) - this.room.getTimePassed();
    }

    public void tick() {
        if (this.getTimeLeft() == 0) {
            execute();
            this.room.getEvents().remove(this);
        }
    }

    public int getTime() {
        return this.time;
    }

    public void execute() {
        switch (this.eventType) {
            case REFILL:
                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle(TranslatableList.REFILL_EVENT_TITLE.get(mswPlayer).get(0), TranslatableList.REFILL_EVENT_TITLE.get(mswPlayer).get(1), 4, 10, 4));
                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.BLOCK_CHEST_LOCKED, 50, 50));
                break;
            case TNTRAIN:
                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle(TranslatableList.TNTRAIN_EVENT_TITLE.get(mswPlayer).get(0), TranslatableList.TNTRAIN_EVENT_TITLE.get(mswPlayer).get(1), 4, 10, 4));
                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.ENTITY_TNT_PRIMED, 50, 50));
                this.room.getPlayers().forEach(player -> player.spawnAbovePlayer(TNTPrimed.class));
                break;
            case BORDERSHRINK:
                this.room.getBossBar().setDeathMatch();

                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle("", TranslatableLine.TITLE_DEATHMATCH.get(mswPlayer), 10, 20, 5));
                this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 50, 50));

                int factor = Math.max(1, MSWConfig.file().getInt("Config.Death-Match-Shrink-Factor", 2));

                this.room.getBorder().setSize((double) this.room.getBorderSize() / factor, 30L);
                this.room.getBorder().setCenter(this.room.getMapCuboid().getCenter());
                break;
            case LUCKYBLOCK_SPAWN:
                executeLuckyBlockSpawn();
                break;
            case LUCKYBLOCK_RAIN:
                executeLuckyBlockRain();
                break;
            case LUCKYBLOCK_TREASURE:
                executeLuckyBlockTreasure();
                break;
        }
    }

    private void executeLuckyBlockSpawn() {
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle(TranslatableList.LUCKYBLOCK_SPAWN_EVENT_TITLE.get(mswPlayer).get(0), TranslatableList.LUCKYBLOCK_SPAWN_EVENT_TITLE.get(mswPlayer).get(1), 4, 10, 4));
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 50, 50));

        List<String> blockNames = MSWConfig.file().getStringList("Config.LuckyBlock.Blocks");
        Material luckyMat = Material.SPONGE;
        if (!blockNames.isEmpty()) {
            try {
                luckyMat = Material.valueOf(blockNames.get(0).trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int count = MSWConfig.file().getInt("Config.LuckyBlock.Events.Spawn-Amount", 5);
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            for (MSWPlayer p : this.room.getPlayers()) {
                if (p.getPlayer() == null) continue;
                Location center = p.getPlayer().getLocation();
                int ox = rand.nextInt(-8, 9);
                int oz = rand.nextInt(-8, 9);
                Location target = center.clone().add(ox, 0, oz);
                target.setY(this.room.getMSWWorld().getWorld().getHighestBlockYAt(target) + 1);

                if (target.getBlock().getType() == Material.AIR || target.getBlock().getType() == Material.CAVE_AIR) {
                    target.getBlock().setType(luckyMat);
                }
            }
        }
    }

    private void executeLuckyBlockRain() {
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle(TranslatableList.LUCKYBLOCK_RAIN_EVENT_TITLE.get(mswPlayer).get(0), TranslatableList.LUCKYBLOCK_RAIN_EVENT_TITLE.get(mswPlayer).get(1), 4, 10, 4));
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.ENTITY_ENDER_DRAGON_FLAP, 50, 50));

        List<String> blockNames = MSWConfig.file().getStringList("Config.LuckyBlock.Blocks");
        Material luckyMat = Material.SPONGE;
        if (!blockNames.isEmpty()) {
            try {
                luckyMat = Material.valueOf(blockNames.get(0).trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        int count = MSWConfig.file().getInt("Config.LuckyBlock.Events.Rain-Amount", 15);
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            for (MSWPlayer p : this.room.getPlayers()) {
                if (p.getPlayer() == null) continue;
                Location center = p.getPlayer().getLocation();
                int ox = rand.nextInt(-6, 7);
                int oz = rand.nextInt(-6, 7);
                int oy = rand.nextInt(8, 20);
                Location target = center.clone().add(ox, oy, oz);

                Block below = target.clone().subtract(0, 1, 0).getBlock();
                if (below.getType() != Material.AIR && below.getType() != Material.CAVE_AIR) {
                    target.getBlock().setType(luckyMat);
                    this.room.getMSWWorld().getWorld().spawnParticle(Particle.CLOUD, target.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.01);
                }
            }
        }
    }

    private void executeLuckyBlockTreasure() {
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.sendTitle(TranslatableList.LUCKYBLOCK_TREASURE_EVENT_TITLE.get(mswPlayer).get(0), TranslatableList.LUCKYBLOCK_TREASURE_EVENT_TITLE.get(mswPlayer).get(1), 4, 10, 4));
        this.room.getAllPlayers().forEach(mswPlayer -> mswPlayer.playSound(Sound.ENTITY_PLAYER_LEVELUP, 50, 50));

        List<String> blockNames = MSWConfig.file().getStringList("Config.LuckyBlock.Blocks");
        String luckyBlockId = blockNames.isEmpty() ? "SPONGE" : blockNames.get(0).trim();

        for (MSWPlayer p : this.room.getPlayers()) {
            if (p.getPlayer() == null) continue;
            Map<String, Object> data = new HashMap<>();
            data.put("MATERIAL", luckyBlockId.startsWith("ITEMSADDER:") ? luckyBlockId : luckyBlockId.toUpperCase());
            data.put("AMOUNT", 1);
            data.put("NAME", "&e&lLucky Block");
            try {
                ItemStack item = ItemStackSpringer.getItemDeSerialized(data);
                if (item != null) {
                    p.getPlayer().getInventory().addItem(item);
                    p.getPlayer().getWorld().spawnParticle(Particle.TOTEM, p.getPlayer().getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public String serialize() {
        return this.eventType.name() + "@" + this.time;
    }

    public ItemStack getItem() {
        return Itens.createItem(this.getEventType().getIcon(), 1, this.getEventType().getName() + " &r&f@ &b" + Text.formatSeconds(this.getTimeLeft()), Text.color(Arrays.asList("&a&nLeft-Click&r&f to edit", "&c&nQ (Drop)&r&f to remove")));
    }

    public void setTime(int seconds) {
        this.time = seconds;
    }

    public enum EventType {
        REFILL(Material.CHEST), TNTRAIN(Material.TNT), BORDERSHRINK(Material.SPAWNER),
        LUCKYBLOCK_SPAWN(Material.SPONGE), LUCKYBLOCK_RAIN(Material.SLIME_BLOCK), LUCKYBLOCK_TREASURE(Material.ENDER_CHEST);

        final Material icon;

        EventType(Material icon) {
            this.icon = icon;
        }

        public Material getIcon() {
            return this.icon;
        }

        public String getName() {
            return Text.color(MSWConfig.file().getString("Config.Languages.Strings.Events." + this.name()));
        }
    }
}
