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
import org.furranystudio.avalinexus.worldgen.feature.SnowDriftFeature;

public final class ModFeatures {

    public static final RegistryEntry<MapCodec<SnowDriftFeature>> SNOW_DRIFT = ModRegistry.register(Registries.FEATURE_TYPE, "snow_drift",
        key -> SnowDriftFeature.CODEC);

    private ModFeatures() {
    }

    public static void init() {
    }
}
