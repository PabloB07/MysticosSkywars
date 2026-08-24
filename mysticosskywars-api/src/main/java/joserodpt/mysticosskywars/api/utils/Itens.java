package joserodpt.mysticosskywars.api.utils;

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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Itens {

    private static final Map<UUID, String> MOJANG_TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final String MOJANG_PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    /** Resolves an ItemsAdder item without a hard dependency on its API. */
    public static ItemStack itemsAdder(String id, int amount) {
        if (id == null || !Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) return null;
        try {
            Class<?> customStack = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Method getInstance = customStack.getMethod("getInstance", String.class);
            Object stack = getInstance.invoke(null, id);
            if (stack == null) return null;
            Method itemStack = customStack.getMethod("getItemStack");
            ItemStack result = ((ItemStack) itemStack.invoke(stack)).clone();
            result.setAmount(Math.max(1, Math.min(result.getMaxStackSize(), amount)));
            return result;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public static ItemStack createHead(Player player, int quantidade, String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, quantidade);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(Text.color(name));
        skull.setLore(Text.color(lore));
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);

        applyPlayerTexture(item, player);
        return item;
    }

    /** Applies the player's Mojang texture asynchronously without blocking the server thread. */
    public static void applyPlayerTexture(ItemStack item, Player player) {
        if (item == null || player == null || !(item.getItemMeta() instanceof SkullMeta)) return;

        UUID uuid = player.getUniqueId();
        String texture = MOJANG_TEXTURE_CACHE.get(uuid);
        if (texture != null) {
            applyTexture(item, texture);
            refreshPlayerHeads(player, item);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                String fetched = fetchMojangTexture(uuid);
                if (fetched == null) return;
                MOJANG_TEXTURE_CACHE.put(uuid, fetched);
                Bukkit.getScheduler().runTask(MysticosSkywarsAPI.getInstance().getPlugin(), () -> {
                    applyTexture(item, fetched);
                    refreshPlayerHeads(player, item);
                });
            });
        }
    }

    private static void refreshPlayerHeads(Player player, ItemStack texturedItem) {
        if (!player.isOnline() || !(texturedItem.getItemMeta() instanceof SkullMeta)) return;
        String displayName = texturedItem.getItemMeta().getDisplayName();
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack current = player.getInventory().getItem(slot);
            if (current == null || current.getType() != Material.PLAYER_HEAD || !current.hasItemMeta()) continue;
            if (displayName.equals(current.getItemMeta().getDisplayName())) {
                player.getInventory().setItem(slot, texturedItem.clone());
            }
        }
    }

    private static String fetchMojangTexture(UUID uuid) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(MOJANG_PROFILE_URL + uuid.toString().replace("-", "") + "?unsigned=false");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            String json = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject profile = JsonParser.parseString(json).getAsJsonObject();
            for (com.google.gson.JsonElement property : profile.getAsJsonArray("properties")) {
                JsonObject value = property.getAsJsonObject();
                if ("textures".equals(value.get("name").getAsString())) {
                    return value.get("value").getAsString();
                }
            }
        } catch (Exception ignored) {
            // Bukkit's player profile remains the fallback if Mojang is unavailable.
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    private static void applyTexture(ItemStack item, String encodedTexture) {
        try {
            SkullMeta skull = (SkullMeta) item.getItemMeta();
            String skinUrl = textureUrl(encodedTexture);
            if (skinUrl != null) {
                org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
                profile.getTextures().setSkin(new URL(skinUrl));
                skull.setOwnerProfile(profile);
                item.setItemMeta(skull);
                return;
            }
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class)
                    .newInstance(UUID.randomUUID(), null);
            Object property = propertyClass.getConstructor(String.class, String.class)
                    .newInstance("textures", encodedTexture);
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            properties.getClass().getMethod("put", Object.class, Object.class)
                    .invoke(properties, "textures", property);

            Field profileField = findField(skull.getClass(), "profile");
            profileField.setAccessible(true);
            profileField.set(skull, profile);
            item.setItemMeta(skull);
        } catch (Exception ignored) {
            // Paper/Bukkit profile handling remains the fallback on API changes.
        }
    }

    private static String textureUrl(String encodedTexture) {
        try {
            String json = new String(Base64.getDecoder().decode(encodedTexture), StandardCharsets.UTF_8);
            JsonObject textures = JsonParser.parseString(json).getAsJsonObject();
            return textures.getAsJsonObject("textures").getAsJsonObject("SKIN")
                    .get("url").getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static ItemStack addLore(ItemStack i, List<String> lor) {
        if (i != null) {
            ItemStack is = i.clone();
            ItemMeta meta;
            if (!is.hasItemMeta()) {
                meta = Bukkit.getItemFactory().getItemMeta(is.getType());
            } else {
                meta = is.getItemMeta();
            }

            List<String> lore;
            if (!meta.hasLore()) {
                lore = new ArrayList<>();
            } else {
                lore = meta.getLore();
            }
            lore.add("§9");
            lore.addAll(Text.color(lor));
            meta.setLore(lore);
            is.setItemMeta(meta);
            return is;
        } else {
            return null;
        }
    }

    public static ItemStack renameItem(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(lore));
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, int quantidade, String nome) {
        ItemStack item = new ItemStack(material, quantidade);
        ItemMeta meta = item.getItemMeta();
        if (nome != null) {
            meta.setDisplayName(Text.color(nome));
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, int quantidade, String nome, List<String> desc) {
        ItemStack item = new ItemStack(material, quantidade);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(nome));
        meta.setLore(Text.color(desc));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItemLoreEnchanted(Material m, int i, String name, List<String> desc) {
        ItemStack item = new ItemStack(m, i);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(name));
        meta.setLore(Text.color(desc));
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}
