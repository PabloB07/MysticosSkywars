package joserodpt.mysticosskywars.api.managers.world;

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

import joserodpt.mysticosskywars.api.managers.world.engines.SWWorldDefaultEngine;
import joserodpt.mysticosskywars.api.managers.world.engines.SWWorldSchematicEngine;
import joserodpt.mysticosskywars.api.map.MSWMap;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class MSWWorld {

    private final SWWorldEngine engine;

    public MSWWorld(MSWMap gameRoom, World w, WorldType wt) {
        this.engine = (wt == WorldType.DEFAULT ? new SWWorldDefaultEngine(w, gameRoom) : new SWWorldSchematicEngine(w, gameRoom.getShematicName(), gameRoom));
        this.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        this.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.getWorld().setGameRule(GameRule.DO_INSOMNIA, false);
        this.getWorld().setGameRule(GameRule.DO_PATROL_SPAWNING, false);
    }

    public World getWorld() {
        return this.engine.getWorld();
    }

    public void resetWorld(MSWMap.OperationReason rr) {
        if (rr != MSWMap.OperationReason.SHUTDOWN) {
            this.engine.getWorld().getEntities().stream().filter(entity -> entity.getType() != EntityType.PLAYER).forEach(Entity::remove);
        }
        this.engine.resetWorld(rr);
    }

    public void deleteWorld(MSWMap.OperationReason rr) {
        this.engine.deleteWorld(rr);
    }

    public void setTime(long l) {
        this.engine.setTime(l);
    }

    public String getName() {
        return this.engine.getName();
    }

    public MSWWorld.WorldType getType() {
        return this.engine.getType();
    }

    public void save() {
        this.engine.save();
    }

    public enum WorldType {DEFAULT, SCHEMATIC}
}
