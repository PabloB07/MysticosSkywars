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

import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MSWTeamCage extends MSWCage {

    private final List<MSWPlayer> players = new ArrayList<>();

    public MSWTeamCage(int id, int x, int y, int z) {
        super(id, x, y, z);
    }

    @Override
    public boolean isEmpty() {
        return this.getPlayers().isEmpty();
    }

    @Override
    public void tpPlayer(MSWPlayer p) {
        // Adjust tpLocation based on the number of players to avoid overlapping
        Location tpLocation = getLocation().clone().add(0, 1, 0);
        p.teleport(MathUtils.lookAt(tpLocation, super.getMap().getSpectatorLocation()));
        this.players.add(p);
    }

    @Override
    public void setCage(Material m) {
        World w = map.getMSWWorld().getWorld();

        int xCage = x;
        int yCage = y;
        int zCage = z;

        // chao
        w.getBlockAt(xCage + 1, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage - 1, zCage + 1).setType(m);
        w.getBlockAt(xCage + 1, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage + 1).setType(m);

        yCage = yCage + 4;

        // teto
        w.getBlockAt(xCage + 1, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage - 1, zCage + 1).setType(m);
        w.getBlockAt(xCage + 1, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage - 1).setType(m);
        w.getBlockAt(xCage - 1, yCage - 1, zCage + 1).setType(m);

        // paredes 1 e 3
        yCage = y;
        xCage = xCage + 2;
        w.getBlockAt(xCage, yCage, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage, yCage, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage + 1).setType(m);

        xCage = x;
        xCage = xCage - 2;
        w.getBlockAt(xCage, yCage, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage, yCage, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage - 1).setType(m);
        w.getBlockAt(xCage, yCage, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage + 1).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage + 1).setType(m);

        // paredes 3 e 4
        xCage = x;
        zCage = zCage - 2;
        w.getBlockAt(xCage, yCage, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage + 2, zCage).setType(m);

        zCage = z;
        zCage = zCage + 2;
        w.getBlockAt(xCage, yCage, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage + 1, zCage).setType(m);
        w.getBlockAt(xCage, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage - 1, yCage + 2, zCage).setType(m);
        w.getBlockAt(xCage + 1, yCage + 2, zCage).setType(m);
    }

    @Override
    public void setCage() {
        setCage(this.players.isEmpty() ? Material.GLASS : this.players.get(0).getCageBlock());
    }

    @Override
    public void clearCage() {
        setCage(Material.AIR);
        this.players.clear();
    }

    @Override
    public void addPlayer(MSWPlayer p) {
        this.players.add(p);
        p.setPlayerCage(this);
        this.setCage();
        this.tpPlayer(p);
        p.setInvincible(true);
    }

    @Override
    public void removePlayer(MSWPlayer p) {
        this.players.remove(p);
        p.setPlayerCage(null);
    }

    @Override
    public Collection<MSWPlayer> getPlayers() {
        return this.players;
    }

    @Override
    public void open() {
        Material m = Material.AIR;

        int[] xOffset = {0, 1, -1};
        int[] zOffset = {0, 1, -1};

        for (int dx : xOffset) {
            for (int dz : zOffset) {
                map.getMSWWorld().getWorld().getBlockAt(x + dx, y - 1, z + dz).setType(m);
            }
        }
    }
}