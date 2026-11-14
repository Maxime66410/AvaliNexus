/**
 * File: AvaliLiquids.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class AvaliLiquids {

    public static final int BUCKET = 1000;
    public static final int WATER_TINT = 0xFF3F76E4;

    public static final RegistryEntry<DataComponentType<FluidContent>> STORED_FLUID = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "stored_fluid",
        key -> DataComponentType.<FluidContent>builder().persistent(FluidContent.CODEC).networkSynchronized(FluidContent.STREAM_CODEC).build());

    private AvaliLiquids() {
    }

    public static void init() {
    }

    public static AvaliFluidKind kindOf(Fluid fluid) {
        for (AvaliFluidKind kind : AvaliFluidKind.values()) {
            if (fluid.isSame(ModFluids.source(kind))) {
                return kind;
            }
        }
        return null;
    }

    public static Fluid source(Fluid fluid) {
        AvaliFluidKind kind = kindOf(fluid);
        if (kind != null) {
            return ModFluids.source(kind);
        }
        return fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
    }

    public static int tint(Fluid fluid) {
        AvaliFluidKind kind = kindOf(fluid);
        return kind == null ? WATER_TINT : kind.tint();
    }

    public static Component name(Fluid fluid) {
        Fluid source = source(fluid);
        Block block = source.defaultFluidState().createLegacyBlock().getBlock();
        if (block != Blocks.AIR) {
            return block.getName();
        }
        if (hasBucket(source)) {
            return new ItemStack(source.getBucket()).getHoverName();
        }
        return Component.literal(BuiltInRegistries.FLUID.getKey(source).toString());
    }

    public static boolean hasBucket(Fluid fluid) {
        return fluid != Fluids.EMPTY && source(fluid).getBucket() != Items.AIR;
    }

    public static Fluid fromBucket(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
            return source(bucket.getContent());
        }
        return Fluids.EMPTY;
    }

    public static ItemStack filledBucket(Fluid fluid) {
        return hasBucket(fluid) ? new ItemStack(source(fluid).getBucket()) : new ItemStack(Items.BUCKET);
    }
}
