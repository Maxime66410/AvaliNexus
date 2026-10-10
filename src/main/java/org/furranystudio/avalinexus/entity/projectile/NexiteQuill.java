/**
 * File: NexiteQuill.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.entity.projectile;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.furranystudio.avalinexus.sound.ModSounds;

public class NexiteQuill extends ThrowableItemProjectile {

    private static final int LIFETIME = 100;
    private static final DustParticleOptions TRAIL = new DustParticleOptions(0xFF8C1A, 0.6F);
    private static final double TRAIL_SPACING = 0.4;

    private float damage = 4.0F;

    public NexiteQuill(EntityType<? extends NexiteQuill> type, Level level) {
        super(type, level);
    }

    public NexiteQuill(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.NEXITE_QUILL.get(), shooter, level, new ItemStack(RailWeapons.NEXITE_QUILL.get()));
        this.damage = damage;
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
        // Vanilla only finds hits where the path enters a hitbox, point blank shots start inside it
        if (tickCount == 0 && !level().isClientSide()) {
            for (Entity target : level().getEntities(this, getBoundingBox(), this::canHitEntity)) {
                if (target.getBoundingBox().contains(position())) {
                    onHit(new EntityHitResult(target));
                    return;
                }
            }
        }
        super.tick();
        if (level().isClientSide()) {
            spawnTrail();
        }
        if (!level().isClientSide() && tickCount > LIFETIME) {
            discard();
        }
    }

    private void spawnTrail() {
        if (tickCount < 2) {
            return;
        }
        double dx = getX() - xo;
        double dy = getY() - yo;
        double dz = getZ() - zo;
        int steps = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) / TRAIL_SPACING));
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;
            level().addParticle(TRAIL, xo + dx * t, yo + dy * t, zo + dz * t, 0.0, 0.0, 0.0);
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
        // In 26.3 the hit cooldown is damageCooldownTime, rapid fire and pellets would lose most hits to it
        if (target instanceof LivingEntity living) {
            living.damageCooldownTime = 0;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 4, 0.05, 0.05, 0.05, 0.1);
            ModSounds.play(level, this, ModSounds.NEXITE_QUILL_IMPACT.get(), 0.5F, 0.12F);
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
