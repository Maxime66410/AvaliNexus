/**
 * File: AvaliNexusNeoForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-27
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
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ColdExposure;
import org.furranystudio.avalinexus.entity.avali.AvaliMusicPlayer;
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
        Config.load();

        NeoForgeFluids.init();
        AvaliNexus.registerContent();
        NeoForgeNetwork.initSender();
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> NeoForgeNetwork.register(event));
        modEventBus.addListener((RegisterEvent event) -> {
            NeoForgeFluids.register(event);
            ModRegistry.registerAll(event::register);
        });
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
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> AvaliMusicPlayer.tick(event.getServer()));

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            AvaliNexusNeoForgeClient.init(modEventBus, modContainer);
        }

        modEventBus.addListener((FMLCommonSetupEvent event) -> {
            AvaliNexus.commonSetup();
            event.enqueueWork(ModBiomes::registerRegions);
            event.enqueueWork(AvaliNexus::registerDispenseBehaviors);
        });
    }
}
