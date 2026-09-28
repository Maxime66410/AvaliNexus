/**
 * File: AvaliCushionRenderer.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.cushion;

import net.minecraft.client.renderer.entity.CushionRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CushionRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.Cushion;
import org.furranystudio.avalinexus.AvaliNexus;

public class AvaliCushionRenderer extends CushionRenderer {

    private static final Identifier TEXTURE = AvaliNexus.id("textures/entity/cushion/avali_cushion.png");

    public AvaliCushionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(Cushion cushion, CushionRenderState state, float partialTick) {
        super.extractRenderState(cushion, state, partialTick);
        state.texture = TEXTURE;
    }
}
