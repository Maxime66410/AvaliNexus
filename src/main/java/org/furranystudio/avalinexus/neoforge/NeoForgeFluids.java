/**
 * File: NeoForgeFluids.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.neoforge;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.fluid.AvaliFluid;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;

import java.util.EnumMap;
import java.util.Map;

// NeoForge moves entities through fluids with a fluid type, so our fluids carry one each
final class NeoForgeFluids {

    private static final Map<AvaliFluidKind, FluidType> TYPES = new EnumMap<>(AvaliFluidKind.class);

    private NeoForgeFluids() {
    }

    static void init() {
        Platform.setFluidFactory((kind, source) -> source ? new Source(kind) : new Flowing(kind));
    }

    static void register(RegisterEvent event) {
        for (AvaliFluidKind kind : AvaliFluidKind.values()) {
            event.register(NeoForgeRegistries.Keys.FLUID_TYPES, AvaliNexus.id(kind.fluidName()), () -> type(kind));
        }
    }

    private static FluidType type(AvaliFluidKind kind) {
        return TYPES.computeIfAbsent(kind, it -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.avalinexus." + it.fluidName())
            .canSwim(true)
            .canDrown(true)
            .canPushEntity(true)
            .supportsBoating(true)
            .canExtinguish(it != AvaliFluidKind.FUEL)
            .canConvertToSource(it.infinite())
            .lightLevel(it.lightLevel())
            .viscosity(it == AvaliFluidKind.FUEL ? 3000 : 1000)));
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
