package joserodpt.mysticosskywars.api.player.tab;

import org.bukkit.entity.Player;

import java.util.List;

public interface MSWPlayerTabInterface {
    void addPlayers(Player p);

    void addPlayers(List<Player> p);

    void removePlayers(Player p);

    void reset();

    void clear();

    void setHeaderFooter(String h, String f);

    void updateRoomTAB();
}
