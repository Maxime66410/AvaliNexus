/**
 * File: AvaliVisibleFaces.java
 * Author: Maxime66410
 * Created: 2026-11-07
 * Last Modified: 2026-11-08
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.google.common.collect.MapMaker;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class AvaliVisibleFaces {

    private static final Map<BakedGeoModel, Map<List<Identifier>, Map<GeoCube, boolean[]>>> CACHE = new MapMaker().weakKeys().makeMap();

    private AvaliVisibleFaces() {
    }

    public static void render(RenderPassInfo<?> info, List<Identifier> textures, VertexConsumer buffer, int light, int overlay, int color) {
        try (Zone zone = Profiler.get().zone("avali_faces")) {
            BakedGeoModel model = info.model();
            Map<GeoCube, boolean[]> faces = CACHE.computeIfAbsent(model, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(textures, key -> scan(model, key));
            for (GeoBone bone : model.topLevelBones()) {
                renderBone(info, info.poseStack(), bone, faces, buffer, light, overlay, color);
            }
        }
    }

    private static void renderBone(RenderPassInfo<?> info, PoseStack poseStack, GeoBone bone, Map<GeoCube, boolean[]> faces,
                                   VertexConsumer buffer, int light, int overlay, int color) {
        if (isSkipped(bone)) {
            return;
        }
        poseStack.pushPose();
        RenderUtil.prepMatrixForBoneAndUpdateListeners(poseStack, bone, info);
        BoneSnapshot snapshot = bone.frameSnapshot;
        if (!(bone instanceof CuboidGeoBone cuboid)) {
            bone.render(info, poseStack, buffer, light, overlay, color);
        } else if (snapshot == null || !snapshot.isHidden()) {
            for (GeoCube cube : cuboid.cubes) {
                boolean[] visible = faces.get(cube);
                if (visible != null) {
                    renderCube(poseStack, cube, visible, buffer, light, overlay, color);
                }
            }
        }
        if (snapshot == null || !snapshot.areChildrenHidden()) {
            for (GeoBone child : bone.children()) {
                renderBone(info, poseStack, child, faces, buffer, light, overlay, color);
            }
        }
        poseStack.popPose();
    }

    private static boolean isSkipped(GeoBone bone) {
        BoneSnapshot snapshot = bone.frameSnapshot;
        if (!(bone instanceof CuboidGeoBone) || snapshot == null || !snapshot.isHidden() || bone.positionListeners != null) {
            return false;
        }
        for (GeoLocator locator : bone.locators()) {
            if (locator.positionListeners != null) {
                return false;
            }
        }
        if (snapshot.areChildrenHidden()) {
            return true;
        }
        for (GeoBone child : bone.children()) {
            if (!isSkipped(child)) {
                return false;
            }
        }
        return true;
    }

    private static void renderCube(PoseStack poseStack, GeoCube cube, boolean[] visible, VertexConsumer buffer, int light, int overlay, int color) {
        poseStack.pushPose();
        cube.translateToPivotPoint(poseStack);
        cube.rotate(poseStack);
        cube.translateAwayFromPivotPoint(poseStack);
        Matrix3f normalPose = poseStack.last().normal();
        Matrix4f pose = poseStack.last().pose();
        GeoQuad[] quads = cube.quads();
        for (int i = 0; i < quads.length; i++) {
            GeoQuad quad = quads[i];
            if (quad != null && visible[i]) {
                Vector3f normal = normalPose.transform(quad.normalVec());
                RenderUtil.fixInvertedFlatCube(cube, normal);
                quad.render(pose, normal, buffer, light, overlay, color);
            }
        }
        poseStack.popPose();
    }

    private static Map<GeoCube, boolean[]> scan(BakedGeoModel model, List<Identifier> textures) {
        Map<GeoCube, boolean[]> faces = new IdentityHashMap<>();
        List<NativeImage> images = new ArrayList<>(textures.size());
        boolean missing = false;
        for (Identifier texture : textures) {
            NativeImage image = load(texture);
            if (image == null) {
                missing = true;
            } else {
                images.add(image);
            }
        }
        try {
            for (GeoBone bone : model.topLevelBones()) {
                scanBone(bone, missing ? List.of() : images, missing, faces);
            }
        } finally {
            images.forEach(NativeImage::close);
        }
        return faces;
    }

    private static void scanBone(GeoBone bone, List<NativeImage> images, boolean missing, Map<GeoCube, boolean[]> faces) {
        if (bone instanceof CuboidGeoBone cuboid) {
            for (GeoCube cube : cuboid.cubes) {
                GeoQuad[] quads = cube.quads();
                boolean[] visible = new boolean[quads.length];
                boolean any = false;
                for (int i = 0; i < quads.length; i++) {
                    visible[i] = quads[i] != null && (missing || hasPixels(images, quads[i]));
                    any |= visible[i];
                }
                if (any) {
                    faces.put(cube, visible);
                }
            }
        }
        for (GeoBone child : bone.children()) {
            scanBone(child, images, missing, faces);
        }
    }

    private static boolean hasPixels(List<NativeImage> images, GeoQuad quad) {
        for (NativeImage image : images) {
            if (hasPixels(image, quad)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPixels(NativeImage image, GeoQuad quad) {
        float minU = Float.MAX_VALUE;
        float minV = Float.MAX_VALUE;
        float maxU = -Float.MAX_VALUE;
        float maxV = -Float.MAX_VALUE;
        for (GeoVertex vertex : quad.vertices()) {
            minU = Math.min(minU, vertex.texU());
            minV = Math.min(minV, vertex.texV());
            maxU = Math.max(maxU, vertex.texU());
            maxV = Math.max(maxV, vertex.texV());
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int x0 = Mth.clamp(Mth.floor(minU * width), 0, width - 1);
        int y0 = Mth.clamp(Mth.floor(minV * height), 0, height - 1);
        int x1 = Mth.clamp(Mth.ceil(maxU * width) - 1, x0, width - 1);
        int y1 = Mth.clamp(Mth.ceil(maxV * height) - 1, y0, height - 1);
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (image.getPixel(x, y) >>> 24 != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static NativeImage load(Identifier texture) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            return null;
        }
    }
}
