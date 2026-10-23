/**
 * File: AvaliNexusNeoForgeClient.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.neoforge;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.ModKeyCategory;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.client.block.ModBlockTints;
import org.furranystudio.avalinexus.client.cushion.AvaliCushionRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.client.fluid.AvaliFluidModels;
import org.furranystudio.avalinexus.client.heater.HeaterScreen;
import org.furranystudio.avalinexus.client.loom.AvaliLoomScreen;
import org.furranystudio.avalinexus.client.machine.AmmoniaRefineryScreen;
import org.furranystudio.avalinexus.client.machine.CryoFreezerScreen;
import org.furranystudio.avalinexus.client.machine.GraphenePressScreen;
import org.furranystudio.avalinexus.client.machine.WeaponWorkbenchScreen;
import org.furranystudio.avalinexus.client.reputation.ReputationClient;
import org.furranystudio.avalinexus.client.settings.AvaliSettingsScreen;
import org.furranystudio.avalinexus.client.tank.GrapheneTankRenderer;
import org.furranystudio.avalinexus.client.tapestry.AvaliTapestryRenderer;
import org.furranystudio.avalinexus.client.weapon.NexiteQuillRenderer;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.furranystudio.avalinexus.client.weapon.RailGunHud;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.inventory.ModMenus;

public final class AvaliNexusNeoForgeClient {

    private AvaliNexusNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        AvaliNexusClient.init();
        modEventBus.addListener((RegisterFluidModelsEvent event) -> {
            for (AvaliFluidKind kind : AvaliFluidKind.values()) {
                event.register(AvaliFluidModels.model(kind), ModFluids.source(kind), ModFluids.flowing(kind));
            }
        });
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new AvaliSettingsScreen(parent));
        modEventBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(ModMenus.HEATER.get(), HeaterScreen::new);
            event.register(ModMenus.AVALI_LOOM.get(), AvaliLoomScreen::new);
            event.register(ModMenus.AMMONIA_REFINERY.get(), AmmoniaRefineryScreen::new);
            event.register(ModMenus.CRYO_FREEZER.get(), CryoFreezerScreen::new);
            event.register(ModMenus.GRAPHENE_PRESS.get(), GraphenePressScreen::new);
            event.register(ModMenus.WEAPON_WORKBENCH.get(), WeaponWorkbenchScreen::new);
        });
        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(AvaliNexus.id("dialogue"), DialogueHud::render));
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(ModKeyCategory.AVALI_NEXUS);
            event.register(DialogueKeys.CURSOR);
            event.register(RailGunClient.RELOAD);
            event.register(ReputationClient.OPEN);
        });
        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(AvaliNexus.id("rail_gun"), RailGunHud::render));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> RailGunClient.tick());
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> ReputationClient.tick());
        NeoForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered event) -> {
            if (event.isAttack() && RailGunClient.blocksAttack()) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((ComputeFovModifierEvent event) ->
            event.setNewFovModifier(RailGunClient.fovModifier(event.getPlayer(), event.getNewFovModifier())));
        modEventBus.addListener((RegisterColorHandlersEvent.BlockTintSources event) -> ModBlockTints.register(event::register));
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
            event.registerEntityRenderer(ModEntities.NEXITE_QUILL.get(), NexiteQuillRenderer::new);
            event.registerEntityRenderer(ModEntities.AVALI_EGG.get(), ThrownItemRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.GRAPHENE_TANK.get(), GrapheneTankRenderer::new);
        });
    }
}
