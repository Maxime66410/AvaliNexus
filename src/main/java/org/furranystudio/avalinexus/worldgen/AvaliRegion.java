/**
 * File: AvaliRegion.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.List;
import java.util.function.Consumer;

// Same layout as vanilla, the icy lowlands and the taigas next to them go to the polar taiga
public class AvaliRegion extends Region {

    // Mountains, rivers and oceans stay vanilla
    private static final List<ResourceKey<Biome>> TAKEN = List.of(
        Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES,
        Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);

    public AvaliRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder -> TAKEN.forEach(biome -> builder.replaceBiome(biome, ModBiomes.POLAR_TAIGA)));
    }
}
