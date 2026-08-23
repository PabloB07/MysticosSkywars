package joserodpt.mysticosskywars.api.leaderboards;

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

import java.util.UUID;

public class MSWLeaderboardRow {

    private final UUID uuid;
    private final String player;
    private final int total;
    private int place;

    public MSWLeaderboardRow(UUID uuid, String player, int statistic) {
        this.uuid = uuid;
        this.player = player;
        this.total = statistic;
    }

    public MSWLeaderboardRow() {
        this.uuid = UUID.randomUUID();
        this.player = "?";
        this.total = 0;
    }

    public String getPlayer() {
        return this.player;
    }

    public String getText() {
        return "&a" + this.place + ". &b" + this.player + " &f- &b" + this.total;
    }

    public MSWLeaderboardRow setPlace(int i) {
        this.place = i;
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }
}
