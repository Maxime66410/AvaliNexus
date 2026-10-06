/**
 * File: NexiteQuillRenderer.java
 * Author: Maxime66410
 * Created: 2026-10-07
 * Last Modified: 2026-10-07
 */
package org.furranystudio.avalinexus.client.weapon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import org.furranystudio.avalinexus.entity.projectile.NexiteQuill;

// Draws the quill model nose first along its flight, the model points north in Blockbench
public class NexiteQuillRenderer extends EntityRenderer<NexiteQuill, NexiteQuillRenderer.State> {

    private static final float SCALE = 0.5F;

    private final ItemModelResolver itemModelResolver;

    public NexiteQuillRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NexiteQuill quill, State state, float partialTick) {
        super.extractRenderState(quill, state, partialTick);
        state.yRot = quill.getYRot(partialTick);
        state.xRot = quill.getXRot(partialTick);
        itemModelResolver.updateForNonLiving(state.item, quill.getItem(), ItemDisplayContext.NONE, quill);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, state.yRot + 180.0F);
        poseStack.rotateDegrees(Axis.XP, state.xRot);
        poseStack.scale(SCALE, SCALE, SCALE);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    // The magnetic charge makes it glow in the dark
    @Override
    protected int getBlockLightLevel(NexiteQuill quill, BlockPos pos) {
        return 15;
    }

    public static class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float yRot;
        public float xRot;
    }
}
