/**
 * File: AvaliMeleeGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;

public class AvaliMeleeGoal extends MeleeAttackGoal {

    private static final int TICKS_PER_SECOND = 20;
    private static final int JUMP_LEAD_TICKS = 6;

    private final AvaliEntity avali;
    private int cooldown;
    private int jumpCooldown;
    private boolean critJump;
    private boolean airborne;

    public AvaliMeleeGoal(AvaliEntity avali) {
        super(avali, 1.0, true);
        this.avali = avali;
    }

    @Override
    public boolean canUse() {
        return !avali.isBaby() && super.canUse();
    }

    @Override
    public void start() {
        super.start();
        cooldown = 0;
        critJump = false;
        airborne = false;
        avali.getGear().drawWeapon();
        avali.getNavigation().setSpeedModifier(AvaliFight.data(avali).chaseSpeed());
    }

    @Override
    public void stop() {
        super.stop();
        lowerShield();
    }

    @Override
    public void tick() {
        if (cooldown > 0) {
            cooldown--;
        }
        if (jumpCooldown > 0) {
            jumpCooldown--;
        }
        updateCritJump();
        super.tick();
        avali.getNavigation().setSpeedModifier(AvaliFight.data(avali).chaseSpeed());
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        boolean waitingForFall = critJump && !avali.onGround() && !avali.canCriticalAttack(target);
        if (!waitingForFall && canPerformAttack(target)) {
            lowerShield();
            super.checkAndPerformAttack(target);
            return;
        }
        boolean close = avali.distanceTo(target) <= AvaliFight.data(avali).shieldDistance();
        if (close && hasShield() && !avali.isUsingItem()) {
            avali.startUsingItem(InteractionHand.OFF_HAND);
        } else if (!close) {
            lowerShield();
        }
    }

    @Override
    protected void resetAttackCooldown() {
        double speed = avali.getAttributeValue(Attributes.ATTACK_SPEED);
        cooldown = Math.max(1, (int) Math.round(TICKS_PER_SECOND / speed));
    }

    @Override
    protected boolean isTimeToAttack() {
        return cooldown <= 0;
    }

    private void updateCritJump() {
        if (!avali.onGround()) {
            airborne = true;
        } else if (airborne) {
            airborne = false;
            critJump = false;
        }
        LivingEntity target = avali.getTarget();
        AvaliCombat.Crit crit = AvaliFight.data(avali).crit();
        if (target != null && avali.onGround() && !avali.isInWater() && jumpCooldown <= 0 && cooldown <= JUMP_LEAD_TICKS
                && avali.distanceTo(target) <= crit.jumpDistance()) {
            avali.getJumpControl().jump();
            jumpCooldown = crit.jumpCooldown();
            critJump = true;
        }
    }

    private boolean hasShield() {
        return avali.getGear().isShield(avali.getOffhandItem());
    }

    private void lowerShield() {
        if (avali.isUsingItem() && avali.getUsedItemHand() == InteractionHand.OFF_HAND) {
            avali.stopUsingItem();
        }
    }
}
