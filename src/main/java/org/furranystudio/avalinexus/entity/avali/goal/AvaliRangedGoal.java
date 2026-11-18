/**
 * File: AvaliRangedGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-11-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;

import java.util.EnumSet;

public class AvaliRangedGoal extends Goal {

    private static final double BOW_RANGE = 16.0;
    private static final int BOW_DRAW_TICKS = 20;
    private static final int BOW_INTERVAL = 20;
    private static final int LOST_SIGHT_TICKS = 60;
    private static final int CROSSBOW_MIN_DELAY = 20;
    private static final int CROSSBOW_EXTRA_DELAY = 20;
    private static final float ARROW_SPEED = 1.6F;
    private static final float ARC = 0.2F;
    private static final double TARGET_HEIGHT = 1.0 / 3.0;
    private static final float BASE_UNCERTAINTY = 14.0F;
    private static final float UNCERTAINTY_PER_DIFFICULTY = 4.0F;

    private final AvaliEntity avali;
    private int cooldown;
    private int shotsLeft;
    private int unseenTicks;

    public AvaliRangedGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean inRange(AvaliEntity avali, LivingEntity target, double margin) {
        return avali.getGear().hasRanged() && avali.distanceTo(target) > AvaliFight.data(avali).rangedMin() + margin;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = avali.getTarget();
        return !avali.isBaby() && target != null && target.isAlive() && inRange(avali, target, 0.0);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        avali.getGear().drawRanged();
        avali.setAggressive(true);
        cooldown = 0;
        shotsLeft = 0;
        unseenTicks = 0;
    }

    @Override
    public void stop() {
        avali.setAggressive(false);
        avali.getNavigation().stop();
        if (isBowOrCrossbow(avali.getUseItem())) {
            avali.stopUsingItem();
        }
    }

    @Override
    public void tick() {
        LivingEntity target = avali.getTarget();
        if (target == null) {
            return;
        }
        AvaliCombat.Data data = AvaliFight.data(avali);
        ItemStack weapon = avali.getMainHandItem();
        double maxRange = isBowOrCrossbow(weapon) ? BOW_RANGE : data.rangedMax();
        avali.getLookControl().setLookAt(target, 30.0F, 30.0F);
        boolean seen = avali.getSensing().hasLineOfSight(target);
        unseenTicks = seen ? 0 : unseenTicks + 1;
        boolean inReach = seen && avali.distanceTo(target) <= maxRange;
        if (inReach) {
            avali.getNavigation().stop();
            faceTarget(target);
        } else {
            avali.getNavigation().moveTo(target, data.chaseSpeed());
        }
        if (weapon.getItem() instanceof BowItem) {
            tickBow(target, inReach);
            return;
        }
        if (weapon.getItem() instanceof CrossbowItem crossbow) {
            tickCrossbow(target, weapon, crossbow, inReach);
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (inReach && weapon.getItem() instanceof RailGunItem item) {
            AvaliCombat.Burst burst = data.burst();
            if (shotsLeft <= 0) {
                shotsLeft = Mth.nextInt(avali.getRandom(), burst.minShots(), burst.maxShots());
            }
            item.shootAt(avali, weapon, target);
            shotsLeft--;
            cooldown = shotsLeft > 0 ? item.stats().interval() : Mth.nextInt(avali.getRandom(), burst.minPause(), burst.maxPause());
        }
    }

    private void tickBow(LivingEntity target, boolean inReach) {
        if (avali.isUsingItem()) {
            if (unseenTicks > LOST_SIGHT_TICKS) {
                avali.stopUsingItem();
            } else if (inReach && avali.getTicksUsingItem() >= BOW_DRAW_TICKS) {
                int pull = avali.getTicksUsingItem();
                avali.stopUsingItem();
                shootArrow(target, BowItem.getPowerForTime(pull));
                cooldown = BOW_INTERVAL;
            }
        } else if (--cooldown <= 0 && inReach) {
            avali.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }

    private void tickCrossbow(LivingEntity target, ItemStack weapon, CrossbowItem crossbow, boolean inReach) {
        if (!CrossbowItem.isCharged(weapon)) {
            if (!avali.isUsingItem()) {
                avali.startUsingItem(InteractionHand.MAIN_HAND);
            } else if (avali.getTicksUsingItem() >= CrossbowItem.getChargeDuration(weapon, avali)) {
                avali.releaseUsingItem();
                cooldown = CROSSBOW_MIN_DELAY + avali.getRandom().nextInt(CROSSBOW_EXTRA_DELAY);
            }
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (inReach) {
            crossbow.performShooting(avali.level(), avali, InteractionHand.MAIN_HAND, weapon, ARROW_SPEED, uncertainty(), target);
        }
    }

    private void shootArrow(LivingEntity target, float power) {
        if (!(avali.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack bow = avali.getMainHandItem();
        ItemStack projectile = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(avali, projectile, power, bow);
        double dx = target.getX() - avali.getX();
        double dy = target.getY(TARGET_HEIGHT) - arrow.getY();
        double dz = target.getZ() - avali.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        Projectile.spawnProjectileUsingShoot(arrow, level, projectile, dx, dy + horizontal * ARC, dz, ARROW_SPEED, uncertainty());
        avali.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (avali.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private float uncertainty() {
        return BASE_UNCERTAINTY - avali.level().getDifficulty().getId() * UNCERTAINTY_PER_DIFFICULTY;
    }

    private static boolean isBowOrCrossbow(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    private void faceTarget(LivingEntity target) {
        float yaw = (float) (Mth.atan2(target.getZ() - avali.getZ(), target.getX() - avali.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        avali.setYRot(yaw);
        avali.setYBodyRot(yaw);
    }
}
