/**
 * File: AvaliRangedGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;

import java.util.EnumSet;

public class AvaliRangedGoal extends Goal {

    private final AvaliEntity avali;
    private int cooldown;
    private int shotsLeft;

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
    }

    @Override
    public void stop() {
        avali.setAggressive(false);
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = avali.getTarget();
        if (target == null) {
            return;
        }
        AvaliCombat.Data data = AvaliFight.data(avali);
        avali.getLookControl().setLookAt(target, 30.0F, 30.0F);
        boolean seen = avali.getSensing().hasLineOfSight(target);
        if (!seen || avali.distanceTo(target) > data.rangedMax()) {
            avali.getNavigation().moveTo(target, data.chaseSpeed());
        } else {
            avali.getNavigation().stop();
            faceTarget(target);
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        ItemStack gun = avali.getMainHandItem();
        if (seen && gun.getItem() instanceof RailGunItem item && avali.distanceTo(target) <= data.rangedMax()) {
            AvaliCombat.Burst burst = data.burst();
            if (shotsLeft <= 0) {
                shotsLeft = Mth.nextInt(avali.getRandom(), burst.minShots(), burst.maxShots());
            }
            item.shootAt(avali, gun, target);
            shotsLeft--;
            cooldown = shotsLeft > 0 ? item.stats().interval() : Mth.nextInt(avali.getRandom(), burst.minPause(), burst.maxPause());
        }
    }

    private void faceTarget(LivingEntity target) {
        float yaw = (float) (Mth.atan2(target.getZ() - avali.getZ(), target.getX() - avali.getX()) * Mth.RAD_TO_DEG) - 90.0F;
        avali.setYRot(yaw);
        avali.setYBodyRot(yaw);
    }
}
