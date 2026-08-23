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

import joserodpt.mysticosskywars.api.config.MSWLanguage;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerManagerAPI {
    public abstract void loadPlayer(Player p);

    public abstract MSWPlayer getPlayer(Player p);

    public abstract void savePlayer(MSWPlayer p, MSWPlayer.PlayerData pd);

    public abstract void setLanguage(MSWPlayer player, MSWLanguage lang);

    public abstract void loadPlayers();

    public abstract int getPlayingPlayers(MapManagerAPI.MapGamemodes pt);

    public abstract void stopScoreboards();

    public abstract Collection<MSWPlayer> getPlayers();

    public abstract void addPlayer(MSWPlayer mswPlayer);

    public abstract void removePlayer(MSWPlayer mswPlayer);

    public abstract void trackPlayer(MSWPlayer gp);

    public abstract List<UUID> getTeleporting();

    public abstract Map<UUID, MSWMap> getFastJoin();
}
