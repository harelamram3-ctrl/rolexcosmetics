package com.cosmetics.fitting.listeners;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final CosmeticsFittingPlugin plugin;

    public PlayerConnectionListener(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getFittingRoomManager().markLeft(event.getPlayer());
        plugin.getMirrorManager().removeMirrorFor(event.getPlayer());
        plugin.getCosmeticsManager().clearSession(event.getPlayer());
    }
}
