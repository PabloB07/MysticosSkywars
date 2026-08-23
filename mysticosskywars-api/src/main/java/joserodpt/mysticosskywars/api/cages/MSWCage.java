package joserodpt.mysticosskywars.api.cages;

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

import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;

public abstract class MSWCage {
    protected final int id;
    protected final int x, y, z;
    protected MSWMap map;

    public MSWCage(int id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MSWCage(int id, Location l) {
        this(id, l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public int getID() {
        return this.id;
    }

    public Location getLocation() {
        return new Location(this.map.getMSWWorld().getWorld(), this.x, this.y, this.z).add(0.5, 0, 0.5);
    }

    public MSWMap getMap() {
        return this.map;
    }

    public void setMap(MSWMap map) {
        this.map = map;
    }

    public abstract boolean isEmpty();

    public abstract void setCage();

    public abstract void addPlayer(MSWPlayer p);

    public abstract void removePlayer(MSWPlayer p);

    public abstract void tpPlayer(MSWPlayer p);

    public abstract Collection<MSWPlayer> getPlayers();

    public abstract void clearCage();

    public abstract void setCage(Material m);

    public abstract void open();
}