package joserodpt.mysticosskywars.api.map;

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
import joserodpt.mysticosskywars.api.cages.MSWCage;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWMapsConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.database.PlayerGameHistoryRow;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.modes.teams.MSWTeam;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.MSWPlayerItems;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabInterface;
import joserodpt.mysticosskywars.api.utils.BungeecordUtils;
import joserodpt.mysticosskywars.api.utils.CountdownTimer;
import joserodpt.mysticosskywars.api.utils.Demolition;
import joserodpt.mysticosskywars.api.utils.Itens;
import joserodpt.mysticosskywars.api.utils.MapCuboid;
import joserodpt.mysticosskywars.api.utils.MathUtils;
import joserodpt.mysticosskywars.api.utils.Text;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class MSWMap {

    private final String name;
    private String displayName;
    private Location spectatorLocation;
    private final String schematicName;
    private final int maxPlayers;
    private int borderSize;
    private int timePassed;
    private int timeEndGame;
    private int timeToStart;
    private int maxGameTime;
    private int invincibilitySeconds;
    private int startingPlayers;
    private boolean specEnabled, instantEnding, ranked, borderEnabled;
    private boolean unregistered = false;

    private final MSWWorld world;
    private MapCuboid mapCuboid;
    private final Map<Location, MSWChest> chests;
    private final Map<Location, MSWSign> signs;
    private WorldBorder border;

    private MapState state;
    private MSWBossbar bossbar;
    private MSWChest.Tier chestTier = MSWChest.Tier.NORMAL;
    private CountdownTimer mapTimer, startMapTimer, finishingTimer;
    private BukkitTask timeCounterTask;
    private ProjectileType projectileType = ProjectileType.NORMAL;
    private TimeType timeType = TimeType.DAY;
    private List<MSWMapEvent> events;
    private final List<MSWPlayer> inMap = new ArrayList<>();
    private final Map<UUID, Integer> chestVotes = new HashMap<>();
    private final Map<UUID, Integer> projectileVotes = new HashMap<>();
    private final Map<UUID, Integer> timeVotes = new HashMap<>();

    public MSWMap(String nome, String displayName, World w, String schematicName, MSWWorld.WorldType wt, MapState state, int maxPlayers, Location spectatorLocation, Boolean specEnabled, Boolean instantEnding, Boolean borderEnabled, Location pos1, Location pos2, Map<Location, MSWChest> chests, Boolean rankd, Boolean unregistered) {
        this.name = nome;
        this.displayName = displayName;
        this.schematicName = schematicName;
        this.unregistered = unregistered;

        this.world = new MSWWorld(this, w, wt);

        if (pos1 != null && pos2 != null) {
            this.mapCuboid = new MapCuboid(pos1, pos2);
            this.borderSize = Math.max(this.mapCuboid.getSizeX(), this.mapCuboid.getSizeZ()); //set bigger size from x or z
            this.border = w.getWorldBorder();
            this.border.setCenter(this.mapCuboid.getCenter());
            this.border.setSize(this.borderSize);
        }

        this.state = state;
        this.maxPlayers = maxPlayers;
        this.maxGameTime = MSWMapsConfig.file().getInt(this.getName() + ".Settings.Max-Game-Time", -1);
        if (this.maxGameTime == -1) {
            this.maxGameTime = MSWConfig.file().getInt("Config.Maximum-Game-Time." + this.getGameMode().getSimpleName());
            MSWMapsConfig.file().set(this.getName() + ".Settings.Max-Game-Time", this.getMaxGameTime());
            MSWMapsConfig.save();
        }

        this.invincibilitySeconds = MSWMapsConfig.file().getInt(this.getName() + ".Settings.Invincibility-Seconds", -1);
        if (this.invincibilitySeconds == -1) {
            this.invincibilitySeconds = MSWConfig.file().getInt("Config.Invincibility-Seconds");
            MSWMapsConfig.file().set(this.getName() + ".Settings.Invincibility-Seconds", this.getInvincibilitySeconds());
            MSWMapsConfig.save();
        }

        this.timeEndGame = MSWMapsConfig.file().getInt(this.getName() + ".Settings.Time-End-Game", -1);
        if (this.timeEndGame == -1) {
            this.timeEndGame = MSWConfig.file().getInt("Config.Time-EndGame");
            MSWMapsConfig.file().set(this.getName() + ".Settings.Time-End-Game", this.getTimeEndGame());
            MSWMapsConfig.save();
        }

        this.timeToStart = MSWMapsConfig.file().getInt(this.getName() + ".Settings.Time-To-Start", -1);
        if (this.timeToStart == -1) {
            this.timeToStart = MSWConfig.file().getInt("Config.Time-To-Start");
            MSWMapsConfig.file().set(this.getName() + ".Settings.Time-To-Start", this.getTimeEndGame());
            MSWMapsConfig.save();
        }

        this.spectatorLocation = spectatorLocation;
        this.specEnabled = specEnabled;
        this.instantEnding = instantEnding;
        this.borderEnabled = borderEnabled;

        this.chests = chests;
        this.ranked = rankd;

        this.events = parseEvents();

        this.chestVotes.put(UUID.randomUUID(), 2);
        this.projectileVotes.put(UUID.randomUUID(), 1);
        this.timeVotes.put(UUID.randomUUID(), 1);

        this.bossbar = new MSWBossbar(this);

        //load signs
        this.signs = loadSigns();
    }

    public MSWMap(String nome) {
        this.name = nome;
        this.displayName = nome;
        this.world = null;
        this.mapCuboid = null;
        this.chests = null;
        this.signs = null;
        this.maxPlayers = -1;
        this.border = null;
        this.borderSize = -1;
        this.spectatorLocation = null;
        this.schematicName = "";
    }

    public String forceStart(MSWPlayer p) {
        if (canStartMap()) {
            return TranslatableLine.CMD_CANT_FORCESTART.get(p, true);
        } else {
            switch (this.getState()) {
                case PLAYING:
                case FINISHING:
                    return TranslatableLine.ALREADY_STARTED.get(p, true);
                default:
                    this.forceStartMap();
                    return TranslatableLine.CMD_MATCH_FORCESTART.get(p, true);
            }
        }
    }

    public void startTimers() {
        this.mapTimer = new CountdownTimer(MysticosSkywarsAPI.getInstance().getPlugin(), this.getMaxGameTime(), () -> {
        }, () -> {
        }, (t) -> {
            this.bossbar.tick();
            if (this.getInvincibilitySeconds() == t.getPassedSeconds()) {
                this.getPlayers().forEach(mswPlayer -> {
                    mswPlayer.setInvincible(false);
                    TranslatableLine.INVINCIBILITY_END.send(mswPlayer, true);
                });
            }
        }
        );
        this.mapTimer.scheduleTimer();

        this.timeCounterTask = new BukkitRunnable() {
            public void run() {
                ++timePassed;
                tickEvents();
            }
        }.runTaskTimer(MysticosSkywarsAPI.getInstance().getPlugin(), 0, 20);
    }

    private void tickEvents() {
        new ArrayList<>(this.events).forEach(MSWMapEvent::tick);
    }

    public BukkitTask getTimeCounterTask() {
        return this.timeCounterTask;
    }

    public CountdownTimer getMapTimer() {
        return this.mapTimer;
    }

    public CountdownTimer getStartMapTimer() {
        return this.startMapTimer;
    }

    public Location getPOS1() {
        return this.mapCuboid.getPOS1();
    }

    public Location getPOS2() {
        return this.mapCuboid.getPOS2();
    }

    public ProjectileType getProjectileTier() {
        return this.projectileType;
    }

    public TimeType getTimeType() {
        return this.timeType;
    }

    public void setRanked(Boolean ranked) {
        this.ranked = ranked;
    }

    public Boolean isRanked() {
        return this.ranked;
    }

    public Boolean isBorderEnabled() {
        return this.borderEnabled;
    }

    public boolean isFull() {
        return this.getPlayerCount() == this.getMaxPlayers();
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setStartingPlayers(int startingPlayers) {
        this.startingPlayers = startingPlayers;
    }

    public int getStartingPlayers() {
        return this.startingPlayers;
    }

    public MSWBossbar getBossBar() {
        return this.bossbar;
    }

    public WorldBorder getBorder() {
        return this.border;
    }

    public int getPlayerCount() {
        return this.getPlayers().size();
    }

    public List<MSWPlayer> getPlayers() {
        List<MSWPlayer> players = new ArrayList<>();
        for (MSWPlayer mswPlayer : this.inMap) {
            if (mswPlayer.getState() == MSWPlayer.PlayerState.PLAYING || mswPlayer.getState() == MSWPlayer.PlayerState.CAGE)
                players.add(mswPlayer);
        }

        return players;
    }

    public List<MSWPlayer> getAllPlayers() {
        return this.inMap;
    }

    public int getSpectatorsCount() {
        return this.getSpectators().size();
    }

    public List<MSWPlayer> getSpectators() {
        List<MSWPlayer> players = new ArrayList<>();
        for (MSWPlayer mswPlayer : this.inMap) {
            if (mswPlayer.getState() == MSWPlayer.PlayerState.SPECTATOR || mswPlayer.getState() == MSWPlayer.PlayerState.EXTERNAL_SPECTATOR)
                players.add(mswPlayer);
        }
        return players;
    }

    public MSWWorld getMSWWorld() {
        return this.world;
    }

    public void kickPlayers(String msg) {
        for (MSWPlayer p : new ArrayList<>(this.inMap)) {
            if (msg != null) {
                p.sendMessage(Text.color(msg));
            } else {
                TranslatableLine.ARENA_RESET.send(p, true);
            }
            this.removePlayer(p);
        }
    }

    public MapState getState() {
        return this.state;
    }

    public void setState(MapState w) {
        this.state = w;
        this.getMysticosSkywarsAPI().getEventsAPI().callRoomStateChange(this);
        if (this.bossbar != null)
            this.bossbar.setState(w);
    }

    abstract public boolean canStartMap();

    abstract public void removePlayer(MSWPlayer p);

    public Location getSpectatorLocation() {
        return this.spectatorLocation == null ? this.getMSWWorld().getWorld().getSpawnLocation() : new Location(this.getMSWWorld().getWorld(), this.spectatorLocation.getBlockX(), this.spectatorLocation.getBlockY(), this.spectatorLocation.getBlockZ());
    }

    public void setTierType(MSWChest.Tier tier) {
        this.chestTier = tier;
        this.getChests().forEach(swChest -> swChest.setLoot(tier.getChest(swChest.getType()), tier.getMaxItemsPerChest()));
    }

    public void setTime(TimeType tt) {
        this.timeType = tt;
        switch (this.timeType) {
            case DAY:
                this.getMSWWorld().getWorld().setTime(0);
                break;
            case NIGHT:
                this.getMSWWorld().getWorld().setTime(13000);
                break;
            case SUNSET:
                this.getMSWWorld().getWorld().setTime(11999);
                break;
            case RAIN:
                this.getMSWWorld().getWorld().setStorm(true);
                break;
        }
    }

    public void setProjectiles(ProjectileType pt) {
        this.projectileType = pt;
    }

    abstract public void addPlayer(MSWPlayer gp);

    public boolean isSpectatorEnabled() {
        return this.specEnabled;
    }

    public boolean isInstantEndEnabled() {
        return this.instantEnding;
    }

    public MSWChest.Tier getChestTier() {
        return this.chestTier;
    }

    public int getTimePassed() {
        return this.timePassed;
    }

    abstract public void resetArena(OperationReason rr);

    public void setSpectating(boolean b) {
        this.specEnabled = b;
    }

    public void setInstantEnding(boolean b) {
        this.instantEnding = b;
    }

    public void spectate(MSWPlayer p, SpectateType st, Location killLoc) {
        p.setInvincible(true);
        p.setFlying(true);
        p.setGameMode(org.bukkit.GameMode.CREATIVE);

        switch (st) {
            case INSIDE_GAME:
                p.setState(MSWPlayer.PlayerState.SPECTATOR);
                p.heal();
                p.getPlayer().teleport(killLoc.add(0, 1, 0));

                if (p.hasTeam()) {
                    MSWTeam t = p.getTeam();
                    t.removeMember(p);
                    if (t.isEliminated()) {
                        new Demolition(this.getSpectatorLocation(), p.getPlayerCage(), 5, 3).start(MysticosSkywarsAPI.getInstance().getPlugin());
                    }
                }
                //update tab
                if (!p.isBot()) {
                    for (MSWPlayer mswPlayer : this.inMap) {
                        if (!mswPlayer.isBot()) {
                            MSWPlayerTabInterface rt = mswPlayer.getTab();
                            rt.removePlayers(p.getPlayer());
                            rt.updateRoomTAB();
                        }
                    }
                }

                if (p.hasKit()) {
                    p.getPlayerKit().cancelTasks();
                }

                this.sendLog(p, false);

                //click to play again
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(" > " + TranslatableLine.PLAY_AGAIN.get(p)));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msw play " + this.getGameMode().name().toLowerCase()));
                p.getPlayer().spigot().sendMessage(component);

                this.checkWin();
                break;
            case EXTERNAL:
                this.inMap.add(p);
                p.setPlayerMap(this);
                this.getBossBar().addPlayer(p.getPlayer());

                p.setState(MSWPlayer.PlayerState.SPECTATOR);
                p.teleport(this.getSpectatorLocation());
                p.heal();

                //update tab
                if (!p.isBot()) {
                    for (MSWPlayer mswPlayer : this.inMap) {
                        if (!mswPlayer.isBot()) {
                            MSWPlayerTabInterface rt = mswPlayer.getTab();
                            List<Player> players = this.getPlayers().stream().map(MSWPlayer::getPlayer).collect(Collectors.toList());
                            rt.clear();
                            rt.addPlayers(players);
                            rt.updateRoomTAB();
                        }
                    }
                }

                TranslatableLine.MATCH_SPECTATE.send(p, true);
                break;
        }

        p.getInventory().clear();
        MSWPlayerItems.SPECTATOR.giveSet(p);
    }

    abstract public void checkWin();

    abstract public GameMode getGameMode();

    abstract public Collection<MSWCage> getCages();

    abstract public Collection<MSWTeam> getTeams();

    abstract public int getMaxTeamsNumber();

    abstract public int getMaxTeamsMembers();

    public void clear() {
        this.world.deleteWorld(OperationReason.RESET);
    }

    public void reset() {
        this.setState(MapState.RESETTING);

        this.kickPlayers(null);
        this.resetArena(OperationReason.RESET);
    }

    public MapCuboid getMapCuboid() {
        return this.mapCuboid;
    }

    public int getBorderSize() {
        return this.borderSize;
    }

    public void addVote(UUID u, VoteType vt, int i) {
        switch (vt) {
            case CHESTS:
                this.chestVotes.put(u, i);
                break;
            case PROJECTILES:
                this.projectileVotes.put(u, i);
                break;
            case TIME:
                this.timeVotes.put(u, i);
                break;
        }
    }

    public Map<UUID, Integer> getChestVotes() {
        return this.chestVotes;
    }

    public Map<UUID, Integer> getProjectileVotes() {
        return this.projectileVotes;
    }

    public Map<UUID, Integer> getTimeVotes() {
        return this.timeVotes;
    }

    public boolean hasVotedFor(VoteType vt, UUID uuid) {
        switch (vt) {
            case CHESTS:
                return this.chestVotes.containsKey(uuid);
            case TIME:
                return this.timeVotes.containsKey(uuid);
            case PROJECTILES:
                return this.projectileVotes.containsKey(uuid);
        }
        return false;
    }

    public Collection<MSWChest> getChests() {
        return this.chests.values();
    }

    public Map<Location, MSWChest> getChestsMap() {
        return this.chests;
    }

    public List<MSWMapEvent> getEvents() {
        this.events.sort(Comparator.comparingInt(MSWMapEvent::getTime));
        return this.events;
    }

    public int getMaxGameTime() {
        return this.maxGameTime;
    }

    public int getInvincibilitySeconds() {
        return this.invincibilitySeconds;
    }

    public int getTimeEndGame() {
        return this.timeEndGame;
    }

    public int getTimeToStart() {
        return this.timeToStart;
    }

    public MSWChest getChest(Location l) {
        return this.getChests().stream().filter(chest -> chest.getLocation().equals(l)).findFirst().orElse(null);
    }

    public String getShematicName() {
        return this.schematicName;
    }

    private Map<Location, MSWSign> loadSigns() {
        Map<Location, MSWSign> list = new HashMap<>();

        if (MSWMapsConfig.file().isList(this.getName() + ".Signs")) {
            for (String i : MSWMapsConfig.file().getStringList(this.getName() + ".Signs")) {

                String[] signData = i.split("<");
                World w = Bukkit.getWorld(signData[0]);
                int x = Integer.parseInt(signData[1]);
                int y = Integer.parseInt(signData[2]);
                int z = Integer.parseInt(signData[3]);

                Location l = new Location(w, x, y, z);
                assert w != null;
                list.put(l, new MSWSign(this, w.getBlockAt(l)));
            }
        }
        return list;
    }

    public void addSign(Block b) {
        this.signs.put(b.getLocation(), new MSWSign(this, b));
        this.saveSigns();
    }

    public void updateSigns() {
        this.getSigns().forEach(MSWSign::update);
    }

    public void removeSign(Block b) {
        MSWSign tmp = null;
        for (MSWSign sign : this.getSigns()) {
            if (sign.getBlock().equals(b)) {
                tmp = sign;
                sign.getBehindBlock().setType(Material.BLACK_CONCRETE);
                sign.delete();
            }
        }
        if (tmp != null) {
            this.signs.remove(tmp.getLocation());
        }

        this.saveSigns();
    }

    private void saveSigns() {
        MSWMapsConfig.file().set(this.getName() + ".Signs", this.getSigns().stream().map(MSWSign::getLocationSerialized)
                .collect(Collectors.toCollection(ArrayList::new)));
        MSWMapsConfig.save();
    }

    public Collection<MSWSign> getSigns() {
        return this.signs.values();
    }

    public void sendLog(MSWPlayer p, boolean winner) {
        if (p.getPlayer() != null) {
            TranslatableList.MAP_END_LOG.get(p).forEach(s -> p.sendCenterMessage(s.replace("%recvcoins%", Text.formatDouble(p.getGameBalance())).replace("%totalcoins%", MysticosSkywarsAPI.getInstance().getCurrencyAdapterAPI().getCoinsFormatted(p)).replace("%kills%", p.getStatistics(MSWPlayer.PlayerStatistics.GAME_KILLS) + "").replace("%time%", Text.formatSeconds(this.getMapTimer().getPassedSeconds()))));

            MysticosSkywarsAPI.getInstance().getDatabaseManagerAPI().saveNewGameHistory(new PlayerGameHistoryRow(p.getPlayer(), this.getName(), this.getGameMode().name(), this.isRanked(), this.getStartingPlayers(), p.getStatistics(MSWPlayer.PlayerStatistics.GAME_KILLS), winner, this.getTimePassed()), true);

            p.saveData(MSWPlayer.PlayerData.GAME);
        }
    }

    abstract public void forceStartMap();

    protected void calculateVotes() {
        //chest calculate
        int bigger = MathUtils.mostFrequentElement(getChestVotes().values());
        switch (bigger) {
            case 1:
                this.setTierType(MSWChest.Tier.BASIC);
                break;
            case 3:
                this.setTierType(MSWChest.Tier.EPIC);
                break;
            default:
                this.setTierType(MSWChest.Tier.NORMAL);
                break;
        }

        //projectile calculate
        bigger = MathUtils.mostFrequentElement(getProjectileVotes().values());
        if (bigger == 2) {
            this.setProjectiles(ProjectileType.BREAK_BLOCKS);
        } else {
            this.setProjectiles(ProjectileType.NORMAL);
        }

        //time calculate
        bigger = MathUtils.mostFrequentElement(getTimeVotes().values());
        switch (bigger) {
            case 2:
                this.setTime(TimeType.SUNSET);
                break;
            case 3:
                this.setTime(TimeType.NIGHT);
                break;
            case 4:
                this.setTime(TimeType.RAIN);
                break;
            default:
                this.setTime(TimeType.DAY);
                break;
        }
    }

    protected void cancelMapStart() {
        getStartMapTimer().killTask();
        for (MSWPlayer p : getAllPlayers()) {
            TranslatableLine.ARENA_CANCEL.send(p, true);
            p.sendActionbar(TranslatableLine.ARENA_CANCEL.get(p));
            p.setBarNumber(0);
        }

        this.setState(MapState.WAITING);
    }

    protected void commonRemovePlayer(MSWPlayer p) {
        this.getBossBar().removePlayer(p.getPlayer());

        p.setBarNumber(0);
        p.setInvincible(false);
        if (p.getInventory() != null) {
            p.getInventory().clear();
        }
        TranslatableLine.MATCH_LEAVE.send(p, true);

        if (!MSWConfig.file().getBoolean("Config.Shops.Only-Buy-Kits-Per-Match")) {
            p.setKit(null);
        }

        p.setState(MSWPlayer.PlayerState.LOBBY_OR_NOGAME);
        p.setFlying(false);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        p.heal();

        if (p.hasKit()) {
            p.getPlayerKit().cancelTasks();
        }

        this.inMap.remove(p);
        p.setPlayerMap(null);

        //update tab
        if (!p.isBot()) {
            MSWPlayerTabInterface rt = p.getTab();
            rt.reset();
            rt.updateRoomTAB();
        }
        for (MSWPlayer player : this.getPlayers()) {
            if (!player.isBot()) {
                MSWPlayerTabInterface rt = player.getTab();
                rt.clear();
                List<Player> players = this.getPlayers().stream().map(MSWPlayer::getPlayer).collect(Collectors.toList());
                rt.addPlayers(players);
                rt.updateRoomTAB();
            }
        }

        boolean isBungeeEnabled = MSWConfig.file().getBoolean("Config.Bungeecord.Enabled");
        boolean shouldKickPlayer = MSWConfig.file().getBoolean("Config.Bungeecord.Kick-Player");
        boolean kicked = false;

        if (isBungeeEnabled) {
            if (shouldKickPlayer) {
                if (p.getPlayer() != null) {
                    kicked = true;
                    p.getPlayer().kickPlayer(TranslatableLine.BUNGEECORD_KICK_MESSAGE.getSingle());
                }
            } else {
                TranslatableLine.BUNGEECORD_KICK_MESSAGE.sendSingle(p);
                BungeecordUtils.connect(MSWConfig.file().getString("Config.Bungeecord.Lobby-Server"), p.getPlayer(), this.getMysticosSkywarsAPI().getPlugin());
            }
        }

        if (!kicked) {
            MysticosSkywarsAPI.getInstance().getLobbyManagerAPI().tpToLobby(p);
            MSWPlayerItems.LOBBY.giveSet(p);
        }

        if ((this.getState() == MapState.PLAYING || this.getState() == MapState.FINISHING) && !MysticosSkywarsAPI.getInstance().getMapManagerAPI().shutdown) {
            checkWin();
        }

        //call api
        MysticosSkywarsAPI.getInstance().getEventsAPI().callRoomStateChange(this);
    }

    abstract public int minimumPlayersToStartMap();

    protected void startRoom() {
        this.startMapTimer = new CountdownTimer(MysticosSkywarsAPI.getInstance().getPlugin(), this.getTimeToStart(), () -> {
            //
        }, this::forceStartMap, (t) -> {
            if (getPlayerCount() < minimumPlayersToStartMap()) {
                t.killTask();
                for (MSWPlayer p : this.inMap) {
                    if (p.getWorld() != this.getMSWWorld().getWorld() && p.hasCage()) {
                        p.getPlayerCage().tpPlayer(p);
                    }

                    TranslatableLine.ARENA_CANCEL.send(p, true);
                    p.sendActionbar(TranslatableLine.ARENA_CANCEL.get(p));
                    p.setBarNumber(0);
                }
                this.setState(MapState.WAITING);
            } else {
                this.setState(MapState.STARTING);
                for (MSWPlayer p : this.inMap) {
                    if (p.getWorld() != this.getMSWWorld().getWorld() && p.hasCage()) {
                        p.getPlayerCage().tpPlayer(p);
                    }

                    if (!MSWConfig.file().getBoolean("Config.Disable-Map-Starting-Countdown.Message")) {
                        p.sendMessage(TranslatableLine.ARENA_START_COUNTDOWN.get(p, true).replace("%time%", Text.formatSeconds(t.getSecondsLeft())));
                    }
                    if (!MSWConfig.file().getBoolean("Config.Disable-Map-Starting-Countdown.Actionbar")) {
                        p.sendActionbar(TranslatableLine.ARENA_START_COUNTDOWN.get(p).replace("%time%", Text.formatSeconds(t.getSecondsLeft())));
                    }

                    p.setBarNumber(t.getSecondsLeft(), this.getTimeToStart());
                }
            }
        });

        this.startMapTimer.scheduleTimer();
    }

    protected void commonResetArena(OperationReason rr) {
        this.setState(MapState.RESETTING);

        if (this.timeCounterTask != null) {
            this.timeCounterTask.cancel();
        }
        if (this.mapTimer != null) {
            this.mapTimer.killTask();
        }
        if (this.finishingTimer != null) {
            this.finishingTimer.killTask();
        }
        if (this.startMapTimer != null) {
            this.startMapTimer.killTask();
            this.startMapTimer = null;
        }

        if (rr != OperationReason.SHUTDOWN) {
            this.getChests().forEach(MSWChest::clear);
        }
        this.world.resetWorld(rr);

        this.inMap.clear();

        this.chestVotes.clear();
        this.projectileVotes.clear();
        this.timeVotes.clear();

        this.chestVotes.put(UUID.randomUUID(), 2);
        this.projectileVotes.put(UUID.randomUUID(), 1);
        this.timeVotes.put(UUID.randomUUID(), 1);

        if (this.bossbar != null)
            this.bossbar.reset();

        this.events = parseEvents();

        this.timePassed = 0;

        if (rr != OperationReason.SHUTDOWN) {
            this.getChests().forEach(MSWChest::setChest);
        }

        this.setState(MapState.AVAILABLE);
    }

    public List<MSWMapEvent> parseEvents() {
        if (this.events != null)
            this.events.clear();
        List<MSWMapEvent> ret = new ArrayList<>();

        List<String> list = MSWMapsConfig.file().isList(this.getName() + ".Events") ?
                MSWMapsConfig.file().getStringList(this.getName() + ".Events")
                : MSWConfig.file().getStringList("Config.Events." + this.getGameMode().getSimpleName());

        for (String s : list) {
            String[] parse = s.split("@");
            if (parse.length != 2) {
                //try to parse with old separation char
                parse = s.split("&");
                if (parse.length != 2) {
                    Bukkit.getLogger().warning("Invalid event format: " + s);
                    continue;
                }
            }

            MSWMapEvent.EventType et;
            try {
                et = MSWMapEvent.EventType.valueOf(parse[0]);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Invalid event type: " + parse[0]);
                continue;
            }

            int time = Integer.parseInt(parse[1]);
            ret.add(new MSWMapEvent(this, et, time));
        }

        ret.add(new MSWMapEvent(this, MSWMapEvent.EventType.BORDERSHRINK, getMaxGameTime()));
        return ret;
    }

    public void setFinishingTimer(CountdownTimer finishingTimer) {
        this.finishingTimer = finishingTimer;
    }

    public CountdownTimer getFinishingTimer() {
        return this.finishingTimer;
    }

    protected MysticosSkywarsAPI getMysticosSkywarsAPI() {
        return MysticosSkywarsAPI.getInstance();
    }

    public void setBorderEnabled(boolean b) {
        this.borderEnabled = b;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setUnregistered(boolean b) {
        this.unregistered = b;
        if (b) {
            this.kickPlayers(null);
        }
        this.save(Data.SETTINGS, true);
    }

    public boolean isUnregistered() {
        return this.unregistered;
    }

    public abstract void removeCage(Location loc);

    public abstract void addCage(Location location);

    public void addChest(Block b, MSWChest.Type t) {
        BlockData blockData = b.getBlockData();
        BlockFace f = ((Directional) blockData).getFacing();
        this.chests.put(b.getLocation(), new MSWChest(t, b.getLocation(), f));
        this.save(Data.CHESTS, true);
    }

    public void removeChest(Location loc) {
        for (Location location : this.chests.keySet()) {
            if (location.getX() == loc.getX() && location.getY() == loc.getY() && location.getZ() == loc.getZ()) {
                this.chests.remove(location);
                this.save(Data.CHESTS, true);
                break;
            }
        }
    }

    public void setBoundaries(Location pos1, Location pos2) {
        this.mapCuboid = new MapCuboid(pos1, pos2);
        this.borderSize = Math.max(this.mapCuboid.getSizeX(), this.mapCuboid.getSizeZ()); //set bigger size from x or z
        this.border = this.getMSWWorld().getWorld().getWorldBorder();
        this.border.setCenter(this.mapCuboid.getCenter());
        this.border.setSize(this.borderSize);
        this.save(Data.BORDER, true);
    }

    public void setSpectatorLocation(Location location) {
        this.spectatorLocation = location;
        this.save(Data.SPECT_LOC, true);
    }

    public void setMaxGameTime(int seconds) {
        this.maxGameTime = seconds;
        this.save(Data.SETTINGS, true);
    }

    public void setTimeEndGame(int seconds) {
        this.timeEndGame = seconds;
        this.save(Data.SETTINGS, true);
    }

    public void setTimeToStart(int seconds) {
        this.timeToStart = seconds;
        this.save(Data.SETTINGS, true);
    }

    public void setInvincibilitySeconds(int seconds) {
        this.invincibilitySeconds = seconds;
        this.save(Data.SETTINGS, true);
    }

    public void addEvent(MSWMapEvent mswMapEvent) {
        this.events.add(mswMapEvent);
        this.save(Data.EVENTS, true);
    }

    public void removeEvent(MSWMapEvent a) {
        this.events.remove(a);
        this.save(Data.EVENTS, true);
    }

    public ItemStack getIconForPlayer(MSWPlayer p) {
        return Itens.createItem(this.getState().getStateMaterial(this.isRanked()),
                Math.min(64, Math.max(1, this.getPlayerCount())),
                TranslatableLine.ITEM_MAP_NAME.get(p).replace("%map%", this.getName()).replace("%displayname%", this.getDisplayName()).replace("%mode%", this.getGameMode().getDisplayName(p)) + (this.isRanked() ? " &bRANKED" : ""),
                variableListForIcon(TranslatableList.ITEMS_MAP_DESCRIPTION.get(p)));
    }

    private List<String> variableListForIcon(List<String> list) {
        if (this.isUnregistered()) {
            list.add("&c&lUNREGISTERED");
        }
        return list.stream()
                .map(s -> s.replace("%players%", String.valueOf(this.getPlayerCount()))
                        .replace("%maxplayers%", String.valueOf(this.getMaxPlayers())))
                .collect(Collectors.toList());
    }

    public abstract MSWMap duplicate(String newName);

    public enum Data {
        ALL, SETTINGS, WORLD, NAME, TYPE, NUM_PLAYERS, CAGES, CHESTS, SPECT_LOC, BORDER, EVENTS
    }

    public void save(Data d, boolean save) {
        switch (d) {
            case ALL:
                this.save(Data.SETTINGS, false);
                this.save(Data.WORLD, false);
                this.save(Data.NAME, false);
                this.save(Data.TYPE, false);
                this.save(Data.NUM_PLAYERS, false);
                this.save(Data.CAGES, false);
                this.save(Data.CHESTS, false);
                this.save(Data.SPECT_LOC, false);
                this.save(Data.BORDER, false);
                this.save(Data.EVENTS, false);
                break;
            case WORLD:
                // World
                MSWMapsConfig.file().set(this.getName() + ".world", this.getMSWWorld().getName());
                break;
            case NAME:
                MSWMapsConfig.file().set(this.getName() + ".name", this.name);
                break;
            case NUM_PLAYERS:
                MSWMapsConfig.file().set(this.getName() + ".number-of-players", this.getMaxPlayers());
                break;
            case TYPE:
                MSWMapsConfig.file().set(this.getName() + ".type", this.getMSWWorld().getType().name());
                if (this.getMSWWorld().getType() == MSWWorld.WorldType.SCHEMATIC) {
                    MSWMapsConfig.file().set(this.getName() + ".schematic", this.getShematicName());
                }
                break;
            case CAGES:
                MSWMapsConfig.file().remove(this.getName() + ".Locations.Cages");
                switch (this.getGameMode()) {
                    case SOLO:
                        for (MSWCage c : this.getCages()) {
                            Location loc = c.getLocation();
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getID() + ".X", loc.getBlockX());
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getID() + ".Y", loc.getBlockY());
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getID() + ".Z", loc.getBlockZ());
                        }
                        break;
                    case TEAMS:
                        for (MSWTeam c : this.getTeams()) {
                            Location loc = c.getTeamCage().getLocation();
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getTeamCage().getID() + ".X", loc.getBlockX());
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getTeamCage().getID() + ".Y", loc.getBlockY());
                            MSWMapsConfig.file().set(this.getName() + ".Locations.Cages." + c.getTeamCage().getID() + ".Z", loc.getBlockZ());
                        }
                        break;
                }
                break;
            case CHESTS:
                int chestID = 1;
                MSWMapsConfig.file().remove(this.getName() + ".Chests");
                for (MSWChest chest : this.getChests()) {
                    MSWMapsConfig.file().set(this.getName() + ".Chests." + chestID + ".LocationX", chest.getLocation().getBlockX());
                    MSWMapsConfig.file().set(this.getName() + ".Chests." + chestID + ".LocationY", chest.getLocation().getBlockY());
                    MSWMapsConfig.file().set(this.getName() + ".Chests." + chestID + ".LocationZ", chest.getLocation().getBlockZ());
                    String face;
                    try {
                        BlockFace f = ((Directional) chest.getChestBlock().getBlockData()).getFacing();
                        face = f.name();
                    } catch (Exception ignored) {
                        face = "NORTH";
                    }
                    MSWMapsConfig.file().set(this.getName() + ".Chests." + chestID + ".Face", face);
                    MSWMapsConfig.file().set(this.getName() + ".Chests." + chestID + ".Type", chest.getType().name());
                    ++chestID;
                }
                break;
            case EVENTS:
                MSWMapsConfig.file().set(this.getName() + ".Events", this.getEvents().stream().filter(mswMapEvent -> mswMapEvent.getEventType() != MSWMapEvent.EventType.BORDERSHRINK).map(MSWMapEvent::serialize).collect(Collectors.toList()));
                break;
            case SPECT_LOC:
                MSWMapsConfig.file().set(this.getName() + ".Locations.Spectator.X", this.getSpectatorLocation().getX());
                MSWMapsConfig.file().set(this.getName() + ".Locations.Spectator.Y", this.getSpectatorLocation().getY());
                MSWMapsConfig.file().set(this.getName() + ".Locations.Spectator.Z", this.getSpectatorLocation().getZ());
                MSWMapsConfig.file().set(this.getName() + ".Locations.Spectator.Yaw", this.getSpectatorLocation().getYaw());
                MSWMapsConfig.file().set(this.getName() + ".Locations.Spectator.Pitch", this.getSpectatorLocation().getPitch());
                break;
            case SETTINGS:
                MSWMapsConfig.file().set(this.getName() + ".Settings.Unregistered", this.isUnregistered());
                MSWMapsConfig.file().set(this.getName() + ".Settings.DisplayName", this.getDisplayName());
                MSWMapsConfig.file().set(this.getName() + ".Settings.GameType", this.getGameMode().name());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Spectator", this.isSpectatorEnabled());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Instant-End", this.isInstantEndEnabled());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Ranked", this.isRanked());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Border", this.isBorderEnabled());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Invincibility-Seconds", this.getInvincibilitySeconds());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Time-End-Game", this.getTimeEndGame());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Time-To-Start", this.getTimeToStart());
                MSWMapsConfig.file().set(this.getName() + ".Settings.Max-Game-Time", this.getMaxGameTime());
                break;
            case BORDER:
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS1-X", this.getPOS1().getX());
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS1-Y", this.getPOS1().getY());
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS1-Z", this.getPOS1().getZ());
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS2-X", this.getPOS2().getX());
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS2-Y", this.getPOS2().getY());
                MSWMapsConfig.file().set(this.getName() + ".World.Border.POS2-Z", this.getPOS2().getZ());
                break;
        }
        if (save)
            MSWMapsConfig.save();
    }

    //enums
    public enum MapState {
        AVAILABLE, STARTING, WAITING, PLAYING, FINISHING, RESETTING;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case AVAILABLE:
                    return TranslatableLine.MAP_STATE_AVAILABLE.get(p);
                case STARTING:
                    return TranslatableLine.MAP_STATE_STARTING.get(p);
                case WAITING:
                    return TranslatableLine.MAP_STATE_WAITING.get(p);
                case PLAYING:
                    return TranslatableLine.MAP_STATE_PLAYING.get(p);
                case FINISHING:
                    return TranslatableLine.MAP_STATE_FINISHING.get(p);
                case RESETTING:
                    return TranslatableLine.MAP_STATE_RESETTING.get(p);
                default:
                    return "?";
            }
        }

        public String getDefaultTranslation() {
            switch (this) {
                case AVAILABLE:
                    return TranslatableLine.MAP_STATE_AVAILABLE.getDefault();
                case STARTING:
                    return TranslatableLine.MAP_STATE_STARTING.getDefault();
                case WAITING:
                    return TranslatableLine.MAP_STATE_WAITING.getDefault();
                case PLAYING:
                    return TranslatableLine.MAP_STATE_PLAYING.getDefault();
                case FINISHING:
                    return TranslatableLine.MAP_STATE_FINISHING.getDefault();
                case RESETTING:
                    return TranslatableLine.MAP_STATE_RESETTING.getDefault();
                default:
                    return "?";
            }
        }

        public Material getStateMaterial(boolean ranked) {
            switch (this) {
                case WAITING:
                    return ranked ? Material.LIGHT_BLUE_CONCRETE : Material.LIGHT_BLUE_WOOL;
                case AVAILABLE:
                    return ranked ? Material.GREEN_CONCRETE : Material.GREEN_WOOL;
                case STARTING:
                    return ranked ? Material.YELLOW_CONCRETE : Material.YELLOW_WOOL;
                case PLAYING:
                    return ranked ? Material.RED_CONCRETE : Material.RED_WOOL;
                case FINISHING:
                    return ranked ? Material.PURPLE_CONCRETE : Material.PURPLE_WOOL;
                case RESETTING:
                    return ranked ? Material.BLACK_CONCRETE : Material.BLACK_WOOL;
                default:
                    return ranked ? Material.BEACON : Material.DIRT;
            }
        }

        public ItemStack getStateIcon(Boolean ranked) {
            switch (this) {
                case AVAILABLE:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&aAvailable",
                            "&7Starting",
                            "&7Waiting",
                            "&7Playing",
                            "&7Finishing",
                            "&7Resetting"
                    ));
                case STARTING:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&aStarting",
                            "&7Waiting",
                            "&7Playing",
                            "&7Finishing",
                            "&7Resetting"
                    ));
                case WAITING:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&7Starting",
                            "&aWaiting",
                            "&7Playing",
                            "&7Finishing",
                            "&7Resetting"
                    ));
                case PLAYING:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&7Starting",
                            "&7Waiting",
                            "&aPlaying",
                            "&7Finishing",
                            "&7Resetting"
                    ));
                case FINISHING:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&7Starting",
                            "&7Waiting",
                            "&7Playing",
                            "&aFinishing",
                            "&7Resetting"
                    ));
                case RESETTING:
                    return Itens.createItem(getStateMaterial(ranked), 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&7Starting",
                            "&7Waiting",
                            "&7Playing",
                            "&7Finishing",
                            "&aResetting"
                    ));
                default:
                    return Itens.createItem(Material.BEACON, 1, "&9Map Status", Arrays.asList(
                            "&fClick to change the map status.",
                            "",
                            "&7Available",
                            "&7Starting",
                            "&7Waiting",
                            "&7Playing",
                            "&7Finishing",
                            "&7Resetting"
                    ));
            }
        }
    }

    public enum OperationReason {SHUTDOWN, RESET, LOAD}

    public enum GameMode {
        SOLO, TEAMS;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case SOLO:
                    return TranslatableLine.SOLO_MODE.get(p);
                case TEAMS:
                    return TranslatableLine.TEAMS_MODE.get(p);
                default:
                    return "?";
            }
        }

        public String getDefaultTranslation() {
            switch (this) {
                case SOLO:
                    return TranslatableLine.SOLO_MODE.getDefault();
                case TEAMS:
                    return TranslatableLine.TEAMS_MODE.getDefault();
                default:
                    return "?";
            }
        }

        public String getSimpleName() {
            switch (this) {
                case SOLO:
                    return "Solo";
                case TEAMS:
                    return "Teams";
                default:
                    return "?";
            }
        }
    }

    public enum VoteType {
        CHESTS, PROJECTILES, TIME
    }

    public enum ProjectileType {
        NORMAL, BREAK_BLOCKS;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case NORMAL:
                    return TranslatableLine.VOTE_PROJECTILE_NORMAL.get(p);
                case BREAK_BLOCKS:
                    return TranslatableLine.VOTE_PROJECTILE_BREAK.get(p);
                default:
                    return "?";
            }
        }
    }

    public enum TimeType {
        DAY, NIGHT, RAIN, SUNSET;

        public String getDisplayName(MSWPlayer p) {
            switch (this) {
                case DAY:
                    return TranslatableLine.VOTE_TIME_DAY.get(p);
                case NIGHT:
                    return TranslatableLine.VOTE_TIME_NIGHT.get(p);
                case RAIN:
                    return TranslatableLine.VOTE_TIME_RAIN.get(p);
                case SUNSET:
                    return TranslatableLine.VOTE_TIME_SUNSET.get(p);
                default:
                    return "?";
            }
        }
    }

    public enum SpectateType {INSIDE_GAME, EXTERNAL}

}
