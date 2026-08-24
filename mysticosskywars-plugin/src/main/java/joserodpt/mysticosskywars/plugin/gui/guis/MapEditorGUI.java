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
import joserodpt.mysticosskywars.api.config.MSWMapsConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.MSWMapEvent;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.PlayerInput;
import joserodpt.mysticosskywars.api.utils.Text;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Complete Map Editor GUI - a single unified interface for all map editing operations.
 * Consolidates map dashboard, settings, events, and creation tools into one GUI.
 */
public class MapEditorGUI {

    private static final Map<UUID, MapEditorGUI> inventories = new HashMap<>();
    private Inventory inv;
    private final UUID uuid;
    private final MSWMap map;
    private final MSWPlayer editor;

    private final ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "");
    private final ItemStack glassBorder = Itens.createItem(Material.GRAY_STAINED_GLASS_PANE, 1, "");

    public MapEditorGUI(MSWPlayer editor, MSWMap map) {
        this.uuid = editor.getUUID();
        this.map = map;
        this.editor = editor;

        inv = Bukkit.getServer().createInventory(null, 54, Text.color("&0&l" + map.getDisplayName() + " &8| &7Map Editor"));

        loadInv();
    }

    private void loadInv() {
        inv.clear();

        // Top border
        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}) {
            inv.setItem(slot, placeholder);
        }

        // Map info (top center)
        inv.setItem(4, createMapInfoItem());

        // Row 1: Quick Actions (slots 9-17)
        inv.setItem(9, glassBorder);
        inv.setItem(10, createToggleItem("&9Spectator", map.isSpectatorEnabled(),
                "&7Allow dead players to spectate",
                Material.ENDER_EYE));
        inv.setItem(11, createToggleItem("&9Ranked", map.isRanked(),
                "&7Enable ranked mode",
                Material.DIAMOND_SWORD));
        inv.setItem(12, createToggleItem("&9Border", map.isBorderEnabled(),
                "&7Enable world border",
                Material.ITEM_FRAME));
        inv.setItem(13, createToggleItem("&9Instant End", map.isInstantEndEnabled(),
                "&7End game instantly on win",
                Material.DRAGON_HEAD));
        inv.setItem(14, Itens.createItem(Material.PISTON, 1, "&e&lEvents",
                Arrays.asList("&7Click to manage map events",
                        "&7Current: &b" + map.getEvents().size() + " events")));
        inv.setItem(15, Itens.createItem(Material.CLOCK, 1, "&b&lTimers",
                Arrays.asList("&7Max Game: &e" + Text.formatSeconds(map.getMaxGameTime()),
                        "&7End Game: &e" + Text.formatSeconds(map.getTimeEndGame()),
                        "&7Start: &e" + Text.formatSeconds(map.getTimeToStart()),
                        "&7Invincibility: &e" + Text.formatSeconds(map.getInvincibilitySeconds()))));
        inv.setItem(16, Itens.createItem(Material.NAME_TAG, 1, "&d&lDisplay Name",
                Arrays.asList("&7Current: &b" + map.getDisplayName(),
                        "&7Click to change")));
        inv.setItem(17, glassBorder);

        // Row 2: Status + Actions (slots 18-26)
        inv.setItem(18, glassBorder);
        inv.setItem(19, map.getState().getStateIcon(map.isRanked()));
        inv.setItem(20, Itens.createItem(Material.BEACON, 1, "&a&lCages",
                Arrays.asList("&7Cages: &b" + map.getCages().size(),
                        "&7Click to teleport to cages")));
        inv.setItem(21, Itens.createItem(Material.CHEST, 1, "&6&lChests",
                Arrays.asList("&7Chests: &b" + map.getChests().size(),
                        "&7Click to manage chests")));
        inv.setItem(22, Itens.createItem(Material.ENDER_PEARL, 1, "&5&lSpectator Loc",
                Arrays.asList("&7Click to set spectator location",
                        "&7At your current position")));
        inv.setItem(23, Itens.createItem(Material.COMPASS, 1, "&c&lBoundaries",
                Arrays.asList("&7Click to set map boundaries",
                        "&7Using WorldEdit selection")));
        inv.setItem(24, Itens.createItem(Material.PLAYER_HEAD, 1, "&b&lPlayer Info",
                Arrays.asList("&7Players: &b" + map.getPlayerCount() + "/" + map.getMaxPlayers(),
                        "&7Spectators: &b" + map.getSpectatorsCount())));
        inv.setItem(25, Itens.createItem(Material.BARRIER, 1, "&4&lReset Map",
                Arrays.asList("&cClick to reset this map",
                        "&4WARNING: All players will be kicked!")));
        inv.setItem(26, glassBorder);

        // Row 3: Bottom actions (slots 27-35)
        inv.setItem(27, glassBorder);
        inv.setItem(28, Itens.createItem(Material.FEATHER, 1, "&9Time of Day &e" + map.getTimeType().name(),
                Arrays.asList("&7Click to cycle time type",
                        "&7Current: &b" + map.getTimeType().name())));
        inv.setItem(29, Itens.createItem(Material.ARROW, 1, "&9Projectiles &e" + map.getProjectileTier().name(),
                Arrays.asList("&7Click to cycle projectile type",
                        "&7Current: &b" + map.getProjectileTier().name())));
        inv.setItem(30, Itens.createItem(Material.CHEST_MINECART, 1, "&9Chest Tier &e" + map.getChestTier().name(),
                Arrays.asList("&7Click to cycle chest tier",
                        "&7Current: &b" + map.getChestTier().name())));
        inv.setItem(31, Itens.createItem(Material.PAPER, 1, "&e&lRename",
                Arrays.asList("&7Click to change the",
                        "&7map's display name")));
        inv.setItem(32, Itens.createItem(Material.MAP, 1, "&e&lDuplicate",
                Arrays.asList("&7Click to duplicate this map",
                        "&7Creates a copy of the world")));
        inv.setItem(33, Itens.createItem(Material.LIME_DYE, 1, "&a&lRegister",
                Arrays.asList(map.isUnregistered() ? "&7Click to register this map" : "&7Click to unregister this map",
                        "&7Status: " + (map.isUnregistered() ? "&cUnregistered" : "&aRegistered"))));
        inv.setItem(34, Itens.createItem(Material.SLIME_BALL, 1, "&7&lEvent Editor",
                Arrays.asList("&7Advanced event editor",
                        "&7Click to open")));
        inv.setItem(35, glassBorder);

        // Bottom border
        for (int slot : new int[]{36, 37, 38, 39, 40, 41, 42, 43, 44}) {
            inv.setItem(slot, placeholder);
        }

        // Close button
        inv.setItem(49, Itens.createItem(Material.BIRCH_DOOR, 1, "&cClose Editor",
                Collections.singletonList("&fClick to close this menu")));
    }

    private ItemStack createMapInfoItem() {
        return Itens.createItem(Material.MAP, 1, "&b&l" + map.getDisplayName(),
                Arrays.asList(
                        "&7Name: &f" + map.getName(),
                        "&7Mode: &b" + map.getGameMode().getSimpleName(),
                        "&7State: &b" + map.getState().getDefaultTranslation(),
                        "&7Players: &b" + map.getPlayerCount() + "/" + map.getMaxPlayers(),
                        "&7Ranked: " + (map.isRanked() ? "&aYes" : "&cNo"),
                        "&7World: &b" + (map.getMSWWorld() != null ? map.getMSWWorld().getName() : "N/A"),
                        "",
                        "&7&nQuick Settings:",
                        " &7Spectator: " + (map.isSpectatorEnabled() ? "&aON" : "&cOFF"),
                        " &7Border: " + (map.isBorderEnabled() ? "&aON" : "&cOFF"),
                        " &7Instant End: " + (map.isInstantEndEnabled() ? "&aON" : "&cOFF"),
                        " &7Ranked: " + (map.isRanked() ? "&aON" : "&cOFF")
                ));
    }

    private ItemStack createToggleItem(String name, boolean enabled, String desc, Material icon) {
        return Itens.createItem(icon, 1, name + (enabled ? " &a&lON" : " &c&lOFF"),
                Arrays.asList(desc, "&7Click to toggle"));
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

                MapEditorGUI current = inventories.get(uuid);
                if (e.getInventory().getHolder() != current.getInventory().getHolder()) return;

                e.setCancelled(true);
                MSWPlayer gp = MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayer(p);
                if (gp == null) return;

                switch (e.getRawSlot()) {
                    // Toggle buttons (row 1)
                    case 10:
                        current.map.setSpectating(!current.map.isSpectatorEnabled());
                        current.map.save(MSWMap.Data.SETTINGS, true);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 11:
                        current.map.setRanked(!current.map.isRanked());
                        current.map.save(MSWMap.Data.SETTINGS, true);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 12:
                        current.map.setBorderEnabled(!current.map.isBorderEnabled());
                        current.map.save(MSWMap.Data.SETTINGS, true);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 13:
                        current.map.setInstantEnding(!current.map.isInstantEndEnabled());
                        current.map.save(MSWMap.Data.SETTINGS, true);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 14:
                        // Open event editor
                        p.closeInventory();
                        Bukkit.getScheduler().runTaskLater(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                            MapEventEditorGUI gui = new MapEventEditorGUI(p, current.map);
                            gui.openInventory(p);
                        }, 1);
                        break;
                    case 15:
                        // Timer settings - max game time
                        p.closeInventory();
                        new PlayerInput(p, input -> {
                            try {
                                int seconds = Integer.parseInt(input);
                                current.map.setMaxGameTime(seconds);
                                openEditor(gp, current.map);
                            } catch (NumberFormatException e1) {
                                p.sendMessage(Text.color("&cInvalid number."));
                                openEditor(gp, current.map);
                            }
                        }, input -> openEditor(gp, current.map));
                        break;
                    case 16:
                        // Display name
                        p.closeInventory();
                        new PlayerInput(p, input -> {
                            current.map.setDisplayName(Text.color(input));
                            current.map.save(MSWMap.Data.SETTINGS, true);
                            openEditor(gp, current.map);
                        }, input -> openEditor(gp, current.map));
                        break;

                    // Row 2 actions
                    case 19:
                        // State toggle
                        switch (current.map.getState()) {
                            case AVAILABLE:
                                current.map.setState(MSWMap.MapState.STARTING);
                                break;
                            case FINISHING:
                                current.map.setState(MSWMap.MapState.RESETTING);
                                break;
                            case PLAYING:
                                current.map.setState(MSWMap.MapState.FINISHING);
                                break;
                            case RESETTING:
                                current.map.setState(MSWMap.MapState.AVAILABLE);
                                break;
                            case STARTING:
                                current.map.setState(MSWMap.MapState.WAITING);
                                break;
                            case WAITING:
                                current.map.setState(MSWMap.MapState.PLAYING);
                                break;
                        }
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        gp.sendMessage(TranslatableLine.GAME_STATUS_SET.get(gp, true).replace("%status%", current.map.getState().getDisplayName(gp)));
                        current.loadInv();
                        break;
                    case 20:
                        // Teleport to cages
                        if (!current.map.getCages().isEmpty()) {
                            var cage = current.map.getCages().iterator().next();
                            p.teleport(cage.getLocation().add(0, 1, 0));
                            p.sendMessage(Text.color("&aTeleported to first cage."));
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        }
                        break;
                    case 22:
                        // Set spectator location
                        current.map.setSpectatorLocation(p.getLocation());
                        p.sendMessage(Text.color("&aSpectator location has been set!"));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                        current.loadInv();
                        break;
                    case 23:
                        // Set boundaries using WorldEdit
                        try {
                            WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
                            if (wep != null) {
                                Region r = wep.getSession(p).getSelection(wep.getSession(p).getSelectionWorld());
                                if (r != null) {
                                    current.map.setBoundaries(
                                            new org.bukkit.Location(current.map.getMSWWorld().getWorld(),
                                                    r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ()),
                                            new org.bukkit.Location(current.map.getMSWWorld().getWorld(),
                                                    r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ()));
                                    p.sendMessage(Text.color("&aBoundaries have been set!"));
                                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                                } else {
                                    p.sendMessage(Text.color("&cNo WorldEdit selection found. Use //pos1 and //pos2 first!"));
                                }
                            } else {
                                p.sendMessage(Text.color("&cWorldEdit is not installed!"));
                            }
                        } catch (Exception ex) {
                            p.sendMessage(Text.color("&cError setting boundaries: " + ex.getMessage()));
                        }
                        current.loadInv();
                        break;
                    case 25:
                        // Reset map
                        current.map.reset();
                        TranslatableLine.MAP_RESET_DONE.sendDefault(p, true);
                        p.closeInventory();
                        break;

                    // Row 3 actions
                    case 28:
                        // Time type cycle
                        MSWMap.TimeType[] times = MSWMap.TimeType.values();
                        int currentIdx = java.util.Arrays.asList(times).indexOf(current.map.getTimeType());
                        current.map.setTime(times[(currentIdx + 1) % times.length]);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 29:
                        // Projectile type cycle
                        MSWMap.ProjectileType[] projectiles = MSWMap.ProjectileType.values();
                        int projIdx = java.util.Arrays.asList(projectiles).indexOf(current.map.getProjectileTier());
                        current.map.setProjectiles(projectiles[(projIdx + 1) % projectiles.length]);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 30:
                        // Chest tier cycle
                        joserodpt.mysticosskywars.api.chests.MSWChest.Tier[] tiers = joserodpt.mysticosskywars.api.chests.MSWChest.Tier.values();
                        int tierIdx = java.util.Arrays.asList(tiers).indexOf(current.map.getChestTier());
                        current.map.setTierType(tiers[(tierIdx + 1) % tiers.length]);
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 31:
                        // Rename
                        p.closeInventory();
                        new PlayerInput(p, input -> {
                            current.map.setDisplayName(Text.color(input));
                            current.map.save(MSWMap.Data.SETTINGS, true);
                            p.sendMessage(Text.color("&aMap renamed to: &b" + input));
                            openEditor(gp, current.map);
                        }, input -> openEditor(gp, current.map));
                        break;
                    case 32:
                        // Duplicate
                        p.closeInventory();
                        new PlayerInput(p, input -> {
                            MSWMap duplicated = current.map.duplicate(input);
                            if (duplicated != null) {
                                p.sendMessage(Text.color("&aMap duplicated as: &b" + input));
                            } else {
                                p.sendMessage(Text.color("&cFailed to duplicate map."));
                            }
                        }, input -> {});
                        break;
                    case 33:
                        // Register/Unregister
                        current.map.setUnregistered(!current.map.isUnregistered());
                        p.sendMessage(Text.color(current.map.isUnregistered() ? "&cMap unregistered." : "&aMap registered."));
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        current.loadInv();
                        break;
                    case 34:
                        // Advanced event editor
                        p.closeInventory();
                        Bukkit.getScheduler().runTaskLater(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                            MapEventEditorGUI gui = new MapEventEditorGUI(p, current.map);
                            gui.openInventory(p);
                        }, 1);
                        break;

                    // Close
                    case 49:
                        p.closeInventory();
                        break;
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

    public static void openEditor(MSWPlayer player, MSWMap map) {
        MapEditorGUI gui = new MapEditorGUI(player, map);
        gui.openInventory(player);
    }
}
