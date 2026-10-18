/**
 * File: AvaliEatGoal.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali.goal;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.avali.AvaliBehavior;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliGear;

import java.util.EnumSet;

public class AvaliEatGoal extends Goal {

    private static final int CHECK_INTERVAL = 20;
    private static final int EAT_TICKS = 32;
    private static final int CHEW_INTERVAL = 4;
    private static final int CALM_TICKS = 100;
    private static final int PARTICLES = 5;

    private final AvaliEntity avali;
    private int checkCooldown;
    private int eatTicks;
    private boolean done;

    public AvaliEatGoal(AvaliEntity avali) {
        this.avali = avali;
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (--checkCooldown > 0) {
            return false;
        }
        checkCooldown = CHECK_INTERVAL;
        return isHungry() && isCalm() && avali.getGear().hasFood();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !done && isCalm() && !avali.getMainHandItem().isEmpty();
    }

    @Override
    public void start() {
        done = false;
        eatTicks = EAT_TICKS;
        avali.getGear().stash(avali.getMainHandItem());
        avali.setItemSlot(EquipmentSlot.MAINHAND, avali.getGear().takeFood());
    }

    @Override
    public void tick() {
        ItemStack food = avali.getMainHandItem();
        if (--eatTicks % CHEW_INTERVAL == 0) {
            chew(food);
        }
        if (eatTicks > 0) {
            return;
        }
        avali.heal(AvaliGear.nutrition(food) * AvaliBehavior.get(avali.level().getServer()).life().healPerNutrition());
        food.shrink(1);
        avali.playSound(SoundEvents.PLAYER_BURP, 0.5F, avali.getRandom().nextFloat() * 0.1F + 0.9F);
        if (!isHungry()) {
            done = true;
            return;
        }
        if (food.isEmpty()) {
            avali.setItemSlot(EquipmentSlot.MAINHAND, avali.getGear().takeFood());
        }
        eatTicks = EAT_TICKS;
    }

    @Override
    public void stop() {
        avali.getGear().stash(avali.getMainHandItem());
        avali.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    private void chew(ItemStack food) {
        if (food.isEmpty()) {
            return;
        }
        avali.playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, avali.getRandom().nextFloat() * 0.2F + 0.9F);
        if (avali.level() instanceof ServerLevel level) {
            AvaliBehavior.Life life = AvaliBehavior.get(level.getServer()).life();
            double scale = avali.isBaby() ? 0.5 : 1.0;
            Vec3 mouth = avali.getEyePosition().add(avali.getLookAngle().scale(life.eatMouthForward() * scale))
                .add(0, life.eatMouthHeight() * scale, 0);
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, food.getItem()), mouth.x, mouth.y, mouth.z,
                PARTICLES, 0.05, 0.05, 0.05, 0.05);
        }
    }

    private boolean isHungry() {
        float threshold = AvaliBehavior.get(avali.level().getServer()).life().eatBelowHealth();
        return avali.getHealth() < avali.getMaxHealth() * threshold;
    }

    private boolean isCalm() {
        return avali.getTarget() == null && !avali.isSleeping() && avali.tickCount - avali.getLastHurtByMobTimestamp() > CALM_TICKS;
    }
}
