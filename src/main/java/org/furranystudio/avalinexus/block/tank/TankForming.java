/**
 * File: TankForming.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TankForming {

    private static final int CLUSTER_LIMIT = 512;
    private static final Comparator<BlockPos> ORDER = Comparator.<BlockPos>comparingInt(pos -> pos.getY())
        .thenComparingInt(pos -> pos.getX()).thenComparingInt(pos -> pos.getZ());

    private TankForming() {
    }

    public static void reform(Level level, BlockPos start) {
        if (!TankStructure.isTank(level, start)) {
            return;
        }
        List<BlockPos> cluster = new ArrayList<>(cluster(level, start));
        cluster.sort(ORDER);
        Set<BlockPos> inCluster = new HashSet<>(cluster);
        Map<BlockPos, Integer> amounts = new HashMap<>();
        Set<BlockPos> seen = new HashSet<>();
        Set<BlockPos> loose = new HashSet<>();
        List<Set<BlockPos>> brokenGroups = new ArrayList<>();
        List<Box> boxes = new ArrayList<>();
        for (BlockPos pos : cluster) {
            if (seen.contains(pos)) {
                continue;
            }
            Set<BlockPos> group = TankStructure.members(level, pos);
            group.retainAll(inCluster);
            seen.addAll(group);
            Fluid fluid = Fluids.EMPTY;
            for (BlockPos member : group) {
                if (level.getBlockEntity(member) instanceof GrapheneTankBlockEntity tank) {
                    amounts.put(member, tank.content().amount());
                    if (!tank.content().isEmpty()) {
                        fluid = tank.content().fluid();
                    }
                }
            }
            Box bounds = Box.around(group, fluid);
            if (bounds.volume() == group.size()) {
                boxes.add(bounds);
            } else {
                brokenGroups.add(group);
                loose.addAll(group);
                for (BlockPos member : group) {
                    boxes.add(Box.around(Set.of(member), fluid));
                }
            }
        }
        merge(boxes);
        Map<Box, Integer> totals = new HashMap<>();
        for (Box box : boxes) {
            int total = 0;
            for (BlockPos pos : box.positions()) {
                if (!loose.contains(pos)) {
                    total += amounts.getOrDefault(pos, 0);
                }
            }
            totals.put(box, total);
            build(level, box);
        }
        for (Set<BlockPos> group : brokenGroups) {
            int left = 0;
            for (BlockPos pos : group) {
                left += amounts.getOrDefault(pos, 0);
            }
            List<Box> targets = new ArrayList<>();
            for (Box box : boxes) {
                if (group.stream().anyMatch(box::contains)) {
                    targets.add(box);
                }
            }
            targets.sort(Comparator.comparingInt(Box::volume).reversed());
            for (Box box : targets) {
                int given = Math.min(left, box.volume() * GrapheneTankBlockEntity.CAPACITY - totals.get(box));
                totals.put(box, totals.get(box) + given);
                left -= given;
            }
        }
        for (Box box : boxes) {
            TankStructure.at(level, box.min()).fill(box.fluid, totals.get(box));
        }
    }

    private static Set<BlockPos> cluster(Level level, BlockPos start) {
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        found.add(start);
        queue.add(start);
        while (!queue.isEmpty() && found.size() < CLUSTER_LIMIT) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (TankStructure.isTank(level, next) && found.add(next)) {
                    queue.add(next);
                }
            }
        }
        return found;
    }

    private static void merge(List<Box> boxes) {
        while (true) {
            Box best = null;
            int first = -1;
            int second = -1;
            for (int i = 0; i < boxes.size(); i++) {
                for (int j = i + 1; j < boxes.size(); j++) {
                    Box joined = boxes.get(i).join(boxes.get(j));
                    if (joined != null && (best == null || joined.volume() > best.volume())) {
                        best = joined;
                        first = i;
                        second = j;
                    }
                }
            }
            if (best == null) {
                return;
            }
            boxes.remove(second);
            boxes.remove(first);
            boxes.add(best);
        }
    }

    private static void build(Level level, Box box) {
        for (BlockPos pos : box.positions()) {
            BlockState current = level.getBlockState(pos);
            BlockState state = current
                .setValue(PipeBlock.WEST, pos.getX() > box.x0)
                .setValue(PipeBlock.EAST, pos.getX() < box.x1)
                .setValue(PipeBlock.NORTH, pos.getZ() > box.z0)
                .setValue(PipeBlock.SOUTH, pos.getZ() < box.z1)
                .setValue(PipeBlock.DOWN, pos.getY() > box.y0)
                .setValue(PipeBlock.UP, pos.getY() < box.y1);
            if (state != current) {
                level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    // Rectangular prism that contains a set of tank blocks
    private static final class Box {

        final int x0, y0, z0, x1, y1, z1;
        final Fluid fluid;

        Box(int x0, int y0, int z0, int x1, int y1, int z1, Fluid fluid) {
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.fluid = fluid;
        }

        static Box around(Set<BlockPos> positions, Fluid fluid) {
            int x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, z0 = Integer.MAX_VALUE;
            int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE, z1 = Integer.MIN_VALUE;
            for (BlockPos pos : positions) {
                x0 = Math.min(x0, pos.getX());
                y0 = Math.min(y0, pos.getY());
                z0 = Math.min(z0, pos.getZ());
                x1 = Math.max(x1, pos.getX());
                y1 = Math.max(y1, pos.getY());
                z1 = Math.max(z1, pos.getZ());
            }
            return new Box(x0, y0, z0, x1, y1, z1, fluid);
        }

        int volume() {
            return (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
        }

        BlockPos min() {
            return new BlockPos(x0, y0, z0);
        }

        boolean contains(BlockPos pos) {
            return pos.getX() >= x0 && pos.getX() <= x1 && pos.getY() >= y0 && pos.getY() <= y1 && pos.getZ() >= z0 && pos.getZ() <= z1;
        }

        List<BlockPos> positions() {
            List<BlockPos> positions = new ArrayList<>();
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    for (int z = z0; z <= z1; z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }
            return positions;
        }

        Box join(Box other) {
            if (fluid != Fluids.EMPTY && other.fluid != Fluids.EMPTY && !fluid.isSame(other.fluid)) {
                return null;
            }
            Box joined = new Box(Math.min(x0, other.x0), Math.min(y0, other.y0), Math.min(z0, other.z0),
                Math.max(x1, other.x1), Math.max(y1, other.y1), Math.max(z1, other.z1),
                fluid != Fluids.EMPTY ? fluid : other.fluid);
            if (joined.volume() != volume() + other.volume()) {
                return null;
            }
            if (joined.x1 - joined.x0 >= TankStructure.MAX_WIDTH || joined.z1 - joined.z0 >= TankStructure.MAX_WIDTH
                || joined.y1 - joined.y0 >= TankStructure.MAX_HEIGHT) {
                return null;
            }
            return joined;
        }
    }
}
