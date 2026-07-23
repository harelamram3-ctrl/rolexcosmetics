package com.cosmetics.fitting.mirror;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A packet-only "mirror" clone of a player: a fake player entity visible ONLY
 * to the owning player, positioned facing them, wearing whatever cosmetics
 * are currently equipped. Built entirely with ProtocolLib so no other plugin
 * dependency (Citizens etc.) is required.
 */
public class MirrorEntity {

    private final Player owner;
    private final UUID mirrorUuid = UUID.randomUUID();
    private final int entityId;
    private final WrappedGameProfile profile;
    private Location location;
    private boolean spawned = false;

    public MirrorEntity(Player owner, int entityId) {
        this.owner = owner;
        this.entityId = entityId;
        // Clone the owner's skin/profile so the mirror shows the same skin.
        WrappedGameProfile ownerProfile = WrappedGameProfile.fromPlayer(owner);
        this.profile = new WrappedGameProfile(mirrorUuid, ownerProfile.getName());
        for (WrappedSignedProperty prop : ownerProfile.getProperties().get("textures")) {
            this.profile.getProperties().put("textures", prop);
        }
    }

    public void spawn(Location loc) {
        this.location = loc;
        sendPlayerInfoAdd();
        sendSpawnEntity();
        sendHeadRotation();
        sendEquipment(owner.getInventory().getHelmet(), owner.getInventory().getChestplate());
        spawned = true;

        // Remove from the tab list shortly after spawn - we only needed the
        // PLAYER_INFO packet so the client has the skin cached for rendering.
        owner.getServer().getScheduler().runTaskLater(
                com.cosmetics.fitting.CosmeticsFittingPlugin.getInstance(),
                this::sendPlayerInfoRemove,
                40L
        );
    }

    public void updateLocation(Location loc) {
        this.location = loc;
        if (!spawned) return;

        PacketContainer teleport = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        teleport.getIntegers().write(0, entityId);
        teleport.getDoubles()
                .write(0, loc.getX())
                .write(1, loc.getY())
                .write(2, loc.getZ());
        teleport.getBytes()
                .write(0, (byte) (loc.getYaw() * 256.0F / 360.0F))
                .write(1, (byte) (loc.getPitch() * 256.0F / 360.0F));
        teleport.getBooleans().write(0, true);
        sendPacket(teleport);
        sendHeadRotation();
    }

    public void updateEquipment(ItemStack helmet, ItemStack chest) {
        sendEquipment(helmet, chest);
    }

    private void sendPlayerInfoAdd() {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoActions().write(0,
                java.util.EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER));
        PlayerInfoData data = new PlayerInfoData(
                profile,
                0,
                EnumWrappers.NativeGameMode.CREATIVE,
                WrappedChatComponent.fromText(profile.getName())
        );
        packet.getPlayerInfoDataLists().write(1, List.of(data));
        sendPacket(packet);
    }

    private void sendPlayerInfoRemove() {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        packet.getUUIDLists().write(0, Collections.singletonList(mirrorUuid));
        sendPacket(packet);
    }

    private void sendSpawnEntity() {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packet.getIntegers().write(0, entityId);
        packet.getUUIDs().write(0, mirrorUuid);
        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());
        packet.getBytes()
                .write(0, (byte) (location.getYaw() * 256.0F / 360.0F))
                .write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
        sendPacket(packet);
    }

    private void sendHeadRotation() {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, entityId);
        packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
        sendPacket(packet);
    }

    private void sendEquipment(ItemStack helmet, ItemStack chest) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityId);

        List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = new java.util.ArrayList<>();
        pairs.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.HEAD,
                helmet == null ? new ItemStack(org.bukkit.Material.AIR) : helmet));
        pairs.add(new com.comphenix.protocol.wrappers.Pair<>(EnumWrappers.ItemSlot.CHEST,
                chest == null ? new ItemStack(org.bukkit.Material.AIR) : chest));
        packet.getSlotStackPairLists().write(0, pairs);

        sendPacket(packet);
    }

    public void remove() {
        if (!spawned) return;
        PacketContainer destroy = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, Collections.singletonList(entityId));
        sendPacket(destroy);
        sendPlayerInfoRemove();
        spawned = false;
    }

    private void sendPacket(PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(owner, packet);
        } catch (Exception e) {
            com.cosmetics.fitting.CosmeticsFittingPlugin.getInstance()
                    .getLogger().warning("שגיאה בשליחת פאקט מראה: " + e.getMessage());
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isSpawned() {
        return spawned;
    }
}
