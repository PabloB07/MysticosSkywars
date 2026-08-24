package joserodpt.mysticosskywars.api.managers.holograms;

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

import org.bukkit.Location;

/**
 * Extended hologram interface for lobby holograms (leaderboards, winner displays, etc.)
 */
public interface MSWLobbyHologram {

    /**
     * Spawns a multi-line hologram at the given location.
     */
    void spawn(Location loc, String id);

    /**
     * Updates a specific line of the hologram.
     */
    void setLine(int line, String text);

    /**
     * Adds a new line to the hologram.
     */
    void addLine(String text);

    /**
     * Clears all lines and removes the hologram.
     */
    void delete();

    /**
     * Returns the hologram type.
     */
    HologramType getType();

    /**
     * Returns the unique ID of this hologram.
     */
    String getId();

    /**
     * Returns whether this hologram is currently active.
     */
    boolean isActive();
}
