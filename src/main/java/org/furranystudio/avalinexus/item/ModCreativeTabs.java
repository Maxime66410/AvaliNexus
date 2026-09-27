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
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.Platform;
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
                output.accept(ModItems.NEXITE_ORE.get());
                output.accept(ModItems.DEEPSLATE_NEXITE_ORE.get());
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
