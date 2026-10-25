/**
 * File: AvaliEntity.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-25
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
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.dialogue.DialogueManager;
import org.furranystudio.avalinexus.effect.ModEffects;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliFace;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliMood;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliCreeperGoal;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.reputation.VillageFields;
import org.furranystudio.avalinexus.entity.avali.goal.CompanionFollowGoal;
import org.furranystudio.avalinexus.entity.avali.goal.CompanionGatherGoal;
import org.furranystudio.avalinexus.entity.avali.goal.CompanionHealerGoal;
import org.furranystudio.avalinexus.entity.avali.goal.CompanionTargetGoal;
import org.furranystudio.avalinexus.entity.avali.goal.CompanionWaitGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliCushionGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliEatGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliFleeGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliFieldGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliGiveGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliHeatGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliHotBiomeGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliMeleeGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliMusicGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliPackGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliPickupGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliRangedGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliRestGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliShelterGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliSleepGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliStrollGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliSwimGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliTalkGoal;
import org.furranystudio.avalinexus.entity.avali.goal.AvaliTargetGoal;
import org.furranystudio.avalinexus.entity.avali.pose.AvaliPose;
import org.furranystudio.avalinexus.sound.ModSounds;
import org.furranystudio.avalinexus.trade.AvaliShop;
import org.furranystudio.avalinexus.trade.TradeData;
import org.furranystudio.avalinexus.worldgen.structure.AvaliSettlements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final int BUSY_CHECK_INTERVAL = 5;
    private static final int REFORGE_INTERVAL = 20;
    private static final int COMPANION_CHECK_INTERVAL = 20;
    private static final int TELEPORT_TRIES = 10;
    private static final int LOVE_INTERVAL = 20;
    private static final double MELEE_REACH = Math.sqrt(2.04F) - 0.6F;
    private static final RawAnimation LOWER_EARS = RawAnimation.begin().thenPlayAndHold("Lower Ears");
    private static final RawAnimation EMPTY = RawAnimation.begin().thenLoop("empty");
    private static final RawAnimation NAP = RawAnimation.begin().thenLoop("NAP");
    private static final RawAnimation SLEEP_BODY = RawAnimation.begin().thenLoop("sleeping");
    private static final RawAnimation SLEEP_EAR = RawAnimation.begin().thenLoop("sleeping_Ear");
    private static final RawAnimation SLEEP_FOOT = RawAnimation.begin().thenLoop("sleeping_Foot");
    private static final RawAnimation RIDING = RawAnimation.begin().thenLoop("riding");
    private static final double HAPPY_RANGE = 6.0;
    private static final int HAPPY_TICKS = 30;
    private static final int ANGRY_PARTICLES = 5;
    private static final int ANGRY_BURST_INTERVAL = 20;
    private static final int ANGRY_BURSTS = 3;
    private static final int GESTURE_CHANCE = 400;
    private static final int GESTURE_MIN_TICKS = 40;
    private static final int GESTURE_MAX_TICKS = 100;
    private static final int PACK_CHECK_INTERVAL = 40;
    private static final int RETURN_TIMEOUT_TICKS = 2400;
    private static final int DEFEND_MEMORY_TICKS = 1200;
    private static final double PACK_JOIN_RANGE = 32.0;
    private static final int DIRECT_WALK_DISTANCE = 24;
    private static final float WATER_MALUS = 16.0F;
    private static final float WATER_BORDER_MALUS = 8.0F;
    private static final float FIRE_NEIGHBOR_MALUS = 16.0F;
    private static final float LEASH_HEIGHT = 24.0F / 31.0F;
    private static final float LEASH_FORWARD = 0.15F;
    private static final Identifier CRIT_BONUS = AvaliNexus.id("crit_bonus");
    private static final AvaliGesture[] IDLE_GESTURES = Arrays.stream(AvaliGesture.values())
        .filter(AvaliGesture::playsWhenIdle).toArray(AvaliGesture[]::new);

    private static final EntityDataAccessor<Integer> DATA_MOOD = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GESTURE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_POSE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_NAP = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_PACK = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_FEMALE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BASE_COLOR = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PATTERN_COLOR = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FEATHERS_COLOR = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_SKIN = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_PROFESSION = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_DIALOGUE_FACE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_OWNER = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_ROLE = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AvaliEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AvaliGear gear = new AvaliGear(this);
    private int swimGraceTicks;
    private float swimPitch;
    private float swimPitchO;
    private int hurtTicks;
    private int angryTicks;
    private AvaliMood hurtMood = AvaliMood.ANGRY;
    private int happyTicks;
    private boolean playerNearby;
    private int gestureTicks;
    private boolean justLoaded;
    private boolean named;
    private boolean styled;
    private boolean layered;
    private boolean panicking;
    private boolean dodging;
    private boolean wasBaby;
    private ServerPlayer talkingTo;
    private UUID packId;
    private List<UUID> parents = List.of();
    private BlockPos bed;
    private Vec3 packCenter;
    private int packCheck;
    private AvaliShop shop;
    private LivingEntity threat;
    private long threatTime;
    private int lastCombatTick;
    private long corneredUntil;
    private AvaliConcert concert;
    private long musicRestUntil;
    private BlockPos waitPos;
    private long returningSince = -1;
    private LivingEntity attackedBy;
    private long attackedTime;
    private final Map<Identifier, Long> songDays = new HashMap<>();
    private UUID kitId;
    private long loveUntil;
    private UUID loveCause;
    private int breedCheck;
    private BlockPos camp;
    private boolean explorer;
    private boolean village;
    private BlockPos workshop;
    private int eggTime = -1;

    public AvaliEntity(EntityType<? extends AvaliEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.FIRE, -1.0F);
        setPathfindingMalus(PathType.FIRE_IN_NEIGHBOR, FIRE_NEIGHBOR_MALUS);
        setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        getNavigation().setCanOpenDoors(true);
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            setGuaranteedDrop(slot);
        }
        breedCheck = random.nextInt(LOVE_INTERVAL * 30);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.ATTACK_DAMAGE, 1.0)
            .add(Attributes.ATTACK_SPEED, 4.0);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToPlayer) {
        return false;
    }

    public static boolean checkAvaliSpawnRules(EntityType<AvaliEntity> type, ServerLevelAccessor level, EntitySpawnReason reason,
                                               BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) <= 8 || AvaliSettlements.isInSettlement(level.getChunk(pos))) {
            return false;
        }
        return level.getBlockState(pos.below()).is(Blocks.SNOW) || Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AvaliSwimGoal(this, 1.0));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(1, new AvaliTalkGoal(this));
        goalSelector.addGoal(1, new AvaliCreeperGoal(this));
        goalSelector.addGoal(2, new AvaliFleeGoal(this));
        goalSelector.addGoal(2, new AvaliHeatGoal(this));
        goalSelector.addGoal(2, new AvaliEatGoal(this));
        goalSelector.addGoal(3, new AvaliMeleeGoal(this));
        goalSelector.addGoal(3, new AvaliRangedGoal(this));
        goalSelector.addGoal(3, new AvaliHotBiomeGoal(this));
        goalSelector.addGoal(3, new CompanionHealerGoal(this));
        goalSelector.addGoal(4, new CompanionGatherGoal(this));
        goalSelector.addGoal(5, new CompanionFollowGoal(this));
        goalSelector.addGoal(5, new CompanionWaitGoal(this));
        goalSelector.addGoal(4, new AvaliSleepGoal(this, 0.8));
        goalSelector.addGoal(4, new AvaliMusicGoal(this));
        goalSelector.addGoal(5, new AvaliPackGoal(this));
        goalSelector.addGoal(6, new AvaliShelterGoal(this));
        goalSelector.addGoal(6, new AvaliGiveGoal(this));
        goalSelector.addGoal(6, new AvaliFieldGoal(this));
        goalSelector.addGoal(6, new AvaliPickupGoal(this));
        goalSelector.addGoal(7, new AvaliCushionGoal(this, 0.8));
        goalSelector.addGoal(7, new AvaliRestGoal(this));
        goalSelector.addGoal(7, new AvaliStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new CompanionTargetGoal(this));
        targetSelector.addGoal(1, new AvaliTargetGoal(this));
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
        builder.define(DATA_BASE_COLOR, 0xFFFFFF);
        builder.define(DATA_PATTERN_COLOR, 0xFFFFFF);
        builder.define(DATA_FEATHERS_COLOR, 0xFFFFFF);
        builder.define(DATA_SKIN, "");
        builder.define(DATA_PROFESSION, "");
        builder.define(DATA_OWNER, "");
        builder.define(DATA_OWNER_NAME, "");
        builder.define(DATA_ROLE, CompanionRole.FIGHTER.ordinal());
        builder.define(DATA_WAITING, false);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && amount > 0.0F) {
            boolean fightsBack = false;
            if (source.getEntity() instanceof LivingEntity attacker && AvaliFight.isValidEnemy(attacker) && !isAlly(attacker)) {
                fightsBack = reactTo(attacker);
            }
            hurtMood = fightsBack ? AvaliMood.ANGRY : AvaliMood.SCARED;
            if (source.getEntity() instanceof ServerPlayer player && !isAlly(player)) {
                AvaliReputation.change(player, getPack(), AvaliReputation.Change.HIT);
            } else if (source.getEntity() instanceof Enemy && source.getEntity() instanceof LivingEntity monster) {
                attackedBy = monster;
                attackedTime = level.getGameTime();
            }
            hurtTicks = AvaliFight.data(this).hurtFaceSeconds() * 20;
            stopGesture();
            if (talkingTo != null) {
                DialogueManager.close(talkingTo, true);
            }
        }
        return hurt;
    }

    private boolean reactTo(LivingEntity attacker) {
        alertPack(attacker);
        return AvaliFight.canAttack(this, AvaliFight.enemies(this), false);
    }

    public boolean canWitness(ServerPlayer thief) {
        return AvaliFight.isValidEnemy(thief) && !isAlly(thief);
    }

    public void witness(ServerPlayer thief, boolean attack) {
        hurtMood = !attack || reactTo(thief) ? AvaliMood.ANGRY : AvaliMood.SCARED;
        hurtTicks = AvaliFight.data(this).hurtFaceSeconds() * 20;
        stopGesture();
        playNoise();
        angryTicks = (ANGRY_BURSTS - 1) * ANGRY_BURST_INTERVAL;
        showAnger();
    }

    private void showAnger() {
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + getBbHeight() * 0.5 + 1.0, getZ(), ANGRY_PARTICLES,
                getBbWidth() * 0.5, getBbHeight() * 0.5, getBbWidth() * 0.5, 0.0);
        }
    }

    private void alertPack(LivingEntity attacker) {
        remember(attacker);
        if (packId == null) {
            return;
        }
        double radius = AvaliFight.data(this).allyRadius();
        for (AvaliEntity member : level().getEntitiesOfClass(AvaliEntity.class, getBoundingBox().inflate(radius),
                other -> other != this && packId.equals(other.packId) && !other.isCompanion() && distanceTo(other) <= radius)) {
            member.remember(attacker);
        }
    }

    public void remember(LivingEntity attacker) {
        threat = attacker;
        threatTime = level().getGameTime();
    }

    public void setCornered(int ticks) {
        corneredUntil = level().getGameTime() + ticks;
    }

    public boolean isCornered() {
        return level().getGameTime() < corneredUntil;
    }

    public LivingEntity getThreat() {
        if (threat == null) {
            return null;
        }
        AvaliCombat.Data data = AvaliFight.data(this);
        if (!AvaliFight.isValidEnemy(threat) || threat.level() != level()
                || level().getGameTime() - threatTime > data.grudgeSeconds() * 20L || distanceTo(threat) > data.grudgeDistance()) {
            threat = null;
        }
        return threat;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (isAlly(target)) {
            return false;
        }
        ItemStack weapon = getWeaponItem();
        boolean critical = canCriticalAttack(target);
        AttributeInstance damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (critical && damage != null) {
            damage.addTransientModifier(new AttributeModifier(CRIT_BONUS,
                AvaliFight.data(this).crit().multiplier() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        boolean hit = super.doHurtTarget(level, target);
        if (damage != null) {
            damage.removeModifier(CRIT_BONUS);
        }
        if (hit && critical) {
            playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
            level.getChunkSource().sendToTrackingPlayersAndSelf(target, new ClientboundAnimatePacket(target, ClientboundAnimatePacket.CRITICAL_HIT));
        }
        if (hit && target instanceof LivingEntity living) {
            weapon.postHurtEnemy(living, this);
            gear.putAwayHilt();
        }
        return hit;
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        ItemStack weapon = getMainHandItem();
        double bonus = weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .compute(Attributes.ENTITY_INTERACTION_RANGE, 0.0, EquipmentSlot.MAINHAND);
        if (weapon.has(DataComponents.ATTACK_RANGE) || bonus <= 0.0) {
            return super.isWithinMeleeAttackRange(target);
        }
        return getAttackBoundingBox(MELEE_REACH + bonus).intersects(target.getBoundingBox());
    }

    public boolean isTooCloseFor(LivingEntity target) {
        AttackRange range = getMainHandItem().get(DataComponents.ATTACK_RANGE);
        return range != null && range.effectiveMinRange(this) > 0.0F
            && getAttackBoundingBox(range.effectiveMinRange(this)).intersects(target.getBoundingBox());
    }

    public boolean canCriticalAttack(Entity target) {
        return fallDistance > 0.0 && !onGround() && !onClimbable() && !isInWater() && !hasEffect(MobEffects.BLINDNESS)
            && !isPassenger() && target instanceof LivingEntity && !isSprinting();
    }

    @Override
    public ItemStack getItemBlockingWith() {
        return isUsingItem() && getUseItem().has(DataComponents.BLOCKS_ATTACKS) ? getUseItem() : null;
    }

    @Override
    public float applyItemBlocking(ServerLevel level, DamageSource source, float damage) {
        ItemStack shield = getItemBlockingWith();
        InteractionHand hand = getUsedItemHand();
        float blocked = super.applyItemBlocking(level, source, damage);
        BlocksAttacks blocks = shield == null ? null : shield.get(DataComponents.BLOCKS_ATTACKS);
        if (blocked > 0.0F && blocks != null) {
            int wear = blocks.itemDamage().apply(blocked);
            if (wear > 0) {
                shield.hurtAndBreak(wear, this, hand.asEquipmentSlot());
            }
        }
        return blocked;
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
        output.putBoolean("avalinexus_layers", layered);
        output.putBoolean("avalinexus_female", isFemale());
        output.putInt("avalinexus_tint", entityData.get(DATA_BASE_COLOR));
        output.putInt("avalinexus_pattern_color", entityData.get(DATA_PATTERN_COLOR));
        output.putInt("avalinexus_feathers_color", entityData.get(DATA_FEATHERS_COLOR));
        output.putString("avalinexus_skin", getSkin());
        output.putString("avalinexus_profession", getProfession());
        output.storeNullable("avalinexus_pack_id", UUIDUtil.CODEC, packId);
        output.store("avalinexus_parents", UUIDUtil.CODEC.listOf(), parents);
        output.storeNullable("avalinexus_bed", BlockPos.CODEC, bed);
        output.storeNullable("avalinexus_owner", UUIDUtil.CODEC, getOwnerId());
        output.putString("avalinexus_owner_name", entityData.get(DATA_OWNER_NAME));
        output.putInt("avalinexus_role", getRole().ordinal());
        output.putBoolean("avalinexus_waiting", isWaiting());
        output.storeNullable("avalinexus_wait_pos", BlockPos.CODEC, waitPos);
        output.putLong("avalinexus_returning_since", returningSince);
        output.storeNullable("avalinexus_kit", UUIDUtil.CODEC, kitId);
        output.storeNullable("avalinexus_camp", BlockPos.CODEC, camp);
        output.putBoolean("avalinexus_village", village);
        output.storeNullable("avalinexus_workshop", BlockPos.CODEC, workshop);
        output.putInt("avalinexus_egg_time", eggTime);
        gear.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        justLoaded = true;
        shop = input.read("avalinexus_shop", AvaliShop.CODEC).orElse(null);
        setPack(input.getStringOr("avalinexus_pack", ""));
        named = input.getBooleanOr("avalinexus_named", false);
        styled = input.getBooleanOr("avalinexus_styled", false);
        layered = input.getBooleanOr("avalinexus_layers", false);
        entityData.set(DATA_FEMALE, input.getBooleanOr("avalinexus_female", false));
        entityData.set(DATA_BASE_COLOR, input.getIntOr("avalinexus_tint", 0xFFFFFF));
        entityData.set(DATA_PATTERN_COLOR, input.getIntOr("avalinexus_pattern_color", 0xFFFFFF));
        entityData.set(DATA_FEATHERS_COLOR, input.getIntOr("avalinexus_feathers_color", 0xFFFFFF));
        entityData.set(DATA_SKIN, input.getStringOr("avalinexus_skin", ""));
        entityData.set(DATA_PROFESSION, input.getStringOr("avalinexus_profession", ""));
        packId = input.read("avalinexus_pack_id", UUIDUtil.CODEC).orElse(null);
        parents = input.read("avalinexus_parents", UUIDUtil.CODEC.listOf()).orElse(List.of());
        bed = input.read("avalinexus_bed", BlockPos.CODEC).orElse(null);
        entityData.set(DATA_OWNER, input.read("avalinexus_owner", UUIDUtil.CODEC).map(UUID::toString).orElse(""));
        entityData.set(DATA_OWNER_NAME, input.getStringOr("avalinexus_owner_name", ""));
        entityData.set(DATA_ROLE, Math.clamp(input.getIntOr("avalinexus_role", 0), 0, CompanionRole.values().length - 1));
        entityData.set(DATA_WAITING, input.getBooleanOr("avalinexus_waiting", false));
        waitPos = input.read("avalinexus_wait_pos", BlockPos.CODEC).orElse(null);
        returningSince = input.getLongOr("avalinexus_returning_since", -1L);
        kitId = input.read("avalinexus_kit", UUIDUtil.CODEC).orElse(null);
        camp = input.read("avalinexus_camp", BlockPos.CODEC).orElse(null);
        village = input.getBooleanOr("avalinexus_village", false);
        workshop = input.read("avalinexus_workshop", BlockPos.CODEC).orElse(null);
        eggTime = input.getIntOr("avalinexus_egg_time", -1);
        gear.load(input);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isSecondaryUseActive() || isSleeping()) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer && isBusy()) {
            serverPlayer.sendSystemMessage(Component.translatable("avalinexus.dialogue.busy"), true);
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && AvaliBreeding.useItem(this, serverPlayer, hand)) {
            return InteractionResult.SUCCESS;
        }
        if (getMood().earsDown()) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            DialogueManager.open(serverPlayer, this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isQuiet() ? null : ModSounds.AVALI_NOISE.get();
    }

    private boolean isQuiet() {
        return !level().isClientSide() && (isSleeping() || isNapping() || talkingTo != null || isInWater() || isBusy());
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
            kit.camp = camp;
            kit.village = village;
            kit.parents = List.of(getUUID(), partner.getUUID());
            kit.inheritLooks(this, partner instanceof AvaliEntity other ? other : this);
        }
        return kit;
    }

    // Avalis spawned together form one pack and share its name
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData data) {
        if (!(data instanceof PackData)) {
            AvaliEntity nearest = reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.DISPENSER ? nearestPack(level) : null;
            data = nearest != null ? new PackData(nearest.getPack(), nearest.packId)
                : new PackData(AvaliNames.pack(level.getServer(), getRandom()), UUID.randomUUID());
        }
        setPack(((PackData) data).pack);
        packId = ((PackData) data).id;
        giveName(level.getServer());
        giveLooks(level.getServer());
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (!isBaby() && reason != EntitySpawnReason.BREEDING) {
            gear.giveSpawnGear(getRandom());
            pairOnSpawn((PackData) data);
        }
        return result;
    }

    public void joinCamp(ServerLevelAccessor level, BlockPos camp, String pack, UUID packId) {
        this.camp = camp;
        finalizeSpawn(level, level.getCurrentDifficultyAt(blockPosition()), EntitySpawnReason.STRUCTURE, new PackData(pack, packId));
    }

    public void joinVillage(ServerLevelAccessor level, BlockPos plaza, String pack, UUID packId, BlockPos bed, BlockPos workshop, String shop) {
        village = true;
        this.bed = bed;
        this.workshop = workshop;
        if (shop != null) {
            setProfession(shop);
        }
        joinCamp(level, plaza, pack, packId);
    }

    private void tickEgg() {
        AvaliBreeding.Egg config = AvaliBreeding.get(level().getServer()).egg();
        if (eggTime < 0) {
            eggTime = Mth.nextInt(random, config.minSeconds(), config.maxSeconds()) * 20;
        }
        if (--eggTime <= 0) {
            spawnAtLocation((ServerLevel) level(), new ItemStack(ModItems.AVALI_EGG.get()));
            playSound(SoundEvents.CHICKEN_EGG, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            gameEvent(GameEvent.ENTITY_PLACE);
            eggTime = -1;
        }
    }

    public static void hatch(ServerLevel level, Vec3 pos, float yRot) {
        AvaliEntity kit = ModEntities.AVALI.get().create(level, EntitySpawnReason.TRIGGERED);
        if (kit == null) {
            return;
        }
        kit.setBaby(true);
        kit.snapTo(pos.x, pos.y, pos.z, yRot, 0.0F);
        AvaliEntity member = kit.openPack(level);
        PackData data = member != null ? new PackData(member.getPack(), member.packId)
            : new PackData(AvaliNames.pack(level.getServer(), kit.getRandom()), UUID.randomUUID());
        if (member != null) {
            kit.camp = member.camp;
            kit.village = member.village;
        }
        kit.finalizeSpawn(level, level.getCurrentDifficultyAt(kit.blockPosition()), EntitySpawnReason.TRIGGERED, data);
        level.addFreshEntityWithPassengers(kit);
    }

    private AvaliEntity openPack(ServerLevel level) {
        double radius = AvaliBehavior.get(level.getServer()).pack().eggJoinRadius();
        int max = AvaliBreeding.get(level.getServer()).baby().packMax();
        List<AvaliEntity> nearby = new ArrayList<>(level.getEntitiesOfClass(AvaliEntity.class, getBoundingBox().inflate(radius),
            other -> other.packId != null && !other.isCompanion() && distanceTo(other) <= radius));
        nearby.sort((first, second) -> Double.compare(distanceToSqr(first), distanceToSqr(second)));
        Set<UUID> checked = new HashSet<>();
        for (AvaliEntity other : nearby) {
            if (checked.add(other.packId) && other.packMembers().size() < max) {
                return other;
            }
        }
        return null;
    }

    private AvaliEntity nearestPack(ServerLevelAccessor level) {
        double radius = AvaliBehavior.get(level.getServer()).pack().eggJoinRadius();
        return level.getEntitiesOfClass(AvaliEntity.class, getBoundingBox().inflate(radius),
                other -> other != this && other.packId != null && !other.isCompanion() && distanceTo(other) <= radius)
            .stream().min((first, second) -> Double.compare(distanceToSqr(first), distanceToSqr(second))).orElse(null);
    }

    private void pairOnSpawn(PackData pack) {
        AvaliEntity mate = pack.single.stream().filter(other -> AvaliBreeding.canBond(this, other)).findFirst().orElse(null);
        if (mate == null) {
            pack.single.add(this);
            return;
        }
        pack.single.remove(mate);
        BondData.get(level().getServer()).link(this, mate);
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
        setColors(AvaliAppearance.rollColors(server, getPack(), getRandom()));
        entityData.set(DATA_SKIN, AvaliAppearance.rollRare(server, getRandom()));
    }

    private void giveLayers(net.minecraft.server.MinecraftServer server) {
        AvaliAppearance.Colors rolled = AvaliAppearance.rollColors(server, getPack(), getRandom());
        setColors(new AvaliAppearance.Colors(getColors().base(), rolled.pattern(), rolled.feathers()));
    }

    private void setColors(AvaliAppearance.Colors colors) {
        layered = true;
        entityData.set(DATA_BASE_COLOR, colors.base());
        entityData.set(DATA_PATTERN_COLOR, colors.pattern());
        entityData.set(DATA_FEATHERS_COLOR, colors.feathers());
    }

    private void inheritLooks(AvaliEntity mother, AvaliEntity father) {
        net.minecraft.server.MinecraftServer server = level().getServer();
        styled = true;
        entityData.set(DATA_FEMALE, AvaliAppearance.rollFemale(server, getRandom()));
        setColors(AvaliAppearance.mixColors(server, mother.getColors(), father.getColors(), getRandom()));
        List<String> rares = new ArrayList<>();
        for (AvaliEntity parent : List.of(mother, father)) {
            if (!parent.getSkin().isEmpty()) {
                rares.add(parent.getSkin());
            }
        }
        entityData.set(DATA_SKIN, !rares.isEmpty() && getRandom().nextFloat() < AvaliBreeding.get(server).birth().rareInheritChance()
            ? rares.get(getRandom().nextInt(rares.size())) : AvaliAppearance.rollRare(server, getRandom()));
    }

    public boolean isFemale() {
        return entityData.get(DATA_FEMALE);
    }

    public AvaliAppearance.Colors getColors() {
        return new AvaliAppearance.Colors(entityData.get(DATA_BASE_COLOR), entityData.get(DATA_PATTERN_COLOR), entityData.get(DATA_FEATHERS_COLOR));
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
        Component pack = Component.translatable("avalinexus.pack." + getPack());
        if (isBaby() || getProfession().isEmpty()) {
            return Component.translatable("avalinexus.avali.title", name, pack);
        }
        Component job = Component.translatable("avalinexus.profession." + getProfession() + (isFemale() ? ".female" : ""));
        return Component.translatable("avalinexus.avali.title_job", name, pack, job);
    }

    public String getProfession() {
        return entityData.get(DATA_PROFESSION);
    }

    public void setProfession(String profession) {
        entityData.set(DATA_PROFESSION, profession);
    }

    private void assignProfession() {
        net.minecraft.server.MinecraftServer server = level().getServer();
        TradeData.Settings settings = TradeData.settings(server);
        String inherited = "";
        if (!parents.isEmpty() && random.nextFloat() < settings.inheritChance()
                && level().getEntity(parents.get(random.nextInt(parents.size()))) instanceof AvaliEntity parent
                && settings.professions().contains(parent.getProfession())) {
            inherited = parent.getProfession();
        }
        String fieldJob = !parents.isEmpty() && village && camp != null ? VillageFields.job((ServerLevel) level(), camp) : null;
        if (fieldJob != null && packMembers().stream().noneMatch(member -> member != this && !member.isBaby() && fieldJob.equals(member.getProfession()))) {
            setProfession(fieldJob);
            return;
        }
        setProfession(inherited.isEmpty() ? TradeData.randomProfession(server, random) : inherited);
    }

    private static final class PackData extends AgeableMob.AgeableMobGroupData {
        private final String pack;
        private final UUID id;
        private final List<AvaliEntity> single = new ArrayList<>();

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
            if (!isBaby() && getProfession().isEmpty()) {
                assignProfession();
            }
            if (!styled) {
                giveLooks(level().getServer());
            } else if (!layered) {
                giveLayers(level().getServer());
            }
            if (--packCheck <= 0) {
                packCheck = PACK_CHECK_INTERVAL;
                updatePack();
            }
            if (angryTicks > 0 && --angryTicks % ANGRY_BURST_INTERVAL == 0) {
                showAnger();
            }
            updateMood();
            updateGesture();
            updateWeapon();
            if (concert != null && concert.isEnded()) {
                concert = null;
            }
            if (tickCount % REFORGE_INTERVAL == 0) {
                gear.tickHilts();
            }
            if (isCompanion() && tickCount % COMPANION_CHECK_INTERVAL == 0) {
                updateCompanion();
            }
            if (attackedBy != null && tickCount % COMPANION_CHECK_INTERVAL == 0) {
                updateDefenders();
            }
            if (!isBaby() && !isCompanion() && --breedCheck <= 0) {
                breedCheck = AvaliBreeding.get(level().getServer()).checkSeconds() * 20;
                AvaliBreeding.tick(this);
            }
            if (isFemale() && !isBaby() && !isCompanion()) {
                tickEgg();
            }
            if (isInLove() && tickCount % LOVE_INTERVAL == 0) {
                if (level().getGameTime() >= loveUntil) {
                    stopLove();
                } else {
                    AvaliBreeding.tickLove(this);
                }
            }
            if (talkingTo != null && (!talkingTo.isAlive() || talkingTo.level() != level()
                    || distanceTo(talkingTo) > DialogueManager.MAX_DISTANCE)) {
                DialogueManager.close(talkingTo, true);
            }
            if (talkingTo != null && tickCount % BUSY_CHECK_INTERVAL == 0 && isBusy()) {
                ServerPlayer player = talkingTo;
                DialogueManager.close(player, true);
                player.sendSystemMessage(Component.translatable("avalinexus.dialogue.busy"), true);
            }
        }
    }

    private void updateWeapon() {
        if (getTarget() != null) {
            lastCombatTick = tickCount;
        } else if (tickCount - lastCombatTick > AvaliFight.data(this).weaponStowSeconds() * 20) {
            gear.stowWeapon();
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
        List<AvaliEntity> members = packMembers();
        Vec3 sum = Vec3.ZERO;
        for (AvaliEntity member : members) {
            sum = sum.add(member.position());
        }
        packCenter = camp != null ? Vec3.atBottomCenterOf(camp) : members.isEmpty() ? position() : sum.scale(1.0 / members.size());
        explorer = camp != null && workshop == null && isElectedExplorer(members, settlement());
        if (!isCompanion() && returningSince < 0) {
            PackHomeData.get(level().getServer()).update(packId, BlockPos.containing(packCenter));
        }
        if (returningSince >= 0) {
            Vec3 home = getReturnTarget();
            if (position().distanceTo(home) <= behavior.pack().regroupRadius()) {
                returningSince = -1;
            } else if (level().getGameTime() - returningSince > RETURN_TIMEOUT_TICKS) {
                returningSince = -1;
                teleportNear(BlockPos.containing(home), true);
            }
        }
        if (bed != null && level().isLoaded(bed) && (!isBedHead(bed)
                || Vec3.atCenterOf(bed).distanceTo(packCenter) > behavior.life().bedMaxPackDistance())) {
            bed = null;
        }
    }

    private boolean isElectedExplorer(List<AvaliEntity> members, AvaliBehavior.Camp settings) {
        int count = settings.explorersMin() + Math.floorMod(packId.hashCode(), settings.explorersMax() - settings.explorersMin() + 1);
        List<UUID> adults = members.stream().filter(member -> !member.isBaby() && !member.isCompanion() && member.workshop == null)
            .map(Entity::getUUID).sorted().toList();
        int rank = adults.indexOf(getUUID());
        return rank >= 0 && rank < count;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, getBbHeight() * LEASH_HEIGHT, getBbWidth() * LEASH_FORWARD);
    }

    public BlockPos getCamp() {
        return camp;
    }

    public boolean isVillager() {
        return village;
    }

    public boolean isExplorer() {
        return explorer;
    }

    public BlockPos getWorkshop() {
        return workshop;
    }

    public AvaliBehavior.Camp settlement() {
        AvaliBehavior.Data behavior = AvaliBehavior.get(level().getServer());
        return village ? behavior.village().settlement() : behavior.camp();
    }

    public double packRange() {
        if (camp == null) {
            return AvaliBehavior.get(level().getServer()).pack().maxDistance();
        }
        return explorer && !shouldRegroup() ? settlement().explorerRadius() : settlement().anchorRadius();
    }

    public List<AvaliEntity> packMembers() {
        if (packId == null) {
            return List.of(this);
        }
        return level().getEntitiesOfClass(AvaliEntity.class, getBoundingBox().inflate(AvaliBehavior.get(level().getServer()).pack().maxDistance() * 2),
            other -> packId.equals(other.packId) && (other == this || !other.isCompanion()));
    }

    public boolean isBedHead(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        return state.hasProperty(BlockStateProperties.BED_PART) && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD;
    }

    public boolean shouldRegroup() {
        return level().isDarkOutside()
            || level().isThundering() && level().precipitationAt(blockPosition()) == Biome.Precipitation.SNOW;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        ServerPlayer killer = source.getEntity() instanceof ServerPlayer player ? player
            : getKillCredit() instanceof ServerPlayer player ? player : null;
        if (killer != null && !isAlly(killer)) {
            AvaliReputation.change(killer, getPack(), isBaby() ? AvaliReputation.Change.KILL_BABY : AvaliReputation.Change.KILL_ADULT);
        }
        if (level() instanceof ServerLevel level) {
            BondData.get(level.getServer()).remove(getUUID());
        }
        if (level() instanceof ServerLevel level && isCompanion()) {
            ServerPlayer owner = getOwner();
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("avalinexus.companion.died", getName()));
            }
            CompanionData.get(level.getServer()).remove(getOwnerId(), getUUID());
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        gear.dropAll(level);
    }

    @Override
    public void hurtArmor(DamageSource source, float damage) {
        doHurtEquipment(source, damage, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }

    @Override
    public void hurtHelmet(DamageSource source, float damage) {
        doHurtEquipment(source, damage, EquipmentSlot.HEAD);
    }

    public boolean isCompanion() {
        return !entityData.get(DATA_OWNER).isEmpty();
    }

    public UUID getOwnerId() {
        String owner = entityData.get(DATA_OWNER);
        return owner.isEmpty() ? null : UUID.fromString(owner);
    }

    public boolean isOwnedBy(Player player) {
        return isCompanion() && player.getUUID().equals(getOwnerId());
    }

    public ServerPlayer getOwner() {
        UUID owner = getOwnerId();
        return owner == null || level().getServer() == null ? null : level().getServer().getPlayerList().getPlayer(owner);
    }

    public boolean isAlly(Entity entity) {
        if (!isCompanion() || entity == null) {
            return false;
        }
        UUID owner = getOwnerId();
        return entity.getUUID().equals(owner) || entity instanceof AvaliEntity other && owner.equals(other.getOwnerId());
    }

    public boolean isPackmate(AvaliEntity other) {
        if (isCompanion()) {
            return isAlly(other);
        }
        return packId != null && packId.equals(other.packId) && !other.isCompanion();
    }

    public CompanionRole getRole() {
        return CompanionRole.values()[entityData.get(DATA_ROLE)];
    }

    public void setRole(CompanionRole role) {
        entityData.set(DATA_ROLE, role.ordinal());
    }

    public boolean isWaiting() {
        return entityData.get(DATA_WAITING);
    }

    public boolean isFollowingOwner() {
        return isCompanion() && !isWaiting();
    }

    public void setWaiting(boolean waiting) {
        entityData.set(DATA_WAITING, waiting);
        waitPos = waiting ? blockPosition() : null;
        getNavigation().stop();
    }

    public BlockPos getWaitPos() {
        return waitPos == null ? blockPosition() : waitPos;
    }

    public void recruit(ServerPlayer player) {
        entityData.set(DATA_OWNER, player.getUUID().toString());
        entityData.set(DATA_OWNER_NAME, player.getGameProfile().name());
        setRole(CompanionRole.FIGHTER);
        setWaiting(false);
        setTarget(null);
        CompanionData.get(player.level().getServer()).add(player.getUUID(), getUUID());
    }

    public void dismiss() {
        if (level() instanceof ServerLevel level && isCompanion()) {
            CompanionData.get(level.getServer()).remove(getOwnerId(), getUUID());
        }
        entityData.set(DATA_OWNER, "");
        entityData.set(DATA_OWNER_NAME, "");
        setRole(CompanionRole.FIGHTER);
        setWaiting(false);
        setTarget(null);
        returningSince = level().getGameTime();
        packCheck = 0;
        Vec3 home = getReturnTarget();
        if (position().distanceTo(home) > AvaliCompanions.get(level().getServer()).homeTeleportDistance()
                || !level().isLoaded(BlockPos.containing(home))) {
            returningSince = -1;
            teleportNear(BlockPos.containing(home), true);
        }
    }

    public boolean isReturningHome() {
        return returningSince >= 0;
    }

    public UUID getPartnerId() {
        BondData.Bond bond = level().getServer() == null ? null : BondData.get(level().getServer()).bond(getUUID());
        return bond == null ? null : bond.partner();
    }

    public String getPartnerName() {
        BondData.Bond bond = level().getServer() == null ? null : BondData.get(level().getServer()).bond(getUUID());
        return bond == null ? "" : bond.name();
    }

    public AvaliEntity getPartner() {
        UUID partner = getPartnerId();
        return partner != null && level() instanceof ServerLevel level && level.getEntity(partner) instanceof AvaliEntity avali && avali.isAlive()
            ? avali : null;
    }

    public boolean hasGrowingKit() {
        return kitId != null && level() instanceof ServerLevel level && level.getEntity(kitId) instanceof AvaliEntity kit && kit.isAlive() && kit.isBaby();
    }

    public void setKit(UUID kit) {
        kitId = kit;
    }

    public void startLove(ServerPlayer player, int ticks) {
        loveCause = player.getUUID();
        loveUntil = level().getGameTime() + ticks;
    }

    public void stopLove() {
        loveCause = null;
        loveUntil = 0;
    }

    public boolean isInLove() {
        return loveUntil > 0;
    }

    public ServerPlayer getLoveCause() {
        return loveCause == null || level().getServer() == null ? null : level().getServer().getPlayerList().getPlayer(loveCause);
    }

    public void joinPackOf(AvaliEntity other) {
        setPack(other.getPack());
        packId = other.packId;
        camp = other.camp;
        village = other.village;
        bed = null;
        returningSince = level().getGameTime();
        packCheck = 0;
    }

    public void spawnLoner(double minDistance, double maxDistance) {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = getPackCenter();
        for (int i = 0; i < TELEPORT_TRIES; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = Mth.nextDouble(random, minDistance, maxDistance);
            int x = Mth.floor(center.x + Math.cos(angle) * distance);
            int z = Mth.floor(center.z + Math.sin(angle) * distance);
            if (!level.isLoaded(BlockPos.containing(x, center.y, z))) {
                continue;
            }
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (!level.getFluidState(pos.below()).isEmpty() || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                continue;
            }
            AvaliEntity loner = ModEntities.AVALI.get().create(level, EntitySpawnReason.EVENT);
            if (loner == null) {
                return;
            }
            loner.snapTo(x + 0.5, pos.getY(), z + 0.5, random.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(loner)) {
                continue;
            }
            loner.camp = camp;
            loner.village = village;
            loner.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, new PackData(getPack(), packId));
            loner.entityData.set(DATA_FEMALE, !isFemale());
            level.addFreshEntityWithPassengers(loner);
            return;
        }
    }

    @Override
    protected void onEffectAdded(MobEffectInstance effect, Entity source) {
        super.onEffectAdded(effect, source);
        if (!level().isClientSide() && effect.getEffect().value() == ModEffects.NEXITE_TONIC.get()) {
            AvaliBreeding.onTonic(this, source);
        }
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance effect, boolean refresh, Entity source) {
        super.onEffectUpdated(effect, refresh, source);
        if (!level().isClientSide() && effect.getEffect().value() == ModEffects.NEXITE_TONIC.get()) {
            AvaliBreeding.onTonic(this, source);
        }
    }

    public Vec3 getReturnTarget() {
        BlockPos home = packId == null || level().getServer() == null ? null : PackHomeData.get(level().getServer()).home(packId);
        return home == null ? getPackCenter() : Vec3.atBottomCenterOf(home);
    }

    private void updateDefenders() {
        if (!attackedBy.isAlive()) {
            if (attackedBy.getKillCredit() instanceof ServerPlayer player) {
                AvaliReputation.change(player, getPack(), AvaliReputation.Change.DEFEND);
            }
            attackedBy = null;
        } else if (level().getGameTime() - attackedTime > DEFEND_MEMORY_TICKS || attackedBy.level() != level()) {
            attackedBy = null;
        }
    }

    private void updateCompanion() {
        if (isWaiting()) {
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null || owner.level() != level() || owner.isDeadOrDying() || owner.isSpectator()) {
            setWaiting(true);
            return;
        }
        if (!isPassenger() && distanceTo(owner) > AvaliCompanions.get(level().getServer()).teleportDistance()) {
            teleportNear(owner.blockPosition(), false);
        }
    }

    private void teleportNear(BlockPos center, boolean surface) {
        for (int i = 0; i < TELEPORT_TRIES; i++) {
            BlockPos pos = center.offset(Mth.nextInt(random, -3, 3), Mth.nextInt(random, -1, 1), Mth.nextInt(random, -3, 3));
            if (Math.abs(pos.getX() - center.getX()) < 2 && Math.abs(pos.getZ() - center.getZ()) < 2) {
                continue;
            }
            if (level().getBlockState(pos.below()).isFaceSturdy(level(), pos.below(), net.minecraft.core.Direction.UP)
                    && level().noCollision(this, getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(position())))) {
                stopRiding();
                teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                getNavigation().stop();
                return;
            }
        }
        for (int i = 0; surface && i < TELEPORT_TRIES; i++) {
            int x = center.getX() + Mth.nextInt(random, -3, 3);
            int z = center.getZ() + Mth.nextInt(random, -3, 3);
            BlockPos pos = new BlockPos(x, level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level().getFluidState(pos.below()).isEmpty()
                    && level().getBlockState(pos.below()).isFaceSturdy(level(), pos.below(), net.minecraft.core.Direction.UP)) {
                stopRiding();
                teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                getNavigation().stop();
                return;
            }
        }
    }

    @Override
    public Component getDisplayName() {
        Component name = super.getDisplayName();
        String owner = entityData.get(DATA_OWNER_NAME);
        return owner.isEmpty() ? name : Component.translatable("avalinexus.companion.name", name, owner);
    }

    public AvaliGear getGear() {
        return gear;
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
        if (hurtTicks > 0) {
            hurtTicks--;
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
        if (panicking || dodging) {
            mood = AvaliMood.SCARED;
        } else if (getTarget() != null) {
            mood = AvaliMood.ANGRY;
        } else if (hurtTicks > 0) {
            mood = hurtMood;
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

    public AvaliConcert getConcert() {
        return concert;
    }

    public void setConcert(AvaliConcert concert) {
        this.concert = concert;
    }

    public long getMusicRestUntil() {
        return musicRestUntil;
    }

    public void setMusicRestUntil(long musicRestUntil) {
        this.musicRestUntil = musicRestUntil;
    }

    public boolean playedToday(Identifier song, long day) {
        return songDays.getOrDefault(song, -1L) == day;
    }

    public void markPlayed(Identifier song, long day) {
        songDays.put(song, day);
    }

    public void setDodging(boolean dodging) {
        this.dodging = dodging;
    }

    public boolean isBusy() {
        if (getTarget() != null || panicking || dodging || concert != null) {
            return true;
        }
        return AvaliFight.swellingCreeper(this) != null || AvaliFight.shouldFlee(this, AvaliFight.enemies(this));
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
        AvaliMood mood = getMood();
        if (mood.earsDown()) {
            return mood.face();
        }
        int dialogueFace = entityData.get(DATA_DIALOGUE_FACE);
        if (dialogueFace >= 0) {
            return AvaliFace.values()[dialogueFace];
        }
        AvaliGesture gesture = getGesture();
        return gesture != null && gesture.face() != null ? gesture.face() : mood.face();
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
