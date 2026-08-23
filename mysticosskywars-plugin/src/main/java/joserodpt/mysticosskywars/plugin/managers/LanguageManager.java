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

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import joserodpt.mysticosskywars.api.Debugger;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.config.MSWLanguage;
import joserodpt.mysticosskywars.api.config.MSWLanguagesOldConfig;
import joserodpt.mysticosskywars.api.managers.LanguageManagerAPI;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class LanguageManager extends LanguageManagerAPI {

    final MysticosSkywarsAPI rsa;

    public LanguageManager(MysticosSkywarsAPI rsa) {
        this.rsa = rsa;
    }

    @Override
    public void loadLanguages() {
        this.getLanguages().clear();

        //check if "languages" folder exists
        File languagesFolder = new File(rsa.getPlugin().getDataFolder(), "languages");
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();

            //if folder doesn't exist, the old pre 1.1 language.yml file still exists
            if (MSWLanguagesOldConfig.file() != null && MSWLanguagesOldConfig.file().contains("Languages")) {
                MSWLanguagesOldConfig.file().getSection("Languages").getRoutesAsStrings(false).forEach(lang -> {
                    rsa.getLogger().info("Converting language file " + lang + " to the new format...");
                    Section section = MSWLanguagesOldConfig.file().getSection("Languages." + lang);

                    String name = section.getString("Language-Specific.Translation-Key");
                    try {
                        YamlDocument doc = YamlDocument.create(new File(languagesFolder, name + ".yml"));
                        doc.set(name, section);
                        doc.save();
                        rsa.getLogger().warning("Language file " + lang + " converted successfully!");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            //copy default language files
            for (String langFile : new String[]{"en_us", "pt_pt", "es_es", "fr_fr", "zh_cn", "pl_pl"}) {
                //check if the language file exists
                File file = new File(languagesFolder, langFile + ".yml");
                if (!file.exists()) {
                    try {
                        Files.copy(Objects.requireNonNull(rsa.getPlugin().getResource("languages/" + langFile + ".yml")), file.toPath());
                    } catch (IOException e) {
                        rsa.getLogger().severe("Could not copy language file " + langFile + " -> " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        //loop through files in the languages folder
        for (File file : Objects.requireNonNull(languagesFolder.listFiles())) {
            if (file.getName().endsWith(".yml") && !file.getName().equals("version.yml")) {
                try {
                    MSWLanguage l = new MSWLanguage(file);
                    this.langList.put(l.getKey(), l);
                } catch (Exception e) {
                    rsa.getLogger().severe("Could not load language file " + file.getName() + " -> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        String simpleVersion = rsa.getSimpleServerVersion();
        File folder = new File(rsa.getPlugin().getDataFolder(), "languages");
        File translationVersionFile = new File(folder, "version.yml");

        // Check if the language file exists
        if (translationVersionFile.exists()) {
            try {
                String version = new String(Files.readAllBytes(translationVersionFile.toPath()));
                if (!version.equals(simpleVersion)) {
                    rsa.getLogger().info("Updating language files from " + version + " to " + simpleVersion + ". Downloading new versions...");
                    for (MSWLanguage language : getLanguages()) {
                        try {
                            language.downloadLanguageFile();
                        } catch (Exception e) {
                            rsa.getLogger().severe("Could not update language file " + language.getKey() + " -> " + e.getMessage());
                            Debugger.print(LanguageManager.class, e.getMessage());
                        }
                    }

                    Files.write(translationVersionFile.toPath(), simpleVersion.getBytes());
                }
            } catch (IOException e) {
                rsa.getLogger().severe("Could not update language files -> " + e.getMessage());
                Debugger.print(LanguageManager.class, e.getMessage());
            }
        } else {
            try {
                Files.createDirectories(folder.toPath());
                Files.write(translationVersionFile.toPath(), simpleVersion.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getDefaultLanguage() {
        String configuredLanguage = MSWConfig.file().getString("Config.Languages.Default-Language");
        if (configuredLanguage == null || !this.langList.containsKey(configuredLanguage)) {
            configuredLanguage = MSWConfig.file().getString("Config.Default-Language");
        }

        return configuredLanguage != null && this.langList.containsKey(configuredLanguage)
                ? configuredLanguage
                : this.langList.keySet().stream().findFirst().get();
    }

    public MSWLanguage getDefaultLanguageObject() {
        return this.langList.get(getDefaultLanguage());
    }

    @Override
    public boolean areLanguagesEmpty() {
        return getLanguages().isEmpty();
    }

    @Override
    public Collection<MSWLanguage> getLanguages() {
        return this.langList.values();
    }

    @Override
    public Map<String, MSWLanguage> getLanguagesMap() {
        return this.langList;
    }

    @Override
    public String getPrefix() {
        return Text.color(MSWConfig.file().getString("Config.Prefix"));
    }

    @Override
    public String getMaterialName(MSWPlayer p, Material mat) {
        return this.langList.containsKey(p.getLanguage()) ? this.langList.get(p.getLanguage()).getMaterialName(mat) : getDefaultLanguageObject().getMaterialName(mat);
    }

    @Override
    public String getMaterialName(Material mat) {
        return getDefaultLanguageObject().getMaterialName(mat);
    }

    @Override
    public String getEnchantmentName(MSWPlayer p, Enchantment ench) {
        return this.langList.containsKey(p.getLanguage()) ? this.langList.get(p.getLanguage()).getEnchantmentName(ench) : getDefaultLanguageObject().getEnchantmentName(ench);
    }

    @Override
    public String getEntityName(MSWPlayer p, EntityType type) {
        return this.langList.containsKey(p.getLanguage()) ? this.langList.get(p.getLanguage()).getEntityName(type) : getDefaultLanguageObject().getEntityName(type);
    }

    @Override
    public MSWLanguage getLanguage(String language) {
        return this.langList.get(language);
    }

}
