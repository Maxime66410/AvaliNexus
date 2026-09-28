/**
 * File: AvaliTapestryRenderer.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.client.tapestry;

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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.entity.tapestry.AvaliTapestry;
import org.furranystudio.avalinexus.item.TapestryItems;

// Draws the tapestry item flat against its support, its model already holds the tinted cloth and the orange bands
public class AvaliTapestryRenderer extends EntityRenderer<AvaliTapestry, AvaliTapestryRenderer.State> {

    private final ItemModelResolver itemModelResolver;

    public AvaliTapestryRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AvaliTapestry tapestry, State state, float partialTick) {
        super.extractRenderState(tapestry, state, partialTick);
        state.direction = tapestry.getDirection();
        state.turn = tapestry.getTurn();
        state.center = tapestry.getBoundingBox().getCenter().subtract(tapestry.position());
        itemModelResolver.updateForNonLiving(state.item, new ItemStack(TapestryItems.get(tapestry.getPattern(), tapestry.getColor())), ItemDisplayContext.FIXED, tapestry);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.translate(state.center.x, state.center.y, state.center.z);
        // Same turn as the item frame so the front of the item faces out
        Direction direction = state.direction;
        if (direction.getAxis().isHorizontal()) {
            poseStack.rotateDegrees(Axis.YP, 180.0F - direction.toYRot());
        } else {
            poseStack.rotateDegrees(Axis.XP, -90.0F * direction.getAxisDirection().getStep());
            poseStack.rotateDegrees(Axis.YP, 180.0F);
        }
        poseStack.rotateDegrees(Axis.ZP, 90.0F * state.turn);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    // A slab or stairs in the same spot would leave it pitch black, so it takes the brightest light around it
    @Override
    protected int getSkyLightLevel(AvaliTapestry tapestry, BlockPos pos) {
        return brightest(tapestry, LightLayer.SKY);
    }

    @Override
    protected int getBlockLightLevel(AvaliTapestry tapestry, BlockPos pos) {
        return brightest(tapestry, LightLayer.BLOCK);
    }

    private static int brightest(AvaliTapestry tapestry, LightLayer layer) {
        BlockPos pos = tapestry.getPos();
        Direction facing = tapestry.getDirection();
        int light = 0;
        for (Direction side : Direction.values()) {
            // Skip the support block behind it
            if (side != facing.getOpposite()) {
                light = Math.max(light, tapestry.level().getBrightness(layer, pos.relative(side)));
            }
        }
        return Math.max(light, tapestry.level().getBrightness(layer, pos));
    }

    public static class State extends EntityRenderState {
        public Direction direction = Direction.NORTH;
        public int turn;
        public Vec3 center = Vec3.ZERO;
        public final ItemStackRenderState item = new ItemStackRenderState();
    }
}
