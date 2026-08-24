package joserodpt.mysticosskywars.plugin.listeners;

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

import joserodpt.mysticosskywars.api.events.MSWPlayerWinEvent;
import joserodpt.mysticosskywars.plugin.MysticosSkywars;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listens for win events and updates the last winner hologram.
 */
public class HologramWinListener implements Listener {

    private final MysticosSkywars plugin;

    public HologramWinListener(MysticosSkywars plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWin(MSWPlayerWinEvent event) {
        if (plugin.getLobbyHologramManager() != null) {
            plugin.getLobbyHologramManager().setLastWinner(
                    event.getWinner().getDisplayName(),
                    event.getMap().getDisplayName()
            );
        }
    }
}
