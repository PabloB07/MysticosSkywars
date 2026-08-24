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
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWHologramConfig;
import joserodpt.mysticosskywars.api.leaderboards.MSWLeaderboard;
import joserodpt.mysticosskywars.api.managers.HologramManagerAPI;
import joserodpt.mysticosskywars.api.managers.holograms.HologramType;
import joserodpt.mysticosskywars.api.managers.holograms.MSWHologram;
import joserodpt.mysticosskywars.api.managers.holograms.MSWLobbyHologram;
import joserodpt.mysticosskywars.api.managers.holograms.support.DHLobbyHologram;
import joserodpt.mysticosskywars.api.managers.holograms.support.HDLobbyHologram;
import joserodpt.mysticosskywars.api.managers.holograms.support.NoLobbyHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all lobby holograms: winner display, leaderboards, server info, etc.
 */
public class LobbyHologramManager {

    private final HologramManagerAPI hologramManagerAPI;
    private final JavaPlugin plugin;
    private final Map<String, MSWLobbyHologram> holograms = new HashMap<>();
    private final Map<String, HologramType> hologramTypes = new HashMap<>();
    private BukkitTask refreshTask;

    public LobbyHologramManager(HologramManagerAPI hologramManagerAPI, JavaPlugin plugin) {
        this.hologramManagerAPI = hologramManagerAPI;
        this.plugin = plugin;
    }

    /**
     * Creates a new lobby hologram of the specified type.
     */
    public MSWLobbyHologram createHologram(String id, HologramType type, Location loc) {
        MSWLobbyHologram holo = createHologramInstance(id, type);
        holo.spawn(loc, id);
        holograms.put(id, holo);
        hologramTypes.put(id, type);
        return holo;
    }

    /**
     * Removes a hologram by its ID.
     */
    public void removeHologram(String id) {
        MSWLobbyHologram holo = holograms.remove(id);
        hologramTypes.remove(id);
        if (holo != null) {
            holo.delete();
        }
    }

    /**
     * Gets a hologram by its ID.
     */
    public MSWLobbyHologram getHologram(String id) {
        return holograms.get(id);
    }

    /**
     * Gets all registered holograms.
     */
    public Map<String, MSWLobbyHologram> getAllHolograms() {
        return new HashMap<>(holograms);
    }

