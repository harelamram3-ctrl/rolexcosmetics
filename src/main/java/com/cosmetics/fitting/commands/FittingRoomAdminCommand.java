package com.cosmetics.fitting.commands;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FittingRoomAdminCommand implements CommandExecutor {

    private final CosmeticsFittingPlugin plugin;

    public FittingRoomAdminCommand(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("פקודה זו זמינה לשחקנים בלבד.");
            return true;
        }
        if (!player.hasPermission("cosmetics.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("שימוש: /fittingroom <setpos1|setpos2|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setpos1" -> {
                plugin.getFittingRoomManager().setPos1(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "פינה 1 של חדר ההלבשה נקבעה למיקומך.");
            }
            case "setpos2" -> {
                plugin.getFittingRoomManager().setPos2(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "פינה 2 של חדר ההלבשה נקבעה למיקומך.");
            }
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getFittingRoomManager().loadRegion();
                plugin.getCosmeticsManager().loadCatalog();
                player.sendMessage(ChatColor.GREEN + "הקונפיג נטען מחדש.");
            }
            default -> player.sendMessage("שימוש: /fittingroom <setpos1|setpos2|reload>");
        }
        return true;
    }
}
