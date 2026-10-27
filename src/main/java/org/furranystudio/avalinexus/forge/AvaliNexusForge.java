/**
 * File: AvaliNexusForge.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
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
import org.furranystudio.avalinexus.command.AvaliNexusCommand;
import org.furranystudio.avalinexus.entity.ColdExposure;
import org.furranystudio.avalinexus.entity.avali.AvaliMusicPlayer;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.MonsterTargeting;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

@Mod(AvaliNexus.MODID)
public class AvaliNexusForge {

    public AvaliNexusForge(FMLJavaModLoadingContext context) {
        Platform.init(FMLPaths.GAMEDIR.get(), CreativeModeTab::builder);
        Config.registerSettings();
        Config.load();

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
        TickEvent.ServerTickEvent.Post.BUS.addListener(event -> AvaliMusicPlayer.tick(event.server()));

        RegisterCommandsEvent.BUS.addListener(event -> AvaliNexusCommand.register(event.getDispatcher()));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            AvaliNexusForgeClient.init(context);
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(event -> {
            AvaliNexus.commonSetup();
            event.enqueueWork(ModBiomes::registerRegions);
            event.enqueueWork(AvaliNexus::registerDispenseBehaviors);
        });
    }
}
