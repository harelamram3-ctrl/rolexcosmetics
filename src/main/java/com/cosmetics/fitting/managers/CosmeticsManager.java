package com.cosmetics.fitting.managers;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import com.cosmetics.fitting.model.Cosmetic;
import com.cosmetics.fitting.model.CosmeticCategory;
import com.cosmetics.fitting.model.CosmeticSlot;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the cosmetics catalog from config.yml and handles equipping/removing
 * cosmetics on players (backed by ItemsAdder custom items for hats/wings,
 * and Bukkit Particle for particle trails).
 */
public class CosmeticsManager {

    private final CosmeticsFittingPlugin plugin;
    private final Map<String, CosmeticCategory> categories = new LinkedHashMap<>();

    // Tracks what each player currently has equipped per slot, purely for the
    // fitting-room session (not persisted globally - hook your own storage here
    // if you want cosmetics to persist outside the fitting room).
    private final Map<UUID, Map<CosmeticSlot, Cosmetic>> equipped = new ConcurrentHashMap<>();

    public CosmeticsManager(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
        loadCatalog();
    }

    public void loadCatalog() {
        categories.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("cosmetics");
        if (root == null) {
            plugin.getLogger().warning("לא נמצא סעיף 'cosmetics' ב-config.yml");
            return;
        }

        for (String categoryKey : root.getKeys(false)) {
            ConfigurationSection catSection = root.getConfigurationSection(categoryKey);
            if (catSection == null) continue;

            String displayName = catSection.getString("display-name", categoryKey);
            CosmeticSlot slot = CosmeticSlot.valueOf(catSection.getString("slot", "HELMET").toUpperCase());

            CosmeticCategory category = new CosmeticCategory(categoryKey, displayName, slot);

            for (Map<?, ?> raw : catSection.getMapList("items")) {
                String id = String.valueOf(raw.get("id"));
                String name = String.valueOf(raw.get("name"));
                Particle particle = null;
                if (slot == CosmeticSlot.PARTICLE && raw.get("particle") != null) {
                    try {
                        particle = Particle.valueOf(String.valueOf(raw.get("particle")).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Particle לא תקין: " + raw.get("particle"));
                    }
                }
                category.addCosmetic(new Cosmetic(id, name, categoryKey, slot, particle));
            }

            categories.put(categoryKey, category);
        }

        plugin.getLogger().info("נטענו " + categories.size() + " קטגוריות קוסמטיקס.");
    }

    public Map<String, CosmeticCategory> getCategories() {
        return categories;
    }

    public CosmeticCategory getCategory(String key) {
        return categories.get(key);
    }

    /**
     * Equips a cosmetic on the given player. For HELMET/CHEST slots this fetches
     * the actual ItemsAdder CustomStack and places it in the relevant armor slot.
     * For PARTICLE slots it just records the selection - MirrorManager/particle
     * ticking reads this map to know what trail to spawn.
     */
    public void equip(Player player, Cosmetic cosmetic) {
        Map<CosmeticSlot, Cosmetic> playerMap = equipped.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        playerMap.put(cosmetic.getSlot(), cosmetic);

        PlayerInventory inv = player.getInventory();
        switch (cosmetic.getSlot()) {
            case HELMET -> {
                ItemStack item = resolveItemsAdderStack(cosmetic.getId());
                if (item != null) inv.setHelmet(item);
            }
            case CHEST -> {
                ItemStack item = resolveItemsAdderStack(cosmetic.getId());
                if (item != null) inv.setChestplate(item);
            }
            case PARTICLE -> {
                // handled by MirrorManager's tick task reading the equipped map
            }
        }
    }

    public void remove(Player player, CosmeticSlot slot) {
        Map<CosmeticSlot, Cosmetic> playerMap = equipped.get(player.getUniqueId());
        if (playerMap != null) playerMap.remove(slot);

        PlayerInventory inv = player.getInventory();
        switch (slot) {
            case HELMET -> inv.setHelmet(null);
            case CHEST -> inv.setChestplate(null);
            case PARTICLE -> {
                // nothing to clear on the player entity itself
            }
        }
    }

    public Cosmetic getEquipped(Player player, CosmeticSlot slot) {
        Map<CosmeticSlot, Cosmetic> playerMap = equipped.get(player.getUniqueId());
        return playerMap == null ? null : playerMap.get(slot);
    }

    public Map<CosmeticSlot, Cosmetic> getAllEquipped(Player player) {
        return equipped.getOrDefault(player.getUniqueId(), Map.of());
    }

    public void clearSession(Player player) {
        equipped.remove(player.getUniqueId());
    }

    /**
     * Resolves an ItemsAdder namespace:id into an actual ItemStack via the API.
     * Returns null (and logs a warning) if ItemsAdder doesn't recognize the id -
     * this usually means the id in config.yml doesn't match what's registered
     * in ItemsAdder's own item config.
     */
    public ItemStack resolveItemsAdderStack(String namespacedId) {
        CustomStack stack = CustomStack.getInstance(namespacedId);
        if (stack == null) {
            plugin.getLogger().warning("ItemsAdder לא הכיר את הפריט: " + namespacedId
                    + " - ודא שהוא רשום נכון בקונפיג של ItemsAdder.");
            return null;
        }
        return stack.getItemStack();
    }
}
