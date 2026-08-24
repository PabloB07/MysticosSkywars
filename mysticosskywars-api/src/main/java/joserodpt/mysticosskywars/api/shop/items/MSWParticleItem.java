package joserodpt.mysticosskywars.api.shop.items;

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

import joserodpt.mysticosskywars.api.shop.MSWBuyableItem;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.Map;

public class MSWParticleItem extends MSWBuyableItem {

    public MSWParticleItem(String configKey, String displayName, Material material, Double price, String permission, String particleName) {
        super(configKey, displayName, material, price, permission, ItemCategory.BOW_PARTICLE, Map.of("Particle", particleName));
    }

    public Particle getParticle() {
        String configured = String.valueOf(this.getExtrasMap().get("Particle")).toUpperCase();
        String normalized = configured;
        String legacy = null;
        switch (configured) {
            case "EXPLOSION_NORMAL":
            case "EXPLOSION_LARGE":
            case "EXPLOSION_HUGE": normalized = "EXPLOSION"; legacy = configured; break;
            case "FIREWORKS_SPARK": normalized = "FIREWORK"; legacy = configured; break;
            case "WATER_BUBBLE": normalized = "BUBBLE_POP"; legacy = configured; break;
            case "WATER_SPLASH": normalized = "SPLASH"; legacy = configured; break;
            case "WATER_WAKE": normalized = "FISHING"; legacy = configured; break;
            case "SMOKE_NORMAL": normalized = "SMOKE"; legacy = configured; break;
            case "SMOKE_LARGE": normalized = "LARGE_SMOKE"; legacy = configured; break;
            case "CRIT_MAGIC": normalized = "ENCHANTED_HIT"; legacy = configured; break;
            default: break;
        }
        try {
            return Particle.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            try {
                return legacy == null ? Particle.CLOUD : Particle.valueOf(legacy);
            } catch (IllegalArgumentException ignored) {
                return Particle.CLOUD;
            }
        }
    }
}
