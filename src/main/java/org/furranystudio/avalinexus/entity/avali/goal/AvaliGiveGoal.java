/**
 * File: AvaliGiveGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.EnumSet;

public class AvaliGiveGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int REPATH_INTERVAL = 20;
    private static final int MAX_WALK_TICKS = 400;
    private static final double REACH_SQ = 6.25;
    private static final double SPEED = 1.0;
    private static final double THROW_SPEED = 0.3;
    private static final int PICKUP_DELAY = 40;

    private final AvaliEntity avali;
    private Player player;
    private int checkCooldown;
    private int walkTicks;

    public AvaliGiveGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger() || avali.isSleeping() || !avali.getGear().hasToGive()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        double range = AvaliBehavior.get(avali.level().getServer()).life().giveRange();
        if (avali.isCompanion()) {
            player = avali.getOwner();
            return player != null && player.level() == avali.level() && !player.isSpectator() && avali.distanceTo(player) <= range;
        }
        player = avali.level().getNearestPlayer(avali, range);
        if (player == null || player.isSpectator()) {
            drop(null);
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return player != null && player.isAlive() && avali.getGear().hasToGive();
    }

    @Override
    public void start() {
        walkTicks = 0;
        avali.getNavigation().moveTo(player, SPEED);
    }

    @Override
    public void tick() {
        walkTicks++;
        avali.getLookControl().setLookAt(player);
        if (avali.distanceToSqr(player) < REACH_SQ) {
            drop(player);
        } else if (walkTicks > MAX_WALK_TICKS) {
            drop(null);
        } else if (walkTicks % REPATH_INTERVAL == 0) {
            avali.getNavigation().moveTo(player, SPEED);
        }
    }

    @Override
    public void stop() {
        avali.getNavigation().stop();
        player = null;
    }

    private void drop(Player towards) {
        for (ItemStack stack : avali.getGear().drainToGive()) {
            ItemEntity item = new ItemEntity(avali.level(), avali.getX(), avali.getEyeY() - 0.3, avali.getZ(), stack);
            item.setThrower(avali);
            item.setPickUpDelay(PICKUP_DELAY);
            if (towards != null) {
                Vec3 direction = towards.position().subtract(avali.position()).normalize();
                item.setDeltaMovement(direction.x * THROW_SPEED, 0.2, direction.z * THROW_SPEED);
            }
            avali.level().addFreshEntity(item);
        }
    }
}
