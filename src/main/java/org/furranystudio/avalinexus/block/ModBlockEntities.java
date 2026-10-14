/**
 * File: ModBlockEntities.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.furranystudio.avalinexus.block.heater.HeaterBlockEntity;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
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

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
