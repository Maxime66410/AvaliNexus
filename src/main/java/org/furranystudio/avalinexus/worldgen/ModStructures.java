/**
 * File: ModStructures.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;
import org.furranystudio.avalinexus.worldgen.structure.AvaliCampStructure;
import org.furranystudio.avalinexus.worldgen.structure.AvaliVillageStructure;
import org.furranystudio.avalinexus.worldgen.structure.CampTerrainProcessor;

public final class ModStructures {

    public static final RegistryEntry<StructureType<AvaliCampStructure>> AVALI_CAMP = ModRegistry.register(Registries.STRUCTURE_TYPE, "avali_camp",
        key -> () -> AvaliCampStructure.CODEC);
    public static final RegistryEntry<StructureType<AvaliVillageStructure>> AVALI_VILLAGE = ModRegistry.register(Registries.STRUCTURE_TYPE, "avali_village",
        key -> () -> AvaliVillageStructure.CODEC);
    public static final RegistryEntry<MapCodec<CampTerrainProcessor>> CAMP_TERRAIN = ModRegistry.register(Registries.STRUCTURE_PROCESSOR, "camp_terrain",
        key -> CampTerrainProcessor.CODEC);

    private ModStructures() {
    }

    public static void init() {
    }
}
