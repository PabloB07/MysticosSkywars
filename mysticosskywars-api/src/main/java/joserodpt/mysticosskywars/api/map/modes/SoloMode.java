package joserodpt.mysticosskywars.api.map.modes;

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
import joserodpt.mysticosskywars.api.cages.MSWSoloCage;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.events.MSWPlayerWinEvent;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.modes.teams.MSWTeam;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.MSWPlayerItems;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabInterface;
import joserodpt.mysticosskywars.api.utils.CountdownTimer;
import joserodpt.mysticosskywars.api.utils.FireworkUtils;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SoloMode extends MSWMap {

    private final Map<Location, MSWCage> cages;

    //para dar setup
    public SoloMode(String nome, String displayName, World w, String schematicName, MSWWorld.WorldType wt, int maxPlayers) {
        super(nome.replace(".schematic", "").replace(".schem", ""), displayName.replace(".schematic", "").replace(".schem", ""), w, schematicName, wt, MapState.RESETTING, maxPlayers, null, true, false, true, null, null, new HashMap<>(), false, true);
        this.cages = new HashMap<>();
    }

    public SoloMode(String nome, String displayName, World w, String schematicName, MSWWorld.WorldType wt, MapState estado, Map<Location, MSWCage> cages, int maxPlayers, Location spectatorLocation, Boolean specEnabled, Boolean instantEnding, Boolean border, Location pos1, Location pos2, Map<Location, MSWChest> chests, Boolean rankd, Boolean unregistered) {
        super(nome, displayName, w, schematicName, wt, estado, maxPlayers, spectatorLocation, specEnabled, instantEnding, border, pos1, pos2, chests, rankd, unregistered);
        this.cages = cages;
        this.cages.forEach((location, mswCage) -> mswCage.setMap(this));
    }

    @Override
    public void forceStartMap() {
        if (canStartMap()) {
            super.cancelMapStart();
        } else {
            this.setState(MapState.PLAYING);
            super.setStartingPlayers(super.getPlayerCount());

            super.getStartMapTimer().killTask();

            super.calculateVotes();

            for (MSWPlayer p : this.getPlayers()) {
                if (p.getPlayer() != null) {
                    p.setBarNumber(0);
                    p.getInventory().clear();

                    super.getBossBar().addPlayer(p.getPlayer());

                    //start msg
                    TranslatableList.MAP_START.get(p).forEach(s -> p.sendCenterMessage(s.replace("%chests%", super.getChestTier().getDisplayName(p)).replace("%kit%", p.getPlayerKit().getDisplayName()).replace("%project%", super.getProjectileTier().getDisplayName(p)).replace("%time%", super.getTimeType().getDisplayName(p))));

                    p.getPlayerKit().give(p);
                    p.setState(MSWPlayer.PlayerState.PLAYING);
                    p.getPlayerCage().open();
                }
            }

            super.startTimers();
        }
    }

    @Override
    public boolean canStartMap() {
        return super.getPlayerCount() < MSWConfig.file().getInt("Config.Min-Players-ToStart");
    }

    @Override
    public void removePlayer(MSWPlayer p) {
        if (p.hasCage()) {
            p.getPlayerCage().removePlayer(p);
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
                    for (MSWCage c : this.cages.values()) {
                        if (c.isEmpty() && p.getPlayer() != null) {
                            c.addPlayer(p);
                            break;
                        }
                    }

                    p.setPlayerMap(this);
                    p.setState(MSWPlayer.PlayerState.CAGE);

                    super.getAllPlayers().add(p);

                    if (p.getPlayer() != null) {
                        super.getBossBar().addPlayer(p.getPlayer());
                        p.heal();
                        List<String> up = TranslatableList.TITLE_ROOMJOIN.get(p);
                        p.getPlayer().sendTitle(up.get(0), up.get(1), 10, 120, 10);
                    }

                    for (MSWPlayer ws : this.getAllPlayers()) {
                        ws.sendMessage(TranslatableLine.PLAYER_JOIN_ARENA.get(p, true).replace("%player%", p.getDisplayName()).replace("%players%", getPlayerCount() + "").replace("%maxplayers%", getMaxPlayers() + ""));
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

                    if (getPlayerCount() == MSWConfig.file().getInt("Config.Min-Players-ToStart")) {
                        startRoom();
                    }
                    break;
            }

            //call api
            super.getMysticosSkywarsAPI().getEventsAPI().callRoomStateChange(this);

            //signal that is ranked
            if (super.isRanked()) p.sendActionbar("&b&lRANKED");
        }
    }

    @Override
    public void resetArena(OperationReason rr) {
        super.commonResetArena(rr);
    }

    @Override
    public void checkWin() {
        if (this.getPlayerCount() == 1 && super.getState() != MapState.FINISHING) {
            this.setState(MapState.FINISHING);

            MSWPlayer p = getPlayers().get(0);
            p.setInvincible(true);

            super.getMapTimer().killTask();
            super.getTimeCounterTask().cancel();

            super.getMysticosSkywarsAPI().getPlayerManagerAPI().getPlayers().forEach(gamePlayer -> gamePlayer.sendMessage(TranslatableLine.WINNER_BROADCAST.get(gamePlayer, true).replace("%winner%", p.getDisplayName()).replace("%map%", super.getName()).replace("%displayname%", super.getDisplayName())));

            // Fire win event
            org.bukkit.Bukkit.getPluginManager().callEvent(new MSWPlayerWinEvent(p, this));

            if (this.isInstantEndEnabled()) {
                this.sendLog(p, true);
                this.kickPlayers(null);
                this.resetArena(OperationReason.RESET);
            } else {
                super.setFinishingTimer(new CountdownTimer(super.getMysticosSkywarsAPI().getPlugin(), this.getTimeEndGame(), () -> {
                    super.getBossBar().tick();
                    if (p.getPlayer() != null) {
                        p.setInvincible(true);
                        p.addStatistic(MSWPlayer.Statistic.SOLO_WIN, 1, this.isRanked());
                        p.executeWinBlock(this.getTimeEndGame() - 2);
                    }

                    for (MSWPlayer g : super.getAllPlayers()) {
                        g.delCage();
                        g.sendMessage(TranslatableLine.MATCH_END.get(g, true).replace("%time%", Text.formatSeconds(this.getTimeEndGame())));
                    }
                }, () -> {
                    super.getBossBar().tick();
                    this.sendLog(p, true);
                    this.kickPlayers(null);
                    this.resetArena(OperationReason.RESET);
                }, (t) -> {
                    super.getAllPlayers().forEach(mswPlayer -> mswPlayer.setBarNumber(t.getSecondsLeft(), this.getTimeEndGame()));
                    super.getBossBar().tick();
                    if (p.getPlayer() != null) {
                        FireworkUtils.spawnRandomFirework(p.getLocation());
                    }
                }));

                super.getFinishingTimer().scheduleTimer();
            }

            super.getChests().forEach(MSWChest::cancelTasks);
            super.getChests().forEach(MSWChest::clearHologram);
        }
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.SOLO;
    }

    @Override
    public Collection<MSWCage> getCages() {
        return this.cages.values();
    }

    @Override
    public Collection<MSWTeam> getTeams() {
        return Collections.emptyList();
    }

    @Override
    public int getMaxTeamsNumber() {
        return 0;
    }

    @Override
    public int getMaxTeamsMembers() {
        return 0;
    }

    @Override
    public int minimumPlayersToStartMap() {
        return MSWConfig.file().getInt("Config.Min-Players-ToStart");
    }

    @Override
    public void removeCage(Location loc) {
        for (Location location : this.cages.keySet()) {
            if (location.getBlockX() == loc.getX() && location.getBlockY() == loc.getY() && location.getBlockZ() == loc.getZ()) {
                this.cages.remove(location);
                this.save(Data.CAGES, true);
                break;
            }
        }
    }

    @Override
    public void addCage(Location location) {
        MSWSoloCage cage = new MSWSoloCage(this.cages.size() + 1, location);
        cage.setMap(this);
        this.cages.put(location, cage);
        this.save(Data.CAGES, true);
    }

    @Override
    public MSWMap duplicate(String newName) {
        World w = MysticosSkywarsAPI.getInstance().getWorldManagerAPI().duplicateWorld(this.getMSWWorld(), newName);
        if (w == null) return null;
        return new SoloMode(newName,
                newName,
                w,
                this.getShematicName(),
                this.getMSWWorld().getType(),
                MapState.AVAILABLE,
                cages,
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
