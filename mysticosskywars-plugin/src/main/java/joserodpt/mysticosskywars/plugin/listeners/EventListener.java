package joserodpt.mysticosskywars.plugin.listeners;

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
import joserodpt.mysticosskywars.api.managers.MapManagerAPI;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {
    private final MysticosSkywarsAPI rs;

    public EventListener(MysticosSkywarsAPI rs) {
        this.rs = rs;
    }

    @EventHandler
    public void blockChangeEvent(EntityChangeBlockEvent e) {
        Entity ent = e.getEntity();

        if (ent.hasMetadata("trailBlock")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void projectileHitEvent(ProjectileHitEvent e) {
        MSWMap match = rs.getMapManagerAPI().getMap(e.getEntity().getWorld());
        if (match != null && match.getProjectileTier() == MSWMap.ProjectileType.BREAK_BLOCKS) {
            Projectile projectile = e.getEntity();
            if (projectile instanceof EnderPearl) {
                return;
            }

            Block block = e.getHitBlock();
            if (block == null)
                return;
            block.breakNaturally();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).contains("[MSW]") || event.getLine(0).contains("[msw]")) {
            event.setLine(0, MysticosSkywarsAPI.getInstance().getLanguageManagerAPI().getPrefix());
            String name = event.getLine(1);

            MSWMap m = rs.getMapManagerAPI().getMap(name);
            MSWPlayer p = rs.getPlayerManagerAPI().getPlayer(event.getPlayer());

            if (m != null && (event.getPlayer().isOp() || p.getPlayer().hasPermission("rs.admin"))) {
                m.addSign(event.getBlock());
            } else {
                TranslatableLine.CMD_NO_MAP_FOUND.send(p, true);
            }
        }
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (MSWConfig.file().getBoolean("Config.Bungeecord.Enabled")) {
            List<MSWMap> maps = new ArrayList<>(rs.getMapManagerAPI().getMaps(MapManagerAPI.MapGamemodes.ALL));
            MSWMap map = maps.get(0);
            event.setMaxPlayers(maps.size() == 1 ? map.getMaxPlayers() : 1);

            if (MSWConfig.file().getBoolean("Config.Bungeecord.Map-State-As-Motd")) {
                event.setMotd(Text.color((maps.size() == 1 ? map.getState().getDefaultTranslation() : "?")));
            } else {
                event.setMotd(Text.color("<white><bold>Mysticos<aqua>Skywars <gray>v<yellow>" + rs.getPlugin().getDescription().getVersion() + "\n<gray>Proxy - Mapa: <white>" + (maps.size() == 1 ? map.getName() : "?") + " > " + (maps.size() == 1 ? map.getState().getDefaultTranslation() : "?")));
            }
        }
    }
}
