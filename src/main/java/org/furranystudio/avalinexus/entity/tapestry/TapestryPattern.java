/**
 * File: TapestryPattern.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.entity.tapestry;

import net.minecraft.world.item.DyeColor;

// Orange band layout drawn over the tinted cloth, each one is its own item in every color
public enum TapestryPattern {
    BRAID("avali_tapestry"),
    CHEVRON("avali_chevron_tapestry");

    private final String name;

    TapestryPattern(String name) {
        this.name = name;
    }

    public String itemName(DyeColor color) {
        return color.getSerializedName() + "_" + name;
    }

    public static TapestryPattern byId(int id) {
        TapestryPattern[] patterns = values();
        return id >= 0 && id < patterns.length ? patterns[id] : BRAID;
    }
}