    /**
     * Refreshes all hologram data based on their type.
     */
    public void refreshAll() {
        if (!MSWHologramConfig.file().getBoolean("Config.Enabled", true)) return;

        for (Map.Entry<String, MSWLobbyHologram> entry : holograms.entrySet()) {
            refreshHologram(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Refreshes a single hologram's data.
     */
    public void refreshHologram(String id, MSWLobbyHologram holo) {
        HologramType type = hologramTypes.get(id);
        if (type == null) return;

        switch (type) {
            case LAST_WINNER:
                refreshLastWinner(holo);
                break;
            case TOP_WINS_SOLO:
                refreshLeaderboard(holo, MSWLeaderboard.MSWLeaderboardCategories.SOLO_WINS, "wins");
                break;
            case TOP_WINS_TEAMS:
                refreshLeaderboard(holo, MSWLeaderboard.MSWLeaderboardCategories.TEAMS_WINS, "wins");
                break;
            case TOP_KILLS:
                refreshLeaderboard(holo, MSWLeaderboard.MSWLeaderboardCategories.KILLS, "kills");
                break;
            case TOP_COINS:
                refreshCoinsLeaderboard(holo);
                break;
            case SERVER_INFO:
                refreshServerInfo(holo);
                break;
            case CUSTOM:
                refreshCustomHologram(id, holo);
                break;
        }
    }

    private void refreshLastWinner(MSWLobbyHologram holo) {
        List<String> lines = MSWHologramConfig.file().getStringList("Config.Lines.Last-Winner");
        if (lines.isEmpty()) {
            lines = new ArrayList<>();
            lines.add("&6&lLast Winner");
            lines.add("&f%winner%");
        }

        String winner = MSWHologramConfig.file().getString("Data.Last-Winner.Name", "None");
        String map = MSWHologramConfig.file().getString("Data.Last-Winner.Map", "None");
        String time = MSWHologramConfig.file().getString("Data.Last-Winner.Time", "Never");

        holo.delete();
        Location loc = null;
        if (holograms.containsKey("last_winner")) {
            // Re-spawn at same location
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i)
                        .replace("%winner%", winner)
                        .replace("%map%", map)
                        .replace("%time%", time);
                if (i == 0) {
                    // First line creates the hologram
                    // Need to get location from config
                    loc = getHologramLocation("last_winner");
                    if (loc != null) {
                        holo.spawn(loc, "last_winner");
                    }
                }
                holo.addLine(line);
            }
        }
    }

    private void refreshLeaderboard(MSWLobbyHologram holo, MSWLeaderboard.MSWLeaderboardCategories category, String statName) {
        List<String> configLines;
        switch (category) {
            case SOLO_WINS:
                configLines = MSWHologramConfig.file().getStringList("Config.Lines.Top-Wins-Solo");
                break;
            case TEAMS_WINS:
                configLines = MSWHologramConfig.file().getStringList("Config.Lines.Top-Wins-Teams");
                break;
            case KILLS:
                configLines = MSWHologramConfig.file().getStringList("Config.Lines.Top-Kills");
                break;
            default:
                configLines = new ArrayList<>();
        }

        if (configLines.isEmpty()) {
            configLines = new ArrayList<>();
            configLines.add("&7Top " + statName);
        }

        MSWLeaderboard lb = MysticosSkywarsAPI.getInstance().getLeaderboardManagerAPI().getLeaderboard(category);

        holo.delete();
        Location loc = getHologramLocation(holo.getId());
        if (loc != null) {
            holo.spawn(loc, holo.getId());
        }

        for (String line : configLines) {
            // Replace leaderboard placeholders
            String processed = line;
            for (int i = 1; i <= 10; i++) {
                String name = lb != null ? lb.getIndex(i) : "&7" + i + ". &f?";
                String[] parts = name.split(" - ");
                String playerName = parts.length > 0 ? parts[0].replaceAll("&[a-f0-9k-or]", "").trim() : "?";
                String value = parts.length > 1 ? parts[1].trim() : "0";

                processed = processed
                        .replace("%player" + i + "%", playerName)
                        .replace("%" + statName + i + "%", value);
            }
            holo.addLine(processed);
        }
    }

    private void refreshCoinsLeaderboard(MSWLobbyHologram holo) {
        List<String> configLines = MSWHologramConfig.file().getStringList("Config.Lines.Top-Coins");
        if (configLines.isEmpty()) {
            configLines = new ArrayList<>();
            configLines.add("&7Top Coins");
        }

        // Use SOLO_WINS as a placeholder since coins don't have their own leaderboard
        MSWLeaderboard lb = MysticosSkywarsAPI.getInstance().getLeaderboardManagerAPI().getLeaderboard(MSWLeaderboard.MSWLeaderboardCategories.SOLO_WINS);

        holo.delete();
        Location loc = getHologramLocation(holo.getId());
        if (loc != null) {
            holo.spawn(loc, holo.getId());
        }

        for (String line : configLines) {
            String processed = line;
            for (int i = 1; i <= 10; i++) {
                String name = lb != null ? lb.getIndex(i) : "&7" + i + ". &f?";
                String[] parts = name.split(" - ");
                String playerName = parts.length > 0 ? parts[0].replaceAll("&[a-f0-9k-or]", "").trim() : "?";
                String value = parts.length > 1 ? parts[1].trim() : "0";

                processed = processed
                        .replace("%player" + i + "%", playerName)
                        .replace("%coins" + i + "%", value);
            }
            holo.addLine(processed);
        }
    }

    private void refreshServerInfo(MSWLobbyHologram holo) {
        List<String> lines = MSWHologramConfig.file().getStringList("Config.Lines.Server-Info");
        if (lines.isEmpty()) {
            lines = new ArrayList<>();
            lines.add("&9MysticosSkywars");
        }

        holo.delete();
        Location loc = getHologramLocation(holo.getId());
        if (loc != null) {
            holo.spawn(loc, holo.getId());
        }

        for (String line : lines) {
            String processed = line
                    .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                    .replace("%games%", String.valueOf(MysticosSkywarsAPI.getInstance().getMapManagerAPI().getMaps(joserodpt.mysticosskywars.api.managers.MapManagerAPI.MapGamemodes.ALL).size()))
                    .replace("%maps%", String.valueOf(MysticosSkywarsAPI.getInstance().getMapManagerAPI().getMapNames().size()));
            holo.addLine(processed);
        }
    }

    private void refreshCustomHologram(String id, MSWLobbyHologram holo) {
        List<String> lines = MSWHologramConfig.file().getStringList("Data.Custom-Holograms." + id + ".Lines");
        if (lines.isEmpty()) return;

        holo.delete();
        Location loc = getHologramLocation(id);
        if (loc != null) {
            holo.spawn(loc, id);
        }

        for (String line : lines) {
            holo.addLine(line);
        }
    }

    private Location getHologramLocation(String id) {
        String path = "Data.Holograms." + id;
        if (!MSWHologramConfig.file().isSection(path)) return null;

        String worldName = MSWHologramConfig.file().getString(path + ".World", "");
        double x = MSWHologramConfig.file().getDouble(path + ".X", 0.0);
        double y = MSWHologramConfig.file().getDouble(path + ".Y", 0.0);
        double z = MSWHologramConfig.file().getDouble(path + ".Z", 0.0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(world, x, y, z);
    }

    private void saveHologramLocation(String id, Location loc) {
        String path = "Data.Holograms." + id;
        MSWHologramConfig.file().set(path + ".World", loc.getWorld().getName());
        MSWHologramConfig.file().set(path + ".X", loc.getX());
        MSWHologramConfig.file().set(path + ".Y", loc.getY());
        MSWHologramConfig.file().set(path + ".Z", loc.getZ());
        MSWHologramConfig.save();
    }

    /**
     * Sets the last winner data and refreshes the hologram.
     */
    public void setLastWinner(String playerName, String mapName) {
        MSWHologramConfig.file().set("Data.Last-Winner.Name", playerName);
        MSWHologramConfig.file().set("Data.Last-Winner.Map", mapName);
        MSWHologramConfig.file().set("Data.Last-Winner.Time", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
        MSWHologramConfig.save();

        MSWLobbyHologram holo = holograms.get("last_winner");
        if (holo != null) {
            refreshHologram("last_winner", holo);
        }
    }

    /**
     * Starts the automatic refresh task.
     */
    public void startRefreshTask() {
        if (!MSWHologramConfig.file().getBoolean("Config.Enabled", true)) return;

        int interval = MSWHologramConfig.file().getInt("Config.Refresh-Interval", 600);
        this.refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                refreshAll();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    /**
     * Stops the refresh task.
     */
    public void stopRefreshTask() {
        if (this.refreshTask != null) {
            this.refreshTask.cancel();
            this.refreshTask = null;
        }
    }

    /**
     * Loads holograms from config.
     */
    public void loadHolograms() {
        if (!MSWHologramConfig.file().getBoolean("Config.Enabled", true)) return;

        if (MSWHologramConfig.file().isSection("Data.Holograms")) {
            for (String id : MSWHologramConfig.file().getSection("Data.Holograms").getRoutesAsStrings(false)) {
                String typeStr = MSWHologramConfig.file().getString("Data.Holograms." + id + ".Type", "CUSTOM");
                try {
                    HologramType type = HologramType.valueOf(typeStr);
                    Location loc = getHologramLocation(id);
                    if (loc != null) {
                        createHologram(id, type, loc);
                    }
                } catch (IllegalArgumentException e) {
                    MysticosSkywarsAPI.getInstance().getLogger().warning("Invalid hologram type: " + typeStr + " for hologram: " + id);
                }
            }
        }

        refreshAll();
        startRefreshTask();
    }

    /**
     * Deletes all holograms.
     */
    public void deleteAll() {
        stopRefreshTask();
        for (MSWLobbyHologram holo : holograms.values()) {
            holo.delete();
        }
        holograms.clear();
        hologramTypes.clear();
    }

    private MSWLobbyHologram createHologramInstance(String id, HologramType type) {
        MSWHologram.HType hType = hologramManagerAPI.getHologramInstance().getType();
        switch (hType) {
            case DECENT_HOLOGRAMS:
                return new DHLobbyHologram(id, type);
            case HOLOGRAPHIC_DISPLAYS:
                return new HDLobbyHologram(id, type);
            default:
                return new NoLobbyHologram(id, type);
        }
    }
}
