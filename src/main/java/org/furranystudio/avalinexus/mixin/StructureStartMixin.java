/**
 * File: StructureStartMixin.java
 * Author: Maxime66410
 * Created: 2026-11-11
 * Last Modified: 2026-11-11
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.furranystudio.avalinexus.worldgen.structure.SettlementGround;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public class StructureStartMixin {

    @Shadow
    @Final
    private Structure structure;

    @Shadow
    @Final
    private PiecesContainer pieceContainer;

    @Inject(method = "placeInChunk", at = @At("TAIL"))
    private void avalinexus$fillFoundations(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                                            BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
        if (SettlementGround.isSettlement(structure)) {
            SettlementGround.fillFoundations(level, chunkBox, pieceContainer.pieces());
        }
    }
}
