package joserodpt.mysticosskywars.api.config;

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

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Configuration file for lobby holograms.
 */
public class MSWHologramConfig {

    private static final String name = "holograms.yml";
    private static YamlDocument document;

    public static void setup(final JavaPlugin plugin) {
        try {
            document = YamlDocument.create(new File(plugin.getDataFolder(), name), plugin.getResource(name),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    dev.dejvokep.boostedyaml.settings.dumper.DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("Version")).build());
        } catch (final IOException e) {
            MysticosSkywarsAPI.getInstance().getLogger().severe("Couldn't setup " + name + "!");
            MysticosSkywarsAPI.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public static YamlDocument file() {
        return document;
    }

    public static void save() {
        try {
            document.save();
        } catch (final IOException e) {
            MysticosSkywarsAPI.getInstance().getLogger().severe("Couldn't save " + name + "!");
        }
    }

    public static void reload() {
        try {
            document.reload();
        } catch (final IOException e) {
            MysticosSkywarsAPI.getInstance().getLogger().severe("Couldn't reload " + name + "!");
        }
    }
}
