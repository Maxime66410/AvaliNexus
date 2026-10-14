/**
 * File: GraphenePipeBlockEntity.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.block.ModBlockEntities;

public class GraphenePipeBlockEntity extends BlockEntity {

    private int amount;
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
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        amount = input.getIntOr("Amount", 0);
    }
}
