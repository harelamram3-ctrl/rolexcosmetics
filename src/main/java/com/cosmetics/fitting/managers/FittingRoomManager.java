package com.cosmetics.fitting.managers;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FittingRoomManager {

    private final CosmeticsFittingPlugin plugin;

    private String worldName;
    private double minX, minY, minZ, maxX, maxY, maxZ;

    private final Set<UUID> playersInRoom = ConcurrentHashMap.newKeySet();

    public FittingRoomManager(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
        loadRegion();
    }

    public void loadRegion() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("fitting-room");
        if (section == null) return;

        worldName = section.getString("world", "world");
        double x1 = section.getDouble("pos1.x");
        double y1 = section.getDouble("pos1.y");
        double z1 = section.getDouble("pos1.z");
        double x2 = section.getDouble("pos2.x");
        double y2 = section.getDouble("pos2.y");
        double z2 = section.getDouble("pos2.z");

        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
    }

    public void setPos1(Location loc) {
        worldName = loc.getWorld().getName();
        plugin.getConfig().set("fitting-room.world", worldName);
        plugin.getConfig().set("fitting-room.pos1.x", loc.getX());
        plugin.getConfig().set("fitting-room.pos1.y", loc.getY());
        plugin.getConfig().set("fitting-room.pos1.z", loc.getZ());
        plugin.saveConfig();
        loadRegion();
    }

    public void setPos2(Location loc) {
        worldName = loc.getWorld().getName();
        plugin.getConfig().set("fitting-room.world", worldName);
        plugin.getConfig().set("fitting-room.pos2.x", loc.getX());
        plugin.getConfig().set("fitting-room.pos2.y", loc.getY());
        plugin.getConfig().set("fitting-room.pos2.z", loc.getZ());
        plugin.saveConfig();
        loadRegion();
    }

    public boolean isInRoom(Location loc) {
        World world = loc.getWorld();
        if (world == null || !world.getName().equals(worldName)) return false;

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public boolean isTracked(Player player) {
        return playersInRoom.contains(player.getUniqueId());
    }

    public void markEntered(Player player) {
        playersInRoom.add(player.getUniqueId());
    }

    public void markLeft(Player player) {
        playersInRoom.remove(player.getUniqueId());
    }

    public double getMirrorDistance() {
        return plugin.getConfig().getDouble("fitting-room.mirror-distance", 2.5);
    }

    public double getMirrorOffsetY() {
        return plugin.getConfig().getDouble("fitting-room.mirror-offset-y", 0.0);
    }
}
