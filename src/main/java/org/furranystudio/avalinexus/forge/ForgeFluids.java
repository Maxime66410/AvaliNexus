/**
 * File: ForgeFluids.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.forge;

import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.fluid.AvaliFluid;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;

import java.util.EnumMap;
import java.util.Map;

// Forge moves entities through fluids with a fluid type, so our fluids carry one each
final class ForgeFluids {

    private static final Map<AvaliFluidKind, FluidType> TYPES = new EnumMap<>(AvaliFluidKind.class);

    private ForgeFluids() {
    }

    static void init() {
        Platform.setFluidFactory((kind, source) -> source ? new Source(kind) : new Flowing(kind));
    }

    static void register(RegisterEvent event) {
        for (AvaliFluidKind kind : AvaliFluidKind.values()) {
            event.register(ForgeRegistries.Keys.FLUID_TYPES, AvaliNexus.id(kind.fluidName()), () -> TYPES.computeIfAbsent(kind, ForgeFluids::create));
        }
    }

    private static FluidType create(AvaliFluidKind kind) {
        return new FluidType(FluidType.Properties.create()
            .descriptionId("block.avalinexus." + kind.fluidName())
            .canSwim(true)
            .canDrown(true)
            .canPushEntity(true)
            .supportsBoating(true)
            .canExtinguish(kind != AvaliFluidKind.FUEL)
            .canConvertToSource(kind.infinite())
            .lightLevel(kind.lightLevel())
            .viscosity(kind == AvaliFluidKind.FUEL ? 3000 : 1000));
    }

    private static FluidType type(AvaliFluidKind kind) {
        return TYPES.computeIfAbsent(kind, ForgeFluids::create);
    }

    private static final class Source extends AvaliFluid.Source {

        Source(AvaliFluidKind kind) {
            super(kind);
        }

        @Override
        public FluidType getFluidType() {
            return type(kind);
        }
    }

    private static final class Flowing extends AvaliFluid.Flowing {

        Flowing(AvaliFluidKind kind) {
            super(kind);
        }

        @Override
        public FluidType getFluidType() {
            return type(kind);
        }
    }
}
