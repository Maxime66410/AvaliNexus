/**
 * File: AvaliCreeperGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;

import java.util.EnumSet;

public class AvaliCreeperGoal extends Goal {

    private static final int CHECK_INTERVAL = 5;
    private static final int REPATH_INTERVAL = 10;

    private final AvaliEntity avali;
    private Creeper creeper;
    private int repath;
    private int checkCooldown;

    public AvaliCreeperGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isPassenger()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        creeper = AvaliFight.swellingCreeper(avali);
        return creeper != null && healthy();
    }

    @Override
    public boolean canContinueToUse() {
        return creeper != null && creeper.isAlive() && creeper.getSwellDir() > 0 && healthy();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        repath = 0;
        avali.setDodging(true);
        if (avali.isSleeping()) {
            avali.stopSleeping();
        }
    }

    @Override
    public void stop() {
        lowerShield();
        avali.setSprinting(false);
        avali.setDodging(false);
        creeper = null;
    }

    @Override
    public void tick() {
        avali.getLookControl().setLookAt(creeper);
        if (avali.getGear().isShield(avali.getOffhandItem())) {
            avali.getNavigation().stop();
            if (!avali.isUsingItem()) {
                avali.startUsingItem(InteractionHand.OFF_HAND);
            }
            return;
        }
        lowerShield();
        if (--repath > 0) {
            return;
        }
        repath = REPATH_INTERVAL;
        AvaliCombat.Creeper data = AvaliFight.data(avali).creeper();
        avali.setSprinting(true);
        Vec3 away = DefaultRandomPos.getPosAway(avali, data.fleeDistance(), 7, creeper.position());
        if (away != null) {
            avali.getNavigation().moveTo(away.x, away.y, away.z, data.fleeSpeed());
        }
    }

    private boolean healthy() {
        return avali.getHealth() > avali.getMaxHealth() * AvaliFight.data(avali).creeper().minHealth();
    }

    private void lowerShield() {
        if (avali.isUsingItem() && avali.getUsedItemHand() == InteractionHand.OFF_HAND) {
            avali.stopUsingItem();
        }
    }
}
