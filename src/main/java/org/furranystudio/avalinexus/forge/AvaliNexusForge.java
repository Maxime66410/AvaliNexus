/**
 * File: AvaliNexusForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.forge;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.registry.ModRegistry;

@Mod(AvaliNexus.MODID)
public class AvaliNexusForge {

    public AvaliNexusForge(FMLJavaModLoadingContext context) {
        Platform.init(FMLPaths.GAMEDIR.get());
        Config.registerSettings();

        AvaliNexus.registerContent();
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> ModRegistry.registerAll(event::register));

        RegisterCommandsEvent.BUS.addListener(event -> AvaliNexusCommand.register(event.getDispatcher()));

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> AvaliNexus.commonSetup());
    }
}
