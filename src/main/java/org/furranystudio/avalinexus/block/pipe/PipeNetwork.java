/**
 * File: PipeNetwork.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.fluid.FluidPort;
import org.furranystudio.avalinexus.fluid.FluidPortBlock;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class PipeNetwork {

    public static final int PIPE_CAPACITY = 100;
    public static final int PERIOD = 10;
    public static final int RATE = 500;
    private static final int LIMIT = 1024;
    private static final int BALANCE_MIN = 50;

    private final Level level;
    private final List<GraphenePipeBlockEntity> pipes = new ArrayList<>();
    private final List<FluidPort> sources = new ArrayList<>();
    private final List<FluidPort> sinks = new ArrayList<>();
    private final List<FluidPort> buffers = new ArrayList<>();
    private Fluid fluid = Fluids.EMPTY;
    private int stored;

    private PipeNetwork(Level level) {
        this.level = level;
    }

    public static PipeNetwork of(Level level, BlockPos start) {
        PipeNetwork network = new PipeNetwork(level);
        Set<BlockPos> visited = new HashSet<>();
        Set<Object> ports = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty() && network.pipes.size() < LIMIT) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof GraphenePipeBlock) || !(level.getBlockEntity(pos) instanceof GraphenePipeBlockEntity pipe)) {
                continue;
            }
            network.pipes.add(pipe);
            network.stored += pipe.amount();
            if (state.getValue(GraphenePipeBlock.FLUID) != PipeFluid.EMPTY) {
                network.fluid = state.getValue(GraphenePipeBlock.FLUID).fluid();
            }
            for (Direction direction : Direction.values()) {
                if (!state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                    continue;
                }
                BlockPos next = pos.relative(direction);
                if (!level.isLoaded(next)) {
                    continue;
                }
                BlockState neighbor = level.getBlockState(next);
                if (neighbor.getBlock() instanceof GraphenePipeBlock) {
                    if (neighbor.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite())) && visited.add(next)) {
                        queue.add(next);
                    }
                } else if (neighbor.getBlock() instanceof FluidPortBlock block && block.hasPort(neighbor, direction.getOpposite())) {
                    FluidPort port = block.port(level, next, neighbor, direction.getOpposite());
                    if (port != null && ports.add(port.identity())) {
                        switch (port.role()) {
                            case SOURCE -> network.sources.add(port);
                            case SINK -> network.sinks.add(port);
                            case BUFFER -> network.buffers.add(port);
                        }
                    }
                }
            }
        }
        if (network.stored <= 0) {
            network.fluid = Fluids.EMPTY;
        }
        return network;
    }

    public Fluid fluid() {
        return fluid;
    }

    public void tick(long time) {
        for (GraphenePipeBlockEntity pipe : pipes) {
            pipe.markHandled(time);
        }
        int capacity = pipes.size() * PIPE_CAPACITY;
        int intake = 0;
        for (FluidPort source : sources) {
            Fluid offered = source.fluid();
            if (offered == Fluids.EMPTY || fluid != Fluids.EMPTY && !fluid.isSame(offered)) {
                continue;
            }
            int room = capacity - stored + room(sinks, offered) + room(buffers, offered);
            int taken = source.drain(offered, Math.min(RATE - intake, room), false);
            if (taken > 0) {
                fluid = offered;
                stored += taken;
                intake += taken;
            }
        }
        int sent = 0;
        for (FluidPort sink : sinks) {
            if (fluid == Fluids.EMPTY || stored <= 0) {
                break;
            }
            int given = sink.fill(fluid, Math.min(stored, RATE - sent), false);
            stored -= given;
            sent += given;
        }
        if (stored > capacity) {
            stored -= fillBuffers(stored - capacity);
        } else if (stored < capacity) {
            int pulled = 0;
            for (FluidPort buffer : buffers) {
                Fluid held = buffer.fluid();
                if (held == Fluids.EMPTY || fluid != Fluids.EMPTY && !fluid.isSame(held)) {
                    continue;
                }
                int got = buffer.drain(held, Math.min(RATE - pulled, capacity - stored), false);
                if (got > 0) {
                    fluid = held;
                    stored += got;
                    pulled += got;
                }
            }
        }
        balance();
        distribute();
    }

    public int take(Fluid wanted, int amount, boolean simulate) {
        Fluid target = wanted != Fluids.EMPTY ? wanted : fluid;
        if (target == Fluids.EMPTY) {
            for (FluidPort buffer : buffers) {
                if (buffer.fluid() != Fluids.EMPTY) {
                    target = buffer.fluid();
                    break;
                }
            }
        }
        if (target == Fluids.EMPTY) {
            return 0;
        }
        int taken = 0;
        for (FluidPort buffer : buffers) {
            if (taken >= amount) {
                break;
            }
            taken += buffer.drain(target, amount - taken, simulate);
        }
        if (taken < amount && fluid != Fluids.EMPTY && fluid.isSame(target)) {
            int fromPipes = Math.min(amount - taken, stored);
            taken += fromPipes;
            if (!simulate) {
                stored -= fromPipes;
                distribute();
            }
        }
        return taken;
    }

    public Fluid peek() {
        if (fluid != Fluids.EMPTY) {
            return fluid;
        }
        for (FluidPort buffer : buffers) {
            if (buffer.fluid() != Fluids.EMPTY) {
                return buffer.fluid();
            }
        }
        return Fluids.EMPTY;
    }

    private void balance() {
        if (fluid == Fluids.EMPTY || buffers.size() < 2) {
            return;
        }
        List<FluidPort> pool = new ArrayList<>();
        long total = 0;
        long space = 0;
        for (FluidPort buffer : buffers) {
            if (buffer.fluid() == Fluids.EMPTY || fluid.isSame(buffer.fluid())) {
                pool.add(buffer);
                total += buffer.amount();
                space += buffer.capacity();
            }
        }
        if (space <= 0) {
            return;
        }
        List<FluidPort> donors = new ArrayList<>();
        List<FluidPort> receivers = new ArrayList<>();
        List<Integer> surpluses = new ArrayList<>();
        List<Integer> deficits = new ArrayList<>();
        for (FluidPort buffer : pool) {
            int gap = buffer.amount() - (int) (total * buffer.capacity() / space);
            if (gap >= BALANCE_MIN) {
                donors.add(buffer);
                surpluses.add(gap);
            } else if (-gap >= BALANCE_MIN) {
                receivers.add(buffer);
                deficits.add(-gap);
            }
        }
        int moved = 0;
        for (int i = 0; i < donors.size() && moved < RATE; i++) {
            int surplus = surpluses.get(i);
            for (int j = 0; j < receivers.size() && surplus > 0 && moved < RATE; j++) {
                int move = Math.min(Math.min(surplus, deficits.get(j)), RATE - moved);
                if (move <= 0) {
                    continue;
                }
                int got = donors.get(i).drain(fluid, move, false);
                int put = receivers.get(j).fill(fluid, got, false);
                if (put < got) {
                    donors.get(i).fill(fluid, got - put, false);
                }
                surplus -= put;
                deficits.set(j, deficits.get(j) - put);
                moved += put;
            }
        }
    }

    private int fillBuffers(int amount) {
        int left = amount;
        List<FluidPort> open = new ArrayList<>(buffers);
        while (left > 0 && !open.isEmpty()) {
            int share = Math.max(1, left / open.size());
            Iterator<FluidPort> iterator = open.iterator();
            while (iterator.hasNext() && left > 0) {
                int wanted = Math.min(share, left);
                int put = iterator.next().fill(fluid, wanted, false);
                left -= put;
                if (put < wanted) {
                    iterator.remove();
                }
            }
        }
        return amount - left;
    }

    private static int room(List<FluidPort> ports, Fluid offered) {
        int room = 0;
        for (FluidPort port : ports) {
            room += port.fill(offered, RATE, true);
        }
        return room;
    }

    private void distribute() {
        if (stored <= 0) {
            stored = 0;
            fluid = Fluids.EMPTY;
        }
        PipeFluid shown = PipeFluid.of(fluid);
        int size = pipes.size();
        for (int i = 0; i < size; i++) {
            GraphenePipeBlockEntity pipe = pipes.get(i);
            pipe.setAmount(stored / size + (i < stored % size ? 1 : 0));
            BlockState state = pipe.getBlockState();
            if (state.getBlock() instanceof GraphenePipeBlock block && state.getValue(GraphenePipeBlock.FLUID) != shown) {
                BlockPos pos = pipe.getBlockPos();
                level.setBlock(pos, block.relink(level, pos, state.setValue(GraphenePipeBlock.FLUID, shown)), Block.UPDATE_CLIENTS);
            }
        }
    }
}
