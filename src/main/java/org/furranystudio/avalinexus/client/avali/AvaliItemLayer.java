/**
 * File: AvaliItemLayer.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

public class AvaliItemLayer<R extends LivingEntityRenderState & GeoRenderState> extends ItemInHandGeoLayer<AvaliEntity, Void, R> {

    private static final float ADULT_HAND_DROP = 9.0F / 16.0F;

    public AvaliItemLayer(EntityRendererProvider.Context context, GeoRenderer<AvaliEntity, Void, R> renderer) {
        super(context, renderer, "right_arm", "left_arm");
    }

    @Override
    public void addRenderData(AvaliEntity avali, Void relatedObject, R state, float partialTick) {
        super.addRenderData(avali, relatedObject, state, partialTick);
        state.addGeckolibData(MAINHAND_SHIELD, false);
        state.addGeckolibData(OFFHAND_SHIELD, false);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemState, ItemDisplayContext context,
                                         R state, SubmitNodeCollector collector, int light) {
        poseStack.translate(0.0F, -ADULT_HAND_DROP * (state.isBaby ? 0.5F : 1.0F), 0.0F);
        super.submitItemStackRender(poseStack, bone, itemState, context, state, collector, light);
    }
}
