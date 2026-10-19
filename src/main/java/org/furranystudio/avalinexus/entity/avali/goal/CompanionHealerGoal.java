/**
 * File: CompanionHealerGoal.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.furranystudio.avalinexus.entity.avali.AvaliCompanions;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;
import org.furranystudio.avalinexus.entity.avali.CompanionRole;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class CompanionHealerGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;
    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_FOOD = 20;
    private static final float MAX_SATURATION = 20.0F;
    private static final double SPLASH_SAFE_DISTANCE = 4.5;
    private static final int HEARTS_PER_LEVEL = 2;

    private final AvaliEntity avali;
    private int checkCooldown;
    private long specialReady;
    private long potionReady;
    private long feedReady;
    private long attackReady;

    public CompanionHealerGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        return avali.isCompanion() && avali.getRole() == CompanionRole.HEALER && !avali.isSleeping();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (--checkCooldown > 0 || !(avali.level() instanceof ServerLevel level)) {
            return;
        }
        checkCooldown = CHECK_INTERVAL;
        ServerPlayer owner = avali.getOwner();
        if (owner == null || owner.level() != level || owner.isDeadOrDying()) {
            return;
        }
        AvaliCompanions.Healer healer = AvaliCompanions.get(level.getServer()).healer();
        long now = level.getGameTime();
        boolean ownerInRange = avali.distanceTo(owner) <= healer.range();
        if (ownerInRange && now >= specialReady && owner.getHealth() <= owner.getMaxHealth() * healer.specialBelowHealth()) {
            special(level, owner, healer);
            specialReady = now + (long) healer.specialCooldownSeconds() * TICKS_PER_SECOND;
            return;
        }
        if (now >= potionReady) {
            LivingEntity patient = patient(owner, healer);
            if (patient != null) {
                boolean critical = patient.getHealth() <= patient.getMaxHealth() * healer.instantBelowHealth();
                throwPotion(level, patient, critical ? Potions.HEALING : Potions.REGENERATION);
                potionReady = now + (long) healer.potionCooldownSeconds() * TICKS_PER_SECOND;
                return;
            }
        }
        if (ownerInRange && now >= feedReady && owner.getFoodData().getFoodLevel() <= healer.feedBelowFood() && !healer.feedItems().isEmpty()) {
            avali.getGear().give(new ItemStack(healer.feedItems().get(avali.getRandom().nextInt(healer.feedItems().size()))));
            feedReady = now + (long) healer.feedCooldownSeconds() * TICKS_PER_SECOND;
            return;
        }
        if (now >= attackReady) {
            LivingEntity enemy = enemy(owner, healer);
            if (enemy != null) {
                Holder<Potion> potion = !enemy.hasEffect(MobEffects.WEAKNESS) ? Potions.WEAKNESS
                    : enemy.isInvertedHealAndHarm() ? Potions.HEALING : Potions.HARMING;
                throwPotion(level, enemy, potion);
                attackReady = now + (long) healer.attackCooldownSeconds() * TICKS_PER_SECOND;
            }
        }
    }

    private void special(ServerLevel level, ServerPlayer owner, AvaliCompanions.Healer healer) {
        owner.setHealth(owner.getMaxHealth());
        owner.getFoodData().setFoodLevel(MAX_FOOD);
        owner.getFoodData().setSaturation(MAX_SATURATION);
        int amplifier = Math.max(0, healer.absorptionHearts() / HEARTS_PER_LEVEL - 1);
        owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, healer.absorptionSeconds() * TICKS_PER_SECOND, amplifier));
        level.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + owner.getBbHeight(), owner.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.6F, 1.4F);
        avali.swingForAttack(InteractionHand.MAIN_HAND);
    }

    private LivingEntity patient(ServerPlayer owner, AvaliCompanions.Healer healer) {
        if (needsCare(owner, healer)) {
            return owner;
        }
        return avali.level().getEntitiesOfClass(AvaliEntity.class, avali.getBoundingBox().inflate(healer.range()),
                other -> avali.isAlly(other) && needsCare(other, healer))
            .stream().min(Comparator.comparingDouble(LivingEntity::getHealth)).orElse(null);
    }

    private boolean needsCare(LivingEntity entity, AvaliCompanions.Healer healer) {
        return entity.isAlive() && avali.distanceTo(entity) <= healer.range() && avali.getSensing().hasLineOfSight(entity)
            && entity.getHealth() <= entity.getMaxHealth() * healer.potionBelowHealth();
    }

    private LivingEntity enemy(ServerPlayer owner, AvaliCompanions.Healer healer) {
        List<LivingEntity> allies = avali.level().getEntitiesOfClass(LivingEntity.class, avali.getBoundingBox().inflate(healer.range() * 2),
            other -> other == avali || avali.isAlly(other));
        return AvaliFight.enemies(avali).stream()
            .filter(enemy -> !(enemy instanceof net.minecraft.world.entity.player.Player) && avali.distanceTo(enemy) <= healer.range()
                && avali.getSensing().hasLineOfSight(enemy)
                && allies.stream().allMatch(ally -> ally.distanceTo(enemy) > SPLASH_SAFE_DISTANCE))
            .min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
    }

    private void throwPotion(ServerLevel level, LivingEntity target, Holder<Potion> potion) {
        double xd = target.getX() + target.getDeltaMovement().x - avali.getX();
        double yd = target.getEyeY() - 1.1 - avali.getY();
        double zd = target.getZ() + target.getDeltaMovement().z - avali.getZ();
        double distance = Math.sqrt(xd * xd + zd * zd);
        avali.getLookControl().setLookAt(target);
        Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, PotionContents.createItemStack(Items.SPLASH_POTION, potion),
            avali, xd, yd + distance * 0.2, zd, distance <= 2.0 ? 0.45F : 0.75F, 2.0F);
        level.playSound(null, avali.getX(), avali.getY(), avali.getZ(), SoundEvents.WITCH_THROW, SoundSource.NEUTRAL, 1.0F, 0.9F);
        avali.swingForAttack(InteractionHand.MAIN_HAND);
    }
}
