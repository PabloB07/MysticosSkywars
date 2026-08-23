package joserodpt.mysticosskywars.api.managers.world.engines;

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


import com.sk89q.worldedit.world.block.BlockTypes;
import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.managers.WorldManagerAPI;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.managers.world.SWWorldEngine;
import joserodpt.mysticosskywars.api.map.MSWMap;
import joserodpt.mysticosskywars.api.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

public class SWWorldSchematicEngine implements SWWorldEngine {

    private final World world;
    private final MSWMap gameRoom;
    private final String schematicName;
    private final WorldManagerAPI wm = MysticosSkywarsAPI.getInstance().getWorldManagerAPI();

    public SWWorldSchematicEngine(World w, String sname, MSWMap gameMode) {
        this.schematicName = sname;
        this.world = w;
        this.world.setAutoSave(false);
        this.gameRoom = gameMode;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public void resetWorld(MSWMap.OperationReason rr) {
        Debugger.print(SWWorldSchematicEngine.class, "Resetting " + this.getName() + " - type: " + this.getType().name());

        switch (rr) {
            case SHUTDOWN:
                //no need
                break;
            case RESET:
                this.deleteWorld(MSWMap.OperationReason.RESET);
            case LOAD:
                if (this.world != null) {
                    //place schematic
                    WorldEditUtils.pasteSchematic(this.schematicName, new Location(this.world, 0, 64, 0), null);

                    this.world.setTime(0);
                    this.world.setStorm(false);

                    WorldBorder wb = this.world.getWorldBorder();

                    wb.setCenter(this.gameRoom.getMapCuboid().getCenter());
                    wb.setSize(this.gameRoom.getBorderSize());

                    this.gameRoom.setState(MSWMap.MapState.AVAILABLE);
                } else {
                    MysticosSkywarsAPI.getInstance().getLogger().severe("ERROR! Could not load " + this.getName());
                }
                break;
        }
    }

    @Override
    public void deleteWorld(MSWMap.OperationReason rr) {
        MysticosSkywarsAPI.getInstance().getWorldManagerAPI().clearDroppedItems(this.getWorld());
        switch (rr) {
            case LOAD:
            case SHUTDOWN:
                this.wm.deleteWorld(this.getName(), true);
                break;
            case RESET:
                WorldEditUtils.setBlocks(this.gameRoom.getPOS1(), this.gameRoom.getPOS2(), BlockTypes.AIR);
                break;
        }
    }

    @Override
    public void setTime(long l) {
        this.world.setTime(l);
    }

    @Override
    public String getName() {
        return this.world != null ? this.world.getName() : this.schematicName;
    }

    @Override
    public MSWWorld.WorldType getType() {
        return MSWWorld.WorldType.SCHEMATIC;
    }

    @Override
    public void save() {
        this.world.save();
    }
}
