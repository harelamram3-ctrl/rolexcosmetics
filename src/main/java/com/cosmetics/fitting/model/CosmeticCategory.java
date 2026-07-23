package com.cosmetics.fitting.model;

import java.util.ArrayList;
import java.util.List;

public class CosmeticCategory {

    private final String key;
    private final String displayName;
    private final CosmeticSlot slot;
    private final List<Cosmetic> cosmetics = new ArrayList<>();

    public CosmeticCategory(String key, String displayName, CosmeticSlot slot) {
        this.key = key;
        this.displayName = displayName;
        this.slot = slot;
    }

    public void addCosmetic(Cosmetic cosmetic) {
        cosmetics.add(cosmetic);
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CosmeticSlot getSlot() {
        return slot;
    }

    public List<Cosmetic> getCosmetics() {
        return cosmetics;
    }
}
