package joserodpt.mysticosskywars.plugin.listeners;

import joserodpt.mysticosskywars.api.MysticosSkywarsAPI;
import joserodpt.mysticosskywars.api.config.MSWConfig;
import joserodpt.mysticosskywars.api.player.MSWPlayer;
import joserodpt.mysticosskywars.api.utils.ItemStackSpringer;
import joserodpt.mysticosskywars.api.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Large, optional LuckyBlock bridge. It has no hard dependency on a LuckyBlock
 * or ItemsAdder plugin and can therefore be used with vanilla blocks, ItemsAdder
 * blocks, or both at the same time.
 */
public final class LuckyBlockListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!MSWConfig.file().getBoolean("Config.LuckyBlock.Enabled", false)) return;
        if (!isLuckyBlock(event.getBlock())) return;

        Player player = event.getPlayer();
        MSWPlayer gamePlayer = MysticosSkywarsAPI.getInstance().getPlayerManagerAPI().getPlayer(player);
        if (MSWConfig.file().getBoolean("Config.LuckyBlock.Break-In-Match-Only", true)
                && (gamePlayer == null || !gamePlayer.isInMatch())) return;

        List<String> configuredRewards = MSWConfig.file().getStringList("Config.LuckyBlock.Rewards");
        if (configuredRewards.isEmpty()) return;

        // Lucky blocks are consumed by the listener, so another plugin cannot
        // duplicate the vanilla drop when a reward is actually opened.
        event.setDropItems(false);

        List<Reward> rewards = new ArrayList<>();
        for (String raw : configuredRewards) rewards.add(Reward.parse(raw));
        int amount = MSWConfig.file().getInt("Config.LuckyBlock.Rewards-Per-Break", 1);
        if (amount <= 0 || amount >= rewards.size()) amount = rewards.size();

        Set<Integer> selected = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            int index = pickWeighted(rewards, selected);
            if (index < 0) break;
            selected.add(index);
            applyReward(player, rewards.get(index).value);
        }

        String broadcast = MSWConfig.file().getString("Config.LuckyBlock.Broadcast", "");
        if (!broadcast.isEmpty()) {
            Bukkit.broadcastMessage(Text.color(broadcast.replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount))));
        }
        playFeedback(event.getBlock().getLocation());
    }

    private int pickWeighted(List<Reward> rewards, Set<Integer> excluded) {
        double total = 0;
        for (int i = 0; i < rewards.size(); i++) if (!excluded.contains(i)) total += rewards.get(i).weight;
        if (total <= 0) return -1;
        double chosen = ThreadLocalRandom.current().nextDouble(total);
        for (int i = 0; i < rewards.size(); i++) {
            if (excluded.contains(i)) continue;
            chosen -= rewards.get(i).weight;
            if (chosen <= 0) return i;
        }
        return -1;
    }

    private boolean isLuckyBlock(Block block) {
        Set<String> types = new HashSet<>(MSWConfig.file().getStringList("Config.LuckyBlock.Blocks"));
        if (types.isEmpty()) types.add(MSWConfig.file().getString("Config.LuckyBlock.Material", "SPONGE"));
        for (String configured : types) {
            String type = configured.trim();
            if (type.equalsIgnoreCase(block.getType().name())) return true;
            if (type.regionMatches(true, 0, "ITEMSADDER:", 0, 11) && isItemsAdderBlock(block, type.substring(11))) return true;
        }
        return false;
    }

    private boolean isItemsAdderBlock(Block block, String expected) {
        try {
            Class<?> customBlock = Class.forName("dev.lone.itemsadder.api.CustomBlock");
            Object value = customBlock.getMethod("byAlreadyPlaced", Block.class).invoke(null, block);
            if (value == null) return false;
            for (String methodName : new String[]{"getNamespacedID", "getNamespacedId", "getId"}) {
                try {
                    Method method = value.getClass().getMethod(methodName);
                    Object id = method.invoke(value);
                    if (id != null && expected.equalsIgnoreCase(String.valueOf(id))) return true;
                } catch (ReflectiveOperationException ignored) { }
            }
            return expected.equalsIgnoreCase(String.valueOf(value));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private void applyReward(Player player, String reward) {
        if (reward.startsWith("COMMAND:")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlayer(reward.substring(8), player));
            return;
        }
        if (reward.startsWith("EFFECT:")) {
            String[] parts = reward.substring(7).split(";");
            PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
            int duration = value(parts, "DURATION", 100);
            int amplifier = Math.max(0, value(parts, "AMPLIFIER", 1) - 1);
            if (type != null) player.addPotionEffect(new PotionEffect(type, duration, amplifier));
            return;
        }
        if (reward.startsWith("EXPLOSION:")) {
            float power = (float) number(reward.substring(10), 2.0);
            player.getWorld().createExplosion(player.getLocation(), power, false, false);
            return;
        }
        if (reward.startsWith("ITEM:")) {
            Map<String, Object> data = new HashMap<>();
            for (String pair : reward.substring(5).split(";")) {
                String[] split = pair.split("=", 2);
                if (split.length == 2) data.put(split[0].toUpperCase(), split[1]);
            }
            data.putIfAbsent("AMOUNT", 1);
            try {
                data.put("AMOUNT", Integer.parseInt(String.valueOf(data.get("AMOUNT"))));
                ItemStack item = ItemStackSpringer.getItemDeSerialized(data);
                if (item != null) player.getInventory().addItem(item);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    private void playFeedback(Location location) {
        String sound = MSWConfig.file().getString("Config.LuckyBlock.Sound", "ENTITY_PLAYER_LEVELUP");
        try { location.getWorld().playSound(location, Sound.valueOf(sound.toUpperCase()), 1f, 1.2f); } catch (IllegalArgumentException ignored) { }
        try { location.getWorld().spawnParticle(Particle.TOTEM, location.clone().add(.5, .5, .5), 18, .35, .35, .35, .1); } catch (IllegalArgumentException ignored) { }
    }

    private String replacePlayer(String value, Player player) { return value.replace("%player%", player.getName()); }

    private int value(String[] parts, String key, int fallback) {
        for (String part : parts) {
            String[] split = part.split("=", 2);
            if (split.length == 2 && key.equalsIgnoreCase(split[0])) {
                try { return Integer.parseInt(split[1]); } catch (NumberFormatException ignored) { return fallback; }
            }
        }
        return fallback;
    }

    private double number(String value, double fallback) {
        try { return Double.parseDouble(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static final class Reward {
        private final double weight;
        private final String value;

        private Reward(double weight, String value) { this.weight = Math.max(0, weight); this.value = value; }

        private static Reward parse(String raw) {
            String[] split = raw.split("\\|", 2);
            if (split.length == 2 && split[0].toUpperCase().startsWith("WEIGHT=")) {
                try { return new Reward(Double.parseDouble(split[0].substring(7)), split[1]); }
                catch (NumberFormatException ignored) { }
            }
            return new Reward(1, raw);
        }
    }
}
