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
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.function.BiConsumer;

public final class ModEntities {

    public static final RegistryEntry<EntityType<AvaliEntity>> AVALI = ModRegistry.register(Registries.ENTITY_TYPE, "avali",
        key -> EntityType.Builder.of(AvaliEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.8F)
            .eyeHeight(1.62F)
            .ridingOffset(-0.75F)
            .build(key));

    private ModEntities() {
    }

    public static void init() {
    }

    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> sink) {
        sink.accept(AVALI.get(), AvaliEntity.createAttributes().build());
    }
}
