/**
 * File: AvaliNexusForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
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
import org.furranystudio.avalinexus.client.heater.HeaterScreen;
import org.furranystudio.avalinexus.client.settings.AvaliSettingsScreen;
import org.furranystudio.avalinexus.inventory.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.furranystudio.avalinexus.client.block.NanocanvasTints;
import org.furranystudio.avalinexus.client.cushion.AvaliCushionRenderer;
import org.furranystudio.avalinexus.client.tapestry.AvaliTapestryRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.furranystudio.avalinexus.client.weapon.RailGunHud;
import org.furranystudio.avalinexus.client.weapon.NexiteQuillRenderer;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ColdExposure;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.MonsterTargeting;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.client.fluid.AvaliFluidModels;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;
import org.furranystudio.avalinexus.fluid.ModFluids;
import net.minecraftforge.client.event.ModelEvent;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

import java.util.function.Predicate;

@Mod(AvaliNexus.MODID)
public class AvaliNexusForge {

    public AvaliNexusForge(FMLJavaModLoadingContext context) {
        Platform.init(FMLPaths.GAMEDIR.get(), CreativeModeTab::builder);
        Config.registerSettings();

        ForgeFluids.init();
        AvaliNexus.registerContent();
        ForgeNetwork.register();
        RegisterEvent.getBus(context.getModBusGroup()).addListener(event -> {
            ForgeFluids.register(event);
            ModRegistry.registerAll(event::register);
        });
        EntityAttributeCreationEvent.BUS.addListener(event -> ModEntities.registerAttributes(event::put));
        SpawnPlacementRegisterEvent.BUS.addListener(event -> ModEntities.registerSpawnPlacements(new ModEntities.SpawnSink() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap,
                                                 SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
            }
        }));
        BuildCreativeModeTabContentsEvent.BUS.addListener(event -> ModCreativeTabs.addToVanillaTab(event.getTabKey(), event));

        EntityJoinLevelEvent.BUS.addListener(event -> {
            if (!event.getLevel().isClientSide()) {
                MonsterTargeting.onEntityJoin(event.getEntity());
            }
        });

        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> ColdExposure.tick(event.server()));

        RegisterCommandsEvent.BUS.addListener(event -> AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            AvaliNexusClient.init();
            ModelEvent.BakeFluidModels.BUS.addListener(event -> {
                for (AvaliFluidKind kind : AvaliFluidKind.values()) {
                    var model = AvaliFluidModels.model(kind).bake(event.materials(), () -> AvaliNexus.id(kind.fluidName()).toString());
                    event.register(ModFluids.source(kind), model);
                    event.register(ModFluids.flowing(kind), model);
                }
            });
            context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(AvaliSettingsScreen::new));
            FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event ->
                event.enqueueWork(() -> MenuScreens.register(ModMenus.HEATER.get(), HeaterScreen::new)));
            AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(AvaliNexus.id("dialogue"), DialogueHud::render));
            RegisterKeyMappingsEvent.BUS.addListener(event -> {
                event.register(DialogueKeys.CURSOR);
                event.register(RailGunClient.RELOAD);
            });
            AddGuiOverlayLayersEvent.BUS.addListener(event -> event.getLayeredDraw().add(AvaliNexus.id("rail_gun"), RailGunHud::render));
            TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> RailGunClient.tick());
            InputEvent.InteractionKeyMappingTriggered.Attack.BUS.addListener((Predicate<InputEvent.InteractionKeyMappingTriggered.Attack>) event -> {
                if (RailGunClient.blocksAttack()) {
                    event.setSwingHand(false);
                    return true;
                }
                return false;
            });
            net.minecraftforge.client.event.ComputeFovModifierEvent.BUS.addListener(event ->
                event.setNewFovModifier(RailGunClient.fovModifier(event.getPlayer(), event.getNewFovModifier())));
            RegisterColorHandlersEvent.Block.BUS.addListener(event -> NanocanvasTints.register(event::register));
            TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> ClientDialogue.tick());
            InputEvent.MouseScrollingEvent.BUS.addListener((Predicate<InputEvent.MouseScrollingEvent>) event ->
                ClientDialogue.onScroll(event.getDeltaY()));
            EntityRenderersEvent.RegisterRenderers.BUS.addListener(event -> {
                event.registerEntityRenderer(ModEntities.AVALI.get(), AvaliRenderer::new);
                event.registerEntityRenderer(ModEntities.AVALI_CUSHION.get(), AvaliCushionRenderer::new);
                event.registerEntityRenderer(ModEntities.AVALI_TAPESTRY.get(), AvaliTapestryRenderer::new);
                event.registerEntityRenderer(ModEntities.NEXITE_QUILL.get(), NexiteQuillRenderer::new);
            });
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> {
            AvaliNexus.commonSetup();
            event.enqueueWork(ModBiomes::registerRegions);
        });
    }
}
