/**
 * File: CompanionGatherGoal.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.entity.avali.AvaliCompanions;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.CompanionRole;

import java.util.Comparator;
import java.util.EnumSet;

public class CompanionGatherGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;
    private static final int REPATH_INTERVAL = 10;
    private static final double REACH_SQ = 2.25;
    private static final double SPEED = 1.1;

    private final AvaliEntity avali;
    private ItemEntity target;
    private int checkCooldown;
    private int repath;

    public CompanionGatherGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || !avali.isFollowingOwner() || avali.getRole() != CompanionRole.GATHERER || avali.isPassenger()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        target = findTarget();
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return avali.isFollowingOwner() && avali.getRole() == CompanionRole.GATHERER && target != null && target.isAlive();
    }

    @Override
    public void start() {
        repath = 0;
    }

    @Override
    public void stop() {
        target = null;
        avali.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (avali.distanceToSqr(target) < REACH_SQ) {
            collect(target);
            target = findTarget();
            repath = 0;
            return;
        }
        if (--repath <= 0) {
            repath = REPATH_INTERVAL;
            avali.getNavigation().moveTo(target, SPEED);
        }
    }

    private void collect(ItemEntity item) {
        if (avali.getGear().wants(item.getItem())) {
            avali.getGear().takeFrom(item);
            return;
        }
        ItemStack stack = item.getItem().copy();
        avali.take(item, stack.getCount());
        avali.getGear().give(stack);
        item.discard();
    }

    private ItemEntity findTarget() {
        ServerPlayer owner = avali.getOwner();
        if (owner == null || owner.level() != avali.level()) {
            return null;
        }
        double radius = AvaliCompanions.get(avali.level().getServer()).gatherRadius();
        return avali.level().getEntitiesOfClass(ItemEntity.class, owner.getBoundingBox().inflate(radius),
                item -> item.isAlive() && !item.hasPickUpDelay() && !(item.getOwner() instanceof AvaliEntity)
                    && (item.getOwner() != owner || avali.getGear().wants(item.getItem())) && owner.distanceTo(item) <= radius)
            .stream().min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
    }
}
