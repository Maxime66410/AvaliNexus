/**
 * File: FluidLook.java
 * Author: Maxime66410
 * Created: 2026-11-13
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.client.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;

public final class FluidLook {

    private static final int OPAQUE = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private FluidLook() {
    }

    private static FluidModel model(Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(AvaliLiquids.source(fluid).defaultFluidState());
    }

    public static TextureAtlasSprite sprite(Fluid fluid) {
        return model(fluid).stillMaterial().sprite();
    }

    public static int color(Fluid fluid, BlockAndTintGetter level, BlockPos pos) {
        BlockTintSource tint = model(fluid).tintSource();
        if (tint == null) {
            return WHITE;
        }
        BlockState state = AvaliLiquids.source(fluid).defaultFluidState().createLegacyBlock();
        return OPAQUE | (level == null || pos == null ? tint.color(state) : tint.colorInWorld(state, level, pos));
    }
}
