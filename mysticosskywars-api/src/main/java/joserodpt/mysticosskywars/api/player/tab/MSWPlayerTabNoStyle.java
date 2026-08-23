package joserodpt.mysticosskywars.api.player.tab;

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
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MSWPlayerTabNoStyle implements MSWPlayerTabInterface {

    private final MSWPlayer player;
    private final List<Player> show = new ArrayList<>();

    public MSWPlayerTabNoStyle(MSWPlayer player) {
        this.player = player;
        clear();
        updateRoomTAB();
    }

    @Override
    public void addPlayers(Player p) {
        if (p.getUniqueId() != this.player.getUUID() && !this.show.contains(p)) {
            this.show.add(p);
        }
    }

    @Override
    public void addPlayers(List<Player> p) {
        this.show.addAll(p);
    }

    @Override
    public void removePlayers(Player p) {
        this.show.remove(p);
    }

    @Override
    public void reset() {
        this.show.addAll(Bukkit.getOnlinePlayers());
    }

    @Override
    public void clear() {
        this.show.clear();
    }

    @Override
    public void setHeaderFooter(String h, String f) {
    }


    @Override
    public void updateRoomTAB() {
        if (!this.player.isBot()) {
            Bukkit.getOnlinePlayers().forEach(pl -> this.player.hidePlayer(MysticosSkywarsAPI.getInstance().getPlugin(), pl));
            this.show.forEach(mswPlayer -> this.player.showPlayer(MysticosSkywarsAPI.getInstance().getPlugin(), mswPlayer));
        }
    }
}
