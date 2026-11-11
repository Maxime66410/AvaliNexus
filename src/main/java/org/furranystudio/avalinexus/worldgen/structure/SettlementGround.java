/**
 * File: SettlementGround.java
 * Author: Maxime66410
 * Created: 2026-11-11
 * Last Modified: 2026-11-11
 */
package org.furranystudio.avalinexus.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.furranystudio.avalinexus.block.ModBlocks;

import java.util.List;

public final class SettlementGround {

    private static final int FOUNDATION_DEPTH = 16;

    private SettlementGround() {
    }

    public static boolean isSettlement(Structure structure) {
        return structure instanceof AvaliVillageStructure || structure instanceof AvaliCampStructure;
    }

    public static void fillFoundations(WorldGenLevel level, BoundingBox chunkBox, List<StructurePiece> pieces) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState foundation = ModBlocks.ICE_BRICKS.get().defaultBlockState();
        for (StructurePiece piece : pieces) {
            BoundingBox box = piece.getBoundingBox();
            if (projection(piece) != StructureTemplatePool.Projection.RIGID || !box.intersects(chunkBox)) {
                continue;
            }
            for (int x = Math.max(box.minX(), chunkBox.minX()); x <= Math.min(box.maxX(), chunkBox.maxX()); x++) {
                for (int z = Math.max(box.minZ(), chunkBox.minZ()); z <= Math.min(box.maxZ(), chunkBox.maxZ()); z++) {
                    if (level.getBlockState(pos.set(x, box.minY(), z)).getCollisionShape(level, pos).isEmpty()) {
                        continue;
                    }
                    for (int y = box.minY() - 1; box.minY() - y <= FOUNDATION_DEPTH && needsFill(level.getBlockState(pos.set(x, y, z))); y--) {
                        level.setBlock(pos, foundation, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static StructureTemplatePool.Projection projection(StructurePiece piece) {
        return piece instanceof PoolElementStructurePiece pool ? pool.getElement().getProjection() : null;
    }

    private static boolean needsFill(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty() || state.is(Blocks.POWDER_SNOW);
    }
}
