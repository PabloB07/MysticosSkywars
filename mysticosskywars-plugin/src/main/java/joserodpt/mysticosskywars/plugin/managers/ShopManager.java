package joserodpt.mysticosskywars.plugin.managers;

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
import joserodpt.mysticosskywars.api.config.MSWShopsConfig;
import joserodpt.mysticosskywars.api.managers.ShopManagerAPI;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.shop.items.MSWParticleItem;
import joserodpt.mysticosskywars.api.shop.items.MSWSpectatorShopItem;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ShopManager extends ShopManagerAPI {
    private final MysticosSkywarsAPI rs;

    private final Map<String, MSWBuyableItem> shopItems = new HashMap<>();

    public ShopManager(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    @Override
    public void loadShopItems() {
        shopItems.clear();

        if (MSWShopsConfig.file().contains("Main-Shop")
                || MSWShopsConfig.file().getInt("Version") == 1
                || MSWShopsConfig.file().getInt("Version") == 2) {
            rs.getLogger().warning("Starting upgrade of Shop Items to new config format...");
            upgradeV2toV3();
            MSWShopsConfig.file().remove("Main-Shop");
            MSWShopsConfig.file().remove("Spectator-Shop");
            MSWShopsConfig.file().set("Version", 3);
            MSWShopsConfig.save();
            return;
        }

        for (String category : MSWShopsConfig.file().getSection("Shops").getRoutesAsStrings(false)) {
            MSWBuyableItem.ItemCategory cat = MSWBuyableItem.ItemCategory.getCategoryByName(category);
            if (cat == null) {
                rs.getLogger().warning("Unknown shop category " + category + ". Skipping it.");
                continue;
            }
            for (String item : MSWShopsConfig.file().getSection("Shops." + category).getRoutesAsStrings(false)) {
                //verify if item already exists
                if (shopItems.containsKey(item)) {
                    rs.getLogger().warning("Item " + item + " already exists in the shop! Skipping.");
                    continue;
                }

                String displayname = MSWShopsConfig.file().getString("Shops." + category + "." + item + ".Displayname");
                String material = MSWShopsConfig.file().getString("Shops." + category + "." + item + ".Material");
                Material parsedMaterial = Material.matchMaterial(material == null ? "BARRIER" : material.toUpperCase());
                if (parsedMaterial == null) {
                    rs.getLogger().warning("Invalid shop material " + material + " in " + category + "." + item + ". Skipping it.");
                    continue;
                }
                double price = MSWShopsConfig.file().getDouble("Shops." + category + "." + item + ".Price");
                String permission = MSWShopsConfig.file().getString("Shops." + category + "." + item + ".Permission");
                Map<String, Object> extras = new HashMap<>();
                if (MSWShopsConfig.file().contains("Shops." + category + "." + item + ".Extras")) {
                    for (String extra : MSWShopsConfig.file().getSection("Shops." + category + "." + item + ".Extras").getRoutesAsStrings(false)) {
                        extras.put(extra, MSWShopsConfig.file().getString("Shops." + category + "." + item + ".Extras." + extra));
                    }
                }

                MSWBuyableItem buyableItem;
                if (cat == MSWBuyableItem.ItemCategory.BOW_PARTICLE) {
                    String particle = MSWShopsConfig.file().getString("Shops." + category + "." + item + ".Extras.Particle");
                    buyableItem = new MSWParticleItem(item, displayname, parsedMaterial, price, permission, particle);
                } else {
                    buyableItem = new MSWBuyableItem(item, displayname, parsedMaterial, price, permission, cat, extras);
                }
                shopItems.put(item, buyableItem);
            }
        }
    }

    private void upgradeV2toV3() {
        int itemCounter = 1;
        for (String shopCategory : MSWShopsConfig.file().getSection("Main-Shop").getRoutesAsStrings(false)) {
            for (String itemInsideCategoryPath : MSWShopsConfig.file().getStringList("Main-Shop." + shopCategory)) {
                String[] parse = itemInsideCategoryPath.split(">");

                if (parse.length != 4 && parse.length != 5) {
                    rs.getLogger().warning("Invalid item format for old config: " + itemInsideCategoryPath + " in category: " + shopCategory + "! Skipping.");
                    continue;
                }

                String material = parse[0];
                String displayname = parse[2];
                String perm = parse[3];
                double price;

                try {
                    price = Double.parseDouble(parse[1]);
                } catch (Exception e) {
                    rs.getLogger().warning("Error while parsing price for Shop Item " + material + "! Skipping.");
                    continue;
                }

                Material m;

                if (parse[0].equalsIgnoreCase("randomblock")) {
                    m = Material.COMMAND_BLOCK;
                } else {
                    try {
                        m = Material.valueOf(parse[0]);
                    } catch (Exception e) {
                        rs.getLogger().warning("Error while parsing material for Shop Item " + material + "! Skipping.");
                        continue;
                    }
                }

                //try to convert displayname to translatable line
                try {
                    Material displayNameMat = Material.valueOf(ChatColor.stripColor(Text.color(displayname).toUpperCase()));
                    displayname = "&b" + rs.getLanguageManagerAPI().getMaterialName(displayNameMat);
                } catch (Exception ignored) {

                }

                MSWBuyableItem item;
                String configPath = "item" + itemCounter;
                switch (parse.length) {
                    case 5:
                        try {
                            Particle.valueOf(parse[4]);
                            item = new MSWParticleItem(configPath, displayname, m, price, perm, parse[4]);
                            ++itemCounter;
                        } catch (Exception e) {
                            rs.getLogger().warning("Error while parsing particle for Legacy Shop Item " + material + "! Skipping.");
                            continue;
                        }

                        break;
                    case 4:
                        item = new MSWBuyableItem(configPath, displayname, m, price, perm, MSWBuyableItem.ItemCategory.getCategoryByName(shopCategory));
                        ++itemCounter;
                        break;
                    default:
                        rs.getLogger().warning("Error while parsing Legacy Shop Item " + material + "! Skipping.");
                        continue;
                }
                shopItems.put(configPath, item);
                item.saveToConfig(false);
            }
        }

        for (String itemInsideCategoryPath : MSWShopsConfig.file().getStringList("Spectator-Shop")) {
            String[] parse = itemInsideCategoryPath.split(">");

            if (parse.length != 4) {
                rs.getLogger().warning("Invalid item format for old config: " + itemInsideCategoryPath + " in category: Spectator-Shop! Skipping.");
                continue;
            }

            String material = parse[0];
            String displayname = parse[2];
            String perm = parse[3];
            double price;

            try {
                price = Double.parseDouble(parse[1]);
            } catch (Exception e) {
                rs.getLogger().warning("Error while parsing price for Shop Item " + material + "! Skipping.");
                continue;
            }

            Material m;

            if (parse[0].equalsIgnoreCase("randomblock")) {
                m = Material.COMMAND_BLOCK;
            } else {
                try {
                    m = Material.valueOf(parse[0]);
                } catch (Exception e) {
                    rs.getLogger().warning("Error while parsing material for Shop Item " + material + "! Skipping.");
                    continue;
                }
            }

            //try to convert displayname to translatable line
            try {
                Material displayNameMat = Material.valueOf(ChatColor.stripColor(Text.color(displayname).toUpperCase()));
                displayname = "&b" + rs.getLanguageManagerAPI().getMaterialName(displayNameMat);
            } catch (Exception ignored) {
            }

            String configPath = "item" + itemCounter;

            MSWSpectatorShopItem item = new MSWSpectatorShopItem(configPath, displayname, m, price, perm);
            ++itemCounter;

            shopItems.put(configPath, item);
            item.saveToConfig(false);
        }

        rs.getLogger().warning("Upgrade of Legacy Shop Items to new config format finished!");
    }

    @Override
    public Collection<MSWBuyableItem> getCategoryContents(MSWBuyableItem.ItemCategory cat) {
        return cat != MSWBuyableItem.ItemCategory.KIT ? this.shopItems.values().stream().filter(a -> a.getCategory() == cat).collect(Collectors.toList()) : rs.getKitManagerAPI().getKitsAsBuyables();
    }

    @Override
    public Collection<MSWBuyableItem> getBoughtItems(MSWBuyableItem.ItemCategory t, MSWPlayer p) {
        Map<String, MSWBuyableItem> items = new HashMap<>();
        if (t == MSWBuyableItem.ItemCategory.KIT) {
            rs.getDatabaseManagerAPI().getPlayerBoughtItemsCategory(p.getPlayer(), t).stream().map(playerBoughtItemsRow -> rs.getKitManagerAPI().getKit(playerBoughtItemsRow)).forEach(mswKit -> items.put(mswKit.getConfigKey(), mswKit));
        } else {
            rs.getDatabaseManagerAPI().getPlayerBoughtItemsCategory(p.getPlayer(), t).stream().map(playerBoughtItemsRow -> this.shopItems.get(playerBoughtItemsRow.getItemID()))
                    .forEach(mswBuyableItem -> items.put(mswBuyableItem.getConfigKey(), mswBuyableItem));
        }

        //add free items
        getCategoryContents(t).stream().filter(mswBuyableItem -> mswBuyableItem.getPrice() == 0).forEach(mswBuyableItem -> {
            if (!items.containsKey(mswBuyableItem.getConfigKey())) {
                items.put(mswBuyableItem.getConfigKey(), mswBuyableItem);
            }
        });


        return items.values();
    }
}
