package com.cosmetics.fitting.gui;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import com.cosmetics.fitting.model.Cosmetic;
import com.cosmetics.fitting.model.CosmeticCategory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class FittingGUI {

    public static final String MAIN_TITLE = "\u00A78\u00A7lחדר הלבשה - קטגוריות";
    public static final String CATEGORY_TITLE_PREFIX = "\u00A78\u00A7lקוסמטיקס: ";

    private final CosmeticsFittingPlugin plugin;

    public FittingGUI(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        List<CosmeticCategory> categories = plugin.getCosmeticsManager().getCategories().values().stream().toList();
        int size = Math.max(9, ((categories.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, MAIN_TITLE);

        for (CosmeticCategory category : categories) {
            ItemStack icon = new ItemStack(iconForSlot(category.getSlot()));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(colorize(category.getDisplayName()));
            meta.setLore(List.of(
                    colorize("&7לחץ כדי לראות פריטים"),
                    colorize("&8(" + category.getCosmetics().size() + " פריטים)")
            ));
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }

        // Exit-room helper item
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName(colorize("&cסגור תפריט"));
        exit.setItemMeta(exitMeta);
        inv.setItem(size - 1, exit);

        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, CosmeticCategory category) {
        List<Cosmetic> cosmetics = category.getCosmetics();
        int size = Math.max(9, ((cosmetics.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, CATEGORY_TITLE_PREFIX + category.getKey());

        for (Cosmetic cosmetic : cosmetics) {
            ItemStack icon;
            if (cosmetic.getParticle() != null) {
                icon = new ItemStack(Material.BLAZE_POWDER);
            } else {
                ItemStack resolved = plugin.getCosmeticsManager().resolveItemsAdderStack(cosmetic.getId());
                icon = resolved != null ? resolved.clone() : new ItemStack(Material.BARRIER);
            }
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(colorize(cosmetic.getDisplayName()));
            meta.setLore(List.of(colorize("&aלחץ כדי למדוד")));
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }

        ItemStack removeItem = new ItemStack(Material.MUD_BRICK);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName(colorize("&cהסר קוסמטיקס בקטגוריה זו"));
        removeItem.setItemMeta(removeMeta);
        inv.setItem(size - 5, removeItem);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(colorize("&eחזרה לקטגוריות"));
        back.setItemMeta(backMeta);
        inv.setItem(size - 1, back);

        player.openInventory(inv);
    }

    private Material iconForSlot(com.cosmetics.fitting.model.CosmeticSlot slot) {
        return switch (slot) {
            case HELMET -> Material.LEATHER_HELMET;
            case CHEST -> Material.ELYTRA;
            case PARTICLE -> Material.NETHER_STAR;
        };
    }

    private String colorize(String text) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }
}
