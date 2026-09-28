/**
 * File: AvaliNexusNeoForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.neoforge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.client.heater.HeaterScreen;
import org.furranystudio.avalinexus.client.settings.AvaliSettingsScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.furranystudio.avalinexus.inventory.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.furranystudio.avalinexus.client.block.NanocanvasTints;
import org.furranystudio.avalinexus.client.cushion.AvaliCushionRenderer;
import org.furranystudio.avalinexus.client.tapestry.AvaliTapestryRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ColdExposure;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.MonsterTargeting;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

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
        modEventBus.addListener((RegisterSpawnPlacementsEvent event) -> ModEntities.registerSpawnPlacements(new ModEntities.SpawnSink() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap,
                                                 SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        }));
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> ModCreativeTabs.addToVanillaTab(event.getTabKey(), event));

        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) -> {
            if (!event.getLevel().isClientSide()) {
                MonsterTargeting.onEntityJoin(event.getEntity());
            }
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> ColdExposure.tick(event.getServer()));

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            AvaliNexusClient.init();
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new AvaliSettingsScreen(parent));
            modEventBus.addListener((RegisterMenuScreensEvent event) -> event.register(ModMenus.HEATER.get(), HeaterScreen::new));
            modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(AvaliNexus.id("dialogue"), DialogueHud::render));
            modEventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(DialogueKeys.CURSOR));
            modEventBus.addListener((RegisterColorHandlersEvent.BlockTintSources event) -> NanocanvasTints.register(event::register));
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> ClientDialogue.tick());
            NeoForge.EVENT_BUS.addListener((InputEvent.MouseScrollingEvent event) -> {
                if (ClientDialogue.onScroll(event.getScrollDeltaY())) {
                    event.setCanceled(true);
                }
            });
            modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
                event.registerEntityRenderer(ModEntities.AVALI.get(), AvaliRenderer::new);
                event.registerEntityRenderer(ModEntities.AVALI_CUSHION.get(), AvaliCushionRenderer::new);
                event.registerEntityRenderer(ModEntities.AVALI_TAPESTRY.get(), AvaliTapestryRenderer::new);
            });
        }

        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            AvaliNexus.commonSetup();
            event.enqueueWork(ModBiomes::registerRegions);
        });
    }
}
