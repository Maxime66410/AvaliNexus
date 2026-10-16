/**
 * File: ModBlockEntities.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.furranystudio.avalinexus.block.freezer.CryoFreezerBlockEntity;
import org.furranystudio.avalinexus.block.press.GraphenePressBlockEntity;
import org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchBlockEntity;
import org.furranystudio.avalinexus.block.heater.HeaterBlockEntity;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
import org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryBlockEntity;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlockEntity;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.Set;

public final class ModBlockEntities {

    public static final RegistryEntry<BlockEntityType<HeaterBlockEntity>> HEATER = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "heater",
        key -> new BlockEntityType<>(HeaterBlockEntity::new, Set.of(ModBlocks.HEATER.get())));

    public static final RegistryEntry<BlockEntityType<GrapheneTankBlockEntity>> GRAPHENE_TANK = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "graphene_tank",
        key -> new BlockEntityType<>(GrapheneTankBlockEntity::new, Set.of(ModBlocks.GRAPHENE_TANK.get())));

    public static final RegistryEntry<BlockEntityType<GraphenePipeBlockEntity>> GRAPHENE_PIPE = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "graphene_pipe",
        key -> new BlockEntityType<>(GraphenePipeBlockEntity::new, Set.of(ModBlocks.GRAPHENE_PIPE.get())));

    public static final RegistryEntry<BlockEntityType<AmmoniaRefineryBlockEntity>> AMMONIA_REFINERY = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "ammonia_refinery",
        key -> new BlockEntityType<>(AmmoniaRefineryBlockEntity::new, Set.of(ModBlocks.AMMONIA_REFINERY.get())));

    public static final RegistryEntry<BlockEntityType<CryoFreezerBlockEntity>> CRYO_FREEZER = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "cryo_freezer",
        key -> new BlockEntityType<>(CryoFreezerBlockEntity::new, Set.of(ModBlocks.CRYO_FREEZER.get())));
    public static final RegistryEntry<BlockEntityType<GraphenePressBlockEntity>> GRAPHENE_PRESS = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "graphene_press",
        key -> new BlockEntityType<>(GraphenePressBlockEntity::new, Set.of(ModBlocks.GRAPHENE_PRESS.get())));
    public static final RegistryEntry<BlockEntityType<WeaponWorkbenchBlockEntity>> WEAPON_WORKBENCH = ModRegistry.register(Registries.BLOCK_ENTITY_TYPE, "weapon_workbench",
        key -> new BlockEntityType<>(WeaponWorkbenchBlockEntity::new, Set.of(ModBlocks.WEAPON_WORKBENCH.get())));

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
