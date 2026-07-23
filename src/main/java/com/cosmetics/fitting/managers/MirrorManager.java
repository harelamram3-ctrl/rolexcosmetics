package com.cosmetics.fitting.managers;

import com.cosmetics.fitting.CosmeticsFittingPlugin;
import com.cosmetics.fitting.model.Cosmetic;
import com.cosmetics.fitting.model.CosmeticSlot;
import com.cosmetics.fitting.mirror.MirrorEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MirrorManager {

    private final CosmeticsFittingPlugin plugin;
    private final Map<UUID, MirrorEntity> mirrors = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(900_000_000);

    private int tickTaskId = -1;

    public MirrorManager(CosmeticsFittingPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnMirrorFor(Player player) {
        if (mirrors.containsKey(player.getUniqueId())) return;

        MirrorEntity mirror = new MirrorEntity(player, idCounter.incrementAndGet());
        Location mirrorLoc = computeMirrorLocation(player);
        mirror.spawn(mirrorLoc);
        mirrors.put(player.getUniqueId(), mirror);
    }

    public void removeMirrorFor(Player player) {
        MirrorEntity mirror = mirrors.remove(player.getUniqueId());
        if (mirror != null) mirror.remove();
    }

    public void removeAllMirrors() {
        mirrors.values().forEach(MirrorEntity::remove);
        mirrors.clear();
    }

    public boolean hasMirror(Player player) {
        return mirrors.containsKey(player.getUniqueId());
    }

    public void refreshEquipment(Player player) {
        MirrorEntity mirror = mirrors.get(player.getUniqueId());
        if (mirror == null) return;
        mirror.updateEquipment(player.getInventory().getHelmet(), player.getInventory().getChestplate());
    }

    /**
     * Places the mirror clone directly in front of the player, facing them
     * (i.e. rotated 180 degrees from the player's own facing), like a real
     * mirror reflection.
     */
    private Location computeMirrorLocation(Player player) {
        double distance = plugin.getFittingRoomManager().getMirrorDistance();
        double offsetY = plugin.getFittingRoomManager().getMirrorOffsetY();

        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().setY(0).normalize();
        Location mirrorLoc = player.getLocation().clone().add(direction.multiply(distance));
        mirrorLoc.setY(mirrorLoc.getY() + offsetY);

        // Face the mirror back toward the player (180 degrees).
        float mirrorYaw = player.getLocation().getYaw() + 180F;
        mirrorLoc.setYaw(mirrorYaw);
        mirrorLoc.setPitch(0);
        return mirrorLoc;
    }

    /**
     * Runs every tick-ish to keep the mirror positioned in front of the player
     * as they turn, and to spawn any active particle-trail cosmetic around the
     * mirror clone so the player can preview it.
     */
    public void startTickTask() {
        tickTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<UUID, MirrorEntity> entry : mirrors.entrySet()) {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) continue;

                MirrorEntity mirror = entry.getValue();
                Location mirrorLoc = computeMirrorLocation(player);
                mirror.updateLocation(mirrorLoc);

                Cosmetic particleCosmetic = plugin.getCosmeticsManager().getEquipped(player, CosmeticSlot.PARTICLE);
                if (particleCosmetic != null && particleCosmetic.getParticle() != null) {
                    player.spawnParticle(
                            particleCosmetic.getParticle(),
                            mirrorLoc.clone().add(0, 1.0, 0),
                            10, 0.3, 0.5, 0.3, 0.01
                    );
                }
            }
        }, 5L, 4L); // every 4 ticks (~0.2s) - smooth enough, cheap enough
    }

    public void stopTickTask() {
        if (tickTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(tickTaskId);
            tickTaskId = -1;
        }
    }
}
