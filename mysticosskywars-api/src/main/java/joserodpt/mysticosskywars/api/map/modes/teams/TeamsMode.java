package joserodpt.mysticosskywars.api.map.modes.teams;

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
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.MSWPlayerItems;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabInterface;
import joserodpt.mysticosskywars.api.utils.CountdownTimer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamsMode extends MSWMap {

    private int maxMembersTeam = 0;
    private int maxTeamsNumber = 0;
    private final Map<Location, MSWTeam> teams;

    //setup
    public TeamsMode(String nome, String displayName, World w, String schematicName, MSWWorld.WorldType wt, int teamsNumber, int playersPerTeam) {
        super(nome, displayName, w, schematicName, wt, MapState.RESETTING, teamsNumber * playersPerTeam, null, true, false, true, null, null, new HashMap<>(), false, true);

        this.teams = new HashMap<>();
        this.maxMembersTeam = playersPerTeam;
        this.maxTeamsNumber = teamsNumber;
    }

    public TeamsMode(String nome, String displayName, World w, String schematicName, MSWWorld.WorldType wt, MapState estado, Map<Location, MSWTeam> teams, int maxPlayers, Location spectatorLocation, Boolean specEnabled, Boolean instantEnding, Boolean border, Location pos1, Location pos2, Map<Location, MSWChest> chests, Boolean rankd, Boolean unregistered) {
        super(nome, displayName, w, schematicName, wt, estado, maxPlayers, spectatorLocation, specEnabled, instantEnding, border, pos1, pos2, chests, rankd, unregistered);

        this.teams = teams;
        this.teams.values().forEach(mswTeam -> mswTeam.getTeamCage().setMap(this));

        for (Location location : this.teams.keySet()) { // extract first team to get max members
            this.maxMembersTeam = teams.get(location).getMaxMembers();
            break;
        }

        this.teams.forEach((loc, team) -> team.getTeamCage().setMap(this));
    }

    @Override
    public void forceStartMap() {
        if (super.getPlayerCount() < this.maxMembersTeam + 1) {
            super.cancelMapStart();
        } else {
            this.setState(MapState.PLAYING);
            super.setStartingPlayers(super.getPlayerCount());

            super.getStartMapTimer().killTask();

            super.calculateVotes();

            for (MSWTeam t : this.getTeams()) {
                for (MSWPlayer p : t.getMembers()) {
                    if (p.getPlayer() != null) {
                        p.setBarNumber(0);
                        p.getInventory().clear();

                        super.getBossBar().addPlayer(p.getPlayer());

                        //start msg
                        TranslatableList.MAP_START.get(p).forEach(s -> p.sendCenterMessage(s.replace("%chests%", super.getChestTier().getDisplayName(p)).replace("%kit%", p.getPlayerKit().getDisplayName()).replace("%project%", super.getProjectileTier().getDisplayName(p)).replace("%time%", super.getTimeType().getDisplayName(p))));

                        p.getPlayerKit().give(p);
                        p.setState(MSWPlayer.PlayerState.PLAYING);
                    }
                }
                t.openCage();
            }

            super.startTimers();
        }
    }

    @Override
    public boolean canStartMap() {
        return super.getPlayerCount() < (this.getMaxTeamsMembers() + 1);
    }

    @Override
    public void removePlayer(MSWPlayer p) {
        if (p.hasTeam()) {
            p.getTeam().removeMember(p);
        }

        super.commonRemovePlayer(p);
    }

    @Override
    public void addPlayer(MSWPlayer p) {
        if (p.getMatch() == this) {
            return;
        }

        if (this.isUnregistered()) {
            TranslatableLine.MAP_IS_UNREGISTERED.send(p, true);
            return;
        }

        if (super.getMysticosSkywarsAPI().getPartiesManagerAPI().checkForParties(p, this)) {
            switch (this.getState()) {
                case RESETTING:
                    TranslatableLine.CANT_JOIN.send(p, true);
                    return;
                case FINISHING:
                case PLAYING:
                    if (this.isSpectatorEnabled()) {
                        spectate(p, SpectateType.EXTERNAL, null);
                    } else {
                        TranslatableLine.SPECTATING_DISABLED.send(p, true);
                        return;
                    }
                    break;
                default:
                    if (this.getPlayerCount() == this.getMaxPlayers()) {
                        if (MSWConfig.file().getBoolean("Config.Bungeecord.Enabled")) {
                            spectate(p, SpectateType.EXTERNAL, null);
                            return;
                        } else {
                            TranslatableLine.ROOM_FULL.send(p, true);
                            return;
                        }
                    }

                    //cage
                    for (MSWTeam c : this.getTeams()) {
                        if (!c.isTeamFull()) {
                            c.addPlayer(p);
                            break;
                        }
                    }

                    p.setPlayerMap(this);
                    p.setState(MSWPlayer.PlayerState.CAGE);

                    for (MSWPlayer ws : super.getAllPlayers()) {
                        if (p.getPlayer() != null) {
                            ws.sendMessage(TranslatableLine.PLAYER_JOIN_ARENA.get(ws, true).replace("%player%", p.getDisplayName()).replace("%players%", this.getPlayerCount() + "").replace("%maxplayers%", getMaxPlayers() + ""));
                        }
                    }

                    super.getAllPlayers().add(p);
                    p.heal();

                    if (p.getPlayer() != null) {
                        super.getBossBar().addPlayer(p.getPlayer());
                        List<String> up = TranslatableList.TITLE_ROOMJOIN.get(p);
                        p.getPlayer().sendTitle(up.get(0), up.get(1), 10, 120, 10);
                    }

                    if (p.getInventory() != null) {
                        p.getInventory().clear();
                    }
                    MSWPlayerItems.CAGE.giveSet(p);

                    //update tab
                    if (!p.isBot()) {
                        for (MSWPlayer player : this.getPlayers()) {
                            if (!player.isBot()) {
                                MSWPlayerTabInterface rt = player.getTab();
                                List<Player> players = this.getPlayers().stream().map(MSWPlayer::getPlayer).collect(Collectors.toList());
                                rt.clear();
                                rt.addPlayers(players);
                                rt.updateRoomTAB();
                            }
                        }
                    }

                    if (this.getPlayerCount() == this.maxMembersTeam + 1) {
                        super.startRoom();
                    }
                    break;
            }

            //call api
            super.getMysticosSkywarsAPI().getEventsAPI().callRoomStateChange(this);

            //signal that is ranked
            if (this.isRanked()) p.sendActionbar("&b&lRANKED");
        }
    }

    @Override
    public void resetArena(OperationReason rr) {
        this.getTeams().forEach(MSWTeam::reset);
        super.commonResetArena(rr);
    }

    private int getAliveTeams() {
        return (int) this.getTeams().stream()
                .filter(t -> !t.isEliminated() && t.getMemberCount() > 0)
                .count();
    }

    @Override
    public void checkWin() {
        if (this.getAliveTeams() == 1 && this.getState() != MapState.FINISHING) {
            this.setState(MapState.FINISHING);

            MSWTeam winMSWTeam = getPlayers().get(0).getTeam();

            super.getMapTimer().killTask();
            super.getTimeCounterTask().cancel();

            super.getMysticosSkywarsAPI().getPlayerManagerAPI().getPlayers().forEach(gamePlayer -> gamePlayer.sendMessage(TranslatableLine.WINNER_BROADCAST.get(gamePlayer, true).replace("%winner%", winMSWTeam.getNames()).replace("%map%", super.getName()).replace("%displayname%", super.getDisplayName())));

            if (this.isInstantEndEnabled()) {
                winMSWTeam.getMembers().forEach(mswPlayer -> this.sendLog(mswPlayer, true));
                this.kickPlayers(null);
                this.resetArena(OperationReason.RESET);
            } else {
                super.setFinishingTimer(new CountdownTimer(super.getMysticosSkywarsAPI().getPlugin(), this.getTimeEndGame(), () -> {
                    for (MSWPlayer p : winMSWTeam.getMembers()) {
                        if (p.getPlayer() != null) {
                            p.setInvincible(true);
                            p.addStatistic(MSWPlayer.Statistic.TEAM_WIN, 1, this.isRanked());
                            p.executeWinBlock(this.getTimeEndGame() - 2);
                        }
                        this.sendLog(p, true);
                    }

                    for (MSWPlayer g : super.getAllPlayers()) {
                        if (g.getPlayer() != null) {
                            g.sendMessage(TranslatableLine.MATCH_END.get(g, true).replace("%time%", Text.formatSeconds(this.getTimeEndGame())));
                            g.getPlayer().sendTitle("", Text.color(TranslatableLine.TITLE_WIN.get(g).replace("%player%", winMSWTeam.getNames())), 10, 40, 10);
                        }
                    }
                }, () -> {
                    winMSWTeam.getMembers().forEach(mswPlayer -> this.sendLog(mswPlayer, true));
                    this.kickPlayers(null);
                    this.resetArena(OperationReason.RESET);
                }, (t) -> {
                    // if (Players.get(0).p != null) {
                    //     firework(Players.get(0));
                    // }

                    super.getAllPlayers().forEach(mswPlayer -> mswPlayer.setBarNumber(t.getSecondsLeft(), this.getTimeEndGame()));
                }));

                super.getFinishingTimer().scheduleTimer();
            }

            super.getChests().forEach(MSWChest::cancelTasks);
            super.getChests().forEach(MSWChest::clearHologram);
        }
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.TEAMS;
    }

    @Override
    public Collection<MSWCage> getCages() {
        return this.getTeams().stream().map(MSWTeam::getTeamCage).collect(Collectors.toList());
    }

    @Override
    public Collection<MSWTeam> getTeams() {
        return this.teams.values();
    }

    @Override
    public int getMaxTeamsNumber() {
        return this.maxTeamsNumber;
    }

    @Override
    public int getMaxTeamsMembers() {
        return this.maxMembersTeam;
    }

    @Override
    public int minimumPlayersToStartMap() {
        return getMaxTeamsMembers() + 1;
    }

    @Override
    public void removeCage(Location loc) {
        for (Location location : this.teams.keySet()) {
            if (location.getBlockX() == loc.getX() && location.getBlockY() == loc.getY() && location.getBlockZ() == loc.getZ()) {
                this.teams.remove(location);
                this.save(Data.CAGES, true);
                break;
            }
        }
    }

    @Override
    public void addCage(Location location) {
        MSWTeam t = new MSWTeam(this.getTeams().size() + 1, this.getMaxTeamsMembers(), location);
        t.getTeamCage().setMap(this);
        this.teams.put(location, t);
        this.save(Data.CAGES, true);
    }

    @Override
    public MSWMap duplicate(String newName) {
        World w = MysticosSkywarsAPI.getInstance().getWorldManagerAPI().duplicateWorld(this.getMSWWorld(), newName);
        if (w == null) return null;
        return new TeamsMode(newName,
                newName,
                w,
                this.getShematicName(),
                this.getMSWWorld().getType(),
                MapState.AVAILABLE,
                teams,
                this.getMaxPlayers(),
                this.getSpectatorLocation(),
                this.isSpectatorEnabled(),
                this.isInstantEndEnabled(),
                this.isBorderEnabled(),
                this.getPOS1(),
                this.getPOS2(),
                this.getChestsMap(),
                this.isRanked(),
                true);
    }
}
