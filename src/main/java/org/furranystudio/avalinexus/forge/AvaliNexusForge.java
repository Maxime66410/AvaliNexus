/**
 * File: AvaliNexusForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.forge;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.MonsterTargeting;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;

import java.util.function.Predicate;

@Mod(AvaliNexus.MODID)
public class AvaliNexusForge {

    public AvaliNexusForge(FMLJavaModLoadingContext context) {
        Platform.init(FMLPaths.GAMEDIR.get(), CreativeModeTab::builder);
        Config.registerSettings();

        AvaliNexus.registerContent();
        ForgeNetwork.register();
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> ModRegistry.registerAll(event::register));
        EntityAttributeCreationEvent.BUS.addListener(event -> ModEntities.registerAttributes(event::put));
        BuildCreativeModeTabContentsEvent.BUS.addListener(event -> ModCreativeTabs.addToVanillaTab(event.getTabKey(), event));

        EntityJoinLevelEvent.BUS.addListener(event -> {
            if (!event.getLevel().isClientSide()) {
                MonsterTargeting.onEntityJoin(event.getEntity());
            }
        });

        RegisterCommandsEvent.BUS.addListener(event -> AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            AvaliNexusClient.init();
            AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(AvaliNexus.id("dialogue"), DialogueHud::render));
            RegisterKeyMappingsEvent.BUS.addListener(event -> event.register(DialogueKeys.CURSOR));
            TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> ClientDialogue.tick());
            InputEvent.MouseScrollingEvent.BUS.addListener((Predicate<InputEvent.MouseScrollingEvent>) event ->
                ClientDialogue.onScroll(event.getDeltaY()));
            EntityRenderersEvent.RegisterRenderers.BUS.addListener(event ->
                event.registerEntityRenderer(ModEntities.AVALI.get(), AvaliRenderer::new));
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> AvaliNexus.commonSetup());
    }
}
