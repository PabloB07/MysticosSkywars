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

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import joserodpt.mysticosskywars.api.managers.holograms.HologramType;
import joserodpt.mysticosskywars.api.managers.holograms.MSWLobbyHologram;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Location;

/**
 * DecentHolograms implementation of lobby holograms.
 */
public class DHLobbyHologram implements MSWLobbyHologram {

    private Hologram holo;
    private final String id;
    private final HologramType type;

    public DHLobbyHologram(String id, HologramType type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public void spawn(Location loc, String hologramId) {
        if (this.holo == null) {
            this.holo = DHAPI.createHologram("MSW-Holo-" + hologramId, loc);
        }
    }

    @Override
    public void setLine(int line, String text) {
        if (this.holo != null) {
            DHAPI.setHologramLine(this.holo, line, Text.color(text));
        }
    }

    @Override
    public void addLine(String text) {
        if (this.holo != null) {
            DHAPI.addHologramLine(this.holo, Text.color(text));
        }
    }

    @Override
    public void delete() {
        if (this.holo != null) {
            this.holo.delete();
        }
        this.holo = null;
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
        return this.holo != null;
    }
}
