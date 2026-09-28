/**
 * File: ModFluids.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.EnumMap;
import java.util.Map;

public final class ModFluids {

    private static final Map<AvaliFluidKind, RegistryEntry<AvaliFluid>> SOURCES = new EnumMap<>(AvaliFluidKind.class);
    private static final Map<AvaliFluidKind, RegistryEntry<AvaliFluid>> FLOWING = new EnumMap<>(AvaliFluidKind.class);
    private static final Map<AvaliFluidKind, RegistryEntry<Block>> BLOCKS = new EnumMap<>(AvaliFluidKind.class);
    private static final Map<AvaliFluidKind, RegistryEntry<BucketItem>> BUCKETS = new EnumMap<>(AvaliFluidKind.class);

    static {
        for (AvaliFluidKind kind : AvaliFluidKind.values()) {
            String name = kind.fluidName();
            SOURCES.put(kind, ModRegistry.register(Registries.FLUID, name, key -> Platform.createFluid(kind, true)));
            FLOWING.put(kind, ModRegistry.register(Registries.FLUID, "flowing_" + name, key -> Platform.createFluid(kind, false)));
            BLOCKS.put(kind, ModRegistry.register(Registries.BLOCK, name, key -> new AvaliLiquidBlock(source(kind), kind, liquid(kind, key))));
            BUCKETS.put(kind, ModRegistry.register(Registries.ITEM, name + "_bucket", key -> new BucketItem(source(kind), bucket(kind, key))));
        }
    }

    private ModFluids() {
    }

    public static void init() {
    }

    public static AvaliFluid source(AvaliFluidKind kind) {
        return SOURCES.get(kind).get();
    }

    public static AvaliFluid flowing(AvaliFluidKind kind) {
        return FLOWING.get(kind).get();
    }

    public static Block block(AvaliFluidKind kind) {
        return BLOCKS.get(kind).get();
    }

    public static Item bucket(AvaliFluidKind kind) {
        return BUCKETS.get(kind).get();
    }

    // Water block settings, plus the glow of the fluid
    private static BlockBehaviour.Properties liquid(AvaliFluidKind kind, ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
            .setId(key)
            .mapColor(kind == AvaliFluidKind.FUEL ? MapColor.COLOR_ORANGE : MapColor.ICE)
            .replaceable()
            .noCollision()
            .strength(100.0F)
            .pushReaction(PushReaction.POPPED)
            .noLootTable()
            .liquid()
            .sound(SoundType.EMPTY)
            .lightLevel(state -> kind.lightLevel());
    }

    private static Item.Properties bucket(AvaliFluidKind kind, ResourceKey<Item> key) {
        Item.Properties properties = new Item.Properties().setId(key).stacksTo(1);
        // Ammonia is refined into the other fluids, giving its bucket back would duplicate buckets
        // the others keep it so a furnace or a heater hands the empty bucket back
        if (kind != AvaliFluidKind.AMMONIA) {
            properties.craftRemainder(Items.BUCKET);
        }
        // Burns 1.6 times as long as a lava bucket in a furnace or a heater
        if (kind == AvaliFluidKind.FUEL) {
            properties.cookingFuel(ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, AvaliNexus.id("cooking/time_fuel_bucket")));
        }
        return properties;
    }
}
