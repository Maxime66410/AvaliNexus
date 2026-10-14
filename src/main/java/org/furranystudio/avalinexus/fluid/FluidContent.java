/**
 * File: FluidContent.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record FluidContent(Fluid fluid, int amount) {

    public static final FluidContent EMPTY = new FluidContent(Fluids.EMPTY, 0);

    public static final Codec<FluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidContent::fluid),
        Codec.INT.fieldOf("amount").forGetter(FluidContent::amount)
    ).apply(instance, FluidContent::of));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.FLUID), FluidContent::fluid,
        ByteBufCodecs.VAR_INT, FluidContent::amount,
        FluidContent::of);

    public static FluidContent of(Fluid fluid, int amount) {
        return amount <= 0 || fluid == Fluids.EMPTY ? EMPTY : new FluidContent(fluid, amount);
    }

    public boolean isEmpty() {
        return amount <= 0;
    }

    public boolean accepts(Fluid other) {
        return isEmpty() || fluid.isSame(other);
    }

    public FluidContent withAmount(int newAmount) {
        return of(fluid, newAmount);
    }
}
