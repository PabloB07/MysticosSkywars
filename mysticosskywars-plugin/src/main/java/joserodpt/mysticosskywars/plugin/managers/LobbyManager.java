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
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.TranslatableLine;
import joserodpt.mysticosskywars.api.managers.LobbyManagerAPI;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.player.MSWPlayerItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LobbyManager extends LobbyManagerAPI {

    public MysticosSkywarsAPI rsa;

    public LobbyManager(MysticosSkywarsAPI rsa) {
        this.rsa = rsa;
    }

    private Location lobbyLOC;
    private Boolean loginTP = true;

    @Override
    public void loadLobby() {
        this.loginTP = MSWConfig.file().getBoolean("Config.Auto-Teleport-To-Lobby");
        if (MSWConfig.file().isSection("Lobby")) {
            double x = MSWConfig.file().getDouble("Lobby.X");
            double y = MSWConfig.file().getDouble("Lobby.Y");
            double z = MSWConfig.file().getDouble("Lobby.Z");
            float yaw = MSWConfig.file().getFloat("Lobby.Yaw");
            float pitch = MSWConfig.file().getFloat("Lobby.Pitch");
            World world = Bukkit.getServer().getWorld(MSWConfig.file().getString("Lobby.World"));
            this.lobbyLOC = new Location(world, x, y, z, yaw, pitch);
        }
    }

    @Override
    public void tpToLobby(Player player) {
        if (this.lobbyLOC != null && player != null) {
            try {
                player.teleport(this.lobbyLOC);
            } catch (Exception e) {
                rsa.getLogger().warning("Error while teleporting player to lobby: " + e.getMessage());
            }
        }
    }

    @Override
    public void tpToLobby(MSWPlayer p) {
        if (this.lobbyLOC != null && p != null) {
            tpToLobby(p.getPlayer());
            TranslatableLine.LOBBY_TELEPORT.send(p, true);
            MSWPlayerItems.LOBBY.giveSet(p);
        } else {
            TranslatableLine.LOBBY_NOT_SET.send(p, true);
        }
    }

    @Override
    public Location getLobbyLocation() {
        return this.lobbyLOC;
    }

    @Override
    public boolean scoreboardInLobby() {
        return MSWConfig.file().getBoolean("Config.Scoreboard-In-Lobby");
    }

    @Override
    public void setLobbyLoc(Location location) {
        this.lobbyLOC = location;
        //give everyone items again

        for (Player p : location.getWorld().getPlayers()) {
            MSWPlayer mswPlayer = rsa.getPlayerManagerAPI().getPlayer(p);
            if (mswPlayer != null) {
                MSWPlayerItems.LOBBY.giveSet(mswPlayer);
            }
        }
    }

    @Override
    public boolean tpLobbyOnJoin() {
        return loginTP && this.lobbyLOC != null;
    }

    @Override
    public boolean isInLobby(World w) {
        if (w == null || this.lobbyLOC == null || this.lobbyLOC.getWorld() == null) {
            return false;
        }
        return this.lobbyLOC != null && this.lobbyLOC.getWorld().equals(w);
    }

}
