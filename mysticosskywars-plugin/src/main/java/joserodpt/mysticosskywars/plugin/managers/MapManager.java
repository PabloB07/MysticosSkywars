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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.cages.MSWCage;
import joserodpt.mysticosskywars.api.cages.MSWSoloCage;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.config.MSWMapsConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.config.TranslatableList;
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.map.MSWSign;
import joserodpt.mysticosskywars.api.map.modes.PlaceholderMode;
import joserodpt.mysticosskywars.api.map.modes.SoloMode;
import joserodpt.mysticosskywars.api.map.modes.teams.MSWTeam;
import joserodpt.mysticosskywars.api.map.modes.teams.TeamsMode;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.MSWPlayerItems;
import joserodpt.mysticosskywars.api.utils.Text;
import joserodpt.mysticosskywars.api.utils.WorldEditUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapManager extends MapManagerAPI {
    private final MysticosSkywarsAPI rs;

    private final Map<String, MSWMap> maps = new HashMap<>();

    public MapManager(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    @Override
    public void loadMaps() {
        this.clearMaps();

        for (String s : MSWMapsConfig.file().getRoot().getRoutesAsStrings(false)) {
            String modeSTR = MSWMapsConfig.file().getString(s + ".Settings.GameType");
            if (modeSTR == null || modeSTR.isEmpty()) {
                rs.getLogger().severe("Mode: " + s + " is invalid! Skipping map: " + s);
                continue;
            }

            try {
                MSWMap.GameMode.valueOf(modeSTR);
            } catch (IllegalArgumentException e) {
                rs.getLogger().severe("Mode: " + s + " isn't supported by this version of MysticosSkywars! Skipping map: " + s);
                continue;
            }

            String worldName = MSWMapsConfig.file().getString(s + ".world");
            String displayName = MSWMapsConfig.file().getString(s + ".Settings.DisplayName");
            if (displayName == null || displayName.isEmpty()) {
                MSWMapsConfig.file().set(s + ".Settings.DisplayName", s);
                MSWMapsConfig.save();
                displayName = s;
            }
            displayName = Text.color(displayName);

            boolean loaded = rs.getWorldManagerAPI().loadWorld(worldName, World.Environment.NORMAL);
            if (loaded) {
                MSWWorld.WorldType wt = getWorldType(MSWMapsConfig.file().getString(s + ".type"));
                Boolean unregistered = MSWMapsConfig.file().getBoolean(s + ".Settings.Unregistered");

                Location specLoc = getSpecLoc(s);
                Map<Location, MSWCage> cgs = getMapCages(s, specLoc.getWorld());

                if (cgs.isEmpty()) {
                    Bukkit.getLogger().severe("[MysticosSkywars] There are no cages in " + worldName + " (possibly a bug? Check config pls!)");
                    continue;
                }

                Map<Location, MSWChest> chests = getMapChests(worldName, s);
                if (chests.isEmpty()) {
                    Bukkit.getLogger().warning("[MysticosSkywars] There are no chests in " + worldName + " (possibly a bug? Check config pls!)");
                }

                World w = Bukkit.getWorld(worldName);

                switch (MSWMap.GameMode.valueOf(modeSTR)) {
                    case SOLO:
                        SoloMode gs = new SoloMode(s, displayName, w, MSWMapsConfig.file().getString(s + ".schematic"), wt, MSWMap.MapState.AVAILABLE, cgs, MSWMapsConfig.file().getInt(s + ".number-of-players"), specLoc, isSpecEnabled(s), isInstantEndingEnabled(s), MSWMapsConfig.file().getBoolean(s + ".Settings.Border"), getPOS1(w, s), getPOS2(w, s), chests, isRanked(s), unregistered);
                        gs.resetArena(MSWMap.OperationReason.LOAD);
                        this.addMap(gs);
                        break;
                    case TEAMS:
                        int numberOfPlayers = MSWMapsConfig.file().getInt(s + ".number-of-players");
                        AtomicInteger tc = new AtomicInteger(1);

                        Map<Location, MSWTeam> ts = new HashMap<>();
                        int teamSize = numberOfPlayers / cgs.size();
                        cgs.forEach((location, value) -> ts.put(location, new MSWTeam(tc.getAndIncrement(), teamSize, location)));

                        TeamsMode teas = new TeamsMode(s, displayName, w, MSWMapsConfig.file().getString(s + ".schematic"), wt, MSWMap.MapState.AVAILABLE, ts, MSWMapsConfig.file().getInt(s + ".number-of-players"), specLoc, isSpecEnabled(s), isInstantEndingEnabled(s), MSWMapsConfig.file().getBoolean(s + ".Settings.Border"), getPOS1(w, s), getPOS2(w, s), chests, isRanked(s), unregistered);
                        teas.resetArena(MSWMap.OperationReason.LOAD);
                        this.addMap(teas);
                        break;
                    default:
                        throw new IllegalStateException("Mode doesnt exist: " + modeSTR);
                }
            }
        }
    }

    @Override
    public void deleteMap(MSWMap map) {
        map.kickPlayers(null);

        map.getSigns().forEach(MSWSign::delete);

        map.getMSWWorld().getWorld().getPlayers().forEach(player -> rs.getLobbyManagerAPI().tpToLobby(player));

        this.maps.remove(map.getName().toLowerCase());
        MSWMapsConfig.file().remove(map.getName());
        MSWMapsConfig.save();
    }

    @Override
    public MSWMap getMap(World w) {
        return this.maps.values().stream().filter(r -> r.getMSWWorld().getWorld().equals(w)).findFirst().orElse(null);
    }

    @Override
    public MSWMap getMap(String s) {
        s = s.toLowerCase();
        return this.maps.get(s);
    }

    @Override
    public List<MSWMap> getMapsForPlayer(MSWPlayer mswPlayer) {
        List<MSWMap> f = new ArrayList<>();
        switch (mswPlayer.getPlayerMapViewerPref()) {
            case MAPV_ALL:
                f.addAll(mswPlayer.getPlayer().hasPermission("msw.admin") || mswPlayer.getPlayer().isOp() ? this.maps.values() : this.maps.values().stream().filter(Predicate.not(MSWMap::isUnregistered)).collect(Collectors.toList()));
                break;
            case MAPV_WAITING:
                f.addAll(this.maps.values().stream().filter(r -> r.getState().equals(MSWMap.MapState.WAITING) && !r.isUnregistered()).collect(Collectors.toList()));
                break;
            case MAPV_STARTING:
                f.addAll(this.maps.values().stream().filter(r -> r.getState().equals(MSWMap.MapState.STARTING) && !r.isUnregistered()).collect(Collectors.toList()));
                break;
            case MAPV_AVAILABLE:
                f.addAll(this.maps.values().stream().filter(r -> r.getState().equals(MSWMap.MapState.AVAILABLE) && !r.isUnregistered()).collect(Collectors.toList()));
                break;
            case MAPV_SPECTATE:
                f.addAll(this.maps.values().stream().filter(r -> (r.getState().equals(MSWMap.MapState.PLAYING) || r.getState().equals(MSWMap.MapState.FINISHING) && !r.isUnregistered())).collect(Collectors.toList()));
                break;
            case SOLO:
                f.addAll(this.getMaps(MapGamemodes.SOLO));
                break;
            case TEAMS:
                f.addAll(this.getMaps(MapGamemodes.TEAMS));
                break;
            case SOLO_RANKED:
                f.addAll(this.getMaps(MapGamemodes.SOLO_RANKED));
                break;
            case TEAMS_RANKED:
                f.addAll(this.getMaps(MapGamemodes.TEAMS_RANKED));
                break;
            default:
                break;
        }
        return f.isEmpty() ? Collections.singletonList(new PlaceholderMode("No Maps Found")) : f;
    }

    @Override
    public Collection<MSWMap> getMaps(MapGamemodes pt) {
        switch (pt) {
            case ALL:
                return this.maps.values();
            case SOLO:
                return this.maps.values().stream().filter(r -> r.getGameMode().equals(MSWMap.GameMode.SOLO) && !r.isUnregistered()).collect(Collectors.toList());
            case TEAMS:
                return this.maps.values().stream().filter(r -> r.getGameMode().equals(MSWMap.GameMode.TEAMS) && !r.isUnregistered()).collect(Collectors.toList());
            case RANKED:
                return this.maps.values().stream().filter(mswGame -> mswGame.isRanked() && !mswGame.isUnregistered()).collect(Collectors.toList());
            case SOLO_RANKED:
                return this.maps.values().stream().filter(r -> r.isRanked() && !r.isUnregistered() && r.getGameMode().equals(MSWMap.GameMode.SOLO)).collect(Collectors.toList());
            case TEAMS_RANKED:
                return this.maps.values().stream().filter(r -> r.isRanked() && !r.isUnregistered() && r.getGameMode().equals(MSWMap.GameMode.TEAMS)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<Location, MSWCage> getMapCages(String mapName, World w) {
        Map<Location, MSWCage> locs = new HashMap<>();
        int id = 0;
        for (String i : MSWMapsConfig.file().getSection(mapName + ".Locations.Cages").getRoutesAsStrings(false)) {
            int x = MSWMapsConfig.file().getInt(mapName + ".Locations.Cages." + i + ".X");
            int y = MSWMapsConfig.file().getInt(mapName + ".Locations.Cages." + i + ".Y");
            int z = MSWMapsConfig.file().getInt(mapName + ".Locations.Cages." + i + ".Z");
            locs.put(new Location(w, x, y, z), new MSWSoloCage(id, x, y, z));
            ++id;
        }
        return locs;
    }

    @Override
    protected Map<Location, MSWChest> getMapChests(String worldName, String section) {
        Map<Location, MSWChest> chests = new HashMap<>();
        if (MSWMapsConfig.file().isSection(section + ".Chests")) {
            for (String i : MSWMapsConfig.file().getSection(section + ".Chests").getRoutesAsStrings(false)) {
                int x = MSWMapsConfig.file().getInt(section + ".Chests." + i + ".LocationX");
                int y = MSWMapsConfig.file().getInt(section + ".Chests." + i + ".LocationY");
                int z = MSWMapsConfig.file().getInt(section + ".Chests." + i + ".LocationZ");
                BlockFace f = BlockFace.valueOf(MSWMapsConfig.file().getString(section + ".Chests." + i + ".Face"));

                MSWChest.Type ct;
                try {
                    ct = MSWChest.Type.valueOf(MSWMapsConfig.file().getString(section + ".Chests." + i + ".Type"));
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Chest type invalid while loading " + worldName + "!! >> Chest id: " + i + ". Assigning NORMAL type.");
                    ct = MSWChest.Type.NORMAL;
                }

                chests.put(new Location(Bukkit.getWorld(worldName), x, y, z), new MSWChest(ct, worldName, x, y, z, f));
            }
        } else {
            Debugger.print(MapManager.class, "There are no chests in " + worldName + " (possibly a bug? Check config pls!)");
        }
        return chests;
    }

    @Override
    public void setupSolo(MSWPlayer p, String mapname, String displayName, MSWWorld.WorldType wt, int maxP) {
        TranslatableLine.GENERATING_WORLD.send(p, true);

        String cleanMapName = mapname.replace(".schematic", "").replace(".schem", "");

        World w = rs.getWorldManagerAPI().createEmptyWorld(cleanMapName, World.Environment.NORMAL);
        if (w != null) {
            MSWMap s = new SoloMode(cleanMapName, displayName, w, mapname, wt, maxP);

            commonSetup(p, mapname, wt, maxP, w, s);
        } else {
            rs.getLogger().warning("Could not create setup world for " + mapname);
        }
    }

    @Override
    public void setupTeams(MSWPlayer p, String mapname, String displayName, MSWWorld.WorldType wt, int teams, int pperteam) {
        TranslatableLine.GENERATING_WORLD.send(p, true);

        String cleanMapName = mapname.replace(".schematic", "").replace(".schem", "");

        World w = rs.getWorldManagerAPI().createEmptyWorld(cleanMapName, World.Environment.NORMAL);
        if (w != null) {
            MSWMap s = new TeamsMode(cleanMapName, cleanMapName, w, cleanMapName, wt, teams, pperteam);

            commonSetup(p, mapname, wt, teams, w, s);
        } else {
            rs.getLogger().warning("Could not create setup world for " + mapname);
        }
    }

    private void commonSetup(MSWPlayer p, String mapname, MSWWorld.WorldType wt, int teams, World w, MSWMap s) {
        w.getBlockAt(0, 64, 0).setType(Material.BEDROCK);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        w.setGameRule(GameRule.DO_INSOMNIA, false);
        w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);

        Location loc = new Location(w, 0, 66, 0);

        Text.sendList(p.getPlayer(), Text.replaceVarInList(TranslatableList.EDIT_MAP.get(p), "%cages%", teams + ""));

        p.getInventory().clear();
        MSWPlayerItems.SETUP.giveSet(p);
        p.getPlayer().setGameMode(org.bukkit.GameMode.CREATIVE);

        if (wt == MSWWorld.WorldType.SCHEMATIC) {
            w.setAutoSave(false);

            p.teleport(loc);

            Bukkit.getScheduler().scheduleSyncDelayedTask(rs.getPlugin(), () -> WorldEditUtils.pasteSchematic(mapname, new Location(p.getWorld(), 0, 64, 0), s), 3 * 20);
        } else {
            w.setAutoSave(true);
            p.teleport(loc);
        }

        this.addMap(s);
    }

    @Override
    public void finishMap(MSWPlayer p) {
        MSWMap map = this.getMap(p.getWorld().getName());
        if (map == null) {
            TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            return;
        }

        if (!map.isUnregistered()) {
            TranslatableLine.MAP_UNREGISTER_TO_EDIT.send(p, true);
            return;
        }

        if (map.getCages().isEmpty()) {
            TranslatableLine.CMD_NO_CAGES_SET.send(p, true);
            return;
        }

        if (map.getGameMode() == MSWMap.GameMode.SOLO && map.getCages().size() != map.getMaxPlayers()) {
            TranslatableLine.CMD_INCORRECT_NUMBER_OF_CAGES_SOLO.send(p, true);
            return;
        }

        if (map.getGameMode() == MSWMap.GameMode.TEAMS && map.getCages().size() != map.getTeams().size()) {
            TranslatableLine.CMD_INCORRECT_NUMBER_OF_CAGES_TEAMS.send(p, true);
            return;
        }

        if (map.getSpectatorLocation() == null) {
            TranslatableLine.CMD_SPEC_LOCATION_NOT_SET.send(p, true);
            return;
        }

        if (map.getMapCuboid() == null || map.getPOS1() == null || map.getPOS2() == null) {
            WorldEditPlugin w = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            try {
                assert w != null;
                Region r = w.getSession(p.getPlayer()).getSelection(w.getSession(p.getPlayer()).getSelectionWorld());

                if (r != null) {
                    map.setBoundaries(new Location(map.getMSWWorld().getWorld(), r.getMinimumPoint().getBlockX(), r.getMinimumPoint().getBlockY(), r.getMinimumPoint().getBlockZ()),
                            new Location(map.getMSWWorld().getWorld(), r.getMaximumPoint().getBlockX(), r.getMaximumPoint().getBlockY(), r.getMaximumPoint().getBlockZ()));
                }
            } catch (Exception e) {
                TranslatableLine.NO_ARENA_BOUNDARIES.send(p, true);
                rs.getLogger().warning("Error while setting arena boundaries for " + map.getName() + " (possibly a bug?)");
                rs.getLogger().warning(e.getMessage());
                return;
            }
        }

        TranslatableLine.SAVING_MAP.send(p, true);

        // Beacon Remove
        map.getCages().forEach(cage -> map.getMSWWorld().getWorld().getBlockAt(cage.getLocation()).setType(Material.AIR));

        //Remove dropped items
        rs.getWorldManagerAPI().clearDroppedItems(map.getMSWWorld().getWorld());

        //worldType
        if (map.getMSWWorld().getType() == MSWWorld.WorldType.DEFAULT) {
            map.getMSWWorld().save();

            //Copy world
            rs.getWorldManagerAPI().copyWorld(map.getMSWWorld().getName(), WorldManager.CopyTo.MSW_FOLDER);

            File template = new File(rs.getPlugin().getDataFolder(), "maps/" + map.getMSWWorld().getName());
            if (!template.isDirectory()) {
                rs.getLogger().severe("Map template was not created: " + template);
                TranslatableLine.MAP_UNREGISTER_TO_EDIT.send(p, true);
                return;
            }
        }

        map.getCages().forEach(mswCage -> mswCage.setMap(map));

        p.getPlayer().getInventory().clear();
        rs.getLobbyManagerAPI().tpToLobby(p);

        // Save Data
        map.save(MSWMap.Data.ALL, true);

        map.setUnregistered(false);
        map.resetArena(MSWMap.OperationReason.RESET);
        TranslatableLine.MAP_REGISTERED.send(p, true);
    }

    @Override
    protected Boolean isInstantEndingEnabled(String s) {
        return MSWMapsConfig.file().getBoolean(s + ".Settings.Instant-End");
    }

    @Override
    protected Location getPOS1(World w, String s) {
        double hx = MSWMapsConfig.file().getDouble(s + ".World.Border.POS1-X");
        double hy = MSWMapsConfig.file().getDouble(s + ".World.Border.POS1-Y");
        double hz = MSWMapsConfig.file().getDouble(s + ".World.Border.POS1-Z");

        return new Location(w, hx, hy, hz);
    }

    @Override
    protected Location getPOS2(World w, String s) {
        double hx = MSWMapsConfig.file().getDouble(s + ".World.Border.POS2-X");
        double hy = MSWMapsConfig.file().getDouble(s + ".World.Border.POS2-Y");
        double hz = MSWMapsConfig.file().getDouble(s + ".World.Border.POS2-Z");

        return new Location(w, hx, hy, hz);
    }

    @Override
    public Boolean isSpecEnabled(String s) {
        return MSWMapsConfig.file().getBoolean(s + ".Settings.Spectator");
    }

    @Override
    public Location getSpecLoc(String nome) {
        double x = MSWMapsConfig.file().getDouble(nome + ".Locations.Spectator.X");
        double y = MSWMapsConfig.file().getDouble(nome + ".Locations.Spectator.Y");
        double z = MSWMapsConfig.file().getDouble(nome + ".Locations.Spectator.Z");
        float pitch = MSWMapsConfig.file().getFloat(nome + ".Locations.Spectator.Pitch");
        float yaw = MSWMapsConfig.file().getFloat(nome + ".Locations.Spectator.Yaw");
        return new Location(Bukkit.getWorld(nome), x, y, z, pitch, yaw);
    }

    @Override
    protected MSWWorld.WorldType getWorldType(String s) {
        return MSWWorld.WorldType.valueOf(s);
    }

    @Override
    protected Boolean isRanked(String s) {
        return MSWMapsConfig.file().getBoolean(s + ".Settings.Ranked");
    }

    @Override
    public void endMaps(boolean shutdown) {
        this.shutdown = shutdown;

        for (MSWMap g : this.maps.values()) {
            g.kickPlayers(TranslatableLine.ADMIN_SHUTDOWN.getSingle());
            g.resetArena(MSWMap.OperationReason.SHUTDOWN);
            g.clear();
        }

        Bukkit.getScoreboardManager().getMainScoreboard().getTeams().forEach(Team::unregister);
    }

    @Override
    public void findNextMap(MSWPlayer player, MSWMap.GameMode type) {
        UUID playerUUID = player.getUUID();
        if (!rs.getPlayerManagerAPI().getTeleporting().contains(playerUUID)) {
            rs.getPlayerManagerAPI().getTeleporting().add(playerUUID);

            Optional<MSWMap> suitableGame = findSuitableGame(type);
            if (suitableGame.isPresent()) {
                if (suitableGame.get().isFull()) {
                    TranslatableLine.ROOM_FULL.send(player, true);
                    rs.getPlayerManagerAPI().getTeleporting().remove(playerUUID);
                    return;
                }

                TranslatableLine.CMD_MAP_FOUND.send(player, true);
                if (player.isInMatch()) {
                    player.getMatch().removePlayer(player);
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                    suitableGame.get().addPlayer(player);
                    rs.getPlayerManagerAPI().getTeleporting().remove(player.getUUID());
                }, 5);
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(player, true);
                rs.getPlayerManagerAPI().getTeleporting().remove(player.getUUID());

                if (rs.getLobbyManagerAPI().getLobbyLocation() != null && rs.getLobbyManagerAPI().getLobbyLocation().getWorld() != null && Objects.equals(rs.getLobbyManagerAPI().getLobbyLocation().getWorld(), player.getWorld())) {
                    rs.getLobbyManagerAPI().tpToLobby(player);
                }
            }
        }
    }

    @Override
    public Optional<MSWMap> findSuitableGame(MSWMap.GameMode type) {
        Optional<MSWMap> mswMap;
        if (type == null) {
            //first, find all games of the type required that are starting
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getState() == MSWMap.MapState.STARTING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games waiting sorting by max players
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getState() == MSWMap.MapState.WAITING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games available sorting by max players
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getState() == MSWMap.MapState.AVAILABLE)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games sorting by max players, excluding playing games or finishing games
            return this.maps.values().stream()
                    .filter(map -> map.getState() != MSWMap.MapState.PLAYING && map.getState() != MSWMap.MapState.FINISHING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
        } else {
            //first, find all games of the type required that are starting
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getGameMode() == type && map.getState() == MSWMap.MapState.STARTING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games waiting sorting by max players
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getGameMode() == type && map.getState() == MSWMap.MapState.WAITING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games available sorting by max players
            mswMap = this.maps.values().stream()
                    .filter(map -> map.getGameMode() == type && map.getState() == MSWMap.MapState.AVAILABLE)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
            if (mswMap.isPresent())
                return mswMap;

            //then, games sorting by max players, excluding playing games or finishing games
            return this.maps.values().stream()
                    .filter(map -> map.getGameMode() == type && map.getState() != MSWMap.MapState.PLAYING && map.getState() != MSWMap.MapState.FINISHING)
                    .max(Comparator.comparingInt(MSWMap::getPlayerCount));
        }
    }

    @Override
    public void clearMaps() {
        this.maps.clear();
    }

    @Override
    public void addMap(MSWMap s) {
        this.maps.put(s.getName().toLowerCase(), s);
    }

    @Override
    public Collection<String> getMapNames() {
        return this.maps.keySet();
    }

    @Override
    public void editMap(MSWPlayer p, MSWMap map) {
        if (!map.isUnregistered()) {
            TranslatableLine.MAP_UNREGISTER_TO_EDIT.send(p, true);
            return;
        }

        p.setGameMode(org.bukkit.GameMode.CREATIVE);
        p.teleport(map.getSpectatorLocation());
        Text.sendList(p.getPlayer(), Text.replaceVarInList(TranslatableList.EDIT_MAP.get(p), "%cages%", map.getGameMode() == MSWMap.GameMode.SOLO ? String.valueOf(map.getMaxPlayers()) : map.getTeams().size() + ""));
        p.getInventory().clear();
        MSWPlayerItems.SETUP.giveSet(p);

        map.getCages().forEach(mswCage -> map.getMSWWorld().getWorld().getBlockAt(mswCage.getLocation()).setType(Material.BEACON));
    }

    @Override
    public void duplicateMap(MSWMap original, String newName) {
        MSWMap newMap = original.duplicate(newName);
        if (newMap != null) {
            //rest world
            newMap.reset();
            this.addMap(newMap);
            newMap.setUnregistered(false);
            newMap.save(MSWMap.Data.ALL, true);
        } else {
            rs.getLogger().warning("Could not duplicate map " + original.getName() + " to " + newName + " (possibly a bug?)");
        }
    }
}
