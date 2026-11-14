/**
 * File: GraphenePipeRenderer.java
 * Author: Maxime66410
 * Created: 2026-11-13
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.client.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.block.GraphenePipeBlock;
import org.furranystudio.avalinexus.block.pipe.GraphenePipeBlockEntity;
import org.furranystudio.avalinexus.block.pipe.PipeFluid;
import org.furranystudio.avalinexus.client.fluid.FluidLook;

public class GraphenePipeRenderer implements BlockEntityRenderer<GraphenePipeBlockEntity, GraphenePipeRenderer.State> {

    private static final float CORE_MIN = 5.5F / 16.0F;
    private static final float CORE_MAX = 10.5F / 16.0F;
    private static final float ARM_MIN = 6.0F / 16.0F;
    private static final float ARM_MAX = 10.0F / 16.0F;

    public GraphenePipeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GraphenePipeBlockEntity pipe, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pipe, state, crumbling);
        state.sprite = null;
        BlockState block = pipe.getBlockState();
        if (!block.hasProperty(GraphenePipeBlock.FLUID) || block.getValue(GraphenePipeBlock.FLUID) != PipeFluid.OTHER || pipe.fluid() == Fluids.EMPTY) {
            return;
        }
        state.sprite = FluidLook.sprite(pipe.fluid());
        state.color = FluidLook.color(pipe.fluid(), pipe.getLevel() instanceof BlockAndTintGetter getter ? getter : null, pipe.getBlockPos());
        for (Direction direction : Direction.values()) {
            state.connected[direction.ordinal()] = block.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction));
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.sprite == null) {
            return;
        }
        TextureAtlasSprite sprite = state.sprite;
        int color = state.color;
        int light = state.lightCoords;
        Direction.Axis straight = state.straightAxis();
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            if (straight != null) {
                box(buffer, pose, sprite, color, light,
                    straight == Direction.Axis.X ? 0.0F : ARM_MIN, straight == Direction.Axis.Y ? 0.0F : ARM_MIN, straight == Direction.Axis.Z ? 0.0F : ARM_MIN,
                    straight == Direction.Axis.X ? 1.0F : ARM_MAX, straight == Direction.Axis.Y ? 1.0F : ARM_MAX, straight == Direction.Axis.Z ? 1.0F : ARM_MAX);
                return;
            }
            box(buffer, pose, sprite, color, light, CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);
            for (Direction direction : Direction.values()) {
                if (state.connected[direction.ordinal()]) {
                    arm(buffer, pose, sprite, color, light, direction);
                }
            }
        });
    }

    private static void arm(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light, Direction direction) {
        float x0 = ARM_MIN;
        float y0 = ARM_MIN;
        float z0 = ARM_MIN;
        float x1 = ARM_MAX;
        float y1 = ARM_MAX;
        float z1 = ARM_MAX;
        switch (direction) {
            case NORTH -> {
                z0 = 0.0F;
                z1 = CORE_MIN;
            }
            case SOUTH -> {
                z0 = CORE_MAX;
                z1 = 1.0F;
            }
            case WEST -> {
                x0 = 0.0F;
                x1 = CORE_MIN;
            }
            case EAST -> {
                x0 = CORE_MAX;
                x1 = 1.0F;
            }
            case DOWN -> {
                y0 = 0.0F;
                y1 = CORE_MIN;
            }
            case UP -> {
                y0 = CORE_MAX;
                y1 = 1.0F;
            }
        }
        box(buffer, pose, sprite, color, light, x0, y0, z0, x1, y1, z1);
    }

    private static void box(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        side(buffer, pose, sprite, color, light, x0, y0, z0, x1, y1, z0, 0, 0, -1);
        side(buffer, pose, sprite, color, light, x1, y0, z1, x0, y1, z1, 0, 0, 1);
        side(buffer, pose, sprite, color, light, x0, y0, z1, x0, y1, z0, -1, 0, 0);
        side(buffer, pose, sprite, color, light, x1, y0, z0, x1, y1, z1, 1, 0, 0);
        flat(buffer, pose, sprite, color, light, x0, z0, x1, z1, y1, 1);
        flat(buffer, pose, sprite, color, light, x0, z0, x1, z1, y0, -1);
    }

    private static void side(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light,
                             float x0, float y0, float z0, float x1, float y1, float z1, float nx, float ny, float nz) {
        boolean alongX = x0 != x1;
        float u0 = sprite.getU(alongX ? x0 : z0);
        float u1 = sprite.getU(alongX ? x1 : z1);
        float v0 = sprite.getV(1.0F - y1);
        float v1 = sprite.getV(1.0F - y0);
        vertex(buffer, pose, x0, y0, z0, u0, v1, color, light, nx, ny, nz);
        vertex(buffer, pose, x1, y0, z1, u1, v1, color, light, nx, ny, nz);
        vertex(buffer, pose, x1, y1, z1, u1, v0, color, light, nx, ny, nz);
        vertex(buffer, pose, x0, y1, z0, u0, v0, color, light, nx, ny, nz);
    }

    private static void flat(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light,
                             float x0, float z0, float x1, float z1, float y, float ny) {
        float u0 = sprite.getU(x0);
        float u1 = sprite.getU(x1);
        float v0 = sprite.getV(z0);
        float v1 = sprite.getV(z1);
        vertex(buffer, pose, x0, y, z0, u0, v0, color, light, 0, ny, 0);
        vertex(buffer, pose, x0, y, z1, u0, v1, color, light, 0, ny, 0);
        vertex(buffer, pose, x1, y, z1, u1, v1, color, light, 0, ny, 0);
        vertex(buffer, pose, x1, y, z0, u1, v0, color, light, 0, ny, 0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v,
                               int color, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }

    public static class State extends BlockEntityRenderState {
        public TextureAtlasSprite sprite;
        public int color;
        public final boolean[] connected = new boolean[6];

        Direction.Axis straightAxis() {
            for (Direction.Axis axis : Direction.Axis.values()) {
                boolean both = connected[Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE).ordinal()]
                    && connected[Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE).ordinal()];
                int count = 0;
                for (boolean linked : connected) {
                    count += linked ? 1 : 0;
                }
                if (both && count == 2) {
                    return axis;
                }
            }
            return null;
        }
    }
}
