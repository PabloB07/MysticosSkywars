package joserodpt.mysticosskywars.plugin.managers;

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

import com.j256.ormlite.stmt.QueryBuilder;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.database.PlayerDataRow;
import joserodpt.mysticosskywars.api.leaderboards.MSWLeaderboard;
import joserodpt.mysticosskywars.api.managers.LeaderboardManagerAPI;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LeaderboardManager extends LeaderboardManagerAPI {
    private final MysticosSkywarsAPI rs;

    public LeaderboardManager(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    public Map<MSWLeaderboard.MSWLeaderboardCategories, MSWLeaderboard> leaderboards = new HashMap<>();

    @Override
    public void refreshLeaderboards() {
        for (MSWLeaderboard.MSWLeaderboardCategories value : MSWLeaderboard.MSWLeaderboardCategories.values()) {
            try {
                this.refreshLeaderboard(value);
            } catch (Exception e) {
                MysticosSkywarsAPI.getInstance().getLogger().severe("Error while loading Leaderboard for " + value.name() + " -> " + e.getMessage());
            }
        }
    }

    @Override
    public void refreshLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l) throws SQLException {
        QueryBuilder<PlayerDataRow, UUID> qb = rs.getDatabaseManagerAPI().getQueryDao().queryBuilder();
        qb.orderBy(l.getDBName(), false);
        MSWLeaderboard lb = getLeaderboard(l, rs.getDatabaseManagerAPI().getQueryDao().query(qb.prepare()));
        this.leaderboards.put(l, lb);
    }

    @Override
    @NotNull
    protected MSWLeaderboard getLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l, List<PlayerDataRow> expansions) {
        MSWLeaderboard lb = new MSWLeaderboard();
        for (int i = 1; i < 11; ++i) {
            PlayerDataRow p;
            try {
                p = expansions.get(i - 1);

                if (p != null) {
                    lb.addRow(p.getUUID(), p.getName(), l.getValue(p));
                }
            } catch (Exception ignored) {
            }
        }
        return lb;
    }

    @Override
    public MSWLeaderboard getLeaderboard(MSWLeaderboard.MSWLeaderboardCategories l) {
        return this.leaderboards.get(l);
    }

}
