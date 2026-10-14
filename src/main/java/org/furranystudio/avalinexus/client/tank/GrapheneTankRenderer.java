/**
 * File: GrapheneTankRenderer.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.client.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.tank.GrapheneTankBlockEntity;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidContent;

public class GrapheneTankRenderer implements BlockEntityRenderer<GrapheneTankBlockEntity, GrapheneTankRenderer.State> {

    private static final SpriteId AVALI_STILL = new SpriteId(TextureAtlas.LOCATION_BLOCKS, AvaliNexus.id("block/fluid/avali_fluid_still"));
    private static final SpriteId WATER_STILL = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still"));
    private static final float INSET = 1.1F / 16.0F;

    private final SpriteGetter sprites;

    public GrapheneTankRenderer(BlockEntityRendererProvider.Context context) {
        sprites = context.sprites();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GrapheneTankBlockEntity tank, State state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tank, state, crumbling);
        FluidContent content = tank.content();
        state.fill = content.amount() / (float) GrapheneTankBlockEntity.CAPACITY;
        state.water = content.fluid().isSame(Fluids.WATER);
        state.color = content.isEmpty() ? 0 : AvaliLiquids.tint(content.fluid());
        BlockState block = tank.getBlockState();
        for (Direction direction : Direction.values()) {
            state.connected[direction.ordinal()] = block.hasProperty(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))
                && block.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction));
        }
        state.surface = !state.joined(Direction.UP) || !(tank.getLevel() != null
            && tank.getLevel().getBlockEntity(tank.getBlockPos().above()) instanceof GrapheneTankBlockEntity above
            && !above.content().isEmpty());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fill <= 0.0F) {
            return;
        }
        TextureAtlasSprite sprite = sprites.get(state.water ? WATER_STILL : AVALI_STILL);
        float x0 = state.joined(Direction.WEST) ? 0.0F : INSET;
        float x1 = state.joined(Direction.EAST) ? 1.0F : 1.0F - INSET;
        float z0 = state.joined(Direction.NORTH) ? 0.0F : INSET;
        float z1 = state.joined(Direction.SOUTH) ? 1.0F : 1.0F - INSET;
        float bottom = state.joined(Direction.DOWN) ? 0.0F : INSET;
        float ceiling = state.joined(Direction.UP) ? 1.0F : 1.0F - INSET;
        float top = bottom + (ceiling - bottom) * Math.min(1.0F, state.fill);
        boolean drawTop = state.fill < 1.0F || state.surface;
        int light = state.lightCoords;
        int color = state.color;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            if (!state.joined(Direction.NORTH)) {
                side(buffer, pose, sprite, color, light, x0, bottom, z0, x1, top, z0, 0, 0, -1);
            }
            if (!state.joined(Direction.SOUTH)) {
                side(buffer, pose, sprite, color, light, x1, bottom, z1, x0, top, z1, 0, 0, 1);
            }
            if (!state.joined(Direction.WEST)) {
                side(buffer, pose, sprite, color, light, x0, bottom, z1, x0, top, z0, -1, 0, 0);
            }
            if (!state.joined(Direction.EAST)) {
                side(buffer, pose, sprite, color, light, x1, bottom, z0, x1, top, z1, 1, 0, 0);
            }
            if (drawTop) {
                flat(buffer, pose, sprite, color, light, x0, z0, x1, z1, top, 1);
            }
            if (!state.joined(Direction.DOWN)) {
                flat(buffer, pose, sprite, color, light, x0, z0, x1, z1, bottom, -1);
            }
        });
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
        public float fill;
        public boolean water;
        public int color;
        public final boolean[] connected = new boolean[6];
        public boolean surface;

        boolean joined(Direction direction) {
            return connected[direction.ordinal()];
        }
    }
}
