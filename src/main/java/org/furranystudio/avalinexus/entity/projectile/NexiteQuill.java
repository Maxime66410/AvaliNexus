/**
 * File: NexiteQuill.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.entity.projectile;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.item.weapon.RailWeapons;

// A rail gun dart: flies almost straight, hits for a fixed damage and breaks on impact
public class NexiteQuill extends ThrowableItemProjectile {

    private static final int LIFETIME = 100;

    private float damage = 4.0F;

    public NexiteQuill(EntityType<? extends NexiteQuill> type, Level level) {
        super(type, level);
    }

    public NexiteQuill(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.NEXITE_QUILL.get(), shooter, level, new ItemStack(RailWeapons.NEXITE_QUILL.get()));
        this.damage = damage;
        // In flight it shows the 3D quill, the item keeps its 2D icon
        ItemStack flying = getItem().copy();
        flying.set(DataComponents.ITEM_MODEL, AvaliNexus.id("nexite_quill_projectile"));
        setItem(flying);
    }

    @Override
    protected Item getDefaultItem() {
        return RailWeapons.NEXITE_QUILL.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.005;
    }

    @Override
    public void tick() {
        // Vanilla only finds a hit where the path enters a hitbox, a dart fired from inside one point blank would fly through
        if (tickCount == 0 && !level().isClientSide()) {
            for (Entity target : level().getEntities(this, getBoundingBox(), this::canHitEntity)) {
                if (target.getBoundingBox().contains(position())) {
                    onHit(new EntityHitResult(target));
                    return;
                }
            }
        }
        super.tick();
        if (!level().isClientSide() && tickCount > LIFETIME) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = result.getEntity();
        Entity owner = getOwner();
        DamageSource source = owner instanceof LivingEntity shooter
            ? damageSources().mobProjectile(this, shooter)
            : damageSources().thrown(this, owner);
        target.hurtServer(level, source, damage);
        // Rapid fire and the shotgun pellets would lose most hits to the damage cooldown
        if (target instanceof LivingEntity living) {
            living.damageCooldownTime = 0;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.05, 0.05, 0.05, 0.1);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        damage = input.getFloatOr("Damage", 4.0F);
    }
}
