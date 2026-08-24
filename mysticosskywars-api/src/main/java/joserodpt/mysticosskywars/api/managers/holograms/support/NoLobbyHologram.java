package joserodpt.mysticosskywars.api.managers.holograms.support;

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

import joserodpt.mysticosskywars.api.managers.holograms.HologramType;
import joserodpt.mysticosskywars.api.managers.holograms.MSWLobbyHologram;
import org.bukkit.Location;

/**
 * No-op implementation when no hologram plugin is available.
 */
public class NoLobbyHologram implements MSWLobbyHologram {

    private final String id;
    private final HologramType type;

    public NoLobbyHologram(String id, HologramType type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public void spawn(Location loc, String hologramId) {
    }

    @Override
    public void setLine(int line, String text) {
    }

    @Override
    public void addLine(String text) {
    }

    @Override
    public void delete() {
    }

    @Override
    public HologramType getType() {
        return this.type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
