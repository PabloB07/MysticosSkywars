package joserodpt.mysticosskywars.api;

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

import com.google.common.base.Preconditions;
import joserodpt.mysticosskywars.api.currency.CurrencyAdapterAPI;
import joserodpt.mysticosskywars.api.managers.AchievementsManagerAPI;
import joserodpt.mysticosskywars.api.managers.DatabaseManagerAPI;
import joserodpt.mysticosskywars.api.managers.HologramManagerAPI;
import joserodpt.mysticosskywars.api.managers.KitManagerAPI;
import joserodpt.mysticosskywars.api.managers.LanguageManagerAPI;
import joserodpt.mysticosskywars.api.managers.LeaderboardManagerAPI;
import joserodpt.mysticosskywars.api.managers.LobbyManagerAPI;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.managers.PartiesManagerAPI;
import joserodpt.mysticosskywars.api.managers.PlayerManagerAPI;
import joserodpt.mysticosskywars.api.managers.ShopManagerAPI;
import joserodpt.mysticosskywars.api.managers.WorldManagerAPI;
import joserodpt.mysticosskywars.api.nms.MSWnms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.logging.Logger;

public abstract class MysticosSkywarsAPI {

    private static MysticosSkywarsAPI instance;

    /**
     * Gets instance of this API
     *
     * @return MysticosSkywarsAPI API instance
     */
    public static MysticosSkywarsAPI getInstance() {
        return instance;
    }

    /**
     * Sets the MysticosSkywars API instance.
     * <b>Note! This method may only be called once</b>
     *
     * @param instance the new instance to set
     */
    public static void setInstance(MysticosSkywarsAPI instance) {
        Preconditions.checkNotNull(instance, "instance");
        Preconditions.checkArgument(MysticosSkywarsAPI.instance == null, "Instance already set");
        MysticosSkywarsAPI.instance = instance;
    }

    public abstract Logger getLogger();

    public abstract String getVersion();

    public abstract MSWnms getNMS();

    public abstract WorldManagerAPI getWorldManagerAPI();

    public abstract MSWEventsAPI getEventsAPI();

    public abstract LanguageManagerAPI getLanguageManagerAPI();

    public abstract PlayerManagerAPI getPlayerManagerAPI();

    public abstract MapManagerAPI getMapManagerAPI();

    public abstract LobbyManagerAPI getLobbyManagerAPI();

    public abstract ShopManagerAPI getShopManagerAPI();

    public abstract KitManagerAPI getKitManagerAPI();

    public abstract PartiesManagerAPI getPartiesManagerAPI();

    public abstract Random getRandom();

    public abstract DatabaseManagerAPI getDatabaseManagerAPI();

    public abstract LeaderboardManagerAPI getLeaderboardManagerAPI();

    public abstract AchievementsManagerAPI getAchievementsManagerAPI();

    public abstract HologramManagerAPI getHologramManagerAPI();

    public abstract CurrencyAdapterAPI getCurrencyAdapterAPI();

    public abstract JavaPlugin getPlugin();

    public abstract String getServerVersion();

    public abstract String getSimpleServerVersion();

    public abstract void reload();

    public abstract Economy getVaultEconomy();
}
