package joserodpt.mysticosskywars.api.kits;

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
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MSWKit extends MSWBuyableItem {
    public enum Perks {ENDER}

    private final KitInventory kitInventory;
    private final List<Perks> kitPerks = new ArrayList<>();
    private int enderTask = -1;

    public MSWKit(String name, String displayname, Double cost, Material ic, KitInventory kitInventory, String perm) {
        super(name, displayname, ic, cost, perm, ItemCategory.KIT);

        this.kitInventory = kitInventory;
    }

    public MSWKit(String name, String displayname, Double cost, KitInventory kitInventory) {
        this(name, displayname, cost, Material.LEATHER_CHESTPLATE, kitInventory, "msw.kit");
    }

    public MSWKit() {
        this("None", "None", 0.0, Material.BARRIER, null, "msw.kit");
        super.setDummy();
    }

    @Override
    public ItemStack getIcon(MSWPlayer p) {
        Pair<Boolean, String> res = this.isBought(p);

        if (res.getKey()) {
            return Itens.createItemLoreEnchanted(this.getMaterial(), this.getAmount(), "&r&f" + this.getDisplayName(), Objects.equals(res.getValue(), "free") ? Collections.singletonList(TranslatableLine.KIT_SELECT.get(p)) : this.getDescription(p, res));
        } else {
            return Itens.createItem(super.getMaterial(), this.getAmount(), "&r&f" + this.getDisplayName(), this.getDescription(p, res));
        }
    }

    private List<String> getDescription(MSWPlayer p, Pair<Boolean, String> boughtPair) {
        List<String> desc = new ArrayList<>();

        if (!boughtPair.getKey() && super.getPrice() != 0)
            desc.add(TranslatableLine.KIT_PRICE.get(p).replace("%price%", super.getPriceFormatted()));

        //contents
        if (this.hasItems()) {
            desc.add("");
            desc.add(TranslatableLine.KIT_CONTAINS.get(p));

            for (ItemStack s : this.getKitInventory().getListInventory()) {
                desc.add(TranslatableLine.KIT_ITEM.get(p).replace("%amount%", s.getAmount() + "").replace("%item%", MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getMaterialName(p, s.getType())));
            }
        }

        if (this.hasPerk(Perks.ENDER)) {
            desc.add("&fx1 Perk: &dEnder");
        }

        desc.add("");
        if (boughtPair.getKey()) {
            desc.add(TranslatableLine.SHOP_BOUGHT_ON.get(p) + boughtPair.getValue());
            desc.add(TranslatableLine.KIT_SELECT.get(p));
        } else {
            desc.add(TranslatableLine.KIT_BUY.get(p));
        }

        return desc;
    }

    public List<Perks> getKitPerks() {
        return this.kitPerks;
    }

    public boolean hasPerk(Perks perk) {
        return this.getKitPerks().contains(perk);
    }

    public boolean hasItems() {
        return this.getKitInventory() != null && this.getKitInventory().hasItems();
    }

    public void addPerk(Perks perk) {
        if (!this.hasPerk(perk)) {
            this.getKitPerks().add(perk);
        }
    }

    public void removePerk(Perks perk) {
        if (this.hasPerk(perk)) {
            this.getKitPerks().remove(perk);
        }
    }

    public void addPerk(String perkName) {
        MSWKit.Perks kp;
        try {
            kp = MSWKit.Perks.valueOf(perkName.toUpperCase());
            this.addPerk(kp);
        } catch (Exception e) {
            MysticosSkywarsAPI.getInstance().getLogger().severe(perkName + " isn't a valid Kit Perk. Ignoring.");
        }
    }

    public void give(MSWPlayer p) {
        if (super.isDummy()) {
            return;
        }

        if (this.getKitInventory() == null) {
            MysticosSkywarsAPI.getInstance().getLogger().severe(this.getName() + " kit content's are null (?) Skipping give order.");
            return;
        }

        if (p.getPlayer().isOp()) {
            this.getKitInventory().giveToPlayer(p);
            return;
        }

        if (!p.isBot() && p.hasKit()) {
            this.getKitInventory().giveToPlayer(p);
            this.startTasks(p);
        }
    }

    public KitInventory getKitInventory() {
        return this.kitInventory;
    }

    private void startTasks(MSWPlayer p) {
        if (this.hasPerk(Perks.ENDER)) {
            this.enderTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                if (p.isInMatch()) {
                    p.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                } else {
                    Bukkit.getScheduler().cancelTask(enderTask);
                }
            }, MSWConfig.file().getInt("Config.Kits.Ender-Pearl-Perk-Give-Interval"));
        }
    }

    public void cancelTasks() {
        if (this.enderTask != -1) {
            Bukkit.getScheduler().cancelTask(this.enderTask);
        }
    }

    @Override
    public String toString() {
        return "MSWKit{" +
                "name=" + super.getName() +
                ", displayName=" + super.getDisplayName() +
                ", kitInventory=" + kitInventory +
                ", kitPerks=" + kitPerks +
                ", enderTask=" + enderTask +
                '}';
    }
}