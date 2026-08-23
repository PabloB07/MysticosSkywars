package joserodpt.mysticosskywars.plugin;

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
import joserodpt.mysticosskywars.api.MSWEventsAPI;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWKitsConfig;
import joserodpt.mysticosskywars.api.config.MSWLanguagesOldConfig;
import joserodpt.mysticosskywars.api.config.MSWMapsConfig;
import joserodpt.mysticosskywars.api.config.MSWShopsConfig;
import joserodpt.mysticosskywars.api.config.chests.BasicChestConfig;
import joserodpt.mysticosskywars.api.config.chests.EPICChestConfig;
import joserodpt.mysticosskywars.api.config.chests.NormalChestConfig;
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
import joserodpt.mysticosskywars.plugin.managers.AchievementsManager;
import joserodpt.mysticosskywars.plugin.managers.DatabaseManager;
import joserodpt.mysticosskywars.plugin.managers.HologramManager;
import joserodpt.mysticosskywars.plugin.managers.KitManager;
import joserodpt.mysticosskywars.plugin.managers.LanguageManager;
import joserodpt.mysticosskywars.plugin.managers.LeaderboardManager;
import joserodpt.mysticosskywars.plugin.managers.LobbyManager;
import joserodpt.mysticosskywars.plugin.managers.MapManager;
import joserodpt.mysticosskywars.plugin.managers.PartiesManager;
import joserodpt.mysticosskywars.plugin.managers.PlayerManager;
import joserodpt.mysticosskywars.plugin.managers.ShopManager;
import joserodpt.mysticosskywars.plugin.managers.WorldManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.logging.Logger;

public class MysticosSkywars extends MysticosSkywarsAPI {

    private final Random rand = new Random();
    private final Logger logger;
    private final MysticosSkywarsPlugin plugin;
    private MSWnms mswNMS;
    private final WorldManagerAPI worldManagerAPI;
    private final LanguageManagerAPI languageManagerAPI;
    private final PlayerManagerAPI playerManagerAPI;
    private final MapManagerAPI mapManagerAPI;
    private final LobbyManagerAPI lobbyManagerAPI;
    private final ShopManagerAPI shopManagerAPI;
    private final KitManagerAPI kitManager = new KitManager();
    private final PartiesManagerAPI partiesManagerAPI;
    private final LeaderboardManagerAPI leaderboardManagerAPI;
    private final AchievementsManagerAPI achievementsManagerAPI;
    public final MSWEventsAPI mswEventsAPI = new MSWEventsAPI();
    private DatabaseManagerAPI databaseManagerAPI;
    private final HologramManagerAPI hologramManagerAPI;
    private CurrencyAdapterAPI currencyAdapterAPI;

    public MysticosSkywars(MysticosSkywarsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        lobbyManagerAPI = new LobbyManager(this);
        worldManagerAPI = new WorldManager(this);
        playerManagerAPI = new PlayerManager(this);
        languageManagerAPI = new LanguageManager(this);
        mapManagerAPI = new MapManager(this);
        shopManagerAPI = new ShopManager(this);
        partiesManagerAPI = new PartiesManager(this);
        leaderboardManagerAPI = new LeaderboardManager(this);
        achievementsManagerAPI = new AchievementsManager(this);
        hologramManagerAPI = new HologramManager(this);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public MSWnms getNMS() {
        return mswNMS;
    }

    @Override
    public WorldManagerAPI getWorldManagerAPI() {
        return worldManagerAPI;
    }

    @Override
    public MSWEventsAPI getEventsAPI() {
        return mswEventsAPI;
    }

    @Override
    public LanguageManagerAPI getLanguageManagerAPI() {
        return this.languageManagerAPI;
    }

    @Override
    public PlayerManagerAPI getPlayerManagerAPI() {
        return this.playerManagerAPI;
    }

    @Override
    public MapManagerAPI getMapManagerAPI() {
        return this.mapManagerAPI;
    }

    @Override
    public LobbyManagerAPI getLobbyManagerAPI() {
        return this.lobbyManagerAPI;
    }

    @Override
    public ShopManagerAPI getShopManagerAPI() {
        return this.shopManagerAPI;
    }

    @Override
    public KitManagerAPI getKitManagerAPI() {
        return this.kitManager;
    }

    @Override
    public PartiesManagerAPI getPartiesManagerAPI() {
        return this.partiesManagerAPI;
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }

    @Override
    public DatabaseManagerAPI getDatabaseManagerAPI() {
        return this.databaseManagerAPI;
    }

    @Override
    public LeaderboardManagerAPI getLeaderboardManagerAPI() {
        return this.leaderboardManagerAPI;
    }

    @Override
    public AchievementsManagerAPI getAchievementsManagerAPI() {
        return this.achievementsManagerAPI;
    }

    @Override
    public HologramManagerAPI getHologramManagerAPI() {
        return this.hologramManagerAPI;
    }

    @Override
    public CurrencyAdapterAPI getCurrencyAdapterAPI() {
        return this.currencyAdapterAPI;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    @Override
    public String getSimpleServerVersion() {
        return Bukkit.getServer().getBukkitVersion().split("-")[0];
    }

    @Override
    public void reload() {
        mapManagerAPI.endMaps(false);

        MSWConfig.reload();
        MSWMapsConfig.reload();
        MSWLanguagesOldConfig.reload();

        Debugger.debug = MSWConfig.file().getBoolean("Debug-Mode");

        //chests
        BasicChestConfig.reload();
        NormalChestConfig.reload();
        EPICChestConfig.reload();

        languageManagerAPI.loadLanguages();
        playerManagerAPI.stopScoreboards();
        playerManagerAPI.loadPlayers();
        MSWShopsConfig.reload();
        MSWKitsConfig.reload();
        kitManager.loadKits();

        achievementsManagerAPI.loadAchievements();
        leaderboardManagerAPI.refreshLeaderboards();

        mapManagerAPI.loadMaps();
        lobbyManagerAPI.loadLobby();
    }

    @Override
    public Economy getVaultEconomy() {
        return this.plugin.getEconomy();
    }

    public void setCurrencyAdapter(CurrencyAdapterAPI c) {
        this.currencyAdapterAPI = c;
    }

    public void setNMS(MSWnms nms) {
        this.mswNMS = nms;
    }

    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManagerAPI = databaseManager;
    }
}
