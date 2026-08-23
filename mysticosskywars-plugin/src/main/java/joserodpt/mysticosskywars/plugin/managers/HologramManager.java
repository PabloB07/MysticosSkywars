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
import joserodpt.mysticosskywars.api.managers.HologramManagerAPI;
import joserodpt.mysticosskywars.api.managers.holograms.MSWHologram;
import org.bukkit.Bukkit;

public class HologramManager extends HologramManagerAPI {

    private MSWHologram.HType selected = MSWHologram.HType.NONE;

    public HologramManager(MysticosSkywarsAPI rsa) {

        //select scoreboard plugin
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            this.selected = MSWHologram.HType.HOLOGRAPHIC_DISPLAYS;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.selected = MSWHologram.HType.DECENT_HOLOGRAMS;
        }

        switch (this.selected) {
            case DECENT_HOLOGRAMS:
                rsa.getLogger().info("Hooked on Decent Holograms!");
                break;
            case HOLOGRAPHIC_DISPLAYS:
                rsa.getLogger().info("Hooked on Holographic Displays!");
                break;
        }
    }

    @Override
    public MSWHologram getHologramInstance() {
        return this.selected.getHologramInstance();
    }
}
