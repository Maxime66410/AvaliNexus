/**
 * File: AvaliPackGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.block.heater.HeaterBlockEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliBreeding;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

public class AvaliPackGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int REPATH_INTERVAL = 40;
    private static final int MAX_WALK_TICKS = 1200;
    private static final double WALK_SPEED = 0.8;
    private static final double RETURN_SPEED = 1.0;
    private static final double COUPLE_REGROUP_RADIUS = 2.0;

    private final AvaliEntity avali;
    private Vec3 target;
    private double arrival;
    private double speed;
    private boolean running;
    private int checkCooldown;
    private int walkTicks;

    public AvaliPackGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger() || avali.isSleeping() || avali.getPackId() == null || avali.isCompanion()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        return pickTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && walkTicks < MAX_WALK_TICKS && avali.position().distanceTo(target) > arrival
            && !avali.isPassenger() && !avali.isSleeping();
    }

    @Override
    public void start() {
        walkTicks = 0;
        avali.setSprinting(running);
        avali.walkTowards(target, speed);
    }

    @Override
    public void tick() {
        walkTicks++;
        if (walkTicks % REPATH_INTERVAL == 0) {
            pickTarget();
            avali.setSprinting(running);
        }
        if (target != null && (avali.getNavigation().isDone() || walkTicks % REPATH_INTERVAL == 0)) {
            avali.walkTowards(target, speed);
        }
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
        avali.setSprinting(false);
        target = null;
    }

    private boolean pickTarget() {
        AvaliBehavior.Pack pack = AvaliBehavior.get(avali.level().getServer()).pack();
        target = null;
        speed = WALK_SPEED;
        running = false;

        if (avali.isBaby()) {
            Entity parent = avali.getParents().stream()
                .map(id -> avali.level().getEntity(id))
                .filter(entity -> entity instanceof AvaliEntity other && other.isAlive() && !other.isCompanion())
                .min(Comparator.comparingDouble(avali::distanceToSqr))
                .orElse(null);
            if (parent != null && avali.distanceTo(parent) > pack.babyDistance()) {
                if (avali.distanceTo(parent) > pack.babyRunDistance()) {
                    running = true;
                    speed = RETURN_SPEED;
                }
                return aim(parent.position(), pack.babyDistance() / 2);
            }
        } else {
            UUID self = avali.getUUID();
            AvaliEntity kit = avali.level().getEntitiesOfClass(AvaliEntity.class,
                    avali.getBoundingBox().inflate(pack.parentDistance() * 3), other -> other.isBaby() && other.getParents().contains(self))
                .stream().min(Comparator.comparingDouble(avali::distanceToSqr)).orElse(null);
            if (kit != null && avali.distanceTo(kit) > pack.parentDistance()) {
                return aim(kit.position(), pack.parentDistance() / 2);
            }
        }

        Vec3 center = avali.getPackCenter();
        if (avali.isReturningHome()) {
            running = true;
            speed = RETURN_SPEED;
            return aim(avali.getReturnTarget(), pack.regroupRadius());
        }
        if (avali.position().distanceTo(center) > pack.maxDistance()) {
            speed = RETURN_SPEED;
            return aim(center, pack.maxDistance() / 2);
        }

        AvaliEntity partner = avali.getPartner();
        if (partner != null && !partner.isCompanion()) {
            if (avali.shouldRegroup() && !avali.isFemale()) {
                return aim(partner.position(), COUPLE_REGROUP_RADIUS);
            }
            double stay = AvaliBreeding.get(avali.level().getServer()).bond().stayDistance();
            if (!avali.shouldRegroup() && avali.distanceTo(partner) > stay) {
                return aim(partner.position(), stay / 2);
            }
        }

        if (avali.shouldRegroup()) {
            BlockPos heater = findHeater(BlockPos.containing(center), pack.heaterSearchRadius());
            if (heater != null) {
                return aim(Vec3.atBottomCenterOf(heater), pack.heaterRadius());
            }
            return aim(center, pack.regroupRadius());
        }
        return false;
    }

    private boolean aim(Vec3 position, double radius) {
        if (avali.position().distanceTo(position) <= radius) {
            return false;
        }
        target = position;
        arrival = radius;
        return true;
    }

    private BlockPos findHeater(BlockPos center, int radius) {
        BlockPos closest = null;
        double closestDistance = (double) radius * radius;
        int minX = (center.getX() - radius) >> 4;
        int maxX = (center.getX() + radius) >> 4;
        int minZ = (center.getZ() - radius) >> 4;
        int maxZ = (center.getZ() + radius) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = avali.level().getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    double distance = entity.getBlockPos().distSqr(center);
                    if (entity instanceof HeaterBlockEntity && distance <= closestDistance) {
                        closest = entity.getBlockPos();
                        closestDistance = distance;
                    }
                }
            }
        }
        return closest;
    }
}
