/**
 * File: AvaliNexusFabric.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;

import java.util.function.Supplier;

public final class AvaliNexusFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.init(FabricLoader.getInstance().getGameDir(), FabricCreativeModeTab::builder);
        Config.registerSettings();

        AvaliNexus.registerContent();
        ModRegistry.registerAll(AvaliNexusFabric::registerVanilla);
        ModEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output ->
            ModCreativeTabs.addToVanillaTab(CreativeModeTabs.SPAWN_EGGS, output));

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            AvaliNexusCommand.register(dispatcher));

        AvaliNexus.commonSetup();
    }

    // Fabric has no RegisterEvent, registries are still open here
    @SuppressWarnings("unchecked")
    private static <T> void registerVanilla(ResourceKey<? extends Registry<T>> registryKey, Identifier id, Supplier<T> value) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
        if (registry == null) {
            throw new IllegalStateException("Unknown registry " + registryKey.identifier() + " for " + id);
        }
        Registry.register(registry, id, value.get());
    }
}
