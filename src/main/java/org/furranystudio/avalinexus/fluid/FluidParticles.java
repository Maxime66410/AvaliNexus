/**
 * File: FluidParticles.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.avalinexus.fluid;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class FluidParticles {

    public static final RegistryEntry<ParticleType<ColorParticleOption>> SPLASH = ModRegistry.register(Registries.PARTICLE_TYPE, "fluid_splash", key -> colored());
    public static final RegistryEntry<ParticleType<ColorParticleOption>> BUBBLE = ModRegistry.register(Registries.PARTICLE_TYPE, "fluid_bubble", key -> colored());

    private FluidParticles() {
    }

    public static void init() {
    }

    public static ParticleOptions splash(Entity entity, ParticleOptions options) {
        BlockPos feet = entity.blockPosition();
        AvaliFluidKind kind = kindAt(entity, feet);
        return swap(options, kind != null ? kind : kindAt(entity, feet.below()));
    }

    public static ParticleOptions drown(Entity entity, ParticleOptions options) {
        return swap(options, kindAt(entity, BlockPos.containing(entity.getEyePosition())));
    }

    private static AvaliFluidKind kindAt(Entity entity, BlockPos pos) {
        return AvaliLiquids.kindOf(entity.level().getFluidState(pos).getType());
    }

    private static ParticleOptions swap(ParticleOptions options, AvaliFluidKind kind) {
        if (kind == null) {
            return options;
        }
        if (options.getType() == ParticleTypes.SPLASH) {
            return ColorParticleOption.create(SPLASH.get(), kind.tint());
        }
        if (options.getType() == ParticleTypes.BUBBLE) {
            return ColorParticleOption.create(BUBBLE.get(), kind.tint());
        }
        return options;
    }

    private static ParticleType<ColorParticleOption> colored() {
        return new ParticleType<>(false) {
            @Override
            public MapCodec<ColorParticleOption> codec() {
                return ColorParticleOption.codec(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                StreamCodec<? super ByteBuf, ColorParticleOption> codec = ColorParticleOption.streamCodec(this);
                return codec;
            }
        };
    }
}
