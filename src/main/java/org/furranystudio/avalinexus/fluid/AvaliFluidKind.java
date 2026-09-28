/**
 * File: AvaliFluidKind.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

// The three Avali fluids and what each one does, they share one gray texture tinted with their color
public enum AvaliFluidKind {
    // Avali lifeblood and the oceans of Avalon, found in polar taiga lakes
    AMMONIA("ammonia", 0xFFD6ECFF, 0, 1, true, true, () -> Blocks.PACKED_ICE),
    // Made from ammonia, the tech fluid of Avali machines
    COOLANT("coolant", 0xFF3FF2FF, 5, 3, false, false, () -> Blocks.BLUE_ICE),
    // Made from ammonia, burns 1.6 times as long as lava, menus sync burn time as a short so 32767 is the cap
    FUEL("fuel", 0xFFFF8C1A, 5, 0, false, false, null);

    private final String name;
    private final int tint;
    private final int lightLevel;
    // Frozen ticks added each tick on top of the vanilla thaw, 0 never freezes
    private final int freezing;
    private final boolean healsAvali;
    private final boolean infinite;
    // What lava turns into when it touches this fluid, null sets the fuel on fire instead
    private final Supplier<Block> lavaResult;

    AvaliFluidKind(String name, int tint, int lightLevel, int freezing, boolean healsAvali, boolean infinite, Supplier<Block> lavaResult) {
        this.name = name;
        this.tint = tint;
        this.lightLevel = lightLevel;
        this.freezing = freezing;
        this.healsAvali = healsAvali;
        this.infinite = infinite;
        this.lavaResult = lavaResult;
    }

    public String fluidName() {
        return name;
    }

    public int tint() {
        return tint;
    }

    public int lightLevel() {
        return lightLevel;
    }

    public int freezing() {
        return freezing;
    }

    public boolean healsAvali() {
        return healsAvali;
    }

    public boolean infinite() {
        return infinite;
    }

    public Block lavaResult() {
        return lavaResult == null ? null : lavaResult.get();
    }
}
