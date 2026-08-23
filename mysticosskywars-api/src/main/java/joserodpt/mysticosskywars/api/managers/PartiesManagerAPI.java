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

import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;

import java.util.HashMap;
import java.util.Map;

public abstract class PartiesManagerAPI {
    public Map<MSWPlayer, MSWPlayer> invites = new HashMap<>();

    public Boolean hasInvite(MSWPlayer p) {
        return invites.containsKey(p);
    }

    public MSWPlayer getInvite(MSWPlayer p) {
        return invites.get(p);
    }

    public abstract void sendInvite(MSWPlayer emissor, MSWPlayer recetor);

    public abstract void acceptInvite(MSWPlayer p);

    public abstract boolean checkForParties(MSWPlayer p, MSWMap swgm);
}
