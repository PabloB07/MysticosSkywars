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

import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWKitsConfig;
import joserodpt.mysticosskywars.api.database.PlayerBoughtItemsRow;
import joserodpt.mysticosskywars.api.kits.KitInventory;
import joserodpt.mysticosskywars.api.kits.MSWKit;
import joserodpt.mysticosskywars.api.managers.KitManagerAPI;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.utils.ItemStackSpringer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitManager extends KitManagerAPI {

    private final Map<String, MSWKit> kits = new HashMap<>();

    @Override
    public void loadKits() {
        this.kits.clear();

        if (MSWKitsConfig.file().isSection("Kits")) {
            Debugger.print(KitManager.class, "KITS: " + MSWKitsConfig.file().getSection("Kits").getRoutesAsStrings(false));

            for (String name : MSWKitsConfig.file().getSection("Kits").getRoutesAsStrings(false)) {
                Debugger.print(KitManager.class, "Loading KIT " + name);

                try {
                    String displayName = Text.color(MSWKitsConfig.file().getString("Kits." + name + ".Display-Name"));
                    Double price = MSWKitsConfig.file().getDouble("Kits." + name + ".Price");

                    String matString = MSWKitsConfig.file().getString("Kits." + name + ".Icon");
                    Material mat;
                    MSWKit mswKit;
                    try {
                        mat = Material.matchMaterial(matString == null ? "BARRIER" : matString.toUpperCase());
                        if (mat == null) throw new IllegalArgumentException("Unknown material");
                    } catch (Exception e) {
                        mat = Material.BARRIER;
                        MysticosSkywarsAPI.getInstance().getLogger().warning(matString + " isn't a valid material [KIT]");
                    }

                    List<Map<String, Object>> inv = (List<Map<String, Object>>) MSWKitsConfig.file().getList("Kits." + name + ".Contents");

                    if (inv == null || inv.isEmpty()) {
                        Debugger.printerr(KitManager.class, "Inventory Itens on " + "Kits." + name + ".Contents" + " are empty! Skipping kit.");
                        continue;
                    }

                    mswKit = new MSWKit(name, displayName, price, mat, new KitInventory(ItemStackSpringer.getItemsDeSerialized(inv)), MSWKitsConfig.file().getString("Kits." + name + ".Permission"));

                    if (MSWKitsConfig.file().isList("Kits." + name + ".Perks")) {
                        MSWKitsConfig.file().getStringList("Kits." + name + ".Perks")
                                .forEach(mswKit::addPerk);
                    }

                    this.kits.put(name, mswKit);

                    Debugger.print(KitManager.class, "Loaded " + mswKit);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error loading kit: " + name + "! Skipping kit.");
                    Bukkit.getLogger().warning(String.valueOf(e.getMessage()));
                }
            }
        }
    }

    @Override
    public void registerKit(MSWKit k) {
        if (k == null || k.getName() == null || k.getName().isEmpty()) {
            Debugger.printerr(KitManager.class, "Kit is null or has no name! Cannot register.");
            return;
        }

        if (this.kits.containsKey(k.getName())) {
            Debugger.printerr(KitManager.class, "Kit with name " + k.getName() + " already exists! Cannot register.");
            return;
        }

        this.kits.put(k.getName(), k);

        MSWKitsConfig.file().set("Kits." + k.getName() + ".Display-Name", k.getDisplayName());
        MSWKitsConfig.file().set("Kits." + k.getName() + ".Price", k.getPrice());
        MSWKitsConfig.file().set("Kits." + k.getName() + ".Icon", k.getMaterial().name());
        MSWKitsConfig.file().set("Kits." + k.getName() + ".Permission", k.getPermission());

        if (!k.getKitPerks().isEmpty()) {
            MSWKitsConfig.file().set("Kits." + k.getName() + ".Perks", k.getKitPerks().stream().map(Enum::name).collect(Collectors.toList()));
        }

        MSWKitsConfig.file().set("Kits." + k.getName() + ".Contents", k.getKitInventory().getSerialized());
        MSWKitsConfig.save();
    }

    @Override
    public void unregisterKit(MSWKit k) {
        this.getKits().remove(k);
        MSWKitsConfig.file().remove("Kits");
        this.getKits().forEach(this::registerKit);
        MSWKitsConfig.save();
    }

    @Override
    public Collection<MSWKit> getKits() {
        return this.kits.values();
    }

    @Override
    public Collection<MSWBuyableItem> getKitsAsBuyables() {
        return new ArrayList<>(this.kits.values());
    }

    @Override
    public MSWKit getKit(String string) {
        return this.kits.get(string);
    }

    @Override
    public MSWKit getKit(PlayerBoughtItemsRow playerBoughtItemsRow) {
        return getKit(playerBoughtItemsRow.getItemID());
    }
}
