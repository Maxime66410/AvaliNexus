/**
 * File: ModFeatures.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;
import org.furranystudio.avalinexus.worldgen.feature.FrozenCavesFeature;
import org.furranystudio.avalinexus.worldgen.feature.SnowDriftFeature;

public final class ModFeatures {

    public static final RegistryEntry<MapCodec<SnowDriftFeature>> SNOW_DRIFT = ModRegistry.register(Registries.FEATURE_TYPE, "snow_drift",
        key -> SnowDriftFeature.CODEC);
    public static final RegistryEntry<MapCodec<FrozenCavesFeature>> FROZEN_CAVES = ModRegistry.register(Registries.FEATURE_TYPE, "frozen_caves",
        key -> FrozenCavesFeature.CODEC);

    private ModFeatures() {
    }

    public static void init() {
    }
}
