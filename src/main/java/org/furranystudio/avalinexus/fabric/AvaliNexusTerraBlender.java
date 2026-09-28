/**
 * File: AvaliNexusTerraBlender.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.fabric;

import org.furranystudio.avalinexus.worldgen.ModBiomes;
import terrablender.api.TerraBlenderApi;

public final class AvaliNexusTerraBlender implements TerraBlenderApi {

    @Override
    public void onTerraBlenderInitialized() {
        ModBiomes.registerRegions();
    }
}
