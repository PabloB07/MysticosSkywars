package joserodpt.mysticosskywars.api.currency;

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

public interface CurrencyAdapterAPI {
    void transferCoins(MSWPlayer toPlayer, MSWPlayer fromPLayer, double amount);

    void addCoins(MSWPlayer p, double amount);

    boolean removeCoins(MSWPlayer p, double amount);

    void setCoins(MSWPlayer p, double amount);

    double getCoins(MSWPlayer p);

    String getCoinsFormatted(MSWPlayer p);
}
