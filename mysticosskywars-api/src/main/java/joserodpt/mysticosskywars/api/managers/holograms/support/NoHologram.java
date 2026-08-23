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

import joserodpt.mysticosskywars.api.managers.holograms.MSWHologram;
import org.bukkit.Location;

public class NoHologram implements MSWHologram {
    @Override
    public void spawnHologram(Location loc) {
    }

    @Override
    public void setTime(int seconds) {
    }

    @Override
    public void deleteHologram() {
    }

    @Override
    public HType getType() {
        return HType.NONE;
    }
}
