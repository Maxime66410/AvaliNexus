/**
 * File: SnowDriftFeature.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.worldgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

// Piles snow layers into smooth drifts over the polar taiga, runs after the vanilla freeze pass
public record SnowDriftFeature() implements Feature {

    public static final MapCodec<SnowDriftFeature> CODEC = MapCodec.unit(SnowDriftFeature::new);

    private static final double DRIFT_SCALE = 1.0 / 24.0;
    private static final double DETAIL_SCALE = 1.0 / 6.0;
    private static final int MAX_LAYERS = 5;

    @Override
    public MapCodec<SnowDriftFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        SimplexNoise noise = new SimplexNoise(new LegacyRandomSource(level.getSeed()));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                pos.set(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
                if (level.getBlockState(pos.below()).is(Blocks.SNOW)) {
                    pos.move(0, -1, 0);
                }
                if (!level.getBiome(pos).is(ModBiomes.POLAR_TAIGA)) {
                    continue;
                }

                BlockState current = level.getBlockState(pos);
                if (!current.isAir() && !current.is(Blocks.SNOW)) {
                    continue;
                }
                // Tree tops keep the thin vanilla layer
                if (level.getBlockState(pos.below()).is(BlockTags.LEAVES)) {
                    continue;
                }

                double drift = noise.get(x * DRIFT_SCALE, z * DRIFT_SCALE) * 0.8 + noise.get(x * DETAIL_SCALE, z * DETAIL_SCALE) * 0.2;
                // Below zero it stays a single layer so mobs can still spawn there
                int layers = Mth.clamp(1 + (int) Math.floor((drift + 0.2) * MAX_LAYERS), 1, MAX_LAYERS);
                BlockState snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
                if (snow.canSurvive(level, pos)) {
                    level.setBlock(pos, snow, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
