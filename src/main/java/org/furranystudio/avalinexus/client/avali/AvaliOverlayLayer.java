/**
 * File: AvaliOverlayLayer.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.function.Function;

public class AvaliOverlayLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<AvaliEntity, Void, R> {

    private final DataTicket<Identifier> ticket;
    private final Function<AvaliEntity, Identifier> picker;
    private final boolean tinted;
    private final int order;

    public AvaliOverlayLayer(GeoRenderer<AvaliEntity, Void, R> renderer, DataTicket<Identifier> ticket, Function<AvaliEntity, Identifier> picker, boolean tinted, int order) {
        super(renderer);
        this.ticket = ticket;
        this.picker = picker;
        this.tinted = tinted;
        this.order = order;
    }

    @Override
    public void addRenderData(AvaliEntity avali, Void relatedObject, R state, float partialTick) {
        Identifier texture = picker.apply(avali);
        if (texture != null) {
            state.addGeckolibData(ticket, texture);
        }
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> info, SubmitNodeCollector collector) {
        Identifier texture = info.getOrDefaultGeckolibData(ticket, null);
        if (!info.willRender() || texture == null) {
            return;
        }
        RenderType type = renderer.getRenderType(info.renderState(), texture);
        int light = info.packedLight();
        int overlay = info.packedOverlay();
        int color = tinted ? info.renderColor() : ARGB.color(ARGB.alpha(info.renderColor()), 0xFFFFFF);
        collector.order(order).submitCustomGeometry(info.poseStack(), type, (pose, buffer) -> {
            PoseStack poseStack = info.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            info.renderPosed(() -> info.model().render(info, buffer, light, overlay, color));
            poseStack.popPose();
        });
    }
}
