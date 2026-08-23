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

import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;

import java.util.Collection;

public abstract class ShopManagerAPI {
    public abstract Collection<MSWBuyableItem> getCategoryContents(MSWBuyableItem.ItemCategory t);

    public abstract Collection<MSWBuyableItem> getBoughtItems(MSWBuyableItem.ItemCategory t, MSWPlayer p);

    public abstract void loadShopItems();
}
