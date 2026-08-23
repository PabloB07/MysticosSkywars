package joserodpt.mysticosskywars.api.effects;

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
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;

public class MSWBowTrail implements MSWTrail {
    private final Particle pa;
    private final Arrow a;
    private final MSWPlayer p;
    private BukkitTask task;

    public MSWBowTrail(Particle bowParticle, Projectile entity, MSWPlayer gp) {
        this.pa = bowParticle;
        this.a = (Arrow) entity;
        this.p = gp;
        startTask();
    }

    @Override
    public void startTask() {
        this.task = Bukkit.getServer().getScheduler().runTaskTimer(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
            if (this.a == null || this.a.isOnGround() || this.a.isDead()) {
                cancelTask();
                return;
            }
            this.a.getLocation().getWorld().spawnParticle(this.pa, this.a.getLocation(), 1);
        }, 1, 1);
    }

    @Override
    public void cancelTask() {
        this.task.cancel();
        this.p.removeTrail(this);
    }

    @Override
    public TrailType getType() {
        return TrailType.BOW;
    }
}
