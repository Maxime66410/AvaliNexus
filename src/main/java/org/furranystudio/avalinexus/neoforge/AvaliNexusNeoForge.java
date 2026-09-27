/**
 * File: AvaliNexusNeoForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.registry.ModRegistry;

@Mod(AvaliNexus.MODID)
public class AvaliNexusNeoForge {

    public AvaliNexusNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Platform.init(FMLPaths.GAMEDIR.get());
        Config.registerSettings();

        AvaliNexus.registerContent();
        modEventBus.addListener((RegisterEvent event) -> ModRegistry.registerAll(event::register));

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            AvaliNexusCommand.register(event.getDispatcher()));

        modEventBus.addListener((FMLCommonSetupEvent event) -> AvaliNexus.commonSetup());
    }
}
