package joserodpt.mysticosskywars.api.managers.holograms;

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

import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Material;

/**
 * Types of lobby holograms that can be placed in the server.
 */
public enum HologramType {
    LAST_WINNER(Material.GOLD_BLOCK, "&6Last Winner"),
    TOP_WINS_SOLO(Material.DIAMOND, "&bTop Wins (Solo)"),
    TOP_WINS_TEAMS(Material.EMERALD, "&aTop Wins (Teams)"),
    TOP_KILLS(Material.IRON_SWORD, "&cTop Kills"),
    TOP_COINS(Material.GOLD_INGOT, "&eTop Coins"),
    SERVER_INFO(Material.BEACON, "&9Server Info"),
    CUSTOM(Material.PAPER, "&fCustom");

    private final Material icon;
    private final String displayName;

    HologramType(Material icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }

    public Material getIcon() {
        return this.icon;
    }

    public String getDisplayName() {
        return Text.color(this.displayName);
    }
}
