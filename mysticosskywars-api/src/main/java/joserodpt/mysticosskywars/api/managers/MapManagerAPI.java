package joserodpt.mysticosskywars.api.managers;

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

import joserodpt.mysticosskywars.api.cages.MSWCage;
import joserodpt.mysticosskywars.api.chests.MSWChest;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class MapManagerAPI {
    public Boolean shutdown = false;

    public abstract void loadMaps();

    public abstract void deleteMap(MSWMap map);

    public abstract MSWMap getMap(World w);

    public abstract MSWMap getMap(String s);

    public abstract void endMaps(boolean shutdown);

    public abstract List<MSWMap> getMapsForPlayer(MSWPlayer mswPlayer);

    public abstract Collection<MSWMap> getMaps(MapGamemodes pt);

    protected abstract Map<Location, MSWCage> getMapCages(String s, World w);

    protected abstract Map<Location, MSWChest> getMapChests(String worldName, String section);

    public abstract void setupSolo(MSWPlayer p, String mapname, String displayName, MSWWorld.WorldType wt, int maxP);

    public abstract void setupTeams(MSWPlayer p, String mapname, String displayName, MSWWorld.WorldType wt, int teams, int pperteam);

    public abstract void finishMap(MSWPlayer p);

    protected abstract MSWWorld.WorldType getWorldType(String s);

    protected abstract Boolean isInstantEndingEnabled(String s);

    protected abstract Location getPOS1(World w, String s);

    protected abstract Location getPOS2(World w, String s);

    public abstract Boolean isSpecEnabled(String s);

    public abstract Location getSpecLoc(String nome);

    protected abstract Boolean isRanked(String s);

    public abstract void findNextMap(MSWPlayer player, MSWMap.GameMode type);

    public abstract Optional<MSWMap> findSuitableGame(MSWMap.GameMode type);

    public abstract void clearMaps();

    public abstract void addMap(MSWMap s);

    public abstract Collection<String> getMapNames();

    public abstract void editMap(MSWPlayer p, MSWMap sw);

    public abstract void duplicateMap(MSWMap original, String newName);

    public enum MapGamemodes {SOLO, SOLO_RANKED, TEAMS, TEAMS_RANKED, RANKED, ALL}
}
