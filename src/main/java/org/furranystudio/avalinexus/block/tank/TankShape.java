/**
 * File: TankShape.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.util.StringRepresentable;

public enum TankShape implements StringRepresentable {
    SINGLE("single", true, true),
    BOTTOM("bottom", true, false),
    MIDDLE("middle", false, false),
    TOP("top", false, true);

    private final String name;
    private final boolean bottomCap;
    private final boolean topCap;

    TankShape(String name, boolean bottomCap, boolean topCap) {
        this.name = name;
        this.bottomCap = bottomCap;
        this.topCap = topCap;
    }

    public static TankShape of(boolean tankBelow, boolean tankAbove) {
        if (tankBelow) {
            return tankAbove ? MIDDLE : TOP;
        }
        return tankAbove ? BOTTOM : SINGLE;
    }

    public boolean bottomCap() {
        return bottomCap;
    }

    public boolean topCap() {
        return topCap;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
