/**
 * File: AvaliTapestry.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.entity.tapestry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.item.TapestryItems;

// Hangs like an item frame but only cares about the block holding it
// Building blocks can go in its spot without popping it, which is the whole point of it being an entity
public class AvaliTapestry extends HangingEntity {

    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AvaliTapestry.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PATTERN = SynchedEntityData.defineId(AvaliTapestry.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TURN = SynchedEntityData.defineId(AvaliTapestry.class, EntityDataSerializers.INT);
    // Quarter turns only, a square cloth turned 45 degrees would stick out of its block
    private static final int TURNS = 4;
    // Cloth thickness, the same one pixel as the item model it renders
    private static final double THICKNESS = 1.0 / 16.0;
    private static final double WALL_OFFSET = 0.5 - THICKNESS / 2.0;

    public AvaliTapestry(EntityType<? extends AvaliTapestry> type, Level level) {
        super(type, level);
    }

    public AvaliTapestry(Level level, BlockPos pos, Direction direction, TapestryPattern pattern, DyeColor color) {
        super(ModEntities.AVALI_TAPESTRY.get(), level, pos);
        setPattern(pattern);
        setColor(color);
        setDirection(direction);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR, DyeColor.WHITE.getId());
        builder.define(DATA_PATTERN, TapestryPattern.BRAID.ordinal());
        builder.define(DATA_TURN, 0);
    }

    public DyeColor getColor() {
        return DyeColor.byId(entityData.get(DATA_COLOR));
    }

    public void setColor(DyeColor color) {
        entityData.set(DATA_COLOR, color.getId());
    }

    public TapestryPattern getPattern() {
        return TapestryPattern.byId(entityData.get(DATA_PATTERN));
    }

    public void setPattern(TapestryPattern pattern) {
        entityData.set(DATA_PATTERN, pattern.ordinal());
    }

    public int getTurn() {
        return entityData.get(DATA_TURN);
    }

    public void setTurn(int turn) {
        entityData.set(DATA_TURN, Math.floorMod(turn, TURNS));
    }

    // Empty hand only, so aiming at it with a block in hand never turns it by mistake
    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide()) {
            setTurn(getTurn() + 1);
            playSound(SoundEvents.WOOL_HIT, 1.0F, 1.0F);
            gameEvent(GameEvent.BLOCK_CHANGE, player);
        }
        return InteractionResult.SUCCESS;
    }

    // Vanilla only allows walls here, the item frame version also allows floors and ceilings
    @Override
    protected void setDirection(Direction direction) {
        setDirectionRaw(direction);
        if (direction.getAxis().isHorizontal()) {
            setXRot(0.0F);
            setYRot(direction.get2DDataValue() * 90);
        } else {
            setXRot(-90 * direction.getAxisDirection().getStep());
            setYRot(0.0F);
        }
        xRotO = getXRot();
        yRotO = getYRot();
        recalculateBoundingBox();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        Vec3 center = Vec3.atCenterOf(pos).relative(direction, -WALL_OFFSET);
        Direction.Axis axis = direction.getAxis();
        return AABB.ofSize(center,
            axis == Direction.Axis.X ? THICKNESS : 1.0,
            axis == Direction.Axis.Y ? THICKNESS : 1.0,
            axis == Direction.Axis.Z ? THICKNESS : 1.0);
    }

    @Override
    public boolean survives() {
        BlockPos support = pos.relative(getDirection().getOpposite());
        return level().getBlockState(support).isSolid();
    }

    // As tough as the nanocanvas it is made of
    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void dropItem(ServerLevel level, Entity causedBy) {
        playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.0F);
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }
        if (causedBy instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        spawnAtLocation(level, getPickResult());
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TapestryItems.get(getPattern(), getColor()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Color", (byte) getColor().getId());
        output.putByte("Pattern", (byte) getPattern().ordinal());
        output.putByte("Turn", (byte) getTurn());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setColor(DyeColor.byId(input.getByteOr("Color", (byte) 0)));
        setPattern(TapestryPattern.byId(input.getByteOr("Pattern", (byte) 0)));
        setTurn(input.getByteOr("Turn", (byte) 0));
    }

    // The client needs the facing right away to place the box, same as the item frame
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, getDirection().get3DDataValue(), getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        setDirection(Direction.from3DDataValue(packet.getData()));
    }
}
