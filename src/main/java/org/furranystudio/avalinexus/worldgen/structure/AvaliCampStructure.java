/**
 * File: AvaliCampStructure.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-21
 */
package org.furranystudio.avalinexus.worldgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.avali.AvaliAppearance;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.AvaliNames;
import org.furranystudio.avalinexus.worldgen.ModStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String TAPESTRY = "avalinexus:avali_tapestry";
    private static final int SPAWN_SEARCH = 3;
    private static final double MAX_FLOOR_HEIGHT = 0.5;
    private static Map<Block, NanocanvasBlocks.Shape> whiteCanvas;

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
                    builder.addPiece(onGround(context, piece));
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
            Identifier template = template(piece);
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

    private static StructurePiece onGround(GenerationContext context, StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece pool)) {
            return piece;
        }
        return new PoolElementStructurePiece(context.structureTemplateManager(), pool.getElement(), pool.getPosition(), 0, pool.getRotation(),
            pool.getBoundingBox(), JigsawStructure.DEFAULT_LIQUID_SETTINGS);
    }

    public static boolean isInCamp(ChunkAccess chunk) {
        return chunk.getAllReferences().keySet().stream().anyMatch(structure -> structure instanceof AvaliCampStructure);
    }

    public static List<BoundingBox> campBoxes(WorldGenLevel level, ChunkAccess chunk) {
        List<BoundingBox> boxes = new ArrayList<>();
        if (!(level instanceof WorldGenRegion region)) {
            return boxes;
        }
        StructureManager manager = region.getLevel().structureManager().forWorldGenRegion(region);
        chunk.getAllReferences().forEach((structure, references) -> {
            if (structure instanceof AvaliCampStructure) {
                manager.fillStartsForStructure(structure, references, start -> start.getPieces().forEach(piece -> boxes.add(piece.getBoundingBox())));
            }
        });
        return boxes;
    }

    private static Identifier template(StructurePiece piece) {
        return piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single
            ? single.getTemplateLocation() : null;
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
        DyeColor dye = closestDye(AvaliAppearance.packColor(server, pack));
        recolorTapestries(level.getChunk(chunkPos.x(), chunkPos.z()), list, dye);

        for (StructurePiece piece : list) {
            BoundingBox box = piece.getBoundingBox();
            if (!box.intersects(chunkBox)) {
                continue;
            }
            Identifier template = template(piece);
            ResourceKey<LootTable> loot = storage.equals(template) ? storageLoot
                : template != null && template.toString().startsWith(tentPrefix) ? tentLoot : null;
            for (BlockPos pos : BlockPos.betweenClosed(Math.max(box.minX(), chunkBox.minX()), Math.max(box.minY(), chunkBox.minY()),
                    Math.max(box.minZ(), chunkBox.minZ()), Math.min(box.maxX(), chunkBox.maxX()), Math.min(box.maxY(), chunkBox.maxY()),
                    Math.min(box.maxZ(), chunkBox.maxZ()))) {
                BlockState state = level.getBlockState(pos);
                NanocanvasBlocks.Shape shape = whiteCanvas().get(state.getBlock());
                if (shape != null && dye != DyeColor.WHITE) {
                    level.setBlock(pos, NanocanvasBlocks.get(shape, dye).withPropertiesOf(state), Block.UPDATE_CLIENTS);
                } else if (loot != null && level.getBlockEntity(pos) instanceof RandomizableContainer) {
                    RandomizableContainer.setBlockEntityLootTable(level, random, pos, loot);
                } else if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                    spawnAvali(level, pos.immutable(), anchor, pack, packId);
                }
            }
        }
    }

    private static void recolorTapestries(ChunkAccess chunk, List<StructurePiece> pieces, DyeColor dye) {
        if (!(chunk instanceof ProtoChunk proto) || dye == DyeColor.WHITE) {
            return;
        }
        for (CompoundTag entity : proto.getEntities()) {
            if (!TAPESTRY.equals(entity.getStringOr("id", "")) || entity.getByteOr("Color", (byte) -1) != DyeColor.WHITE.getId()) {
                continue;
            }
            ListTag position = entity.getListOrEmpty("Pos");
            BlockPos pos = BlockPos.containing(position.getDoubleOr(0, 0.0), position.getDoubleOr(1, 0.0), position.getDoubleOr(2, 0.0));
            if (pieces.stream().anyMatch(piece -> piece.getBoundingBox().inflatedBy(1).isInside(pos))) {
                entity.putByte("Color", (byte) dye.getId());
            }
        }
    }

    private static void spawnAvali(WorldGenLevel level, BlockPos bed, BlockPos anchor, String pack, UUID packId) {
        AvaliEntity avali = ModEntities.AVALI.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (avali == null) {
            return;
        }
        float yaw = level.getRandom().nextFloat() * 360.0F;
        if (!standNear(level, avali, bed, yaw)) {
            avali.snapTo(bed.getX() + 0.5, bed.getY() + 0.6, bed.getZ() + 0.5, yaw, 0.0F);
        }
        avali.joinCamp(level, anchor, pack, packId);
        level.addFreshEntityWithPassengers(avali);
    }

    private static boolean standNear(WorldGenLevel level, AvaliEntity avali, BlockPos bed, float yaw) {
        for (BlockPos pos : BlockPos.withinManhattan(bed, SPAWN_SEARCH)) {
            if (Math.abs(pos.getY() - bed.getY()) > 1) {
                continue;
            }
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            double floor = shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
            if (floor > MAX_FLOOR_HEIGHT
                    || shape.isEmpty() && !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                continue;
            }
            avali.snapTo(pos.getX() + 0.5, pos.getY() + floor, pos.getZ() + 0.5, yaw, 0.0F);
            if (level.noCollision(avali)) {
                return true;
            }
        }
        return false;
    }

        private static DyeColor closestDye(int color) {
        DyeColor closest = DyeColor.WHITE;
        int best = Integer.MAX_VALUE;
        for (DyeColor dye : DyeColor.values()) {
            int other = dye.getTextureDiffuseColor();
            int red = (color >> 16 & 0xFF) - (other >> 16 & 0xFF);
            int green = (color >> 8 & 0xFF) - (other >> 8 & 0xFF);
            int blue = (color & 0xFF) - (other & 0xFF);
            int distance = red * red + green * green + blue * blue;
            if (distance < best) {
                best = distance;
                closest = dye;
            }
        }
        return closest;
    }

    private static Map<Block, NanocanvasBlocks.Shape> whiteCanvas() {
        if (whiteCanvas == null) {
            Map<Block, NanocanvasBlocks.Shape> blocks = new HashMap<>();
            for (NanocanvasBlocks.Shape shape : NanocanvasBlocks.Shape.values()) {
                blocks.put(NanocanvasBlocks.get(shape, DyeColor.WHITE), shape);
            }
            whiteCanvas = blocks;
        }
        return whiteCanvas;
    }
}
