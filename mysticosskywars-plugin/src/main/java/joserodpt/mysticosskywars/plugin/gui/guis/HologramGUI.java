package joserodpt.mysticosskywars.plugin.gui.guis;

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

import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWHologramConfig;
import joserodpt.mysticosskywars.api.managers.holograms.HologramType;
import joserodpt.mysticosskywars.api.managers.holograms.MSWLobbyHologram;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.Text;
import joserodpt.mysticosskywars.plugin.managers.LobbyHologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for managing lobby holograms.
 */
public class HologramGUI {

    private static final Map<UUID, HologramGUI> inventories = new HashMap<>();
    private Inventory inv;
    private final UUID uuid;
    private final MSWPlayer editor;
    private final LobbyHologramManager holoManager;
    private final Map<Integer, String> hologramSlots = new HashMap<>();

    public HologramGUI(MSWPlayer editor, LobbyHologramManager holoManager) {
        this.uuid = editor.getUUID();
        this.editor = editor;
        this.holoManager = holoManager;

        inv = Bukkit.getServer().createInventory(null, 54, Text.color("&9&lHologram Manager"));

        loadInv();
    }

    private void loadInv() {
        inv.clear();
        hologramSlots.clear();

        // Top border
        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}) {
            inv.setItem(slot, Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }

        // Title
        inv.setItem(4, Itens.createItem(Material.BEACON, 1, "&b&lHologram Manager",
                Arrays.asList("&7Manage lobby holograms",
                        "&7Holograms: &b" + holoManager.getAllHolograms().size())));

        // Existing holograms
        int slot = 9;
        for (Map.Entry<String, MSWLobbyHologram> entry : holoManager.getAllHolograms().entrySet()) {
            if (slot >= 45) break;

            MSWLobbyHologram holo = entry.getValue();
            HologramType type = MSWHologramConfig.file().isSection("Data.Holograms." + entry.getKey() + ".Type") ?
                    HologramType.valueOf(MSWHologramConfig.file().getString("Data.Holograms." + entry.getKey() + ".Type", "CUSTOM")) :
                    HologramType.CUSTOM;

            inv.setItem(slot, Itens.createItem(type.getIcon(), 1,
                    "&e" + entry.getKey(),
                    Arrays.asList(
                            "&7Type: " + type.getDisplayName(),
                            "&7Active: " + (holo.isActive() ? "&aYes" : "&cNo"),
                            "",
                            "&aClick to teleport",
                            "&cQ (Drop) to delete"
                    )));
            hologramSlots.put(slot, entry.getKey());
            slot++;
        }

        // Add new hologram buttons
        inv.setItem(36, Itens.createItem(Material.GOLD_BLOCK, 1, "&6Add Last Winner",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(37, Itens.createItem(Material.DIAMOND, 1, "&bAdd Top Wins Solo",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(38, Itens.createItem(Material.EMERALD, 1, "&aAdd Top Wins Teams",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(39, Itens.createItem(Material.IRON_SWORD, 1, "&cAdd Top Kills",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(40, Itens.createItem(Material.GOLD_INGOT, 1, "&eAdd Top Coins",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(41, Itens.createItem(Material.BEACON, 1, "&9Add Server Info",
                Collections.singletonList("&7Click to create at your location")));
        inv.setItem(42, Itens.createItem(Material.PAPER, 1, "&fAdd Custom",
                Collections.singletonList("&7Click to create at your location")));

        // Bottom border + refresh
        for (int slot2 : new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53}) {
            inv.setItem(slot2, Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, ""));
        }

        inv.setItem(49, Itens.createItem(Material.SLIME_BALL, 1, "&a&lRefresh All",
                Collections.singletonList("&7Click to refresh all holograms")));
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (!(clicker instanceof Player)) return;
                if (e.getCurrentItem() == null) return;

                Player p = (Player) clicker;
                UUID uuid = p.getUniqueId();
                if (!inventories.containsKey(uuid)) return;

                HologramGUI current = inventories.get(uuid);
                if (e.getInventory().getHolder() != current.getInventory().getHolder()) return;

                e.setCancelled(true);
                MSWPlayer gp = MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayer(p);
                if (gp == null) return;

                int rawSlot = e.getRawSlot();

                // Existing holograms
                if (current.hologramSlots.containsKey(rawSlot)) {
                    String id = current.hologramSlots.get(rawSlot);

                    if (e.getClick() == ClickType.DROP) {
                        // Delete hologram
                        current.holoManager.removeHologram(id);
                        // Also remove from config
                        MSWHologramConfig.file().remove("Data.Holograms." + id);
                        MSWHologramConfig.save();
                        p.sendMessage(Text.color("&cHologram '" + id + "' deleted."));
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                        current.loadInv();
                    } else {
                        // Teleport to hologram
                        MSWLobbyHologram holo = current.holoManager.getHologram(id);
                        if (holo != null) {
                            // Teleport using config location
                            String path = "Data.Holograms." + id;
                            if (MSWHologramConfig.file().isSection(path)) {
                                String worldName = MSWHologramConfig.file().getString(path + ".World", "");
                                double x = MSWHologramConfig.file().getDouble(path + ".X", 0.0);
                                double y = MSWHologramConfig.file().getDouble(path + ".Y", 0.0);
                                double z = MSWHologramConfig.file().getDouble(path + ".Z", 0.0);
                                org.bukkit.World world = Bukkit.getWorld(worldName);
                                if (world != null) {
                                    p.teleport(new Location(world, x, y, z));
                                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                                }
                            }
                        }
                    }
                    return;
                }

                // Add hologram buttons
                HologramType typeToAdd = null;
                String defaultId = "";

                switch (rawSlot) {
                    case 36:
                        typeToAdd = HologramType.LAST_WINNER;
                        defaultId = "last_winner";
                        break;
                    case 37:
                        typeToAdd = HologramType.TOP_WINS_SOLO;
                        defaultId = "top_wins_solo";
                        break;
                    case 38:
                        typeToAdd = HologramType.TOP_WINS_TEAMS;
                        defaultId = "top_wins_teams";
                        break;
                    case 39:
                        typeToAdd = HologramType.TOP_KILLS;
                        defaultId = "top_kills";
                        break;
                    case 40:
                        typeToAdd = HologramType.TOP_COINS;
                        defaultId = "top_coins";
                        break;
                    case 41:
                        typeToAdd = HologramType.SERVER_INFO;
                        defaultId = "server_info";
                        break;
                    case 42:
                        typeToAdd = HologramType.CUSTOM;
                        defaultId = "custom_" + System.currentTimeMillis();
                        break;
                }

                if (typeToAdd != null) {
                    final String id = defaultId;
                    final HologramType type = typeToAdd;
                    Location loc = p.getLocation().add(0, 2, 0);

                    // Check if hologram already exists
                    if (current.holoManager.getHologram(id) != null) {
                        p.sendMessage(Text.color("&cA hologram with that ID already exists."));
                        return;
                    }

                    current.holoManager.createHologram(id, type, loc);

                    // Save to config
                    MSWHologramConfig.file().set("Data.Holograms." + id + ".Type", type.name());
                    MSWHologramConfig.save();
                    current.holoManager.refreshHologram(id, current.holoManager.getHologram(id));

                    p.sendMessage(Text.color("&aHologram '" + id + "' created!"));
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    current.loadInv();
                    return;
                }

                // Refresh all
                if (rawSlot == 49) {
                    current.holoManager.refreshAll();
                    p.playSound(p.getLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 1f, 1f);
                    p.sendMessage(Text.color("&aAll holograms refreshed!"));
                    current.loadInv();
                }
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) return;
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    public void openInventory(MSWPlayer player) {
        Inventory inv = getInventory();
        InventoryView openInv = player.getPlayer().getOpenInventory();
        if (openInv != null) {
            Inventory openTop = player.getPlayer().getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                player.getPlayer().openInventory(inv);
            }
            register();
        }
    }

    private Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.uuid, this);
    }

    private void unregister() {
        inventories.remove(this.uuid);
    }
}
