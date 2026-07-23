package com.cosmetics.fitting.listeners;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import com.cosmetics.fitting.gui.FittingGUI;
import com.cosmetics.fitting.model.Cosmetic;
import com.cosmetics.fitting.model.CosmeticCategory;
import com.cosmetics.fitting.model.CosmeticSlot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FittingRoomListener implements Listener {

    private final CosmeticsFittingPlugin plugin;
    private final FittingGUI gui;

    public FittingRoomListener(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
        this.gui = new FittingGUI(plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        // Only bother checking when the player actually changed block position.
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        boolean nowInRoom = plugin.getFittingRoomManager().isInRoom(event.getTo());
        boolean wasInRoom = plugin.getFittingRoomManager().isTracked(player);

        if (nowInRoom && !wasInRoom) {
            plugin.getFittingRoomManager().markEntered(player);
            plugin.getMirrorManager().spawnMirrorFor(player);
            player.sendMessage(colorize(plugin.getConfig().getString("messages.entered-room")));
            gui.openMainMenu(player);
        } else if (!nowInRoom && wasInRoom) {
            plugin.getFittingRoomManager().markLeft(player);
            plugin.getMirrorManager().removeMirrorFor(player);
            player.sendMessage(colorize(plugin.getConfig().getString("messages.left-room")));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(FittingGUI.MAIN_TITLE) && !title.startsWith(FittingGUI.CATEGORY_TITLE_PREFIX)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        if (title.equals(FittingGUI.MAIN_TITLE)) {
            handleMainMenuClick(player, clicked);
        } else {
            String categoryKey = title.substring(FittingGUI.CATEGORY_TITLE_PREFIX.length());
            handleCategoryMenuClick(player, clicked, categoryKey);
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clicked) {
        if (clicked.getType() == org.bukkit.Material.BARRIER) {
            player.closeInventory();
            return;
        }
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String clickedName = ChatColor.stripColor(meta.getDisplayName());

        for (CosmeticCategory category : plugin.getCosmeticsManager().getCategories().values()) {
            String catName = ChatColor.stripColor(colorize(category.getDisplayName()));
            if (catName.equals(clickedName)) {
                gui.openCategoryMenu(player, category);
                return;
            }
        }
    }

    private void handleCategoryMenuClick(Player player, ItemStack clicked, String categoryKey) {
        CosmeticCategory category = plugin.getCosmeticsManager().getCategory(categoryKey);
        if (category == null) return;

        if (clicked.getType() == org.bukkit.Material.ARROW) {
            gui.openMainMenu(player);
            return;
        }
        if (clicked.getType() == org.bukkit.Material.MUD_BRICK) {
            plugin.getCosmeticsManager().remove(player, category.getSlot());
            plugin.getMirrorManager().refreshEquipment(player);
            player.sendMessage(colorize(plugin.getConfig().getString("messages.removed")
                    .replace("%item%", category.getDisplayName())));
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String clickedName = ChatColor.stripColor(meta.getDisplayName());

        for (Cosmetic cosmetic : category.getCosmetics()) {
            String cosName = ChatColor.stripColor(colorize(cosmetic.getDisplayName()));
            if (cosName.equals(clickedName)) {
                plugin.getCosmeticsManager().equip(player, cosmetic);
                if (cosmetic.getSlot() != CosmeticSlot.PARTICLE) {
                    plugin.getMirrorManager().refreshEquipment(player);
                }
                player.sendMessage(colorize(plugin.getConfig().getString("messages.equipped")
                        .replace("%item%", cosmetic.getDisplayName())));
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Intentionally left simple: closing the GUI does NOT remove the mirror -
        // the mirror stays until the player physically leaves the fitting-room
        // region, so they can keep browsing/re-opening with /fitting.
    }

    private String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
