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
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.kits.MSWKit;
import joserodpt.mysticosskywars.api.managers.TransactionManager;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.shop.items.MSWParticleItem;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.Pagination;
import joserodpt.mysticosskywars.plugin.gui.GUIManager;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerItemsGUI {

    private final ItemStack placeholder = Itens.createItem(Material.BLACK_STAINED_GLASS_PANE, 1, "&6");
    private static final Map<UUID, PlayerItemsGUI> inventories = new HashMap<>();
    private int pageNumber = 0;
    private Pagination<MSWBuyableItem> p;
    private final Inventory inv;
    private final MSWPlayer mswp;
    private final Map<Integer, MSWBuyableItem> display = new HashMap<>();
    private MSWBuyableItem.ItemCategory cat;

    private boolean cancelOpen = false;

    public PlayerItemsGUI(MSWPlayer mswp, MSWBuyableItem.ItemCategory t) {

        if (MSWConfig.file().getBoolean("Config.Shops.Only-Buy-Kits-Per-Match") && t == MSWBuyableItem.ItemCategory.KIT) {
            inv = null;
            this.mswp = null;
            this.cancelOpen = true;
            ShopGUI kitShop = new ShopGUI(mswp, MSWBuyableItem.ItemCategory.KIT);
            kitShop.openInventory(mswp);
            return;
        }


        this.mswp = mswp;
        this.cat = t;
        this.inv = Bukkit.getServer().createInventory(null, 54, this.cat.getCategoryTitle(mswp));

        List<MSWBuyableItem> items = new ArrayList<>(MysticosSkywarsAPI.getInstance().getShopManagerAPI().getBoughtItems(t, mswp));

        if (!items.isEmpty()) {
            p = new Pagination<>(28, items);
            fillChest(p.getPage(pageNumber));
        } else {
            fillChest(Collections.singletonList(new MSWBuyableItem()));
        }

        register();
    }


    private void fillChest(List<MSWBuyableItem> items) {
        inv.clear();
        display.clear();

        for (int slot : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 45, 53, 52, 51, 50, 49, 48, 47, 46, 45, 36, 27, 18, 9, 44}) {
            inv.setItem(slot, placeholder);
        }

        if (!firstPage()) {
            inv.setItem(18, Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.BUTTONS_BACK_TITLE.getSingle(), Collections.singletonList(TranslatableLine.BUTTONS_BACK_DESC.getSingle())));
            inv.setItem(27, Itens.createItem(Material.YELLOW_STAINED_GLASS, 1, TranslatableLine.BUTTONS_BACK_TITLE.getSingle(), Collections.singletonList(TranslatableLine.BUTTONS_BACK_DESC.getSingle())));
        }

        if (!lastPage()) {
            inv.setItem(26, Itens.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.BUTTONS_NEXT_TITLE.getSingle(), Collections.singletonList(TranslatableLine.BUTTONS_NEXT_DESC.getSingle())));
            inv.setItem(35, Itens.createItem(Material.GREEN_STAINED_GLASS, 1, TranslatableLine.BUTTONS_NEXT_TITLE.getSingle(), Collections.singletonList(TranslatableLine.BUTTONS_NEXT_DESC.getSingle())));
        }

        if (MSWConfig.file().getBoolean("Config.Shops.Enable-Cage-Block-Shop")) {
            inv.setItem(47, Itens.createItem(Material.SPAWNER, 1, TranslatableLine.CAGEBLOCK.get(mswp)));
        } else {
            inv.setItem(47, placeholder);
        }

        inv.setItem(48, Itens.createItem(Material.LEATHER_CHESTPLATE, 1, TranslatableLine.KITS.get(mswp)));

        if (MSWConfig.file().getBoolean("Config.Shops.Enable-Bow-Particles-Shop")) {
            inv.setItem(50, Itens.createItem(Material.BOW, 1, TranslatableLine.BOWPARTICLE.get(mswp)));
        } else {
            inv.setItem(50, placeholder);
        }

        if (MSWConfig.file().getBoolean("Config.Shops.Enable-Win-Block-Shop")) {
            inv.setItem(51, Itens.createItem(Material.FIREWORK_ROCKET, 1, TranslatableLine.WINBLOCK.get(mswp)));
        } else {
            inv.setItem(51, placeholder);
        }

        int pointer = 0;
        int[] slots = new int[]{10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};
        for (MSWBuyableItem item : items) {
            if (item != null) {
                inv.setItem(slots[pointer], item.getIcon(this.mswp));
                display.put(slots[pointer], item);
                ++pointer;
            }
        }
    }

    public static Listener getListener() {
        return new Listener() {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                HumanEntity clicker = e.getWhoClicked();
                if (clicker instanceof Player) {
                    if (e.getCurrentItem() == null) {
                        return;
                    }
                    UUID uuid = clicker.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        PlayerItemsGUI current = inventories.get(uuid);
                        if (e.getInventory().getHolder() != current.getInventory().getHolder()) {
                            return;
                        }

                        e.setCancelled(true);
                        MSWPlayer p = MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayer((Player) clicker);

                        switch (e.getRawSlot()) {
                            case 47:
                                p.closeInventory();
                                if (MSWConfig.file().getBoolean("Config.Shops.Enable-Cage-Block-Shop")) {
                                    PlayerItemsGUI kitShop = new PlayerItemsGUI(p, MSWBuyableItem.ItemCategory.CAGE_BLOCK);
                                    kitShop.openInventory(p);
                                    return;
                                }
                                break;
                            case 48:
                                p.closeInventory();
                                if (MSWConfig.file().getBoolean("Config.Shops.Only-Buy-Kits-Per-Match")) {
                                    ShopGUI kitShop = new ShopGUI(p, MSWBuyableItem.ItemCategory.KIT);
                                    kitShop.openInventory(p);
                                } else {
                                    PlayerItemsGUI kitShop = new PlayerItemsGUI(p, MSWBuyableItem.ItemCategory.KIT);
                                    kitShop.openInventory(p);
                                }
                                break;
                            case 50:
                                p.closeInventory();
                                if (MSWConfig.file().getBoolean("Config.Shops.Enable-Bow-Particles-Shop")) {
                                    PlayerItemsGUI kitShop = new PlayerItemsGUI(p, MSWBuyableItem.ItemCategory.BOW_PARTICLE);
                                    kitShop.openInventory(p);
                                    return;
                                }
                                break;
                            case 51:
                                p.closeInventory();
                                if (MSWConfig.file().getBoolean("Config.Shops.Enable-Win-Block-Shop")) {
                                    PlayerItemsGUI kitShop = new PlayerItemsGUI(p, MSWBuyableItem.ItemCategory.WIN_BLOCK);
                                    kitShop.openInventory(p);
                                    return;
                                }
                                break;
                            case 18:
                            case 27:
                                if (!current.firstPage()) {
                                    backPage(current);
                                    p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50, 50);
                                    return;
                                }
                                break;
                            case 26:
                            case 35:
                                if (!current.lastPage()) {
                                    nextPage(current);
                                    p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50, 50);
                                    return;
                                }
                                break;
                        }


                        if (current.display.containsKey(e.getRawSlot())) {
                            MSWBuyableItem clicked = current.display.get(e.getRawSlot());

                            if (current.cat == MSWBuyableItem.ItemCategory.SPEC_SHOP) {
                                switch (e.getClick()) {
                                    case SWAP_OFFHAND:
                                        clicked.addAmount(1);
                                        current.inv.setItem(e.getRawSlot(), clicked.getIcon(current.mswp));
                                        break;
                                    case DROP:
                                        clicked.addAmount(-1);
                                        current.inv.setItem(e.getRawSlot(), clicked.getIcon(current.mswp));
                                        break;
                                    default:
                                        if (p.getPlayer().hasPermission(clicked.getPermission())) {
                                            TransactionManager cm = new TransactionManager(p, clicked.getPrice(), TransactionManager.Operations.REMOVE, false);
                                            p.closeInventory();

                                            if (cm.removeCoins()) {
                                                p.getWorld().dropItem(p.getLocation(), new ItemStack(clicked.getMaterial(), clicked.getAmount()));

                                                p.sendMessage(TranslatableLine.SHOP_BUY_MESSAGE.get(p, true).replace("%name%", clicked.getDisplayName()).replace("%coins%", clicked.getPriceFormatted()));
                                            } else {
                                                p.sendMessage(TranslatableLine.INSUFICIENT_COINS.get(p, true).replace("%coins%", MysticosSkywarsAPI.getInstance().getCurrencyAdapterAPI().getCoinsFormatted(p)));
                                            }
                                        } else {
                                            TranslatableLine.SHOP_NO_PERM.send(p, true);
                                        }
                                        break;
                                }
                                p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50, 50);
                            } else {
                                if (clicked.isDummy()) {
                                    TranslatableLine.NOT_BUYABLE.send(p, true);
                                    return;
                                }

                                if (e.getClick() == ClickType.RIGHT && clicked instanceof MSWKit) {
                                    GUIManager.openKitPreview(p, (MSWKit) clicked, 0);
                                    return;
                                }

                                switch (current.cat) {
                                    case KIT:
                                        p.setKit(MysticosSkywarsAPI.getInstance().getKitManagerAPI().getKit(clicked.getName()));
                                        p.closeInventory();
                                        break;
                                    case BOW_PARTICLE:
                                        p.setBowParticle(((MSWParticleItem) clicked).getParticle());
                                        break;
                                    case CAGE_BLOCK:
                                        p.setCageBlock(clicked.getMaterial());
                                        break;
                                    case WIN_BLOCK:
                                        p.setWinBlock(clicked.getExtrasMap().containsKey("Random-Blocks") ? "Random-Blocks" : clicked.getMaterial().name());
                                        break;
                                }
                                p.sendMessage(TranslatableLine.PROFILE_SELECTED.get(p, true).replace("%name%", clicked.getDisplayName()).replace("%type%", current.cat.getCategoryTitle(p)));
                            }
                        }
                    }
                }
            }

            private void backPage(PlayerItemsGUI current) {
                if (current.p.exists(current.pageNumber - 1)) {
                    --current.pageNumber;
                }

                current.fillChest(current.p.getPage(current.pageNumber));
            }

            private void nextPage(PlayerItemsGUI current) {
                if (current.p.exists(current.pageNumber + 1)) {
                    ++current.pageNumber;
                }

                current.fillChest(current.p.getPage(current.pageNumber));
            }

            @EventHandler
            public void onClose(InventoryCloseEvent e) {
                if (e.getPlayer() instanceof Player) {
                    if (e.getInventory() == null) {
                        return;
                    }
                    Player p = (Player) e.getPlayer();
                    UUID uuid = p.getUniqueId();
                    if (inventories.containsKey(uuid)) {
                        inventories.get(uuid).unregister();
                    }
                }
            }
        };
    }

    private boolean lastPage() {
        return p == null || pageNumber == (p.totalPages() - 1);
    }

    private boolean firstPage() {
        return pageNumber == 0;
    }

    public void openInventory(MSWPlayer player) {
        if (cancelOpen) {
            return;
        }

        Inventory inv = getInventory();
        InventoryView openInv = player.getPlayer().getOpenInventory();
        if (openInv != null) {
            Inventory openTop = player.getPlayer().getOpenInventory().getTopInventory();
            if (openTop != null && openTop.getType().name().equalsIgnoreCase(inv.getType().name())) {
                openTop.setContents(inv.getContents());
            } else {
                player.getPlayer().openInventory(inv);
            }
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 50, 50);
        }
    }

    private Inventory getInventory() {
        return inv;
    }

    private void register() {
        inventories.put(this.mswp.getUUID(), this);
    }

    private void unregister() {
        inventories.remove(this.mswp.getUUID());
    }
}
