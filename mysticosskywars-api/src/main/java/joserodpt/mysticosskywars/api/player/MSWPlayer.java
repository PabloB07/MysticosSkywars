package joserodpt.mysticosskywars.api.player;

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
import joserodpt.mysticosskywars.api.achievements.MSWAchievement;
import joserodpt.mysticosskywars.api.cages.MSWCage;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.effects.MSWBlockWinTrail;
import joserodpt.mysticosskywars.api.effects.MSWTrail;
import joserodpt.mysticosskywars.api.kits.MSWKit;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.modes.teams.MSWTeam;
import joserodpt.mysticosskywars.api.party.MSWParty;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTab;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabInterface;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabNoStyle;
import joserodpt.mysticosskywars.api.utils.PlayerInput;
import joserodpt.mysticosskywars.api.utils.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MSWPlayer {

    private Player player;
    private String anonName = "?";
    private String language = MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getDefaultLanguage();

    private final List<MSWTrail> MSWTrails = new ArrayList<>();
    private PlayerState state = PlayerState.LOBBY_OR_NOGAME;
    private MapViewerPref playerMapViewerPref;
    private MSWTeam playerTeam;
    private MSWMap playerMap;
    private MSWKit playerKit;
    private MSWCage playerCage;
    private MSWParty playerParty;
    private MSWPlayerScoreboard playerSB;
    private MSWPlayerTabInterface playerTab;

    //statistics
    private int gamekills, kills, deaths, winsSolo, winsTEAMS, loses, gamesPlayed;
    private int rankedTotalkills, rankedDeaths, rankedWinsSolo, rankedWinsTEAMS, rankedLoses, rankedGamesPlayed;

    private Double coins = 0D, balanceGame = 0D;
    private Material cageBlock = Material.GLASS;
    private Particle bowParticle;
    private Material winblockMaterial;
    private Boolean invincible = false, bot = false, winblockRandom = false;

    public MSWPlayer(Player jog, MSWPlayer.PlayerState estado, int kills, int d, int solowin, int teamwin, Double coi, String lang, int l, int gp, int rankedTotalkills, int rankedDeaths, int rankedWinsSolo, int rankedWinsTEAMS, int rankedLoses, int rankedGamesPlayed) {
        this.anonName = Text.anonName();

        this.player = jog;
        this.state = estado;
        this.kills = kills;
        this.winsSolo = solowin;
        this.winsTEAMS = teamwin;
        this.deaths = d;
        this.coins = coi;
        this.language = lang;
        this.loses = l;
        this.gamesPlayed = gp;
        this.playerSB = new MSWPlayerScoreboard(this);

        this.rankedTotalkills = rankedTotalkills;
        this.rankedDeaths = rankedDeaths;
        this.rankedWinsSolo = rankedWinsSolo;
        this.rankedWinsTEAMS = rankedWinsTEAMS;
        this.rankedLoses = rankedLoses;
        this.rankedGamesPlayed = rankedGamesPlayed;

        this.playerTab = MSWConfig.file().getBoolean("Config.Enable-Tab-Formatting", true) ? new MSWPlayerTab(this) : new MSWPlayerTabNoStyle(this);
    }

    public MSWPlayer(boolean anonName) {
        if (anonName) {
            this.anonName = Text.anonName();
        }
        this.bot = true;
    }

    public <T extends Entity> void spawnAbovePlayer(Class<T> c) {
        if (this.player != null) {
            Entity ent = this.getWorld().spawn(this.getLocation().add(0, 3, 0), c);
            if (ent instanceof TNTPrimed) {
                ((TNTPrimed) ent).setFuseTicks(60);
            }
        }
    }

    public boolean isInMatch() {
        return this.playerMap != null;
    }

    public void addStatistic(MSWPlayer.Statistic t, int i, Boolean ranked) {
        switch (t) {
            case SOLO_WIN:
                if (ranked) {
                    this.rankedWinsSolo += i;
                } else {
                    this.winsSolo += i;

                    //achievement
                    MSWAchievement a = MysticosSkywarsAPI.getInstance().getAchievementsManagerAPI().getAchievement(PlayerStatistics.WINS_SOLO, this.winsSolo);
                    if (a != null) {
                        a.giveAchievement(this);
                    }
                }

                this.addStatistic(MSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + MSWConfig.file().getDouble("Config.Coins.Per-Win"));
                this.sendMessage("&e+ &6" + MSWConfig.file().getDouble("Config.Coins.Per-Win") + "&e coins");
                break;
            case TEAM_WIN:
                if (ranked) {
                    this.rankedWinsTEAMS += i;
                } else {
                    this.winsTEAMS += i;

                    //achievement
                    MSWAchievement a = MysticosSkywarsAPI.getInstance().getAchievementsManagerAPI().getAchievement(PlayerStatistics.WINS_TEAMS, this.winsTEAMS);
                    if (a != null) {
                        a.giveAchievement(this);
                    }
                }
                this.addStatistic(MSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + MSWConfig.file().getDouble("Config.Coins.Per-Win"));
                this.sendMessage("&e+ &6" + MSWConfig.file().getDouble("Config.Coins.Per-Win") + "&e coins");
                break;
            case KILL:
                this.gamekills += i;
                this.balanceGame = (this.balanceGame + MSWConfig.file().getDouble("Config.Coins.Per-Kill"));
                this.sendMessage("&e+ &6" + MSWConfig.file().getDouble("Config.Coins.Per-Kill") + "&e coins");

                break;
            case LOSE:
                if (ranked) {
                    this.rankedLoses += i;
                } else {
                    this.loses += i;
                }
                break;
            case DEATH:
                if (ranked) {
                    this.rankedDeaths += i;
                } else {
                    this.deaths += i;
                }
                this.addStatistic(MSWPlayer.Statistic.LOSE, 1, ranked);
                this.addStatistic(MSWPlayer.Statistic.GAMES_PLAYED, 1, ranked);
                this.balanceGame = (this.balanceGame + MSWConfig.file().getDouble("Config.Coins.Per-Death"));
                this.sendMessage("&e- &6" + MSWConfig.file().getDouble("Config.Coins.Per-Death") + "&e coins");
                break;
            case GAMES_PLAYED:
                if (ranked) {
                    this.rankedGamesPlayed += i;
                } else {
                    this.gamesPlayed += i;
                    //achievement
                    MSWAchievement a = MysticosSkywarsAPI.getInstance().getAchievementsManagerAPI().getAchievement(PlayerStatistics.GAMES_PLAYED, this.gamesPlayed);
                    if (a != null) {
                        a.giveAchievement(this);
                    }
                }
                break;
        }
    }

    public void saveData(PlayerData pd) {
        if (pd == PlayerData.GAME) {
            this.kills += this.gamekills;

            MSWAchievement a = MysticosSkywarsAPI.getInstance().getAchievementsManagerAPI().getAchievement(PlayerStatistics.KILLS, this.kills);
            if (a != null) {
                a.giveAchievement(this);
            }

            MysticosSkywarsAPI.getInstance().getCurrencyAdapterAPI().addCoins(this, this.balanceGame);
            this.balanceGame = 0D;
            this.gamekills = 0;
        }

        MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().savePlayer(this, pd);
    }

    public double getGameBalance() {
        return this.balanceGame;
    }

    public void sendMessage(String string) {
        if (!this.bot) {
            this.player.sendMessage(Text.color(string));
        }
    }

    public void enablePlayerScoreboard(boolean val) {
        if (val) {
            this.playerSB = new MSWPlayerScoreboard(this);
        } else {
            this.playerSB.stop();
        }
    }

    public void resetData() {
        MSWPlayer p = this;
        sendMessage("&cAre you sure you want to erase your data? This action is irreversible.");
        sendMessage("&fTo erase your data, type &cyes &fin the chat.");
        new PlayerInput(this.getPlayer(), input -> {
            if (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y")) {
                MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().deletePlayerData(getUUID(), true);
                MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().deletePlayerGameHistory(getUUID(), true);
                MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().deletePlayerBoughtItems(getUUID(), true);
                MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().removePlayer(p);
                getPlayer().kickPlayer(MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getPrefix() + "§4Your data was cleared with success.\n§cPlease join the server again to complete the reset.");
            }
        }, input -> {
        });
    }

    public String getName() {
        return this.player == null ? this.anonName : this.player.getName();
    }

    public void teleport(Location l) {
        if (this.player != null) {
            this.player.teleport(l);
        }
    }

    public void stopTrails() {
        this.MSWTrails.forEach(MSWTrail::cancelTask);
    }

    public void addTrail(MSWTrail t) {
        this.MSWTrails.add(t);
    }

    public void removeTrail(MSWTrail t) {
        this.MSWTrails.remove(t);
    }

    public Location getLocation() {
        return this.player == null ? null : this.player.getLocation();
    }

    public void playSound(Sound s, int i, int i1) {
        if (this.player != null) {
            this.player.playSound(this.player.getLocation(), s, i, i1);
        }
    }

    public World getWorld() {
        return this.player == null ? null : this.player.getWorld();
    }

    public void executeWinBlock(int t) {
        if (t < 0) {
            return;
        }
        if (this.winblockRandom) {
            addTrail(new MSWBlockWinTrail(this, t));
        } else {
            if (this.winblockMaterial != null) {
                addTrail(new MSWBlockWinTrail(this, t, winblockMaterial));
            }
        }
    }

    public void delCage() {
        if (this.playerCage != null) this.playerCage.removePlayer(this);
    }

    public void setFlying(boolean b) {
        if (this.player != null) {
            this.player.setAllowFlight(b);
            this.player.setFlying(b);
        }
    }

    public UUID getUUID() {
        return player == null ? null : player.getUniqueId();
    }

    public String getLanguage() {
        return this.language;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setKit(MSWKit playerKit) {
        this.playerKit = playerKit;
        if (!MSWConfig.file().getBoolean("Config.Shops.Only-Buy-Kits-Per-Match"))
            this.saveData(PlayerData.KIT);
    }

    public void setBowParticle(Particle bowParticle) {
        if (this.bowParticle != null) {
            this.bowParticle = bowParticle;
        }
    }

    public void setCageBlock(Material m) {
        if (m != this.cageBlock) {
            this.cageBlock = m;
            if (isInMatch()) {
                switch (this.getMatch().getGameMode()) {
                    case SOLO:
                        if (hasCage()) {
                            this.playerCage.setCage();
                        }
                        break;
                    case TEAMS:
                        if (hasTeam()) {
                            this.getTeam().getTeamCage().setCage();
                        }
                        break;
                }
            }
        }
        MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().savePlayer(this, PlayerData.CAGE_BLOCK);
    }

    public void setWinBlock(String mat) {
        if (mat.equals("Random-Blocks")) {
            this.winblockRandom = true;
        } else {
            this.winblockRandom = false;
            this.winblockMaterial = Material.getMaterial(mat);
        }
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public void setLanguage(String language) {
        this.language = language;
        this.saveData(PlayerData.LANG);
    }

    public int getStatistics(PlayerStatistics pp, Boolean ranked) {
        switch (pp) {
            case LOSES:
                return ranked ? this.rankedLoses : this.loses;
            case DEATHS:
                return ranked ? this.rankedDeaths : this.deaths;
            case WINS_SOLO:
                return ranked ? this.rankedWinsSolo : this.winsSolo;
            case WINS_TEAMS:
                return ranked ? this.rankedWinsTEAMS : this.winsTEAMS;
            case KILLS:
                return ranked ? this.rankedTotalkills : this.kills;
            case GAMES_PLAYED:
                return ranked ? this.rankedGamesPlayed : this.gamesPlayed;
            case GAME_KILLS:
                return this.gamekills;
        }
        return 1;
    }

    public int getStatistics(PlayerStatistics pp) {
        return getStatistics(pp, this.isInMatch() && this.playerMap.isRanked());
    }

    public List<String> getStats() {
        List<String> list = TranslatableList.STATISTIC_PLAYER_LIST.get(this);

        for (String s : list) {
            list.set(list.indexOf(s), s.replace("%lang%", MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getLanguage(this.getLanguage()).getDisplayName())
                    .replace("%kit%", this.getPlayerKit().getDisplayName())
                    .replace("%cage%", MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getMaterialName(this.getCageBlock()))
                    .replace("%coins%", Text.formatDouble(MysticosSkywarsAPI.getInstance().getCurrencyAdapterAPI().getCoins(this)))
                    .replace("%firstjoin%", MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().getPlayerData(this.getPlayer()).getFirstJoin())
                    .replace("%lastjoin%", MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().getPlayerData(this.getPlayer()).getLastJoin()));
        }

        return list;
    }

    public MSWPlayer.PlayerState getState() {
        return this.state;
    }

    public MSWMap getMatch() {
        return this.playerMap;
    }

    public void setPlayerMap(MSWMap o) {
        this.playerMap = o;
    }

    public MSWPlayerScoreboard getScoreboard() {
        return this.playerSB;
    }

    public MSWKit getPlayerKit() {
        return this.hasKit() ? this.playerKit : new MSWKit();
    }

    public MSWTeam getTeam() {
        return this.playerTeam;
    }

    public void setTeam(MSWTeam o) {
        this.playerTeam = o;
    }

    public Material getCageBlock() {
        return this.cageBlock;
    }

    public Particle getBowParticle() {
        return this.bowParticle;
    }

    public double getLocalCoins() {
        return this.coins;
    }

    public void setLocalCoins(Double v) {
        this.coins = v;
    }

    public void heal() {
        if (this.player != null) {
            this.player.setFireTicks(0);
            this.player.setHealth(20);
            this.player.setFoodLevel(20);

            this.player.getActivePotionEffects().forEach(potionEffect -> this.player.removePotionEffect(potionEffect.getType()));
        }
    }

    public MSWCage getPlayerCage() {
        return this.playerCage;
    }

    public void setPlayerCage(MSWCage c) {
        this.playerCage = c;
    }

    public boolean isInvencible() {
        return this.invincible;
    }

    public void setInvincible(boolean b) {
        this.invincible = b;
    }

    public void leave() {
        if (this.playerMap != null) {
            this.playerMap.removePlayer(this);
        }

        if (this.hasParty()) {
            if (this.getParty().isOwner(this)) {
                this.disbandParty();
            } else {
                this.leaveParty();
            }
        }

        this.stopTrails();

        this.playerSB.stop();
        this.saveData(PlayerData.GAME);
        MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().removePlayer(this);
    }

    public void sendTitle(String s, String s1, int i, int i1, int i2) {
        if (this.player != null) this.player.sendTitle(s, s1, i, i1, i2);
    }

    public boolean hasKit() {
        return this.playerKit != null;
    }

    public boolean hasCage() {
        return this.playerCage != null;
    }

    public PlayerInventory getInventory() {
        if (this.player == null) return null;
        return this.player.getInventory();
    }

    public boolean hasTeam() {
        return this.playerTeam != null;
    }

    public String getDisplayName() {
        return this.bot ? this.anonName : this.player.getDisplayName();
    }

    public void sendCenterMessage(String r) {
        sendMessage(Text.centerMessage(r));
    }

    public boolean isBot() {
        return this.bot;
    }

    public void hidePlayer(Plugin plugin, Player pl) {
        if (!this.bot && pl != null && !MysticosSkywarsAPI.getInstance().getMapManagerAPI().shutdown)
            this.getPlayer().hidePlayer(plugin, pl);
    }

    public void showPlayer(Plugin plugin, Player pl) {
        if (!this.bot && pl != null && !MysticosSkywarsAPI.getInstance().getMapManagerAPI().shutdown)
            this.getPlayer().showPlayer(plugin, pl);
    }

    public MSWPlayerTabInterface getTab() {
        return this.playerTab;
    }

    public enum PlayerData {CAGE_BLOCK, GAME, COINS, LANG, MAPVIEWER_PREF, KIT, FIRST_JOIN, LEGACY_GAME_HISTORY_CLEAR, LEGACY_BOUGHT_ITEMS_CLEAR, LAST_JOIN}

    public void sendActionbar(String s) {
        if (this.player != null)
            this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Text.color(s)));
    }

    public boolean hasParty() {
        return this.playerParty != null;
    }

    public void createParty() {
        this.playerParty = new MSWParty(this);
    }

    public void disbandParty() {
        this.playerParty.disband();
    }

    public MSWParty getParty() {
        return this.playerParty;
    }

    public void joinParty(MSWPlayer player) {
        this.playerParty = player.getParty();
    }

    public void leaveParty() {
        this.playerParty.playerLeave(this);
        this.playerParty = null;
        this.sendMessage(TranslatableLine.PARTY_LEAVE.get(this, true).replace("%player%", this.getDisplayName()));
    }

    public MapViewerPref getPlayerMapViewerPref() {
        return this.playerMapViewerPref;
    }

    public void setPlayerMapViewerPref(MapViewerPref a) {
        this.playerMapViewerPref = a;
    }

    public void closeInventory() {
        if (this.player != null) {
            this.player.closeInventory();
        }
    }

    public void setBarNumber(int xp) {
        if (this.player != null) {
            this.player.setLevel(xp);
            this.player.setExp(xp);
        }
    }

    public void setBarNumber(int xp, int max) {
        if (this.player != null && xp != 0 && max != 0) {
            this.player.setLevel(xp);
            float div = (float) xp / (float) max;
            this.player.setExp(div);
        }
    }

    public void setGameMode(GameMode gameMode) {
        if (!this.isBot()) {
            this.getPlayer().setGameMode(gameMode);
        }
    }

    //ENUMs

    public enum PlayerState {
        LOBBY_OR_NOGAME, CAGE, PLAYING, SPECTATOR, EXTERNAL_SPECTATOR
    }

    public enum Statistic {KILL, SOLO_WIN, TEAM_WIN, LOSE, DEATH, GAMES_PLAYED}

    public enum PlayerStatistics {
        WINS_SOLO, WINS_TEAMS, KILLS, DEATHS, LOSES, GAMES_PLAYED, GAME_KILLS;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case WINS_SOLO:
                    return TranslatableLine.STATISTIC_WINS_SOLO.get(p);
                case WINS_TEAMS:
                    return TranslatableLine.STATISTIC_WINS_TEAMS.get(p);
                case KILLS:
                    return TranslatableLine.STATISTIC_KILLS.get(p);
                case DEATHS:
                    return TranslatableLine.STATISTIC_DEATHS.get(p);
                case LOSES:
                    return TranslatableLine.STATISTIC_LOSES.get(p);
                case GAMES_PLAYED:
                    return TranslatableLine.STATISTIC_GAMES_PLAYED.get(p);
                case GAME_KILLS:
                    return TranslatableLine.STATISTIC_GAMES_KILLS.get(p);
                default:
                    return "?";
            }
        }
    }

    public enum MapViewerPref {
        MAPV_SPECTATE, MAPV_AVAILABLE, MAPV_STARTING, MAPV_WAITING, MAPV_ALL, SOLO, TEAMS, SOLO_RANKED, TEAMS_RANKED;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case MAPV_ALL:
                    return TranslatableLine.MAP_ALL.get(p);
                case MAPV_WAITING:
                    return TranslatableLine.MAP_STATE_WAITING.get(p);
                case MAPV_SPECTATE:
                    return TranslatableLine.MAP_SPECTATE.get(p);
                case MAPV_STARTING:
                    return TranslatableLine.MAP_STATE_STARTING.get(p);
                case MAPV_AVAILABLE:
                    return TranslatableLine.MAP_STATE_AVAILABLE.get(p);
                case SOLO:
                    return MSWMap.GameMode.SOLO.getDisplayName(p);
                case SOLO_RANKED:
                    return TranslatableLine.SOLO_RANKED_MODE.get(p);
                case TEAMS_RANKED:
                    return TranslatableLine.TEAMS_RANKED_MODE.get(p);
                case TEAMS:
                    return MSWMap.GameMode.TEAMS.getDisplayName(p);
                default:
                    return "?";
            }
        }
    }
}
