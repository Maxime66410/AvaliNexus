/**
 * File: AvaliCampStructure.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.worldgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.storage.loot.LootTable;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.worldgen.ModStructures;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AvaliCampStructure extends Structure {

    public static final MapCodec<AvaliCampStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        settingsCodec(instance),
        StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(camp -> camp.startPool),
        Codec.intRange(1, 7).fieldOf("size").forGetter(camp -> camp.size),
        Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(camp -> camp.maxDistance),
        Codec.intRange(0, 64).fieldOf("max_slope").forGetter(camp -> camp.maxSlope),
        Codec.intRange(0, 16).fieldOf("max_step").forGetter(camp -> camp.maxStep),
        Identifier.CODEC.fieldOf("storage").forGetter(camp -> camp.storage),
        Codec.STRING.fieldOf("tent_prefix").forGetter(camp -> camp.tentPrefix),
        Codec.intRange(0, 16).fieldOf("min_tents").forGetter(camp -> camp.minTents),
        Codec.intRange(0, 16).fieldOf("max_tents").forGetter(camp -> camp.maxTents),
        Codec.intRange(1, 64).fieldOf("attempts").forGetter(camp -> camp.attempts),
        LootTable.KEY_CODEC.fieldOf("tent_loot").forGetter(camp -> camp.tentLoot),
        LootTable.KEY_CODEC.fieldOf("storage_loot").forGetter(camp -> camp.storageLoot)
    ).apply(instance, AvaliCampStructure::new));

    private static final int START_OFFSET = 1;
    private static final int SLOPE_SAMPLE = 15;
    private static final long CAMP_SALT = 0x41564C43414D50L;

    private final Holder<StructureTemplatePool> startPool;
    private final int size;
    private final int maxDistance;
    private final int maxSlope;
    private final int maxStep;
    private final Identifier storage;
    private final String tentPrefix;
    private final int minTents;
    private final int maxTents;
    private final int attempts;
    private final ResourceKey<LootTable> tentLoot;
    private final ResourceKey<LootTable> storageLoot;

    public AvaliCampStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int size, int maxDistance, int maxSlope, int maxStep,
                              Identifier storage, String tentPrefix, int minTents, int maxTents, int attempts,
                              ResourceKey<LootTable> tentLoot, ResourceKey<LootTable> storageLoot) {
        super(settings);
        this.startPool = startPool;
        this.size = size;
        this.maxDistance = maxDistance;
        this.maxSlope = maxSlope;
        this.maxStep = maxStep;
        this.storage = storage;
        this.tentPrefix = tentPrefix;
        this.minTents = minTents;
        this.maxTents = maxTents;
        this.attempts = attempts;
        this.tentLoot = tentLoot;
        this.storageLoot = storageLoot;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.AVALI_CAMP.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        if (!isFlatAndDry(context, x, z)) {
            return Optional.empty();
        }
        for (int attempt = 0; attempt < attempts; attempt++) {
            Optional<GenerationStub> stub = JigsawPlacement.addPieces(context, startPool, Optional.empty(), size, new BlockPos(x, START_OFFSET, z),
                false, Optional.of(Heightmap.Types.WORLD_SURFACE_WG), new JigsawStructure.MaxDistance(maxDistance), PoolAliasLookup.EMPTY,
                JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
            if (stub.isEmpty()) {
                return stub;
            }
            List<StructurePiece> pieces = stub.get().getPiecesBuilder().build().pieces();
            if (hasRightPieces(pieces)) {
                StructurePiecesBuilder builder = new StructurePiecesBuilder();
                for (StructurePiece piece : pieces) {
                    if (piece != pieces.getFirst()) {
                        settle(context, piece, pieces.getFirst().getBoundingBox().minY());
                    }
                    builder.addPiece(AvaliSettlements.onGround(context.structureTemplateManager(), piece));
                }
                return Optional.of(new GenerationStub(stub.get().position(), Either.right(builder)));
            }
        }
        return Optional.empty();
    }

    private boolean isFlatAndDry(GenerationContext context, int x, int z) {
        int half = SLOPE_SAMPLE;
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        for (int[] offset : new int[][] {{0, 0}, {-half, -half}, {half, -half}, {-half, half}, {half, half}}) {
            int surface = height(context, x + offset[0], z + offset[1], Heightmap.Types.WORLD_SURFACE_WG);
            if (surface != height(context, x + offset[0], z + offset[1], Heightmap.Types.OCEAN_FLOOR_WG)) {
                return false;
            }
            lowest = Math.min(lowest, surface);
            highest = Math.max(highest, surface);
        }
        return highest - lowest <= maxSlope;
    }

    private static int height(GenerationContext context, int x, int z, Heightmap.Types type) {
        return context.chunkGenerator().getFirstFreeHeight(x, z, type, context.heightAccessor(), context.randomState());
    }

    private boolean hasRightPieces(List<StructurePiece> pieces) {
        int storages = 0;
        int tents = 0;
        for (StructurePiece piece : pieces) {
            Identifier template = AvaliSettlements.template(piece);
            if (storage.equals(template)) {
                storages++;
            } else if (template != null && template.toString().startsWith(tentPrefix)) {
                tents++;
            }
        }
        return storages == 1 && tents >= minTents && tents <= maxTents;
    }

    private void settle(GenerationContext context, StructurePiece piece, int floor) {
        BoundingBox box = piece.getBoundingBox();
        BlockPos center = box.getCenter();
        int ground = Math.clamp(height(context, center.getX(), center.getZ(), Heightmap.Types.WORLD_SURFACE_WG), floor - maxStep, floor + maxStep);
        piece.move(0, ground - box.minY(), 0);
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                           BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
        List<StructurePiece> list = pieces.pieces();
        if (list.isEmpty()) {
            return;
        }
        BoundingBox start = list.getFirst().getBoundingBox();
        BlockPos anchor = new BlockPos(start.getCenter().getX(), start.minY(), start.getCenter().getZ());
        MinecraftServer server = level.getServer();
        String pack = AvaliNames.pack(server, RandomSource.create(level.getSeed() ^ anchor.asLong()));
        UUID packId = new UUID(level.getSeed() ^ CAMP_SALT, anchor.asLong());
        if (chunkBox.isInside(anchor)) {
            AvaliNexus.debug("Generated an Avali camp of the {} pack at {}", pack, anchor.toShortString());
        }
        DyeColor dye = AvaliSettlements.closestDye(AvaliAppearance.packColor(server, pack));
        AvaliSettlements.recolorTapestries(level.getChunk(chunkPos.x(), chunkPos.z()), list, dye);

        for (StructurePiece piece : list) {
            BoundingBox box = piece.getBoundingBox();
            if (!box.intersects(chunkBox)) {
                continue;
            }
            Identifier template = AvaliSettlements.template(piece);
            ResourceKey<LootTable> loot = storage.equals(template) ? storageLoot
                : template != null && template.toString().startsWith(tentPrefix) ? tentLoot : null;
            for (BlockPos pos : BlockPos.betweenClosed(Math.max(box.minX(), chunkBox.minX()), Math.max(box.minY(), chunkBox.minY()),
                    Math.max(box.minZ(), chunkBox.minZ()), Math.min(box.maxX(), chunkBox.maxX()), Math.min(box.maxY(), chunkBox.maxY()),
                    Math.min(box.maxZ(), chunkBox.maxZ()))) {
                BlockState state = level.getBlockState(pos);
                BlockState recolored = AvaliSettlements.recolor(state, dye, false);
                if (recolored != null) {
                    level.setBlock(pos, recolored, Block.UPDATE_CLIENTS);
                } else if (loot != null && level.getBlockEntity(pos) instanceof RandomizableContainer) {
                    RandomizableContainer.setBlockEntityLootTable(level, random, pos, loot);
                } else if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                    AvaliSettlements.spawnAvali(level, pos.immutable(), avali -> avali.joinCamp(level, anchor, pack, packId));
                }
            }
        }
    }
}
