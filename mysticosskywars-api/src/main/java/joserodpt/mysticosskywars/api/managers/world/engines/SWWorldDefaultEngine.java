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

import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.managers.WorldManagerAPI;
import joserodpt.mysticosskywars.api.managers.world.MSWWorld;
import joserodpt.mysticosskywars.api.managers.world.SWWorldEngine;
import joserodpt.mysticosskywars.api.map.MSWMap;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.io.File;
import java.util.Objects;

public class SWWorldDefaultEngine implements SWWorldEngine {

    private final WorldManagerAPI wm = MysticosSkywarsAPI.getInstance().getWorldManagerAPI();
    private World world;
    private final MSWMap gameRoom;
    private final String worldName;

    public SWWorldDefaultEngine(World w, MSWMap gameMode) {
        this.worldName = w.getName();
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
        Debugger.print(SWWorldDefaultEngine.class, "Resetting " + this.getName() + " - type: " + this.getType().name());

        if (Objects.requireNonNull(rr) == MSWMap.OperationReason.SHUTDOWN) {//delete world
            this.deleteWorld(MSWMap.OperationReason.SHUTDOWN);
        } else {
            File mapsFolder = new File(MysticosSkywarsAPI.getInstance().getPlugin().getDataFolder(), "maps");
            File template = new File(mapsFolder, this.getName());
            File currentWorld = new File(MysticosSkywarsAPI.getInstance().getPlugin().getServer().getWorldContainer(), this.getName());

            // A map cannot be reset safely without a template. Recover older maps
            // by creating the missing template before unloading the live world.
            if (!template.isDirectory() && currentWorld.isDirectory()) {
                this.wm.copyWorld(this.getName(), WorldManagerAPI.CopyTo.MSW_FOLDER);
            }

            if (!template.isDirectory()) {
                MysticosSkywarsAPI.getInstance().getLogger().severe(
                        "Cannot reset map " + this.getName() + ": template not found at " + template);
                this.gameRoom.setState(MSWMap.MapState.AVAILABLE);
                return;
            }

            this.deleteWorld(MSWMap.OperationReason.RESET);
            //Copy world
            this.wm.copyWorld(this.getName(), WorldManagerAPI.CopyTo.ROOT);

            //Load world
            this.world = this.wm.createEmptyWorld(this.getName(), World.Environment.NORMAL);
            if (this.world != null) {
                this.world.setTime(0);
                this.world.setStorm(false);
                WorldBorder wb = this.world.getWorldBorder();

                wb.setCenter(this.gameRoom.getMapCuboid().getCenter());
                wb.setSize(this.gameRoom.getBorderSize());

                this.gameRoom.setState(MSWMap.MapState.AVAILABLE);
            } else {
                MysticosSkywarsAPI.getInstance().getLogger().severe("ERROR! Could not load " + this.getName());
            }
        }
    }

    @Override
    public void deleteWorld(MSWMap.OperationReason rr) {
        switch (rr) {
            case LOAD:
                break;
            case SHUTDOWN:
            case RESET:
                this.wm.deleteWorld(this.getName(), true);
                break;
        }
    }

    @Override
    public void setTime(long l) {
        this.world.setTime(l);
    }

    @Override
    public String getName() {
        return this.world != null ? this.world.getName() : this.worldName;
    }

    @Override
    public MSWWorld.WorldType getType() {
        return MSWWorld.WorldType.DEFAULT;
    }

    @Override
    public void save() {
        this.world.save();
    }
}
