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

import joserodpt.mysticosskywars.api.database.PlayerDataRow;
import joserodpt.mysticosskywars.api.leaderboards.MSWLeaderboard;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public abstract class LeaderboardManagerAPI {
    public abstract void refreshLeaderboards();

    public abstract void refreshLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l) throws SQLException;

    @NotNull
    protected abstract MSWLeaderboard getLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l, List<PlayerDataRow> expansions);

    public abstract MSWLeaderboard getLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l);
}
