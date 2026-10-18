/**
 * File: AvaliPickupGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.Comparator;
import java.util.EnumSet;

public class AvaliPickupGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int REPATH_INTERVAL = 20;
    private static final int MAX_WALK_TICKS = 300;
    private static final double REACH = 1.5;
    private static final double REACH_SQ = REACH * REACH;
    private static final double SPEED = 1.0;

    private final AvaliEntity avali;
    private ItemEntity target;
    private int checkCooldown;
    private int walkTicks;

    public AvaliPickupGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger() || avali.isSleeping()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        target = findTarget();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return walkTicks < MAX_WALK_TICKS && isWanted(target);
    }

    @Override
    public void start() {
        walkTicks = 0;
        avali.getNavigation().moveTo(target, SPEED);
    }

    @Override
    public void tick() {
        walkTicks++;
        if (avali.distanceToSqr(target) < REACH_SQ) {
            for (ItemEntity item : avali.level().getEntitiesOfClass(ItemEntity.class, avali.getBoundingBox().inflate(REACH),
                item -> isWanted(item) && avali.distanceToSqr(item) < REACH_SQ)) {
                avali.getGear().takeFrom(item);
            }
            target = findTarget();
            if (target != null) {
                walkTicks = 0;
                avali.getNavigation().moveTo(target, SPEED);
            }
        } else if (walkTicks % REPATH_INTERVAL == 0) {
            avali.getNavigation().moveTo(target, SPEED);
        }
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
        target = null;
    }

    private ItemEntity findTarget() {
        double radius = AvaliBehavior.get(avali.level().getServer()).life().pickupRadius();
        return avali.level().getEntitiesOfClass(ItemEntity.class, avali.getBoundingBox().inflate(radius), this::isWanted)
            .stream().min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
    }

    private boolean isWanted(ItemEntity item) {
        return item != null && item.isAlive() && !item.hasPickUpDelay() && !(item.getOwner() instanceof AvaliEntity)
            && avali.getGear().wants(item.getItem());
    }
}
