package joserodpt.mysticosskywars.api;

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

import joserodpt.mysticosskywars.api.events.MSWRoomStateChangeEvent;
import joserodpt.mysticosskywars.api.map.MSWMap;
import org.bukkit.Bukkit;

public class MSWEventsAPI {
    public void callRoomStateChange(MSWMap g) {
        Bukkit.getPluginManager().callEvent(new MSWRoomStateChangeEvent(g));
        g.updateSigns();
    }
}
