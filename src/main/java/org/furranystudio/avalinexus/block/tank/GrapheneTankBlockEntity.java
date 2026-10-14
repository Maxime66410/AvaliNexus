/**
 * File: GrapheneTankBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidContent;

public class GrapheneTankBlockEntity extends BlockEntity {

    public static final int CAPACITY = 16 * AvaliLiquids.BUCKET;

    private FluidContent content = FluidContent.EMPTY;

    public GrapheneTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAPHENE_TANK.get(), pos, state);
    }

    public FluidContent content() {
        return content;
    }

    public void setContent(FluidContent content) {
        if (content.equals(this.content)) {
            return;
        }
        this.content = content;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!content.isEmpty()) {
            output.store("Fluid", FluidContent.CODEC, content);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        content = input.read("Fluid", FluidContent.CODEC).orElse(FluidContent.EMPTY);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!content.isEmpty()) {
            components.set(AvaliLiquids.STORED_FLUID.get(), content);
            components.set(DataComponents.MAX_STACK_SIZE, 1);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        content = components.getOrDefault(AvaliLiquids.STORED_FLUID.get(), FluidContent.EMPTY);
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
