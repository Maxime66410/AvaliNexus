/**
 * File: ModCreativeTabs.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.Platform;
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
                output.accept(ModItems.GRAPHENE.get());
                output.accept(ModItems.NEXITE_ORE.get());
                output.accept(ModItems.DEEPSLATE_NEXITE_ORE.get());
                output.accept(ModItems.NEXITE_BLOCK.get());
                output.accept(ModItems.GRAPHENE_BLOCK.get());
                output.accept(ModItems.GRAPHENE_SLAB.get());
                output.accept(ModItems.GRAPHENE_STAIRS.get());
                output.accept(ModItems.GRAPHENE_WALL.get());
                output.accept(ModItems.CUT_GRAPHENE_BLOCK.get());
                output.accept(ModItems.CUT_GRAPHENE_SLAB.get());
                output.accept(ModItems.CUT_GRAPHENE_STAIRS.get());
                output.accept(ModItems.CUT_GRAPHENE_WALL.get());
                output.accept(ModItems.CHISELED_GRAPHENE_BLOCK.get());
                output.accept(ModItems.NANOFIBRE.get());
                output.accept(ModItems.NANOFIBRE_WALL.get());
                output.accept(ModItems.AVALI_LAMP.get());
                output.accept(ModItems.CRYSTAL_POT.get());
                output.accept(ModItems.HEATER.get());
                output.accept(ModItems.AEROGEL.get());
                output.accept(ModItems.AEROGEL_PANE.get());
                output.accept(ModItems.AEROGEL_SLAB.get());
                output.accept(ModItems.AEROGEL_STAIRS.get());
                output.accept(ModItems.AEROGEL_WALL.get());
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
