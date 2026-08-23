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

import joserodpt.mysticosskywars.api.database.PlayerBoughtItemsRow;
import joserodpt.mysticosskywars.api.kits.MSWKit;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;

import java.util.Collection;

public abstract class KitManagerAPI {
    public abstract void loadKits();

    public abstract void registerKit(MSWKit k);

    public abstract void unregisterKit(MSWKit k);

    public abstract Collection<MSWKit> getKits();

    public abstract Collection<MSWBuyableItem> getKitsAsBuyables();

    public abstract MSWKit getKit(String string);

    public abstract MSWKit getKit(PlayerBoughtItemsRow playerBoughtItemsRow);
}
