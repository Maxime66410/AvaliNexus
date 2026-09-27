/**
 * File: MonsterTargeting.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.lang.reflect.Field;

public final class MonsterTargeting {

    // targetSelector is protected, reflection keeps this working the same on every loader
    private static final Field TARGET_SELECTOR = findTargetSelector();

    private MonsterTargeting() {
    }

    public static void onEntityJoin(Entity entity) {
        if (!(entity instanceof Monster monster) || entity instanceof NeutralMob || TARGET_SELECTOR == null) {
            return;
        }
        try {
            GoalSelector targetSelector = (GoalSelector) TARGET_SELECTOR.get(monster);
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(monster, AvaliEntity.class, true));
        } catch (IllegalAccessException e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't make {} hostile to Avalis", entity, e);
        }
    }

    private static Field findTargetSelector() {
        try {
            Field field = Mob.class.getDeclaredField("targetSelector");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't access Mob.targetSelector, monsters won't attack Avalis", e);
            return null;
        }
    }
}
