/**
 * File: AvaliFluidModels.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.fluid;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;

// One gray still and flowing texture shared by the three fluids, each one tinted with its own color
public final class AvaliFluidModels {

    private static final Material STILL = new Material(AvaliNexus.id("block/fluid/avali_fluid_still"));
    private static final Material FLOW = new Material(AvaliNexus.id("block/fluid/avali_fluid_flow"));

    private AvaliFluidModels() {
    }

    public static FluidModel.Unbaked model(AvaliFluidKind kind) {
        return new FluidModel.Unbaked(STILL, FLOW, null, BlockTintSources.constant(kind.tint()));
    }
}
