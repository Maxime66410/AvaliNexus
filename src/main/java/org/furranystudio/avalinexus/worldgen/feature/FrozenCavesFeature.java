/**
 * File: FrozenCavesFeature.java
 * Author: Maxime66410
 * Created: 2026-10-10
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.worldgen.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

public record FrozenCavesFeature() implements Feature {

    public static final MapCodec<FrozenCavesFeature> CODEC = MapCodec.unit(FrozenCavesFeature::new);

    private static final int SNOW_ABOVE = 32;
    private static final int ICE_ABOVE = 0;
    private static final int BLEND = 14;
    private static final double NOISE_SCALE = 1.0 / 10.0;
    private static final int SURFACE_MARGIN = 4;

    @Override
    public MapCodec<FrozenCavesFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        SimplexNoise noise = new SimplexNoise(new LegacyRandomSource(level.getSeed() ^ 0x5F3C2A17L));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos side = new BlockPos.MutableBlockPos();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        boolean placed = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                if (!level.getBiome(pos.set(x, surface, z)).is(ModBiomes.POLAR_TAIGA)) {
                    continue;
                }
                int top = surface - SURFACE_MARGIN;
                for (int y = level.getMinY(); y < top; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!isCoatable(state) || !touchesCaveAir(level, pos, side)) {
                        continue;
                    }
                    level.setBlock(pos, coating(noise, random, x, y, z), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    // Ores protect (Easy to find !)
    private static boolean isCoatable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.DIRT) || state.is(Blocks.GRAVEL);
    }

    private static boolean touchesCaveAir(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos side) {
        for (Direction direction : Direction.values()) {
            side.setWithOffset(pos, direction);
            if (level.getBlockState(side).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static BlockState coating(SimplexNoise noise, RandomSource random, int x, int y, int z) {
        double shifted = y + noise.get(x * NOISE_SCALE, y * NOISE_SCALE, z * NOISE_SCALE) * BLEND;
        if (shifted > SNOW_ABOVE) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }
        if (shifted > ICE_ABOVE) {
            return Blocks.ICE.defaultBlockState();
        }
        float blueChance = Mth.clamp((float) (ICE_ABOVE - shifted) / 64.0F, 0.1F, 0.5F);
        return random.nextFloat() < blueChance ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
    }
}
