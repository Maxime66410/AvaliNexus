/**
 * File: AvaliEgg.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.avali.AvaliBreeding;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.item.ModItems;

public class AvaliEgg extends ThrowableItemProjectile {

    private static final byte BREAK_EVENT = 3;
    private static final int BREAK_PARTICLES = 8;

    public AvaliEgg(EntityType<? extends AvaliEgg> type, Level level) {
        super(type, level);
    }

    public AvaliEgg(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.AVALI_EGG.get(), owner, level, stack);
    }

    public AvaliEgg(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.AVALI_EGG.get(), x, y, z, level, stack);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event != BREAK_EVENT || getItem().isEmpty()) {
            super.handleEntityEvent(event);
            return;
        }
        ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(getItem()));
        for (int i = 0; i < BREAK_PARTICLES; i++) {
            level().addParticle(particle, getX(), getY(), getZ(),
                (random.nextFloat() - 0.5) * 0.08, (random.nextFloat() - 0.5) * 0.08, (random.nextFloat() - 0.5) * 0.08);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            if (random.nextDouble() < AvaliBreeding.get(level.getServer()).egg().hatchChance()) {
                AvaliEntity.hatch(level, position(), getYRot());
            }
            level.broadcastEntityEvent(this, BREAK_EVENT);
            discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AVALI_EGG.get();
    }
}
