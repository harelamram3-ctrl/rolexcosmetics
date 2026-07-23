package com.cosmetics.fitting.commands;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import com.cosmetics.fitting.gui.FittingGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FittingCommand implements CommandExecutor {

    private final CosmeticsFittingPlugin plugin;
    private final FittingGUI gui;

    public FittingCommand(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
        this.gui = new FittingGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("פקודה זו זמינה לשחקנים בלבד.");
            return true;
        }

        if (!plugin.getFittingRoomManager().isTracked(player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.not-in-room")));
            return true;
        }

        gui.openMainMenu(player);
        return true;
    }
}
