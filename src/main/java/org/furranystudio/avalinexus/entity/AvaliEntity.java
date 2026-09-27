/**
 * File: AvaliEntity.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.goal.AvaliPanicGoal;
import org.furranystudio.avalinexus.entity.goal.AvaliStrollGoal;
import org.furranystudio.avalinexus.entity.goal.AvaliSwimGoal;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.Arrays;

public class AvaliEntity extends AgeableMob implements GeoEntity {

    private static final RawAnimation BREATHING = RawAnimation.begin().thenLoop("global_IdleBreathing");
    private static final RawAnimation EARS_TOP = RawAnimation.begin().thenLoop("global_EarsIdle");
    private static final RawAnimation EARS_BOTTOM = RawAnimation.begin().thenLoop("global_EarsBottomIdle");
    private static final RawAnimation FEATHERS = RawAnimation.begin().thenLoop("global_Feathers");
    private static final RawAnimation TAIL_IDLE = RawAnimation.begin().thenLoop("global_TailIdle");
    private static final RawAnimation TAIL_WALK = RawAnimation.begin().thenLoop("walking_WalkTail");
    private static final RawAnimation TAIL_RUN = RawAnimation.begin().thenLoop("running_RunTail");
    private static final RawAnimation TAIL_SWIM = RawAnimation.begin().thenLoop("swimming");
    private static final float DEEP_WATER = 0.5F;
    private static final int SWIM_GRACE_TICKS = 10;
    private static final float MAX_SWIM_PITCH = 75.0F;
    private static final int NOISE_INTERVAL = 240;
    private static final RawAnimation LOWER_EARS = RawAnimation.begin().thenPlayAndHold("Lower Ears");
    private static final int ANGRY_TICKS = 30;
    private static final double HAPPY_RANGE = 6.0;
    private static final int HAPPY_TICKS = 30;
    private static final int GESTURE_CHANCE = 400;
    private static final int GESTURE_MIN_TICKS = 40;
    private static final int GESTURE_MAX_TICKS = 100;
    private static final AvaliGesture[] IDLE_GESTURES = Arrays.stream(AvaliGesture.values())
        .filter(AvaliGesture::playsWhenIdle).toArray(AvaliGesture[]::new);

    private static final EntityDataAccessor<Integer> DATA_MOOD = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GESTURE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int swimGraceTicks;
    private float swimPitch;
    private float swimPitchO;
    private int angryTicks;
    private int happyTicks;
    private boolean playerNearby;
    private int gestureTicks;
    private boolean panicking;

    public AvaliEntity(EntityType<? extends AvaliEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvaliSwimGoal(this, 1.0));
        goalSelector.addGoal(2, new AvaliPanicGoal(this, 1.1));
        goalSelector.addGoal(5, new AvaliStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOOD, AvaliMood.NEUTRAL.ordinal());
        builder.define(DATA_GESTURE, -1);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            angryTicks = ANGRY_TICKS;
            stopGesture();
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.AVALI_NOISE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.AVALI_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.AVALI_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return NOISE_INTERVAL;
    }

    public void playNoise() {
        playSound(ModSounds.AVALI_NOISE.get(), getSoundVolume(), getVoicePitch());
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return ModEntities.AVALI.get().create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
            new AnimationController<AvaliEntity>("breathing", test -> test.setAndContinue(BREATHING)),
            new AnimationController<AvaliEntity>("ears_top", 5, test -> test.setAndContinue(getMood().earsDown() ? LOWER_EARS : EARS_TOP)),
            new AnimationController<AvaliEntity>("ears_bottom", 5, test -> test.setAndContinue(getMood().earsDown() ? LOWER_EARS : EARS_BOTTOM)),
            new AnimationController<AvaliEntity>("feathers", test -> test.setAndContinue(FEATHERS)),
            new AnimationController<AvaliEntity>("tail", 5, this::tailAnimation),
            new AnimationController<AvaliEntity>("gesture", 5, test ->
                getGesture() == null ? PlayState.STOP : test.setAndContinue(getGesture().animation())));
    }

    @Override
    public void tick() {
        super.tick();
        if (isSprinting() && isInDeepWater()) {
            swimGraceTicks = SWIM_GRACE_TICKS;
        } else if (swimGraceTicks > 0) {
            swimGraceTicks--;
        }
        swimPitchO = swimPitch;
        swimPitch += (targetSwimPitch() - swimPitch) * 0.15F;

        if (!level().isClientSide()) {
            updateMood();
            updateGesture();
        }
    }

    private void updateMood() {
        if (angryTicks > 0) {
            angryTicks--;
        }
        if (happyTicks > 0) {
            happyTicks--;
        }
        boolean nearby = level().getNearestPlayer(this, HAPPY_RANGE) != null;
        if (nearby && !playerNearby) {
            happyTicks = HAPPY_TICKS;
        }
        playerNearby = nearby;

        AvaliMood mood;
        if (angryTicks > 0) {
            mood = AvaliMood.ANGRY;
        } else if (panicking) {
            mood = AvaliMood.SCARED;
        } else if (happyTicks > 0) {
            mood = AvaliMood.HAPPY;
        } else {
            mood = AvaliMood.NEUTRAL;
        }
        entityData.set(DATA_MOOD, mood.ordinal());
    }

    private void updateGesture() {
        if (getGesture() != null) {
            if (--gestureTicks <= 0 || getMood().earsDown()) {
                stopGesture();
            }
            return;
        }
        if (!getMood().earsDown() && !walkAnimation.isMoving() && random.nextInt(GESTURE_CHANCE) == 0) {
            AvaliGesture gesture = IDLE_GESTURES[random.nextInt(IDLE_GESTURES.length)];
            playGesture(gesture, Mth.nextInt(random, GESTURE_MIN_TICKS, GESTURE_MAX_TICKS));
        }
    }

    public void playGesture(AvaliGesture gesture, int ticks) {
        gestureTicks = ticks;
        entityData.set(DATA_GESTURE, gesture.ordinal());
    }

    public void stopGesture() {
        gestureTicks = 0;
        entityData.set(DATA_GESTURE, -1);
    }

    public void setPanicking(boolean panicking) {
        this.panicking = panicking;
    }

    public AvaliMood getMood() {
        return AvaliMood.values()[entityData.get(DATA_MOOD)];
    }

    public AvaliGesture getGesture() {
        int gesture = entityData.get(DATA_GESTURE);
        return gesture < 0 ? null : AvaliGesture.values()[gesture];
    }

    public AvaliFace getFace() {
        AvaliGesture gesture = getGesture();
        return gesture != null && gesture.face() != null ? gesture.face() : getMood().face();
    }

    // Mobs can't swim like players, so we fake it
    @Override
    public boolean isVisuallySwimming() {
        return swimGraceTicks > 0;
    }

    public float getSwimPitch(float partialTick) {
        return Mth.lerp(partialTick, swimPitchO, swimPitch);
    }

    private float targetSwimPitch() {
        if (!isUnderWater()) {
            return 0.0F;
        }
        Vec3 motion = position().subtract(xo, yo, zo);
        double horizontal = motion.horizontalDistance();
        if (horizontal + Math.abs(motion.y) < 0.01) {
            return 0.0F;
        }
        float pitch = (float) -Math.toDegrees(Math.atan2(motion.y, horizontal));
        return Mth.clamp(pitch, -MAX_SWIM_PITCH, MAX_SWIM_PITCH);
    }

    public boolean isInDeepWater() {
        return isInWater() && (getFluidHeight(FluidTags.WATER) > getBbHeight() * DEEP_WATER
            || level().getFluidState(blockPosition().below()).is(FluidTags.WATER));
    }

    private PlayState tailAnimation(AnimationTest<AvaliEntity> test) {
        if (isVisuallySwimming()) {
            return test.setAndContinue(TAIL_SWIM);
        }
        if (!test.isMoving()) {
            return test.setAndContinue(TAIL_IDLE);
        }
        return test.setAndContinue(isSprinting() ? TAIL_RUN : TAIL_WALK);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
