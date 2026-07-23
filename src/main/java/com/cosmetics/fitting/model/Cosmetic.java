package com.cosmetics.fitting.model;

import org.bukkit.Particle;

public class Cosmetic {

    private final String id;          // ItemsAdder namespace:id, or particle:name for particle-only entries
    private final String displayName;
    private final String categoryKey;
    private final CosmeticSlot slot;
    private final Particle particle;  // only set when slot == PARTICLE

    public Cosmetic(String id, String displayName, String categoryKey, CosmeticSlot slot, Particle particle) {
        this.id = id;
        this.displayName = displayName;
        this.categoryKey = categoryKey;
        this.slot = slot;
        this.particle = particle;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public CosmeticSlot getSlot() {
        return slot;
    }

    public Particle getParticle() {
        return particle;
    }
}
