/**
 * File: ModEntities.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.cushion.AvaliCushion;
import org.furranystudio.avalinexus.entity.tapestry.AvaliTapestry;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.function.BiConsumer;

public final class ModEntities {

    // Matches the spawn placement events on Forge and NeoForge
    public interface SpawnSink {
        <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap,
                                      SpawnPlacements.SpawnPredicate<T> predicate);
    }

    // Vanilla refuses thick snow layers as ground, the polar taiga is covered in them
    private static final SpawnPlacementType ON_GROUND_OR_SNOW = (level, pos, type) ->
        SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
            || (level.getBlockState(pos.below()).is(Blocks.SNOW)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty());

    public static final RegistryEntry<EntityType<AvaliEntity>> AVALI = ModRegistry.register(Registries.ENTITY_TYPE, "avali",
        key -> EntityType.Builder.of(AvaliEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.8F)
            .eyeHeight(1.62F)
            .ridingOffset(-0.75F)
            .build(key));

    // Same settings as the vanilla cushion
    public static final RegistryEntry<EntityType<Cushion>> AVALI_CUSHION = ModRegistry.register(Registries.ENTITY_TYPE, "avali_cushion",
        key -> EntityType.Builder.<Cushion>of(AvaliCushion::new, MobCategory.MISC)
            .noLootTable()
            .sized(1.0F, 0.25F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .dontTrackDeltas()
            .build(key));

    // Same settings as the item frame
    public static final RegistryEntry<EntityType<AvaliTapestry>> AVALI_TAPESTRY = ModRegistry.register(Registries.ENTITY_TYPE, "avali_tapestry",
        key -> EntityType.Builder.<AvaliTapestry>of(AvaliTapestry::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .build(key));

    private ModEntities() {
    }

    public static void init() {
    }

    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> sink) {
        sink.accept(AVALI.get(), AvaliEntity.createAttributes().build());
    }

    public static void registerSpawnPlacements(SpawnSink sink) {
        sink.register(AVALI.get(), ON_GROUND_OR_SNOW, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AvaliEntity::checkAvaliSpawnRules);
    }
}
