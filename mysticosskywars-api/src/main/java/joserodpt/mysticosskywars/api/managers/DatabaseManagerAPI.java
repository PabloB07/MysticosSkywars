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

import com.j256.ormlite.dao.Dao;
import joserodpt.mysticosskywars.api.database.PlayerBoughtItemsRow;
import joserodpt.mysticosskywars.api.database.PlayerDataRow;
import joserodpt.mysticosskywars.api.database.PlayerGameHistoryRow;
import joserodpt.mysticosskywars.api.player.MSWGameHistoryStats;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import joserodpt.mysticosskywars.api.utils.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class DatabaseManagerAPI {
    @NotNull
    protected abstract String getDatabaseURL();

    protected abstract void getPlayerData();

    public abstract Pair<Collection<PlayerGameHistoryRow>, MSWGameHistoryStats> getPlayerGameHistory(Player p);

    public abstract List<PlayerBoughtItemsRow> getPlayerBoughtItems(Player p);

    public abstract List<PlayerBoughtItemsRow> getPlayerBoughtItemsCategory(Player p, MSWBuyableItem.ItemCategory cat);

    public abstract PlayerDataRow getPlayerData(OfflinePlayer p);

    public abstract void savePlayerData(PlayerDataRow playerDataRow, boolean async);

    public abstract void saveNewGameHistory(PlayerGameHistoryRow playerGameHistoryRow, boolean async);

    public abstract void saveNewBoughtItem(PlayerBoughtItemsRow playerBoughtItemsRow, boolean async);

    public abstract void deletePlayerData(UUID playerUUID, boolean async);

    public abstract void deletePlayerGameHistory(UUID playerUUID, boolean async);

    public abstract void deletePlayerBoughtItems(UUID playerUUID, boolean async);

    public abstract Dao<PlayerDataRow, UUID> getQueryDao();

    public abstract Pair<Boolean, String> didPlayerBoughtItem(MSWPlayer p, MSWBuyableItem item);
}
