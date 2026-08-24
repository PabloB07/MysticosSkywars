package joserodpt.mysticosskywars.api.events;

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
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player wins a match.
 */
public class MSWPlayerWinEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final MSWPlayer winner;
    private final MSWMap map;

    public MSWPlayerWinEvent(MSWPlayer winner, MSWMap map) {
        this.winner = winner;
        this.map = map;
    }

    public MSWPlayer getWinner() {
        return this.winner;
    }

    public MSWMap getMap() {
        return this.map;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
