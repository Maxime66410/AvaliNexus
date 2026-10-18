/**
 * File: AvaliTargetGoal.java
 * Author: Maxime66410
 * Created: 2026-10-18
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import org.furranystudio.avalinexus.entity.avali.AvaliCombat;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliFight;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AvaliTargetGoal extends Goal {

    private static final int CHECK_INTERVAL = 10;

    private final AvaliEntity avali;
    private LivingEntity candidate;
    private int checkCooldown;

    public AvaliTargetGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0 || avali.isBaby()) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        candidate = avali.getThreat();
        if (candidate == null) {
            candidate = huntable();
        }
        return candidate != null && AvaliFight.canAttack(avali, foes(candidate), false);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = avali.getTarget();
        if (target == null || !AvaliFight.isValidEnemy(target)) {
            return false;
        }
        AvaliCombat.Data data = AvaliFight.data(avali);
        if (target != avali.getThreat() && avali.distanceTo(target) > data.grudgeDistance()) {
            return false;
        }
        if (--checkCooldown > 0) {
            return true;
        }
        checkCooldown = CHECK_INTERVAL;
        return AvaliFight.canAttack(avali, foes(target), true);
    }

    @Override
    public void start() {
        avali.setTarget(candidate);
    }

    @Override
    public void stop() {
        avali.setTarget(null);
        candidate = null;
    }

    private List<LivingEntity> foes(LivingEntity target) {
        List<LivingEntity> foes = new ArrayList<>(AvaliFight.enemies(avali));
        if (!foes.contains(target)) {
            foes.add(target);
        }
        return foes;
    }

    private LivingEntity huntable() {
        AvaliCombat.Data data = AvaliFight.data(avali);
        List<LivingEntity> prey = avali.level().getEntitiesOfClass(LivingEntity.class, avali.getBoundingBox().inflate(data.enemyRadius()),
            entity -> entity instanceof Enemy && AvaliFight.isValidEnemy(entity) && data.hunts(entity.getType())
                && avali.distanceTo(entity) <= data.enemyRadius() && avali.getSensing().hasLineOfSight(entity));
        return AvaliFight.nearest(avali, prey);
    }
}
