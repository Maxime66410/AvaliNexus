/**
 * File: ModBiomes.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.furranystudio.avalinexus.AvaliNexus;
import terrablender.api.MaterialRuleManager;
import terrablender.api.Regions;

public final class ModBiomes {

    public static final ResourceKey<Biome> POLAR_TAIGA = ResourceKey.create(Registries.BIOME, AvaliNexus.id("polar_taiga"));

    // Vanilla overworld regions weigh 10, so the Avali region covers about a fifth of the world
    private static final int REGION_WEIGHT = 3;

    private ModBiomes() {
    }

    // Called once TerraBlender is ready: common setup on Forge and NeoForge, its own entrypoint on Fabric
    public static void registerRegions() {
        Regions.register(new AvaliRegion(AvaliNexus.id("overworld"), REGION_WEIGHT));
        MaterialRuleManager.addRules(MaterialRuleManager.RuleCategory.OVERWORLD, AvaliNexus.MODID,
            ResourceKey.create(Registries.MATERIAL_RULE, AvaliNexus.id("overworld")));
    }
}
