/**
 * File: TankStructure.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.fluid.FluidContent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public final class TankStructure {

    public static final int MAX_WIDTH = 3;
    public static final int MAX_HEIGHT = 8;

    private final List<List<GrapheneTankBlockEntity>> layers;

    private TankStructure(List<List<GrapheneTankBlockEntity>> layers) {
        this.layers = layers;
    }

    public static TankStructure at(Level level, BlockPos pos) {
        TreeMap<Integer, List<GrapheneTankBlockEntity>> byHeight = new TreeMap<>();
        for (BlockPos member : members(level, pos)) {
            if (level.getBlockEntity(member) instanceof GrapheneTankBlockEntity tank) {
                byHeight.computeIfAbsent(member.getY(), y -> new ArrayList<>()).add(tank);
            }
        }
        return new TankStructure(new ArrayList<>(byHeight.values()));
    }

    public static boolean isTank(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof GrapheneTankBlock;
    }

    private static boolean connected(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof GrapheneTankBlock
            && state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))
            && isTank(level, pos.relative(direction));
    }

    public static Set<BlockPos> members(BlockGetter level, BlockPos start) {
        Set<BlockPos> found = new HashSet<>();
        if (!isTank(level, start)) {
            return found;
        }
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        found.add(start);
        queue.add(start);
        while (!queue.isEmpty() && found.size() < MAX_WIDTH * MAX_WIDTH * MAX_HEIGHT) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (connected(level, pos, direction) && found.add(next)) {
                    queue.add(next);
                }
            }
        }
        return found;
    }

    public Fluid fluid() {
        for (List<GrapheneTankBlockEntity> layer : layers) {
            for (GrapheneTankBlockEntity tank : layer) {
                if (!tank.content().isEmpty()) {
                    return tank.content().fluid();
                }
            }
        }
        return Fluids.EMPTY;
    }

    public int amount() {
        int total = 0;
        for (List<GrapheneTankBlockEntity> layer : layers) {
            for (GrapheneTankBlockEntity tank : layer) {
                total += tank.content().amount();
            }
        }
        return total;
    }

    public int capacity() {
        int blocks = 0;
        for (List<GrapheneTankBlockEntity> layer : layers) {
            blocks += layer.size();
        }
        return blocks * GrapheneTankBlockEntity.CAPACITY;
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

    public void fill(Fluid fluid, int total) {
        spread(fluid, total);
    }

    public void release(GrapheneTankBlockEntity leaving) {
        Fluid fluid = fluid();
        int left = amount();
        for (List<GrapheneTankBlockEntity> layer : layers) {
            for (GrapheneTankBlockEntity tank : layer) {
                if (tank != leaving) {
                    int share = Math.min(left, GrapheneTankBlockEntity.CAPACITY);
                    tank.setContent(FluidContent.of(fluid, share));
                    left -= share;
                }
            }
        }
        leaving.setContent(FluidContent.of(fluid, left));
    }

    private void spread(Fluid fluid, int total) {
        int left = total;
        for (List<GrapheneTankBlockEntity> layer : layers) {
            int size = layer.size();
            int taken = Math.min(left, size * GrapheneTankBlockEntity.CAPACITY);
            for (int i = 0; i < size; i++) {
                int share = taken / size + (i < taken % size ? 1 : 0);
                layer.get(i).setContent(FluidContent.of(fluid, share));
            }
            left -= taken;
        }
        for (List<GrapheneTankBlockEntity> layer : layers) {
            for (GrapheneTankBlockEntity tank : layer) {
                Level level = tank.getLevel();
                if (level != null) {
                    level.updateNeighbourForOutputSignal(tank.getBlockPos(), tank.getBlockState().getBlock());
                }
            }
        }
    }
}
