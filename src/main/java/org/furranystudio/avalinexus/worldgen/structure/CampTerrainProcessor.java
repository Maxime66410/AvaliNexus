/**
 * File: CampTerrainProcessor.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-21
 */
package org.furranystudio.avalinexus.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CampTerrainProcessor implements StructureProcessor {

    public static final CampTerrainProcessor INSTANCE = new CampTerrainProcessor();
    public static final MapCodec<CampTerrainProcessor> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public List<StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos offset, BlockPos pos, List<StructureBlockInfo> originalInfos,
                                                       List<StructureBlockInfo> processedInfos, StructurePlaceSettings settings) {
        Map<Long, Integer> roofs = new HashMap<>();
        Set<BlockPos> filled = new HashSet<>();
        int bottom = Integer.MAX_VALUE;
        for (StructureBlockInfo info : processedInfos) {
            filled.add(info.pos());
            bottom = Math.min(bottom, info.pos().getY());
            if (!info.state().isAir()) {
                roofs.merge(column(info.pos()), info.pos().getY(), Math::max);
            }
        }

        List<StructureBlockInfo> result = new ArrayList<>();
        for (StructureBlockInfo info : processedInfos) {
            Integer roof = roofs.get(column(info.pos()));
            if (!info.state().isAir() || roof != null && info.pos().getY() < roof) {
                result.add(info);
            }
        }
        for (StructureBlockInfo info : processedInfos) {
            Integer roof = roofs.get(column(info.pos()));
            if (roof == null || info.state().isAir() || info.pos().getY() != roof) {
                continue;
            }
            for (int y = bottom; y < roof; y++) {
                BlockPos below = new BlockPos(info.pos().getX(), y, info.pos().getZ());
                if (filled.add(below)) {
                    result.add(new StructureBlockInfo(below, Blocks.AIR.defaultBlockState(), null));
                }
            }
        }
        return result;
    }

    private static long column(BlockPos pos) {
        return BlockPos.asLong(pos.getX(), 0, pos.getZ());
    }

    @Override
    public MapCodec<CampTerrainProcessor> codec() {
        return CODEC;
    }
}
