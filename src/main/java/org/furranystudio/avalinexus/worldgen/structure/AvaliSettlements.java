/**
 * File: AvaliSettlements.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.AerogelBlocks;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class AvaliSettlements {

    private static final String TAPESTRY = "avalinexus:avali_tapestry";
    private static final int SPAWN_SEARCH = 3;
    private static final double MAX_FLOOR_HEIGHT = 0.5;
    private static Map<Block, NanocanvasBlocks.Shape> whiteCanvas;
    private static Map<Block, AerogelBlocks.Shape> whiteAerogel;

    private AvaliSettlements() {
    }

    public static boolean isSettlement(Structure structure) {
        return structure instanceof AvaliCampStructure || structure instanceof AvaliVillageStructure;
    }

    public static boolean isInSettlement(ChunkAccess chunk) {
        return chunk.getAllReferences().keySet().stream().anyMatch(AvaliSettlements::isSettlement);
    }

    public static List<BoundingBox> settlementBoxes(WorldGenLevel level, ChunkAccess chunk) {
        List<BoundingBox> boxes = new ArrayList<>();
        if (!(level instanceof WorldGenRegion region)) {
            return boxes;
        }
        StructureManager manager = region.getLevel().structureManager().forWorldGenRegion(region);
        chunk.getAllReferences().forEach((structure, references) -> {
            if (isSettlement(structure)) {
                manager.fillStartsForStructure(structure, references, start -> start.getPieces().forEach(piece -> boxes.add(piece.getBoundingBox())));
            }
        });
        return boxes;
    }

    public static Identifier template(StructurePiece piece) {
        return piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single
            ? single.getTemplateLocation() : null;
    }

    public static StructurePiece onGround(StructureTemplateManager templates, StructurePiece piece) {
        if (!(piece instanceof PoolElementStructurePiece pool)) {
            return piece;
        }
        PoolElementStructurePiece grounded = new PoolElementStructurePiece(templates, pool.getElement(), pool.getPosition(), 0,
            pool.getRotation(), pool.getBoundingBox(), JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        for (JigsawJunction junction : pool.getJunctions()) {
            grounded.addJunction(junction);
        }
        return grounded;
    }

    public static BlockState recolor(BlockState state, DyeColor dye, boolean aerogel) {
        if (dye == DyeColor.WHITE) {
            return null;
        }
        NanocanvasBlocks.Shape canvas = whiteCanvas().get(state.getBlock());
        if (canvas != null) {
            return NanocanvasBlocks.get(canvas, dye).withPropertiesOf(state);
        }
        AerogelBlocks.Shape glass = aerogel ? whiteAerogel().get(state.getBlock()) : null;
        return glass != null ? AerogelBlocks.get(glass, dye).withPropertiesOf(state) : null;
    }

    public static void recolorTapestries(ChunkAccess chunk, List<StructurePiece> pieces, DyeColor dye) {
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

    public static void spawnAvali(WorldGenLevel level, BlockPos bed, Consumer<AvaliEntity> join) {
        AvaliEntity avali = ModEntities.AVALI.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (avali == null) {
            return;
        }
        float yaw = level.getRandom().nextFloat() * 360.0F;
        if (!standNear(level, avali, bed, yaw)) {
            avali.snapTo(bed.getX() + 0.5, bed.getY() + 0.6, bed.getZ() + 0.5, yaw, 0.0F);
        }
        join.accept(avali);
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

    public static DyeColor closestDye(int color) {
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

    private static Map<Block, AerogelBlocks.Shape> whiteAerogel() {
        if (whiteAerogel == null) {
            Map<Block, AerogelBlocks.Shape> blocks = new HashMap<>();
            for (AerogelBlocks.Shape shape : AerogelBlocks.Shape.values()) {
                blocks.put(AerogelBlocks.get(shape, DyeColor.WHITE), shape);
            }
            whiteAerogel = blocks;
        }
        return whiteAerogel;
    }
}
