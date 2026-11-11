/**
 * File: SettlementGround.java
 * Author: Maxime66410
 * Created: 2026-11-11
 * Last Modified: 2026-11-11
 */
package org.furranystudio.avalinexus.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.entity.ModEntities;

import java.util.ArrayList;
import java.util.List;

public final class SettlementGround {

    private static final int FOUNDATION_DEPTH = 16;
    private static final double SUPPORT_OFFSET = 0.01;

    private SettlementGround() {
    }

    public static boolean isSettlement(Structure structure) {
        return structure instanceof AvaliVillageStructure || structure instanceof AvaliCampStructure;
    }

    public static void fillFoundations(WorldGenLevel level, BoundingBox chunkBox, List<StructurePiece> pieces) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        List<BoundingBox> rigids = new ArrayList<>();
        for (StructurePiece piece : pieces) {
            BoundingBox box = piece.getBoundingBox();
            if (projection(piece) != StructureTemplatePool.Projection.RIGID || !box.intersects(chunkBox)) {
                continue;
            }
            rigids.add(box);
            for (int x = Math.max(box.minX(), chunkBox.minX()); x <= Math.min(box.maxX(), chunkBox.maxX()); x++) {
                for (int z = Math.max(box.minZ(), chunkBox.minZ()); z <= Math.min(box.maxZ(), chunkBox.maxZ()); z++) {
                    if (!level.getBlockState(pos.set(x, box.minY(), z)).getCollisionShape(level, pos).isEmpty()) {
                        fillColumn(level, pos, x, box.minY() - 1, z);
                    }
                }
            }
        }
        if (!rigids.isEmpty() && level.getChunk(chunkBox.minX() >> 4, chunkBox.minZ() >> 4) instanceof ProtoChunk proto) {
            fillUnderCushions(level, pos, chunkBox, rigids, proto);
        }
    }

    private static void fillUnderCushions(WorldGenLevel level, BlockPos.MutableBlockPos pos, BoundingBox chunkBox, List<BoundingBox> rigids,
                                          ProtoChunk proto) {
        String cushion = BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.AVALI_CUSHION.get()).toString();
        for (CompoundTag entity : List.copyOf(proto.getEntities())) {
            if (!cushion.equals(entity.getStringOr("id", ""))) {
                continue;
            }
            ListTag position = entity.getListOrEmpty("Pos");
            BlockPos support = BlockPos.containing(position.getDoubleOr(0, 0.0), position.getDoubleOr(1, 0.0) - SUPPORT_OFFSET,
                position.getDoubleOr(2, 0.0));
            if (!chunkBox.isInside(support) || rigids.stream().noneMatch(box -> box.isInside(support.getX(), box.minY(), support.getZ()))) {
                continue;
            }
            fillColumn(level, pos, support.getX(), support.getY(), support.getZ());
        }
    }

    private static void fillColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int top, int z) {
        BlockState foundation = ModBlocks.ICE_BRICKS.get().defaultBlockState();
        for (int y = top; top - y < FOUNDATION_DEPTH; y--) {
            BlockState state = level.getBlockState(pos.set(x, y, z));
            if (!state.getFluidState().isEmpty()) {
                level.setBlock(pos, foundation, Block.UPDATE_CLIENTS);
                return;
            }
            if (!needsFill(state)) {
                return;
            }
            level.setBlock(pos, foundation, Block.UPDATE_CLIENTS);
        }
    }

    private static StructureTemplatePool.Projection projection(StructurePiece piece) {
        return piece instanceof PoolElementStructurePiece pool ? pool.getElement().getProjection() : null;
    }

    private static boolean needsFill(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty() || state.is(Blocks.POWDER_SNOW);
    }
}
