/**
 * File: PipeFluid.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.pipe;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.ModFluids;

public enum PipeFluid implements StringRepresentable {
    EMPTY("empty", null),
    WATER("water", null),
    AMMONIA("ammonia", AvaliFluidKind.AMMONIA),
    COOLANT("coolant", AvaliFluidKind.COOLANT),
    FUEL("fuel", AvaliFluidKind.FUEL);

    private final String name;
    private final AvaliFluidKind kind;

    PipeFluid(String name, AvaliFluidKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public static PipeFluid of(Fluid fluid) {
        if (fluid.isSame(Fluids.WATER)) {
            return WATER;
        }
        AvaliFluidKind kind = AvaliLiquids.kindOf(fluid);
        if (kind == null) {
            return EMPTY;
        }
        for (PipeFluid value : values()) {
            if (value.kind == kind) {
                return value;
            }
        }
        return EMPTY;
    }

    public Fluid fluid() {
        if (this == WATER) {
            return Fluids.WATER;
        }
        return kind == null ? Fluids.EMPTY : ModFluids.source(kind);
    }

    public int tint() {
        return kind == null ? AvaliLiquids.WATER_TINT : kind.tint();
    }

    public boolean compatible(PipeFluid other) {
        return this == EMPTY || other == EMPTY || this == other;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
