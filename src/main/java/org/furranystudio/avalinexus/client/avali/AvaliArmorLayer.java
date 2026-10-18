/**
 * File: AvaliArmorLayer.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AvaliArmorLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<AvaliEntity, Void, R> {

    private static final Map<EquipmentSlot, List<String>> ROOTS = Map.of(
        EquipmentSlot.HEAD, List.of("armor_helmet"),
        EquipmentSlot.CHEST, List.of("armor_body", "armor_left_arm", "armor_right_arm"),
        EquipmentSlot.LEGS, List.of("armor_leggings_body", "armor_left_leg", "armor_right_leg"),
        EquipmentSlot.FEET, List.of("armor_left_foot", "armor_right_foot"));

    private static final DataTicket<ArmorPieces> ARMOR = DataTicket.create("avalinexus_armor", ArmorPieces.class);

    public record Piece(EquipmentSlot slot, Identifier texture, int color) {
    }

    public record ArmorPieces(List<Piece> pieces) {
    }

    private final EquipmentAssetManager assets;
    private final CustomBoneTextureGeoLayer<AvaliEntity, Void, R> cubeRenderer;
    private final Map<BakedGeoModel, Map<EquipmentSlot, List<CuboidGeoBone>>> bonesByModel = new IdentityHashMap<>();

    public AvaliArmorLayer(GeoRenderer<AvaliEntity, Void, R> renderer, EquipmentAssetManager assets) {
        super(renderer);
        this.assets = assets;
        this.cubeRenderer = new CustomBoneTextureGeoLayer<>(renderer, "armor_helmet", AvaliLooks.ORIGINAL);
    }

    @Override
    public void addRenderData(AvaliEntity avali, Void relatedObject, R state, float partialTick) {
        List<Piece> pieces = new ArrayList<>();
        for (EquipmentSlot slot : ROOTS.keySet()) {
            ItemStack stack = avali.getItemBySlot(slot);
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
                continue;
            }
            EquipmentClientInfo.LayerType type = slot == EquipmentSlot.LEGS
                ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
            int dye = DyedItemColor.getOrDefault(stack, 0);
            for (EquipmentClientInfo.Layer layer : assets.get(equippable.assetId().get()).getLayers(type)) {
                int color = layerColor(layer, dye);
                if (color != 0 && !layer.usePlayerTexture()) {
                    pieces.add(new Piece(slot, layer.getTextureLocation(type), color));
                }
            }
        }
        if (!pieces.isEmpty()) {
            state.addGeckolibData(ARMOR, new ArmorPieces(pieces));
        }
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> info, java.util.function.BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        ArmorPieces armor = info.getOrDefaultGeckolibData(ARMOR, null);
        if (!info.willRender() || armor == null) {
            return;
        }
        Map<EquipmentSlot, List<CuboidGeoBone>> bones = bonesByModel.computeIfAbsent(info.model(), AvaliArmorLayer::collectBones);
        for (Piece piece : armor.pieces()) {
            for (CuboidGeoBone bone : bones.getOrDefault(piece.slot(), List.of())) {
                consumer.accept(bone, (passInfo, posed, collector) -> renderBone(passInfo, bone, piece, collector));
            }
        }
    }

    private void renderBone(RenderPassInfo<R> info, CuboidGeoBone bone, Piece piece, SubmitNodeCollector collector) {
        IntIntPair skinSize = RenderUtil.getTextureDimensions(renderer.getTextureLocation(info.renderState()));
        IntIntPair armorSize = RenderUtil.getTextureDimensions(piece.texture());
        float uScale = (float) skinSize.firstInt() / armorSize.firstInt();
        float vScale = (float) skinSize.secondInt() / armorSize.secondInt();
        RenderType type = renderer.getRenderType(info.renderState(), piece.texture());
        int light = info.packedLight();
        int overlay = info.packedOverlay();
        int color = ARGB.color(ARGB.alpha(info.renderColor()), piece.color());
        collector.submitCustomGeometry(info.poseStack(), type, (pose, buffer) -> {
            PoseStack poseStack = info.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);
            bone.translateAwayFromPivotPoint(poseStack);
            for (GeoCube cube : bone.cubes) {
                poseStack.pushPose();
                cubeRenderer.renderCube(cube, poseStack, buffer, light, overlay, color, uScale, vScale);
                poseStack.popPose();
            }
            poseStack.popPose();
        });
    }

    private static int layerColor(EquipmentClientInfo.Layer layer, int dye) {
        if (layer.dyeable().isEmpty()) {
            return -1;
        }
        int undyed = layer.dyeable().get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
        return dye != 0 ? ARGB.opaque(dye) : undyed;
    }

    private static Map<EquipmentSlot, List<CuboidGeoBone>> collectBones(BakedGeoModel model) {
        Map<EquipmentSlot, List<CuboidGeoBone>> result = new EnumMap<>(EquipmentSlot.class);
        ROOTS.forEach((slot, roots) -> {
            List<CuboidGeoBone> bones = new ArrayList<>();
            for (String root : roots) {
                model.getBone(root).ifPresentOrElse(bone -> collect(bone, bones),
                    () -> AvaliNexus.LOGGER.warn("[AvaliNexus] Avali model has no armor bone {}", root));
            }
            result.put(slot, bones);
        });
        return result;
    }

    private static void collect(GeoBone bone, List<CuboidGeoBone> bones) {
        if (bone instanceof CuboidGeoBone cuboid && cuboid.cubes.length > 0) {
            bones.add(cuboid);
        }
        for (GeoBone child : bone.children()) {
            collect(child, bones);
        }
    }
}
