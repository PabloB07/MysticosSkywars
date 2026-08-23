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
import joserodpt.mysticosskywars.api.config.MSWLanguage;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.database.PlayerBoughtItemsRow;
import joserodpt.mysticosskywars.api.database.PlayerDataRow;
import joserodpt.mysticosskywars.api.database.PlayerGameHistoryRow;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.managers.PlayerManagerAPI;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.tab.MSWPlayerTabInterface;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager extends PlayerManagerAPI {
    private final MysticosSkywarsAPI rs;

    public PlayerManager(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    Map<UUID, MSWMap> fastJoin = new HashMap<>();
    public List<UUID> teleporting = new ArrayList<>();
    private final Map<Player, Player> trackingPlayers = new HashMap<>();
    private final Map<UUID, MSWPlayer> players = new HashMap<>();

    @Override
    public void loadPlayer(Player player) {
        try {
            PlayerDataRow playerDataRow = rs.getDatabaseManagerAPI().getPlayerData(player);

            MSWPlayer p = new MSWPlayer(player, MSWPlayer.PlayerState.LOBBY_OR_NOGAME, playerDataRow.getKills(), playerDataRow.getDeaths(), playerDataRow.getStats_wins_solo(), playerDataRow.getStats_wins_teams(), playerDataRow.getCoins(), playerDataRow.getLanguage(), playerDataRow.getLosses(), playerDataRow.getGames_played(), playerDataRow.getRanked_kills(), playerDataRow.getRanked_deaths(), playerDataRow.getStats_wins_ranked_solo(), playerDataRow.getStats_wins_ranked_teams(), playerDataRow.getLoses_ranked(), playerDataRow.getRanked_games_played());

            String lang = playerDataRow.getLanguage();
            if (lang == null || lang.isEmpty() || !rs.getLanguageManagerAPI().getLanguagesMap().containsKey(lang)) {
                rs.getLogger().info("Player " + player.getName() + " has an invalid language set. Setting default language.");
                p.setLanguage(rs.getLanguageManagerAPI().getDefaultLanguage());
            }

            String firstJoin = playerDataRow.getFirstJoin();
            if (firstJoin == null || firstJoin.isEmpty()) {
                p.saveData(MSWPlayer.PlayerData.FIRST_JOIN);
            }

            //convert legacy data to new format

            String oldGamesHistory = playerDataRow.getGamesListLegacy();
            if (oldGamesHistory != null && !oldGamesHistory.isEmpty()) {
                rs.getLogger().info("Converting legacy game history for " + player.getName());
                String[] split = oldGamesHistory.split("/");
                for (String s : split) {
                    String[] data = s.split(";");
                    if (data.length == 7) {
                        Boolean ranked = Boolean.parseBoolean(data[2]);
                        int jogadores = Integer.parseInt(data[3]);
                        boolean win = Boolean.parseBoolean(data[4]);
                        int seconds = Integer.parseInt(data[5]);

                        rs.getDatabaseManagerAPI().saveNewGameHistory(new PlayerGameHistoryRow(player, data[0], data[1], ranked, jogadores, win, seconds, data[6]), true);
                    }
                }

                p.saveData(MSWPlayer.PlayerData.LEGACY_GAME_HISTORY_CLEAR);
                rs.getLogger().info("Legacy game history converted for " + player.getName() + "!");
            }

            Collection<String> boughtItemsLegacy = playerDataRow.getBoughtItemsLegacy();
            if (boughtItemsLegacy != null && !boughtItemsLegacy.isEmpty()) {
                rs.getLogger().info("Converting legacy bought items for " + player.getName());
                for (String s : boughtItemsLegacy) {
                    String[] data = s.split("\\|");
                    if (data.length == 2) {
                        rs.getDatabaseManagerAPI().saveNewBoughtItem(new PlayerBoughtItemsRow(p, data[0], data[1]), true);
                    }
                }

                p.saveData(MSWPlayer.PlayerData.LEGACY_BOUGHT_ITEMS_CLEAR);
                rs.getLogger().info("Legacy bought items converted for " + player.getName() + "!");
            }

            //end legacy data conversion

            String mapv = playerDataRow.getMapViewerPref();
            if (mapv != null) {
                try {
                    p.setPlayerMapViewerPref(MSWPlayer.MapViewerPref.valueOf(mapv));
                } catch (Exception e) {
                    p.setPlayerMapViewerPref(MSWPlayer.MapViewerPref.MAPV_ALL);
                }
            }

            String cageBlock = playerDataRow.getCageMaterial();
            if (cageBlock != null) {
                try {
                    p.setCageBlock(Material.getMaterial(cageBlock));
                } catch (Exception e) {
                    p.setCageBlock(Material.GLASS);
                }
            }

            String choosenKit = playerDataRow.getChoosen_kit();
            if (choosenKit != null && !choosenKit.isEmpty()) {
                p.setKit(rs.getKitManagerAPI().getKit(choosenKit));
            }

            p.heal();

            rs.getPlayerManagerAPI().getPlayers().stream()
                    .filter(MSWPlayer::isInMatch)
                    .forEach(plr -> {
                        MSWPlayerTabInterface rt = plr.getTab();
                        rt.removePlayers(plr.getPlayer());
                        rt.updateRoomTAB();
                    });

            Bukkit.getOnlinePlayers().forEach(plr -> p.getTab().addPlayers(plr));
            p.getTab().updateRoomTAB();

            rs.getPlayerManagerAPI().addPlayer(p);

            if (rs.getPlayerManagerAPI().getFastJoin().containsKey(player.getUniqueId())) {
                rs.getPlayerManagerAPI().getFastJoin().get(player.getUniqueId()).addPlayer(p);
                rs.getPlayerManagerAPI().getFastJoin().remove(player.getUniqueId());
            } else {
                if (rs.getLobbyManagerAPI().tpLobbyOnJoin()) {
                    rs.getLobbyManagerAPI().tpToLobby(p);
                }
            }

            p.saveData(MSWPlayer.PlayerData.LAST_JOIN);
            return;
        } catch (Exception e) {
            MysticosSkywarsAPI.getInstance().getLogger().severe("Error while loading player data for " + player.getName() + "!");
            e.printStackTrace();
        }
        player.kickPlayer("§cAn error occurred while loading your MysticosSkywars player data.\nPlease try again later and contact an admin.");
    }

    @Override
    public MSWPlayer getPlayer(Player p) {
        return this.players.get(p.getUniqueId());
    }

    @Override
    public void savePlayer(MSWPlayer p, MSWPlayer.PlayerData pd) {
        if (p.getPlayer() != null) {
            PlayerDataRow playerDataRow = rs.getDatabaseManagerAPI().getPlayerData(p.getPlayer());

            switch (pd) {
                case KIT:
                    playerDataRow.setChoosenKit(p.getPlayerKit().getName());
                    break;
                case CAGE_BLOCK:
                    playerDataRow.setCageBlock(p.getCageBlock().name());
                    break;
                case COINS:
                    playerDataRow.setCoins(rs.getCurrencyAdapterAPI().getCoins(p));
                    break;
                case GAME:
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, false, p.getStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, true, p.getStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, true));

                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, false, p.getStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, true, p.getStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, true));

                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.KILLS, false, p.getStatistics(MSWPlayer.PlayerStatistics.KILLS, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.KILLS, true, p.getStatistics(MSWPlayer.PlayerStatistics.KILLS, true));

                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.DEATHS, false, p.getStatistics(MSWPlayer.PlayerStatistics.DEATHS, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.DEATHS, true, p.getStatistics(MSWPlayer.PlayerStatistics.DEATHS, true));

                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.LOSES, false, p.getStatistics(MSWPlayer.PlayerStatistics.LOSES, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.LOSES, true, p.getStatistics(MSWPlayer.PlayerStatistics.LOSES, true));

                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED, false, p.getStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED, false));
                    playerDataRow.setStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED, true, p.getStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED, true));
                    break;
                case LANG:
                    playerDataRow.setLanguage(p.getLanguage());
                    break;
                case MAPVIEWER_PREF:
                    playerDataRow.setMapViewerPref(p.getPlayerMapViewerPref().name());
                    break;
                case FIRST_JOIN:
                    playerDataRow.setFirstJoin();
                    break;
                case LEGACY_GAME_HISTORY_CLEAR:
                    playerDataRow.setGamesListLegacy("");
                    break;
                case LEGACY_BOUGHT_ITEMS_CLEAR:
                    playerDataRow.setBoughtItemsLegacy("");
                    break;
                case LAST_JOIN:
                    playerDataRow.setLastJoin();
                    break;
            }

            rs.getDatabaseManagerAPI().savePlayerData(playerDataRow, true);
        }
    }

    @Override
    public void setLanguage(MSWPlayer player, MSWLanguage l) {
        player.setLanguage(l.getKey());
        player.sendMessage(TranslatableLine.LANGUAGE_SET.get(player, true).replace("%language%", l.getDisplayName()));
        player.closeInventory();
    }

    @Override
    public void loadPlayers() {
        this.players.clear();
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public Map<UUID, MSWMap> getFastJoin() {
        return this.fastJoin;
    }

    @Override
    public int getPlayingPlayers(MapManagerAPI.MapGamemodes pt) {
        return this.rs.getMapManagerAPI().getMaps(pt).stream().mapToInt(MSWMap::getPlayerCount).sum();
    }

    @Override
    public void stopScoreboards() {
        this.players.values().forEach(gamePlayer -> gamePlayer.getScoreboard().stop());
    }

    @Override
    public Collection<MSWPlayer> getPlayers() {
        return this.players.values();
    }

    @Override
    public void addPlayer(MSWPlayer mswPlayer) {
        this.players.put(mswPlayer.getUUID(), mswPlayer);
    }

    @Override
    public void removePlayer(MSWPlayer mswPlayer) {
        this.players.remove(mswPlayer.getUUID());
    }

    @Override
    public void trackPlayer(MSWPlayer gp) {
        List<MSWPlayer> tmp = new ArrayList<>(gp.getMatch().getPlayers());
        tmp.remove(gp);

        Optional<MSWPlayer> search = tmp.stream().filter(c -> c.getState().equals(MSWPlayer.PlayerState.PLAYING)).findAny();
        if (search.isEmpty() || search.get().isBot()) {
            TranslatableLine.NO_TRACKER.send(gp, true);
            return;
        }

        Player player = gp.getPlayer();
        Player target = search.get().getPlayer();

        //Credit GITHUB PlayerCompass
        trackingPlayers.put(player, target);
        gp.sendMessage(TranslatableLine.TRACK_FOUND.get(gp, true).replace("%player%", target.getDisplayName()));

        new BukkitRunnable() {
            public void run() {
                //Cancel task if player is offline or is no longer tracking target
                if (!player.isOnline() || !trackingPlayers.containsKey(player) || !trackingPlayers.get(player).equals(target))
                    this.cancel();

                    //Cancel task if target is offline
                else if (!target.isOnline() || search.get().getState() != MSWPlayer.PlayerState.PLAYING) {
                    if (gp.isInMatch()) {
                        player.setCompassTarget(gp.getMatch().getSpectatorLocation());
                    }
                    this.cancel();
                }

                player.setCompassTarget(target.getLocation());
            }
        }.runTaskTimerAsynchronously(rs.getPlugin(), 5L, 30L);
    }

    @Override
    public List<UUID> getTeleporting() {
        return this.teleporting;
    }
}
