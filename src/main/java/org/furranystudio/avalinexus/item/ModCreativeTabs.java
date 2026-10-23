/**
 * File: ModCreativeTabs.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.effect.ModEffects;
import org.furranystudio.avalinexus.block.AerogelBlocks;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModCreativeTabs {

    public static final RegistryEntry<CreativeModeTab> AVALI_NEXUS = ModRegistry.register(Registries.CREATIVE_MODE_TAB, "avalinexus",
        key -> Platform.creativeTabBuilder()
            .title(Component.translatable("itemGroup.avalinexus"))
            .icon(() -> new ItemStack(ModItems.ICON.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.AVALI_SPAWN_EGG.get());
                output.accept(ModItems.NEXITE_SHARD.get());
                output.accept(ModItems.NEXITE_INGOT.get());
                output.accept(ModItems.NEXITE_NUGGET.get());
                output.accept(ModItems.GRAPHENE.get());
                output.accept(ModItems.AVALI_FEATHER.get());
                output.accept(ModItems.BOND_FEATHER.get());
                output.accept(ModItems.NEST_RATION.get());
                output.accept(ModItems.AVALI_EGG.get());
                output.accept(ModItems.PIRU_SPORE.get());
                output.accept(ModItems.PIRU_FROND.get());
                output.accept(ModItems.PIRU_FLOUR.get());
                output.accept(ModItems.PIRU_BREAD.get());
                output.accept(ModItems.NAKATI_CYST.get());
                output.accept(ModItems.NAKATI_BARK.get());
                output.accept(ModItems.KIRI_FRUIT.get());
                output.accept(ModItems.KIRI_DESSERT.get());
                output.accept(ModItems.SPICED_STEW.get());
                output.accept(PotionContents.createItemStack(Items.POTION, ModEffects.tonicPotion()));
                output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModEffects.tonicPotion()));
                output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModEffects.tonicPotion()));
                output.accept(ModItems.NEXITE_ORE.get());
                output.accept(ModItems.DEEPSLATE_NEXITE_ORE.get());
                output.accept(ModItems.NEXITE_BLOCK.get());
                for (org.furranystudio.avalinexus.fluid.AvaliFluidKind kind : org.furranystudio.avalinexus.fluid.AvaliFluidKind.values()) {
                    output.accept(org.furranystudio.avalinexus.fluid.ModFluids.bucket(kind));
                }
                NexiteGear.ALL.forEach(item -> output.accept(item.get()));
                org.furranystudio.avalinexus.item.weapon.AerotechWeapons.ALL.forEach(item -> output.accept(item.get()));
                org.furranystudio.avalinexus.item.weapon.RailWeapons.ALL.forEach(item -> output.accept(item.get()));
                output.accept(ModItems.GRAPHENE_BLOCK.get());
                output.accept(ModItems.GRAPHENE_SLAB.get());
                output.accept(ModItems.GRAPHENE_STAIRS.get());
                output.accept(ModItems.GRAPHENE_WALL.get());
                output.accept(ModItems.CUT_GRAPHENE_BLOCK.get());
                output.accept(ModItems.CUT_GRAPHENE_SLAB.get());
                output.accept(ModItems.CUT_GRAPHENE_STAIRS.get());
                output.accept(ModItems.CUT_GRAPHENE_WALL.get());
                output.accept(ModItems.GRAPHENE_BRICKS.get());
                output.accept(ModItems.GRAPHENE_BRICK_SLAB.get());
                output.accept(ModItems.GRAPHENE_BRICK_STAIRS.get());
                output.accept(ModItems.GRAPHENE_BRICK_WALL.get());
                output.accept(ModItems.CHISELED_GRAPHENE_BLOCK.get());
                output.accept(ModItems.GRAPHENE_PILLAR.get());
                output.accept(ModItems.GRAPHENE_DOOR.get());
                output.accept(ModItems.GRAPHENE_TRAPDOOR.get());
                output.accept(ModItems.GRAPHENE_VENT.get());
                output.accept(ModItems.GRAPHENE_PIPE.get());
                output.accept(ModItems.GRAPHENE_TANK.get());
                output.accept(ModItems.FLUID_PUMP.get());
                output.accept(ModItems.FLUID_TAP.get());
                output.accept(ModItems.AMMONIA_REFINERY.get());
                output.accept(ModItems.CRYO_FREEZER.get());
                output.accept(ModItems.GRAPHENE_PRESS.get());
                output.accept(ModItems.WEAPON_WORKBENCH.get());
                output.accept(ModItems.ICE_BRICKS.get());
                output.accept(ModItems.ICE_BRICK_SLAB.get());
                output.accept(ModItems.ICE_BRICK_STAIRS.get());
                output.accept(ModItems.ICE_BRICK_WALL.get());
                output.accept(ModItems.PACKED_SNOW.get());
                output.accept(ModItems.PACKED_SNOW_SLAB.get());
                output.accept(ModItems.PACKED_SNOW_STAIRS.get());
                output.accept(ModItems.PACKED_SNOW_WALL.get());
                output.accept(ModItems.CUT_PACKED_SNOW.get());
                output.accept(ModItems.CUT_PACKED_SNOW_SLAB.get());
                output.accept(ModItems.CUT_PACKED_SNOW_STAIRS.get());
                output.accept(ModItems.CUT_PACKED_SNOW_WALL.get());
                output.accept(ModItems.CHISELED_PACKED_SNOW.get());
                output.accept(ModItems.AMMONIA_ICE.get());
                output.accept(ModItems.PACKED_AMMONIA_ICE.get());
                output.accept(ModItems.NANOFIBRE.get());
                output.accept(ModItems.NANOFIBRE_WALL.get());
                output.accept(ModItems.AVALI_LAMP.get());
                output.accept(ModItems.AVALI_LIGHT_STRIP.get());
                output.accept(ModItems.CRYSTAL_POT.get());
                output.accept(ModItems.HEATER.get());
                output.accept(ModItems.TERMINAL.get());
                output.accept(ModItems.AVALI_LOOM.get());
                output.accept(ModItems.AEROGEL.get());
                output.accept(ModItems.AEROGEL_PANE.get());
                output.accept(ModItems.AEROGEL_SLAB.get());
                output.accept(ModItems.AEROGEL_STAIRS.get());
                output.accept(ModItems.AEROGEL_WALL.get());
                output.accept(ModItems.GLOWING_AEROGEL.get());
                for (AerogelBlocks.Shape shape : AerogelBlocks.Shape.values()) {
                    for (DyeColor color : DyeColor.values()) {
                        output.accept(AerogelBlocks.item(shape, color));
                    }
                }
                for (NanocanvasBlocks.Shape shape : NanocanvasBlocks.Shape.values()) {
                    for (DyeColor color : DyeColor.values()) {
                        output.accept(NanocanvasBlocks.item(shape, color));
                    }
                }
                for (TapestryPattern pattern : TapestryPattern.values()) {
                    for (DyeColor color : DyeColor.values()) {
                        output.accept(TapestryItems.get(pattern, color));
                    }
                }
                output.accept(ModItems.AVALI_BED.get());
                output.accept(ModItems.AVALI_CUSHION.get());
                output.accept(ModItems.AVALI_CARPET.get());
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void init() {
    }

    public static void addToVanillaTab(ResourceKey<CreativeModeTab> tab, CreativeModeTab.Output output) {
        if (tab == CreativeModeTabs.SPAWN_EGGS) {
            output.accept(ModItems.AVALI_SPAWN_EGG.get());
        }
    }
}
