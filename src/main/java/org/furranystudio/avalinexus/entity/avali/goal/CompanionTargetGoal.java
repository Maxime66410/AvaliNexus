/**
 * File: CompanionTargetGoal.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;
import org.furranystudio.avalinexus.entity.avali.CompanionRole;

import java.util.EnumSet;
import java.util.List;

public class CompanionTargetGoal extends Goal {

    private final AvaliEntity avali;
    private LivingEntity candidate;
    private int attackerStamp;
    private int targetStamp;

    public CompanionTargetGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!avali.isFollowingOwner() || avali.getRole() != CompanionRole.FIGHTER) {
            return false;
        }
        ServerPlayer owner = avali.getOwner();
        if (owner == null) {
            return false;
        }
        candidate = null;
        if (owner.getLastHurtByMobTimestamp() != attackerStamp && isValid(owner.getLastHurtByMob(), true)) {
            candidate = owner.getLastHurtByMob();
        } else if (owner.getLastHurtMobTimestamp() != targetStamp && isValid(owner.getLastHurtMob(), false)) {
            candidate = owner.getLastHurtMob();
        }
        return candidate != null && AvaliFight.canAttack(avali, List.of(candidate), false);
    }

    @Override
    public void start() {
        ServerPlayer owner = avali.getOwner();
        if (owner != null) {
            attackerStamp = owner.getLastHurtByMobTimestamp();
            targetStamp = owner.getLastHurtMobTimestamp();
        }
        avali.setTarget(candidate);
        avali.remember(candidate);
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    private boolean isValid(LivingEntity entity, boolean attackedOwner) {
        if (entity == null || !AvaliFight.isValidEnemy(entity) || avali.isAlly(entity)) {
            return false;
        }
        return attackedOwner || !(entity instanceof Player);
    }
}
