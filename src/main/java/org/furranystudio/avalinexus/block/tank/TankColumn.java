/**
 * File: TankColumn.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.fluid.FluidContent;

import java.util.ArrayList;
import java.util.List;

public final class TankColumn {

    public static final int MAX_HEIGHT = 8;

    private final List<GrapheneTankBlockEntity> tanks;

    private TankColumn(List<GrapheneTankBlockEntity> tanks) {
        this.tanks = tanks;
    }

    public static TankColumn at(Level level, BlockPos pos) {
        BlockPos bottom = pos;
        while (isTank(level, bottom.below())) {
            bottom = bottom.below();
        }
        List<GrapheneTankBlockEntity> tanks = new ArrayList<>();
        for (BlockPos cursor = bottom; level.getBlockEntity(cursor) instanceof GrapheneTankBlockEntity tank; cursor = cursor.above()) {
            tanks.add(tank);
        }
        return new TankColumn(tanks);
    }

    public static boolean isTank(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof GrapheneTankBlock;
    }

    public static int countBelow(BlockGetter level, BlockPos pos) {
        int count = 0;
        for (BlockPos cursor = pos.below(); isTank(level, cursor); cursor = cursor.below()) {
            count++;
        }
        return count;
    }

    public static int countAbove(BlockGetter level, BlockPos pos) {
        int count = 0;
        for (BlockPos cursor = pos.above(); isTank(level, cursor); cursor = cursor.above()) {
            count++;
        }
        return count;
    }

    public static Fluid fluidBelow(Level level, BlockPos pos) {
        return isTank(level, pos.below()) ? at(level, pos.below()).fluid() : Fluids.EMPTY;
    }

    public static Fluid fluidAbove(Level level, BlockPos pos) {
        return isTank(level, pos.above()) ? at(level, pos.above()).fluid() : Fluids.EMPTY;
    }

    public Fluid fluid() {
        for (GrapheneTankBlockEntity tank : tanks) {
            if (!tank.content().isEmpty()) {
                return tank.content().fluid();
            }
        }
        return Fluids.EMPTY;
    }

    public int amount() {
        int total = 0;
        for (GrapheneTankBlockEntity tank : tanks) {
            total += tank.content().amount();
        }
        return total;
    }

    public int capacity() {
        return tanks.size() * GrapheneTankBlockEntity.CAPACITY;
    }

    public int insert(Fluid fluid, int amount, boolean simulate) {
        Fluid current = fluid();
        if (current != Fluids.EMPTY && !current.isSame(fluid)) {
            return 0;
        }
        int inserted = Math.min(amount, capacity() - amount());
        if (!simulate && inserted > 0) {
            spread(fluid, amount() + inserted);
        }
        return inserted;
    }

    public int extract(int amount, boolean simulate) {
        int extracted = Math.min(amount, amount());
        if (!simulate && extracted > 0) {
            spread(fluid(), amount() - extracted);
        }
        return extracted;
    }

    public void settle() {
        spread(fluid(), amount());
    }

    private void spread(Fluid fluid, int total) {
        int left = total;
        for (GrapheneTankBlockEntity tank : tanks) {
            int share = Math.min(left, GrapheneTankBlockEntity.CAPACITY);
            tank.setContent(FluidContent.of(fluid, share));
            left -= share;
        }
        for (GrapheneTankBlockEntity tank : tanks) {
            Level level = tank.getLevel();
            if (level != null) {
                level.updateNeighbourForOutputSignal(tank.getBlockPos(), tank.getBlockState().getBlock());
            }
        }
    }
}
