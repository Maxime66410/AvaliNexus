/**
 * File: VillageFields.java
 * Author: Maxime66410
 * Created: 2026-10-23
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.reputation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.worldgen.structure.AvaliSettlements;
import org.furranystudio.avalinexus.worldgen.structure.AvaliVillageStructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VillageFields {

    private static final Map<UUID, Map<BlockPos, Block>> SPOTS = new HashMap<>();

    private VillageFields() {
    }

    public static Map<BlockPos, Block> spots(ServerLevel level, BlockPos inVillage) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(inVillage,
            structure -> structure.value() instanceof AvaliVillageStructure);
        if (!start.isValid() || !(start.getStructure() instanceof AvaliVillageStructure village)) {
            return Map.of();
        }
        UUID packId = AvaliVillageStructure.packId(level.getSeed(), start);
        return packId == null ? Map.of() : SPOTS.computeIfAbsent(packId, id -> collect(level, start, village));
    }

    public static Block plantAt(ServerLevel level, BlockPos pos) {
        return spots(level, pos).get(pos);
    }

    public static String job(ServerLevel level, BlockPos inVillage) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(inVillage,
            structure -> structure.value() instanceof AvaliVillageStructure);
        return start.isValid() && start.getStructure() instanceof AvaliVillageStructure village && !spots(level, inVillage).isEmpty()
            ? village.fieldJob() : null;
    }

    private static Map<BlockPos, Block> collect(ServerLevel level, StructureStart start, AvaliVillageStructure village) {
        Map<BlockPos, Block> spots = new HashMap<>();
        List<Block> plants = List.of(ModBlocks.PIRU.get(), ModBlocks.NAKATI.get(), ModBlocks.KIRI.get());
        for (StructurePiece piece : start.getPieces()) {
            Identifier template = AvaliSettlements.template(piece);
            if (!village.isField(template) || !(piece instanceof PoolElementStructurePiece pool)) {
                continue;
            }
            StructureTemplate structure = level.getServer().getStructureTemplateManager().getOrCreate(template);
            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(pool.getRotation());
            for (Block plant : plants) {
                structure.filterBlocks(pool.getPosition(), settings, plant).forEach(info -> spots.put(info.pos(), plant));
            }
        }
        return Map.copyOf(spots);
    }
}
