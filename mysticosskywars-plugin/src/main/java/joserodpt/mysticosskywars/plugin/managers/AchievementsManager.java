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

import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.achievements.MSWAchievement;
import joserodpt.mysticosskywars.api.achievements.types.MSWAchievementRCoin;
import joserodpt.mysticosskywars.api.config.MSWAchievementsConfig;
import joserodpt.mysticosskywars.api.managers.AchievementsManagerAPI;
import joserodpt.mysticosskywars.api.player.MSWPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AchievementsManager extends AchievementsManagerAPI {
    private final MysticosSkywarsAPI rs;

    public AchievementsManager(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    public Map<MSWPlayer.PlayerStatistics, List<MSWAchievement>> achievements = new HashMap<>();

    @Override
    public void loadAchievements() {
        int cats = 0, achi = 0;
        this.achievements.clear();
        //load coin achievements
        for (String dir : MSWAchievementsConfig.file().getSection("Coins").getRoutesAsStrings(false).stream()
                .map(Object::toString)
                .collect(Collectors.toSet())) {
            ++cats;
            MSWPlayer.PlayerStatistics t = null;

            switch (dir) {
                case "Kills":
                    t = MSWPlayer.PlayerStatistics.KILLS;
                    break;
                case "Wins-Solo":
                    t = MSWPlayer.PlayerStatistics.WINS_SOLO;
                    break;
                case "Wins-Teams":
                    t = MSWPlayer.PlayerStatistics.WINS_TEAMS;
                    break;
                case "Games-Played":
                    t = MSWPlayer.PlayerStatistics.GAMES_PLAYED;
                    break;
            }

            List<MSWAchievement> achiv = new ArrayList<>();

            String path = "Coins." + dir;
            for (String meta : MSWAchievementsConfig.file().getSection(path).getRoutesAsStrings(false)) {
                ++achi;
                Double value = MSWAchievementsConfig.file().getDouble(path + "." + meta);
                achiv.add(new MSWAchievementRCoin(t, Integer.parseInt(meta), value));
            }

            this.achievements.put(t, achiv);
        }

        rs.getLogger().info("Loaded " + achi + " rewards for " + cats + " coin categories.");
    }

    @Override
    public List<MSWAchievement> getAchievements(MSWPlayer.PlayerStatistics ds) {
        return this.achievements.get(ds);
    }

    @Override
    public MSWAchievement getAchievement(MSWPlayer.PlayerStatistics ps, int meta) {
        List<MSWAchievement> list = this.achievements.get(ps);
        if (list != null) {
            Optional<MSWAchievement> o = list.stream().filter(c -> c.getGoal() == meta).findFirst();
            if (o.isPresent()) {
                return o.get();
            }
        }
        return null;
    }
}
