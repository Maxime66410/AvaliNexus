/**
 * File: AvaliEntity.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.avali;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.dialogue.DialogueManager;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliFace;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliMood;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliCushionGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliHeatGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliHotBiomeGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliPackGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliPanicGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliRestGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliShelterGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliSleepGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliStrollGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliSwimGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliTalkGoal;
import org.furranystudio.avalinexus.entity.avali.pose.AvaliPose;
import org.furranystudio.avalinexus.sound.ModSounds;
import org.furranystudio.avalinexus.trade.AvaliShop;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    private static final RawAnimation EMPTY = RawAnimation.begin().thenLoop("empty");
    private static final RawAnimation NAP = RawAnimation.begin().thenLoop("NAP");
    private static final RawAnimation SLEEP_BODY = RawAnimation.begin().thenLoop("sleeping");
    private static final RawAnimation SLEEP_EAR = RawAnimation.begin().thenLoop("sleeping_Ear");
    private static final RawAnimation SLEEP_FOOT = RawAnimation.begin().thenLoop("sleeping_Foot");
    private static final RawAnimation RIDING = RawAnimation.begin().thenLoop("riding");
    private static final int ANGRY_TICKS = 30;
    private static final double HAPPY_RANGE = 6.0;
    private static final int HAPPY_TICKS = 30;
    private static final int GESTURE_CHANCE = 400;
    private static final int GESTURE_MIN_TICKS = 40;
    private static final int GESTURE_MAX_TICKS = 100;
    private static final int PACK_CHECK_INTERVAL = 40;
    private static final double PACK_JOIN_RANGE = 32.0;
    private static final int DIRECT_WALK_DISTANCE = 24;
    private static final float WATER_MALUS = 16.0F;
    private static final float WATER_BORDER_MALUS = 8.0F;
    private static final float FIRE_NEIGHBOR_MALUS = 16.0F;
    private static final AvaliGesture[] IDLE_GESTURES = Arrays.stream(AvaliGesture.values())
        .filter(AvaliGesture::playsWhenIdle).toArray(AvaliGesture[]::new);

    private static final EntityDataAccessor<Integer> DATA_MOOD = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GESTURE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_POSE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_NAP = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_PACK = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_FEMALE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TINT = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_SKIN = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_DIALOGUE_FACE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int swimGraceTicks;
    private float swimPitch;
    private float swimPitchO;
    private int angryTicks;
    private int happyTicks;
    private boolean playerNearby;
    private int gestureTicks;
    private boolean justLoaded;
    private boolean named;
    private boolean styled;
    private boolean panicking;
    private boolean wasBaby;
    private ServerPlayer talkingTo;
    private UUID packId;
    private List<UUID> parents = List.of();
    private BlockPos bed;
    private Vec3 packCenter;
    private int packCheck;
    private AvaliShop shop;

    public AvaliEntity(EntityType<? extends AvaliEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.FIRE, -1.0F);
        setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, FIRE_NEIGHBOR_MALUS);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToPlayer) {
        return false;
    }

    public static boolean checkAvaliSpawnRules(EntityType<AvaliEntity> type, ServerLevelAccessor level, EntitySpawnReason reason,
                                               BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) <= 8) {
            return false;
        }
        return level.getBlockState(pos.below()).is(Blocks.SNOW) || Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvaliSwimGoal(this, 1.0));
        goalSelector.addGoal(1, new AvaliTalkGoal(this));
        goalSelector.addGoal(2, new AvaliPanicGoal(this, 1.1));
        goalSelector.addGoal(2, new AvaliHeatGoal(this));
        goalSelector.addGoal(3, new AvaliHotBiomeGoal(this));
        goalSelector.addGoal(4, new AvaliSleepGoal(this, 0.8));
        goalSelector.addGoal(5, new AvaliPackGoal(this));
        goalSelector.addGoal(6, new AvaliShelterGoal(this));
        goalSelector.addGoal(7, new AvaliCushionGoal(this, 0.8));
        goalSelector.addGoal(7, new AvaliRestGoal(this));
        goalSelector.addGoal(7, new AvaliStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOOD, AvaliMood.NEUTRAL.ordinal());
        builder.define(DATA_GESTURE, -1);
        builder.define(DATA_POSE, -1);
        builder.define(DATA_NAP, false);
        builder.define(DATA_DIALOGUE_FACE, -1);
        builder.define(DATA_PACK, "");
        builder.define(DATA_FEMALE, false);
        builder.define(DATA_TINT, 0xFFFFFF);
        builder.define(DATA_SKIN, "");
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            angryTicks = ANGRY_TICKS;
            stopGesture();
            if (talkingTo != null) {
                DialogueManager.close(talkingTo, true);
            }
        }
        return hurt;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (shop != null) {
            output.store("avalinexus_shop", AvaliShop.CODEC, shop);
        }
        output.putString("avalinexus_pack", getPack());
        output.putBoolean("avalinexus_named", named);
        output.putBoolean("avalinexus_styled", styled);
        output.putBoolean("avalinexus_female", isFemale());
        output.putInt("avalinexus_tint", getTint());
        output.putString("avalinexus_skin", getSkin());
        output.storeNullable("avalinexus_pack_id", UUIDUtil.CODEC, packId);
        output.store("avalinexus_parents", UUIDUtil.CODEC.listOf(), parents);
        output.storeNullable("avalinexus_bed", BlockPos.CODEC, bed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        justLoaded = true;
        shop = input.read("avalinexus_shop", AvaliShop.CODEC).orElse(null);
        setPack(input.getStringOr("avalinexus_pack", ""));
        named = input.getBooleanOr("avalinexus_named", false);
        styled = input.getBooleanOr("avalinexus_styled", false);
        entityData.set(DATA_FEMALE, input.getBooleanOr("avalinexus_female", false));
        entityData.set(DATA_TINT, input.getIntOr("avalinexus_tint", 0xFFFFFF));
        entityData.set(DATA_SKIN, input.getStringOr("avalinexus_skin", ""));
        packId = input.read("avalinexus_pack_id", UUIDUtil.CODEC).orElse(null);
        parents = input.read("avalinexus_parents", UUIDUtil.CODEC.listOf()).orElse(List.of());
        bed = input.read("avalinexus_bed", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSecondaryUseActive() || isSleeping() || getMood().earsDown()) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            DialogueManager.open(serverPlayer, this);
        }
        return InteractionResult.SUCCESS;
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
        AvaliEntity kit = ModEntities.AVALI.get().create(level, EntitySpawnReason.BREEDING);
        if (kit != null) {
            kit.setPack(getPack());
            kit.packId = packId;
            kit.parents = List.of(getUUID(), partner.getUUID());
        }
        return kit;
    }

    // Avalis spawned together form one pack and share its name
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data) {
        if (!(data instanceof PackData)) {
            data = new PackData(AvaliNames.pack(level.getServer(), getRandom()), UUID.randomUUID());
        }
        setPack(((PackData) data).pack);
        packId = ((PackData) data).id;
        giveName(level.getServer());
        giveLooks(level.getServer());
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    private void giveName(net.minecraft.server.MinecraftServer server) {
        named = true;
        if (getPack().isEmpty()) {
            setPack(AvaliNames.pack(server, getRandom()));
        }
        if (!hasCustomName()) {
            setCustomName(Component.literal(AvaliNames.name(server, getRandom())));
        }
    }

    private void giveLooks(net.minecraft.server.MinecraftServer server) {
        styled = true;
        entityData.set(DATA_FEMALE, AvaliAppearance.rollFemale(server, getRandom()));
        entityData.set(DATA_TINT, AvaliAppearance.rollColor(server, getPack(), getRandom()));
        entityData.set(DATA_SKIN, AvaliAppearance.rollRare(server, getRandom()));
    }

    public boolean isFemale() {
        return entityData.get(DATA_FEMALE);
    }

    public int getTint() {
        return entityData.get(DATA_TINT);
    }

    public String getSkin() {
        return entityData.get(DATA_SKIN);
    }

    public String getPack() {
        return entityData.get(DATA_PACK);
    }

    public void setPack(String pack) {
        entityData.set(DATA_PACK, pack);
    }

    // Shown in the dialogue and the shop, like Kiri, Frost Pack
    public Component title() {
        Component name = hasCustomName() ? getCustomName() : getType().getDescription();
        if (getPack().isEmpty()) {
            return name;
        }
        return Component.translatable("avalinexus.avali.title", name, Component.translatable("avalinexus.pack." + getPack()));
    }

    private static final class PackData extends AgeableMob.AgeableMobGroupData {
        private final String pack;
        private final UUID id;

        private PackData(String pack, UUID id) {
            super(true);
            this.pack = pack;
            this.id = id;
        }
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
                test.setAndContinue(getGesture() == null ? EMPTY : getGesture().animation())),
            new AnimationController<AvaliEntity>("body_pose", 10, test -> test.setAndContinue(bodyPoseAnimation())),
            new AnimationController<AvaliEntity>("sleep_ear", 10, test -> test.setAndContinue(isSleeping() && !isNapping() ? SLEEP_EAR : EMPTY)),
            new AnimationController<AvaliEntity>("sleep_foot", 10, test -> test.setAndContinue(isSleeping() && !isNapping() ? SLEEP_FOOT : EMPTY)));
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

        if (level().isClientSide() && isBaby() != wasBaby) {
            wasBaby = isBaby();
            cache.getManagerForId(getId()).getAnimationControllers().values().forEach(AnimationController::reset);
        }

        if (!level().isClientSide()) {
            if (justLoaded) {
                justLoaded = false;
                standUpAfterLoad();
            }
            if (!named) {
                giveName(level().getServer());
            }
            if (!styled) {
                giveLooks(level().getServer());
            }
            if (--packCheck <= 0) {
                packCheck = PACK_CHECK_INTERVAL;
                updatePack();
            }
            updateMood();
            updateGesture();
            if (talkingTo != null && (!talkingTo.isAlive() || talkingTo.level() != level()
                    || distanceTo(talkingTo) > DialogueManager.MAX_DISTANCE)) {
                DialogueManager.close(talkingTo, true);
            }
        }
    }

    private void updatePack() {
        AvaliBehavior.Data behavior = AvaliBehavior.get(level().getServer());
        boolean avoidWater = AvaliDangers.get(level().getServer()).avoidWater();
        setPathfindingMalus(PathType.WATER, avoidWater ? WATER_MALUS : PathType.WATER.getMalus());
        setPathfindingMalus(PathType.WATER_BORDER, avoidWater ? WATER_BORDER_MALUS : PathType.WATER_BORDER.getMalus());
        if (packId == null) {
            packId = level().getEntitiesOfClass(AvaliEntity.class, getBoundingBox().inflate(PACK_JOIN_RANGE),
                    other -> other.packId != null && other.getPack().equals(getPack()))
                .stream().map(other -> other.packId).findFirst().orElseGet(UUID::randomUUID);
        }
        List<AvaliEntity> members = level().getEntitiesOfClass(AvaliEntity.class,
            getBoundingBox().inflate(behavior.pack().maxDistance() * 2), other -> packId.equals(other.packId));
        Vec3 sum = Vec3.ZERO;
        for (AvaliEntity member : members) {
            sum = sum.add(member.position());
        }
        packCenter = members.isEmpty() ? position() : sum.scale(1.0 / members.size());
        if (bed != null && level().isLoaded(bed) && (!isBedHead(bed)
                || Vec3.atCenterOf(bed).distanceTo(packCenter) > behavior.life().bedMaxPackDistance())) {
            bed = null;
        }
    }

    public boolean isBedHead(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.hasProperty(BlockStateProperties.BED_PART) && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD;
    }

    public boolean shouldRegroup() {
        return level().isDarkOutside()
            || level().isThundering() && level().precipitationAt(blockPosition()) == Biome.Precipitation.SNOW;
    }

    public boolean isHotBiome(BlockPos pos) {
        return AvaliDangers.get(level().getServer()).isHotBiome(level().getBiome(pos));
    }

    public boolean isShelter(BlockPos pos) {
        return !level().canSeeSky(pos);
    }

    public boolean isShelteredFromRain() {
        BlockPos pos = blockPosition();
        return level().isRaining() && isShelter(pos)
            && level().getBiome(pos).value().getPrecipitationAt(pos, level().getSeaLevel()) == Biome.Precipitation.RAIN;
    }

    public void walkTowards(Vec3 target, double speed) {
        Vec3 goal = target;
        if (distanceToSqr(target) > DIRECT_WALK_DISTANCE * DIRECT_WALK_DISTANCE) {
            Vec3 step = LandRandomPos.getPosTowards(this, 16, 7, target);
            if (step != null) {
                goal = step;
            }
        }
        getNavigation().moveTo(goal.x, goal.y, goal.z, speed);
    }

    public Vec3 getPackCenter() {
        return packCenter == null ? position() : packCenter;
    }

    public UUID getPackId() {
        return packId;
    }

    public List<UUID> getParents() {
        return parents;
    }

    public BlockPos getBed() {
        return bed;
    }

    public void setBed(BlockPos bed) {
        this.bed = bed;
    }

    private void standUpAfterLoad() {
        if (isSleeping()) {
            stopSleeping();
        }
        if (getVehicle() instanceof Cushion) {
            stopRiding();
        }
        setNapping(false);
        setRestPose(null);
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

    private RawAnimation bodyPoseAnimation() {
        if (isSleeping()) {
            return isNapping() ? NAP : SLEEP_BODY;
        }
        if (isPassenger()) {
            return RIDING;
        }
        AvaliPose pose = getRestPose();
        return pose == null ? EMPTY : pose.animation();
    }

    private void updateGesture() {
        if (getGesture() != null) {
            if (--gestureTicks <= 0 || getMood().earsDown() || isSleeping()) {
                stopGesture();
            }
            return;
        }
        if (!getMood().earsDown() && !isSleeping() && !walkAnimation.isMoving() && random.nextInt(GESTURE_CHANCE) == 0) {
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

    public void startTalking(ServerPlayer player) {
        talkingTo = player;
        getNavigation().stop();
    }

    public void stopTalking() {
        talkingTo = null;
        setDialogueFace(null);
    }

    public ServerPlayer getTalkingTo() {
        return talkingTo;
    }

    public AvaliShop getShop() {
        return shop;
    }

    public void setShop(AvaliShop shop) {
        this.shop = shop;
    }

    public void setDialogueFace(AvaliFace face) {
        entityData.set(DATA_DIALOGUE_FACE, face == null ? -1 : face.ordinal());
    }

    public void setNapping(boolean napping) {
        entityData.set(DATA_NAP, napping);
    }

    public boolean isNapping() {
        return entityData.get(DATA_NAP);
    }

    public void setRestPose(AvaliPose pose) {
        entityData.set(DATA_POSE, pose == null ? -1 : pose.ordinal());
    }

    public AvaliPose getRestPose() {
        int pose = entityData.get(DATA_POSE);
        return pose < 0 ? null : AvaliPose.values()[pose];
    }

    public AvaliFace getFace() {
        if (isSleeping()) {
            return AvaliFace.SLEEPING;
        }
        AvaliGesture gesture = getGesture();
        if (gesture != null && gesture.face() != null) {
            return gesture.face();
        }
        int dialogueFace = entityData.get(DATA_DIALOGUE_FACE);
        return dialogueFace >= 0 ? AvaliFace.values()[dialogueFace] : getMood().face();
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
