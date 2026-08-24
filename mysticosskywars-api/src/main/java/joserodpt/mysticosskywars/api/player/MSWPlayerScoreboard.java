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
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.utils.Text;
import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public class MSWPlayerScoreboard {

    private FastBoard fb;
    private final MSWPlayer p;
    private BukkitTask task;
    private boolean disabled;

    public MSWPlayerScoreboard(MSWPlayer r) {
        this.p = r;
        try {
            this.fb = new FastBoard(r.getPlayer());
            if (MSWConfig.file().getBoolean("Config.Scoreboard.Enabled", true)
                    && MysticosSkywarsAPI.getInstance().getLobbyManagerAPI().getLobbyLocation() != null) {
                this.run();
            }
        } catch (final RuntimeException exception) {
            this.disabled = true;
            Bukkit.getLogger().warning("Could not create FastBoard for player " + r.getName()
                    + ": " + exception.getMessage());
        }
    }

    protected String variables(String s, MSWPlayer p) {
        String tmp;

        if (p.isInMatch()) {
            tmp = s.replace("%space%", Text.makeSpace()).replace("%players%", p.getMatch().getPlayerCount() + "").replace("%maxplayers%", p.getMatch().getMaxPlayers() + "").replace("%time%", p.getMatch().getTimePassed() + "").replace("%nextevent%", nextEvent(p.getMatch())).replace("%spectators%", p.getMatch().getSpectatorsCount() + "").replace("%kills%", p.getStatistics(MSWPlayer.PlayerStatistics.GAME_KILLS) + "").replace("%map%", p.getMatch().getName()).replace("%displayname%", p.getMatch().getDisplayName()).replace("%runtime%", Text.formatSeconds(p.getMatch().getTimePassed())).replace("%state%", p.getMatch().getState().getDisplayName(p)).replace("%mode%", p.getMatch().getGameMode().getDisplayName(p)).replace("%solowins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO) + "").replace("%teamwins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS) + "").replace("%loses%", p.getStatistics(MSWPlayer.PlayerStatistics.LOSES) + "").replace("%gamesplayed%", p.getStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED) + "");
        } else {
            tmp = s.replace("%space%", Text.makeSpace()).replace("%coins%", MysticosSkywarsAPI.getInstance().getCurrencyAdapterAPI().getCoinsFormatted(p)).replace("%playing%", "" + MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayingPlayers(MapManagerAPI.MapGamemodes.ALL)).replace("%kills%", p.getStatistics(MSWPlayer.PlayerStatistics.KILLS, false) + "").replace("%deaths%", p.getStatistics(MSWPlayer.PlayerStatistics.DEATHS, false) + "").replace("%solowins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, false) + "").replace("%teamwins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, false) + "").replace("%loses%", p.getStatistics(MSWPlayer.PlayerStatistics.LOSES, false) + "").replace("%gamesplayed%", p.getStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED) + "").replace("%playing%", "" + MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayingPlayers(MapManagerAPI.MapGamemodes.ALL)).replace("%rankedkills%", p.getStatistics(MSWPlayer.PlayerStatistics.KILLS, true) + "").replace("%rankeddeaths%", p.getStatistics(MSWPlayer.PlayerStatistics.DEATHS, true) + "").replace("%rankedsolowins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_SOLO, true) + "").replace("%rankedteamwins%", p.getStatistics(MSWPlayer.PlayerStatistics.WINS_TEAMS, true) + "").replace("%rankedloses%", p.getStatistics(MSWPlayer.PlayerStatistics.LOSES, true) + "").replace("%rankedgamesplayed%", p.getStatistics(MSWPlayer.PlayerStatistics.GAMES_PLAYED, true) + "");
        }

        if (MSWConfig.file().getBoolean("Config.PlaceholderAPI-In-Scoreboard")) {
            tmp = PlaceholderAPI.setPlaceholders(p.getPlayer(), tmp);
        }

        // Resolve this value for every scoreboard state, including custom lines.
        tmp = tmp.replace("%playing%", String.valueOf(
                MysticosSkywarsAPI.getInstance().getPlayerManagerAPI()
                        .getPlayingPlayers(MapManagerAPI.MapGamemodes.ALL)));

        return tmp;
    }

    private String nextEvent(MSWMap match) {
        return match.getEvents().isEmpty() ? "-" : match.getEvents().get(0).getName();
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
        if (this.fb != null && !this.fb.isDeleted()) {
            this.fb.delete();
        }
    }

    public void run() {
        this.task = new BukkitRunnable() {
            public void run() {
                List<String> lista;
                String tit;
                if (p.getState() != null) {
                    switch (p.getState()) {
                        case LOBBY_OR_NOGAME:
                            if (!MysticosSkywarsAPI.getInstance().getLobbyManagerAPI().scoreboardInLobby() || !MysticosSkywarsAPI.getInstance().getLobbyManagerAPI().isInLobby(p.getWorld())) {
                                if (MSWPlayerScoreboard.this.fb != null && !MSWPlayerScoreboard.this.fb.isDeleted()) {
                                    MSWPlayerScoreboard.this.fb.delete();
                                }
                                return;
                            }
                            lista = TranslatableList.SCOREBOARD_LOBBY_LINES.get(p);
                            tit = TranslatableLine.SCOREBOARD_LOBBY_TITLE.get(p);
                            break;
                        case CAGE:
                            lista = TranslatableList.SCOREBOARD_CAGE_LINES.get(p);
                            tit = TranslatableLine.SCOREBOARD_CAGE_TITLE.get(p).replace("%map%", p.getMatch().getName()).replace("%displayname%", p.getMatch().getDisplayName()).replace("%mode%", p.getMatch().getGameMode().name());
                            break;
                        case SPECTATOR:
                        case EXTERNAL_SPECTATOR:
                            lista = TranslatableList.SCOREBOARD_SPECTATOR_LINES.get(p);
                            tit = TranslatableLine.SCOREBOARD_SPECTATOR_TITLE.get(p).replace("%map%", p.getMatch().getName()).replace("%displayname%", p.getMatch().getDisplayName()).replace("%mode%", p.getMatch().getGameMode().name());
                            break;
                        case PLAYING:
                            lista = TranslatableList.SCOREBOARD_PLAYING_LINES.get(p);
                            tit = TranslatableLine.SCOREBOARD_PLAYING_TITLE.get(p).replace("%map%", p.getMatch().getName()).replace("%displayname%", p.getMatch().getDisplayName()).replace("%mode%", p.getMatch().getGameMode().name());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value SCOREBOARD!!! : " + p.getState());
                    }

                    List<String> send = lista.stream()
                            .map(s -> variables(s, p))
                            .collect(Collectors.toList());
                    int maxLines = Math.max(1, MSWConfig.file().getInt("Config.Scoreboard.Max-Lines", 15));
                    displayScoreboard(variables(tit, p), send.subList(0, Math.min(send.size(), maxLines)));
                }
            }
        }.runTaskTimer(MysticosSkywarsAPI.getInstance().getPlugin(), 0L,
                Math.max(1, MSWConfig.file().getInt("Config.Scoreboard.Update-Interval", 20)));
    }

    private void displayScoreboard(String title, List<String> elements) {
        if (this.disabled) {
            return;
        }

        try {
            if (this.fb == null || this.fb.isDeleted()) {
                this.fb = new FastBoard(p.getPlayer());
            }
            this.fb.updateTitle(styleTitle(title));
            this.fb.updateLines(elements.stream().map(this::styleLine).collect(Collectors.toList()));
        } catch (final RuntimeException exception) {
            this.disabled = true;
            if (this.task != null) {
                this.task.cancel();
            }
            try {
                if (this.fb != null && !this.fb.isDeleted()) {
                    this.fb.delete();
                }
            } catch (final RuntimeException ignored) {
                // FastBoard can fail while cleaning up on an incompatible server.
            }
            Bukkit.getLogger().warning("FastBoard disabled for " + p.getName()
                    + " because it could not update the scoreboard: " + exception.getMessage());
        }
    }

    private String styleTitle(String title) {
        return title
                .replace("<gradient:#38bdf8:#a78bfa>", "<gradient:#a855f7:#d8b4fe>")
                .replace("<gray>", "<#4ade80>")
                .replace("<#38bdf8>", "<#facc15>")
                .replace("<#a78bfa>", "<#facc15>")
                .replace("&f", "<#4ade80>")
                .replace("&b", "<#facc15>");
    }

    private String styleLine(String line) {
        return line
                .replace("<gray>", "<#4ade80>")
                .replace("<dark_gray>", "<#4ade80>")
                .replace("<#38bdf8>", "<#facc15>")
                .replace("<#a78bfa>", "<#facc15>")
                .replace("&f", "<#4ade80>")
                .replace("&7", "<#4ade80>")
                .replace("&b", "<#facc15>");
    }
}
