/**
 * File: AvaliRenderer.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.furranystudio.avalinexus.entity.AvaliEntity;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AvaliRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AvaliEntity, R> {

    // Puts the top of the adult skull (31 px) at player height, ears and crest stick out above like a hat
    private static final float SCALE = 1.8F * 16.0F / 31.0F;

    private final Map<BakedGeoModel, List<String>> hiddenBones = new IdentityHashMap<>();

    public AvaliRenderer(EntityRendererProvider.Context context) {
        super(context, new AvaliModel());
        withScale(SCALE);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        R state = renderPassInfo.renderState();
        snapshots.ifPresent("head", head -> head.setRotation(
            head.getRotX() - state.xRot * Mth.DEG_TO_RAD,
            head.getRotY() - state.yRot * Mth.DEG_TO_RAD,
            head.getRotZ()));

        List<String> bones = hiddenBones.computeIfAbsent(renderPassInfo.model(), model ->
            model.boneLookup().get().keySet().stream().filter(AvaliRenderer::isHiddenByDefault).toList());
        for (String bone : bones) {
            snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(true));
        }
    }

    // Hidden in Blockbench but the geo export doesn't keep that, so alt expressions and armor would all show
    private static boolean isHiddenByDefault(String bone) {
        if (bone.startsWith("Eye ") || bone.startsWith("Mouth ")) {
            return !bone.endsWith(" Normal");
        }
        return bone.startsWith("Cheek ") || bone.startsWith("Tongue") || bone.startsWith("Feathers Crest Front")
            || bone.startsWith("Outfit Thigh") || bone.startsWith("Outfit Calf") || bone.startsWith("Outfit Foot")
            || bone.startsWith("Outfit Toes") || bone.startsWith("Helmet") || bone.startsWith("Earplate")
            || bone.startsWith("Chestplate") || bone.startsWith("Leggings") || bone.startsWith("Boots");
    }
}
