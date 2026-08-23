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

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import joserodpt.realpermissions.api.RealPermissionsAPI;
import joserodpt.realpermissions.api.pluginhook.ExternalPlugin;
import joserodpt.realpermissions.api.pluginhook.ExternalPluginPermission;
import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.chests.TierViewer;
import joserodpt.mysticosskywars.api.config.MSWAchievementsConfig;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWKitsConfig;
import joserodpt.mysticosskywars.api.config.MSWLanguagesOldConfig;
import joserodpt.mysticosskywars.api.config.MSWMapsConfig;
import joserodpt.mysticosskywars.api.config.MSWSQLConfig;
import joserodpt.mysticosskywars.api.config.MSWShopsConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.chests.BasicChestConfig;
import joserodpt.mysticosskywars.api.config.chests.EPICChestConfig;
import joserodpt.mysticosskywars.api.config.chests.NormalChestConfig;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.managers.TransactionManager;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.nms.NMS114R1tov116R3;
import joserodpt.mysticosskywars.api.nms.NMS117R1;
import joserodpt.mysticosskywars.api.nms.NMS118R2andUP;
import joserodpt.mysticosskywars.api.utils.GUIBuilder;
import joserodpt.mysticosskywars.api.utils.PlayerInput;
import joserodpt.mysticosskywars.api.utils.Text;
import joserodpt.mysticosskywars.plugin.commands.BaseCommandWA;
import joserodpt.mysticosskywars.plugin.commands.PartyCMD;
import joserodpt.mysticosskywars.plugin.commands.MysticosSkywarsCMD;
import joserodpt.mysticosskywars.plugin.commands.SairCMD;
import joserodpt.mysticosskywars.plugin.currency.LocalCurrencyAdapter;
import joserodpt.mysticosskywars.plugin.currency.VaultCurrencyAdapter;
import joserodpt.mysticosskywars.plugin.gui.guis.AchievementViewerGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.GameHistoryGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.KitSettingsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapDashboardGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapEventEditorGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapSettingsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.MapsListGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.PlayerGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.PlayerItemsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.SettingsGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.ShopGUI;
import joserodpt.mysticosskywars.plugin.gui.guis.VoteGUI;
import joserodpt.mysticosskywars.plugin.listeners.EventListener;
import joserodpt.mysticosskywars.plugin.listeners.PlayerListener;
import joserodpt.mysticosskywars.plugin.listeners.LuckyBlockListener;
import joserodpt.mysticosskywars.plugin.managers.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MysticosSkywarsPlugin extends JavaPlugin {
    private static MysticosSkywarsPlugin pl;

    public static MysticosSkywarsPlugin getPlugin() {
        return pl;
    }

    private MysticosSkywars mysticosSkywars;

    private final PluginManager pm = Bukkit.getPluginManager();

    public void onEnable() {
        final long start = System.currentTimeMillis();
        pl = this;
        mysticosSkywars = new MysticosSkywars(this);
        MysticosSkywarsAPI.setInstance(mysticosSkywars);
        //setup metrics
        new Metrics(this, 16365);

        //verify nms version
        if (!setupNMS()) {
            getLogger().severe("Your server version is not currently supported by MysticosSkywars.");
            getLogger().severe("If you think this is a bug, contact JoseGamer_PT.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        MSWConfig.setup(this);
        MSWLanguagesOldConfig.setup(this);

        mysticosSkywars.getLanguageManagerAPI().loadLanguages();
        if (mysticosSkywars.getLanguageManagerAPI().areLanguagesEmpty()) {
            getLogger().severe("[ERROR] No Languages have been detected. Stopped loading.");
            HandlerList.unregisterAll(this);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Debugger.debug = MSWConfig.file().getBoolean("Debug-Mode");
        Debugger.print(MysticosSkywars.class, "DEBUG MODE ENABLED");
        Debugger.execute();
        mysticosSkywars.getLobbyManagerAPI().loadLobby();

        //config
        MSWAchievementsConfig.setup(this);
        MSWMapsConfig.setup(this);
        MSWSQLConfig.setup(this);
        MSWShopsConfig.setup(this);
        MSWKitsConfig.setup(this);

        //chests
        BasicChestConfig.setup(this);
        NormalChestConfig.setup(this);
        EPICChestConfig.setup(this);

        try {
            mysticosSkywars.setDatabaseManager(new DatabaseManager(mysticosSkywars));
        } catch (SQLException a) {
            getLogger().severe("Error while creating Database Manager for MysticosSkywars: " + a.getMessage());
        }

        pm.registerEvents(new PlayerListener(mysticosSkywars), this);
        pm.registerEvents(new EventListener(mysticosSkywars), this);
        pm.registerEvents(new LuckyBlockListener(), this);
        pm.registerEvents(PlayerInput.getListener(), this);
        pm.registerEvents(GUIBuilder.getListener(), this);
        pm.registerEvents(GameHistoryGUI.getListener(), this);
        pm.registerEvents(MapSettingsGUI.getListener(), this);
        pm.registerEvents(MapDashboardGUI.getListener(), this);
        pm.registerEvents(MapEventEditorGUI.getListener(), this);
        pm.registerEvents(PlayerGUI.getListener(), this);
        pm.registerEvents(ShopGUI.getListener(), this);
        pm.registerEvents(PlayerItemsGUI.getListener(), this);
        pm.registerEvents(MapsListGUI.getListener(), this);
        pm.registerEvents(TierViewer.getListener(), this);
        pm.registerEvents(AchievementViewerGUI.getListener(), this);
        pm.registerEvents(GameHistoryGUI.getListener(), this);
        pm.registerEvents(KitSettingsGUI.getListener(), this);
        pm.registerEvents(VoteGUI.getListener(), this);
        pm.registerEvents(SettingsGUI.getListener(), this);

        mysticosSkywars.getShopManagerAPI().loadShopItems();
        mysticosSkywars.getKitManagerAPI().loadKits();
        getLogger().info("Loaded " + mysticosSkywars.getKitManagerAPI().getKits().size() + " kits.");

        mysticosSkywars.getMapManagerAPI().loadMaps();
        getLogger().info("Loaded " + mysticosSkywars.getMapManagerAPI().getMaps(MapManagerAPI.MapGamemodes.ALL).size() + " maps.");
        mysticosSkywars.getPlayerManagerAPI().loadPlayers();

        if (MSWConfig.file().getBoolean("Config.Bungeecord.Enabled")) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            getLogger().info("BungeeCord mode is enabled.");
        }

        //load achievements
        mysticosSkywars.getAchievementsManagerAPI().loadAchievements();

        //load leaderboard
        mysticosSkywars.getLeaderboardManagerAPI().refreshLeaderboards();

        BukkitCommandManager<CommandSender> commandManager = BukkitCommandManager.create(this);

        //command suggestions
        commandManager.registerSuggestion(SuggestionKey.of("#createsuggestions"), (sender, context) -> IntStream.range(0, 200)
                .mapToObj(i -> "Map" + i)
                .collect(Collectors.toCollection(ArrayList::new)));

        commandManager.registerSuggestion(SuggestionKey.of("#maps"), (sender, context) -> new ArrayList<>(mysticosSkywars.getMapManagerAPI().getMapNames()));
        commandManager.registerSuggestion(SuggestionKey.of("#boolean"), (sender, context) -> Arrays.asList("false", "true"));
        commandManager.registerSuggestion(SuggestionKey.of("#worldtype"), (sender, context) -> Arrays.asList("default", "schematic"));
        commandManager.registerSuggestion(SuggestionKey.of("#kits"), (sender, context) -> mysticosSkywars.getKitManagerAPI().getKits().stream()
                .map(kit -> Text.strip(kit.getName()))
                .collect(Collectors.toList()));

        commandManager.registerArgument(MSWChest.Tier.class, (sender, argument) -> {
            try {
                return MSWChest.Tier.valueOf(argument.toString().toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });
        commandManager.registerArgument(MSWChest.Type.class, (sender, argument) -> {
            try {
                return MSWChest.Type.valueOf(argument.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });
        commandManager.registerArgument(MSWMap.GameMode.class, (sender, argument) -> {
            try {
                return MSWMap.GameMode.valueOf(argument.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });
        commandManager.registerArgument(MSWWorld.WorldType.class, (sender, argument) -> {
            try {
                return MSWWorld.WorldType.valueOf(argument.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });
        commandManager.registerArgument(TransactionManager.Operations.class, (sender, argument) -> {
            try {
                return TransactionManager.Operations.valueOf(argument.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });
        commandManager.registerArgument(MysticosSkywarsCMD.KIT_OPERATION.class, (sender, argument) -> {
            try {
                return MysticosSkywarsCMD.KIT_OPERATION.valueOf(argument.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        });

        //registo de comandos #portugal
        Map<String, BaseCommandWA> commands = new HashMap<>();
        registerCommand("mysticosskywars", new MysticosSkywarsCMD(mysticosSkywars), commands, commandManager);
        registerCommand("leave", new SairCMD(mysticosSkywars), commands, commandManager);
        registerCommand("party", new PartyCMD(mysticosSkywars), commands, commandManager);

        commandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, context) -> sender.sendMessage(mysticosSkywars.getLanguageManagerAPI().getPrefix() + TranslatableLine.CMD_NOT_FOUND.getDefault()));
        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> sender.sendMessage());
        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, context) -> {
            Text.send(sender, mysticosSkywars.getLanguageManagerAPI().getPrefix() + commands.get(context.getCommand()).getWrongUsage(context.getSubCommand()));
        });

        //placeholderAPI support
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooked on PlaceholderAPI!");
            new MysticosSkywarsPlaceholderAPI(mysticosSkywars).register();
        }

        //hook into vault
        if (setupEconomy()) {
            getLogger().info("Vault found and Hooked into!");
            if (MSWConfig.file().getBoolean("Config.Use-Vault-As-Currency")) {
                mysticosSkywars.setCurrencyAdapter(new VaultCurrencyAdapter());
                getLogger().info("Currency via Vault has been enabled.");
            } else {
                mysticosSkywars.setCurrencyAdapter(new LocalCurrencyAdapter());
                getLogger().info("Local currency has been enabled, as specified in the config file.");
            }
        } else {
            mysticosSkywars.setCurrencyAdapter(new LocalCurrencyAdapter());
            getLogger().info("Vault not found. Local currency will be used.");
        }

        //refresh leaderboards
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mysticosSkywars.getLeaderboardManagerAPI()::refreshLeaderboards, MSWConfig.file().getInt("Config.Refresh-Leaderboards"), MSWConfig.file().getInt("Config.Refresh-Leaderboards"));

        if (getServer().getPluginManager().getPlugin("RealPermissions") != null) {
            //register RealMines permissions onto RealPermissions
            try {
                RealPermissionsAPI.getInstance().getHooksAPI().addHook(new ExternalPlugin(this.getDescription().getName(), "&fMysticos&aSkywars", this.getDescription().getDescription(), Material.BOW, Arrays.asList(
                        new ExternalPluginPermission("msw.basic", "Permission for voting on the Basic Chest Tier."),
                        new ExternalPluginPermission("msw.normal", "Permission for voting on the Normal Chest Tier."),
                        new ExternalPluginPermission("msw.epic", "Permission for voting on the Epic Chest Tier."),
                        new ExternalPluginPermission("msw.day", "Permission for voting on the Game Time Day."),
                        new ExternalPluginPermission("msw.sunset", "Permission for voting on the Game Time Sunset."),
                        new ExternalPluginPermission("msw.night", "Permission for voting on the Game Time Night."),
                        new ExternalPluginPermission("msw.normal-projectile", "Permission for voting on the Game Normal Projectiles."),
                        new ExternalPluginPermission("msw.break-projectile", "Permission for voting the on Game Break Projectiles."),
                        new ExternalPluginPermission("msw.join", "Allow access to the maps menu.", List.of("msw list")),
                        new ExternalPluginPermission("msw.kits", "Allow access to the kits menu.", List.of("msw kits")),
                        new ExternalPluginPermission("msw.shop", "Allow access to the shop menu.", List.of("msw shop")),
                        new ExternalPluginPermission("msw.coins", "Allow checking the player's current balance.", List.of("msw coins")),
                        new ExternalPluginPermission("msw.lobby", "Allow teleportation to the lobby.", List.of("msw lobby")),
                        new ExternalPluginPermission("msw.forcestart", "Allow force starting the current match.", List.of("msw forcestart")),
                        new ExternalPluginPermission("msw.leave", "Allow leaving the current match.", List.of("msw leave")),
                        new ExternalPluginPermission("msw.party.owner", "Allow party owner commands.", Arrays.asList("party create", "party disband", "party kick")),
                        new ExternalPluginPermission("msw.party.invite", "Allow party invite commands.", List.of("party invite")),
                        new ExternalPluginPermission("msw.party.accept", "Allow accepting a party invite.", List.of("party accept")),
                        new ExternalPluginPermission("msw.party.leave", "Allow leaving a party.", List.of("party leave"))
                ), this.getDescription().getVersion()));
            } catch (Exception e) {
                getLogger().warning("Error while trying to register MysticosSkywars permissions onto RealPermissions.");
                e.printStackTrace();
            }
        }

        getLogger().info("Finished loading in " + ((System.currentTimeMillis() - start) / 1000F) + " seconds.");
        getLogger().info("<------------- MysticosSkywars vPT ------------->".replace("PT", this.getDescription().getVersion()));
    }

    private void registerCommand(String realmines, BaseCommandWA mineCMD, Map<String, BaseCommandWA> commands, BukkitCommandManager<CommandSender> commandManager) {
        commands.put(realmines, mineCMD);
        commandManager.registerCommand(mineCMD);
    }

    public void logWithColor(String s) {
        getServer().getConsoleSender().sendMessage("[" + this.getDescription().getName() + "] " + Text.color(s));
    }

    public void onDisable() {
        mysticosSkywars.getMapManagerAPI().endMaps(true);

        if (MSWConfig.file().getBoolean("Config.Bungeecord.Enabled")) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        }

        HandlerList.unregisterAll(this);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private boolean setupNMS() {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];
        getLogger().info("Server version: " + version);

        switch (version) {
            case "1.17.1":
            case "1.17":
                getLogger().info("Using the 1.17.1 NMS adapter.");
                mysticosSkywars.setNMS(new NMS117R1());
            case "1.16.5":
            case "1.16.4":
            case "1.16.3":
            case "1.16.2":
            case "1.16.1":
            case "1.16":
            case "1.15.2":
            case "1.15.1":
            case "1.15":
            case "1.14.4":
            case "1.14.3":
            case "1.14.2":
            case "1.14.1":
            case "1.14":
                getLogger().info("Using the 1.14-1.16.5 NMS adapter.");
                mysticosSkywars.setNMS(new NMS114R1tov116R3());
                break;
            default:
                getLogger().info("Using default 1.18.2+ NMS adapter.");
                mysticosSkywars.setNMS(new NMS118R2andUP());
                break;
        }
        return mysticosSkywars.getNMS() != null;
    }

    private static Economy vaultEconomy = null;

    public Economy getEconomy() {
        return vaultEconomy;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

}
