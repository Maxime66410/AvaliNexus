/**
 * File: GraphenePipeBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.block.ModBlockEntities;

public class GraphenePipeBlockEntity extends BlockEntity {

    private int amount;
    private Fluid fluid = Fluids.EMPTY;
    private long handledAt = -1;

    public GraphenePipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAPHENE_PIPE.get(), pos, state);
    }

    public int amount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (this.amount != amount) {
            this.amount = amount;
            setChanged();
        }
    }

    public Fluid fluid() {
        return fluid;
    }

    public void setFluid(Fluid fluid) {
        if (this.fluid.isSame(fluid)) {
            return;
        }
        this.fluid = fluid;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    void markHandled(long time) {
        handledAt = time;
    }

    public void serverTick(ServerLevel level, BlockPos pos) {
        long time = level.getGameTime();
        if (time % PipeNetwork.PERIOD != 0 || handledAt == time) {
            return;
        }
        PipeNetwork.of(level, pos).tick(time);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Amount", amount);
        if (fluid != Fluids.EMPTY) {
            output.store("Fluid", BuiltInRegistries.FLUID.byNameCodec(), fluid);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        amount = input.getIntOr("Amount", 0);
        Fluid loaded = input.read("Fluid", BuiltInRegistries.FLUID.byNameCodec()).orElse(Fluids.EMPTY);
        boolean changed = !loaded.isSame(fluid);
        fluid = loaded;
        if (changed && level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
}
