/**
 * File: AvaliNexusNeoForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.neoforge;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.MonsterTargeting;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;

@Mod(AvaliNexus.MODID)
public class AvaliNexusNeoForge {

    public AvaliNexusNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        Platform.init(FMLPaths.GAMEDIR.get(), CreativeModeTab::builder);
        Config.registerSettings();

        AvaliNexus.registerContent();
        NeoForgeNetwork.initSender();
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> NeoForgeNetwork.register(event));
        modEventBus.addListener((RegisterEvent event) -> ModRegistry.registerAll(event::register));
        modEventBus.addListener((EntityAttributeCreationEvent event) -> ModEntities.registerAttributes(event::put));
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> ModCreativeTabs.addToVanillaTab(event.getTabKey(), event));

        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if (!event.getLevel().isClientSide()) {
                MonsterTargeting.onEntityJoin(event.getEntity());
            }
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            AvaliNexusClient.init();
            modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(ModEntities.AVALI.get(), AvaliRenderer::new));
        }

        modEventBus.addListener((FMLCommonSetupEvent event) -> AvaliNexus.commonSetup());
    }
}
