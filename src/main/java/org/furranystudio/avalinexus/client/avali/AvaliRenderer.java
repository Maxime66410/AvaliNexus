/**
 * File: AvaliRenderer.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Ease;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliFace;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AvaliRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AvaliEntity, R> {

    // Puts the top of the adult skull (31 px) at player height, ears and crest stick out above like a hat
    private static final float SCALE = 1.8F * 16.0F / 31.0F;

    private static final DataTicket<Boolean> BLINKING = DataTicket.create("avalinexus_blinking", Boolean.class);
    private static final DataTicket<Float> SWIM_AMOUNT = DataTicket.create("avalinexus_swim_amount", Float.class);
    private static final DataTicket<Float> SWIM_PITCH = DataTicket.create("avalinexus_swim_pitch", Float.class);
    private static final DataTicket<AvaliFace> FACE = DataTicket.create("avalinexus_face", AvaliFace.class);
    private static final DataTicket<Boolean> RIDING = DataTicket.create("avalinexus_riding", Boolean.class);
    private static final DataTicket<Boolean> RIGHT_HOLDING = DataTicket.create("avalinexus_right_holding", Boolean.class);
    private static final DataTicket<Boolean> LEFT_HOLDING = DataTicket.create("avalinexus_left_holding", Boolean.class);
    private static final DataTicket<Float> SWING = DataTicket.create("avalinexus_swing", Float.class);
    private static final DataTicket<Boolean> SWING_LEFT = DataTicket.create("avalinexus_swing_left", Boolean.class);
    private static final DataTicket<Boolean> BLOCKING = DataTicket.create("avalinexus_blocking", Boolean.class);
    private static final DataTicket<Boolean> BLOCK_LEFT = DataTicket.create("avalinexus_block_left", Boolean.class);
    private static final String[] SIDES = {"Left", "Right"};
    private static final String[] MOUTHS = {"Mouth Left 0", "Mouth Right 0", "Mouth Left 1", "Mouth Right 1", "Mouth Front"};
    private static final int BLINK_CYCLE = 300;
    private static final Map<String, String> ARMOR_ROOTS = Map.of(
        "armor_helmet", "head",
        "armor_body", "body",
        "armor_left_arm", "left_arm",
        "armor_right_arm", "right_arm",
        "armor_leggings_body", "body",
        "armor_left_leg", "left_leg",
        "armor_right_leg", "right_leg",
        "armor_left_foot", "left_leg",
        "armor_right_foot", "right_leg");

    private final Map<BakedGeoModel, List<String>> hiddenBones = new IdentityHashMap<>();

    public AvaliRenderer(EntityRendererProvider.Context context) {
        super(context, new AvaliModel());
        withScale(SCALE);
        withRenderLayer(new AvaliOverlayLayer<>(this, AvaliLooks.PATTERN, AvaliLooks::pattern, true, 1));
        withRenderLayer(new AvaliOverlayLayer<>(this, AvaliLooks.FIXED, AvaliLooks::fixed, false, 2));
        withRenderLayer(new AvaliArmorLayer<>(this, context.getEquipmentAssets()));
        withRenderLayer(new AvaliItemLayer<>(context, this));
    }

    @Override
    public void addRenderData(AvaliEntity avali, Void relatedObject, R state, float partialTick) {
        super.addRenderData(avali, relatedObject, state, partialTick);
        state.addGeckolibData(BLINKING, isBlinking(avali));
        state.addGeckolibData(SWIM_AMOUNT, avali.getSwimAmount(partialTick));
        state.addGeckolibData(SWIM_PITCH, avali.getSwimPitch(partialTick));
        state.addGeckolibData(FACE, avali.getFace());
        state.addGeckolibData(RIDING, avali.isPassenger());
        state.addGeckolibData(AvaliLooks.TEXTURE, AvaliLooks.texture(avali));
        HumanoidArm mainArm = avali.getMainArm();
        boolean rightHanded = mainArm == HumanoidArm.RIGHT;
        state.addGeckolibData(RIGHT_HOLDING, !(rightHanded ? avali.getMainHandItem() : avali.getOffhandItem()).isEmpty());
        state.addGeckolibData(LEFT_HOLDING, !(rightHanded ? avali.getOffhandItem() : avali.getMainHandItem()).isEmpty());
        LivingEntity.SwingDescription swing = avali.getCurrentSwing();
        state.addGeckolibData(SWING, swing == null ? 0.0F : avali.getSwingAnimation(partialTick));
        state.addGeckolibData(SWING_LEFT, swing != null && swing.hand().asArm(mainArm) == HumanoidArm.LEFT);
        state.addGeckolibData(BLOCKING, avali.isUsingItem() && avali.getUsedItemHand() == InteractionHand.OFF_HAND);
        state.addGeckolibData(BLOCK_LEFT, InteractionHand.OFF_HAND.asArm(mainArm) == HumanoidArm.LEFT);
    }

    @Override
    public int getRenderColor(AvaliEntity avali, Void relatedObject, float partialTick) {
        int color = super.getRenderColor(avali, relatedObject, partialTick);
        return AvaliLooks.tinted(avali) ? ARGB.multiply(color, ARGB.opaque(avali.getTint())) : color;
    }

    @Override
    protected void applyRotations(RenderPassInfo<R> renderPassInfo, PoseStack poseStack, float nativeScale) {
        super.applyRotations(renderPassInfo, poseStack, nativeScale);

        R state = renderPassInfo.renderState();
        float swim = state.getOrDefaultGeckolibData(SWIM_AMOUNT, 0.0F);
        if (swim > 0.0F) {
            float pitch = state.getOrDefaultGeckolibData(SWIM_PITCH, 0.0F);
            poseStack.rotateDegrees(Axis.XP, Mth.lerp(swim, 0.0F, -90.0F - pitch));
            poseStack.translate(0.0F, -swim, 0.3F * swim);
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        R state = renderPassInfo.renderState();
        float swim = state.getOrDefaultGeckolibData(SWIM_AMOUNT, 0.0F);
        float pos = state.walkAnimationPos;
        boolean sleeping = state.hasPose(Pose.SLEEPING);
        boolean riding = state.getOrDefaultGeckolibData(RIDING, false);
        float speed = sleeping || riding ? 0.0F : Math.min(state.walkAnimationSpeed, 1.0F);

        Rotation head = sleeping ? new Rotation(0.0F, 0.0F, 0.0F)
            : new Rotation(state.xRot * Mth.DEG_TO_RAD, state.yRot * Mth.DEG_TO_RAD, 0.0F);
        Rotation rightArm = new Rotation(Mth.cos(pos * 0.6662F + Mth.PI) * speed, 0.0F, 0.0F);
        Rotation leftArm = new Rotation(Mth.cos(pos * 0.6662F) * speed, 0.0F, 0.0F);
        Rotation rightLeg = new Rotation(Mth.cos(pos * 0.6662F) * 1.4F * speed, 0.0F, 0.0F);
        Rotation leftLeg = new Rotation(Mth.cos(pos * 0.6662F + Mth.PI) * 1.4F * speed, 0.0F, 0.0F);

        // Vanilla sitting pose, the CPM riding animation goes on top of it
        if (riding) {
            rightArm = new Rotation(rightArm.x - Mth.PI / 5.0F, rightArm.y, rightArm.z);
            leftArm = new Rotation(leftArm.x - Mth.PI / 5.0F, leftArm.y, leftArm.z);
            rightLeg = new Rotation(-1.4137167F, Mth.PI / 10.0F, 0.07853982F);
            leftLeg = new Rotation(-1.4137167F, -Mth.PI / 10.0F, -0.07853982F);
        }

        if (state.getOrDefaultGeckolibData(RIGHT_HOLDING, false)) {
            rightArm = holding(rightArm);
        }
        if (state.getOrDefaultGeckolibData(LEFT_HOLDING, false)) {
            leftArm = holding(leftArm);
        }
        if (state.getOrDefaultGeckolibData(BLOCKING, false)) {
            if (state.getOrDefaultGeckolibData(BLOCK_LEFT, true)) {
                leftArm = new Rotation(leftArm.x - 0.4F, 0.3F, leftArm.z);
            } else {
                rightArm = new Rotation(rightArm.x - 0.4F, -0.3F, rightArm.z);
            }
        }

        float swing = state.getOrDefaultGeckolibData(SWING, 0.0F);
        if (swing > 0.0F) {
            boolean swingLeft = state.getOrDefaultGeckolibData(SWING_LEFT, false);
            float twist = Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F) * 0.2F * (swingLeft ? -1.0F : 1.0F);
            rightArm = new Rotation(rightArm.x, rightArm.y + twist, rightArm.z);
            leftArm = new Rotation(leftArm.x + twist, leftArm.y + twist, leftArm.z);
            float lift = Mth.sin(Ease.outQuart(swing) * Mth.PI) * 1.2F + Mth.sin(swing * Mth.PI) * -(head.x - 0.7F) * 0.75F;
            float roll = Mth.sin(swing * Mth.PI) * -0.4F;
            if (swingLeft) {
                leftArm = new Rotation(leftArm.x - lift, leftArm.y + twist * 2.0F, leftArm.z + roll);
            } else {
                rightArm = new Rotation(rightArm.x - lift, rightArm.y + twist * 2.0F, rightArm.z + roll);
            }
            rotateBone(snapshots, "body", new Rotation(0.0F, twist, 0.0F));
        }

        if (swim > 0.0F) {
            head = new Rotation(Mth.rotLerpRad(swim, head.x, -Mth.PI / 4.0F), head.y, head.z);
            Rotation[] stroke = swimStroke(pos);
            leftArm = leftArm.lerpTo(stroke[0], swim);
            rightArm = rightArm.lerpTo(stroke[1], swim);
            leftLeg = new Rotation(Mth.lerp(swim, leftLeg.x, 0.3F * Mth.cos(pos * 0.33333334F + Mth.PI)), 0.0F, 0.0F);
            rightLeg = new Rotation(Mth.lerp(swim, rightLeg.x, 0.3F * Mth.cos(pos * 0.33333334F)), 0.0F, 0.0F);
        }

        rotateBone(snapshots, "head", head);
        rotateBone(snapshots, "left_arm", leftArm);
        rotateBone(snapshots, "right_arm", rightArm);
        rotateBone(snapshots, "left_leg", leftLeg);
        rotateBone(snapshots, "right_leg", rightLeg);
        ARMOR_ROOTS.forEach((armor, part) -> snapshots.get(part).ifPresent(source -> snapshots.ifPresent(armor, target -> target
            .setRotation(source.getRotX(), source.getRotY(), source.getRotZ())
            .setTranslation(source.getTranslateX(), source.getTranslateY(), source.getTranslateZ()))));

        List<String> bones = hiddenBones.computeIfAbsent(renderPassInfo.model(), model ->
            model.boneLookup().get().keySet().stream().filter(AvaliRenderer::isHiddenByDefault).toList());
        for (String bone : bones) {
            snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(true));
        }

        applyFace(snapshots, state.getOrDefaultGeckolibData(FACE, AvaliFace.NEUTRAL), state.getOrDefaultGeckolibData(BLINKING, false));
    }

    private static void applyFace(BoneSnapshots snapshots, AvaliFace face, boolean blinking) {
        String eyes = blinking ? "Closed" : face.eyes();
        if (!eyes.equals("Normal")) {
            for (String side : SIDES) {
                setVisible(snapshots, "Eye " + side + " Normal", false);
                setVisible(snapshots, "Eye Glow " + side + " Normal", false);
                setVisible(snapshots, "Eye " + side + " " + eyes, true);
                setVisible(snapshots, "Eye Glow " + side + " " + eyes, true);
            }
        }
        if (!face.mouth().equals("Normal")) {
            for (String mouth : MOUTHS) {
                setVisible(snapshots, mouth + " Normal", false);
                setVisible(snapshots, mouth + " " + face.mouth(), true);
            }
        }
        if (face.blush()) {
            for (String side : SIDES) {
                setVisible(snapshots, "Cheek " + side + " Blush", true);
            }
        }
        if (face.tongue()) {
            setVisible(snapshots, "Tongue", true);
        }
    }

    // Player crawl stroke over a 26 unit cycle, returns left then right arm
    private static Rotation[] swimStroke(float pos) {
        float cycle = pos % 26.0F;
        if (cycle < 14.0F) {
            float progress = quadraticArmUpdate(cycle) / quadraticArmUpdate(14.0F);
            return new Rotation[] {
                new Rotation(0.0F, Mth.PI, Mth.PI + 1.8707964F * progress),
                new Rotation(0.0F, Mth.PI, Mth.PI - 1.8707964F * progress)};
        }
        if (cycle < 22.0F) {
            float progress = (cycle - 14.0F) / 8.0F;
            return new Rotation[] {
                new Rotation(Mth.HALF_PI * progress, Mth.PI, 5.012389F - 1.8707964F * progress),
                new Rotation(Mth.HALF_PI * progress, Mth.PI, 1.2707963F + 1.8707964F * progress)};
        }
        float progress = (cycle - 22.0F) / 4.0F;
        float x = Mth.HALF_PI - Mth.HALF_PI * progress;
        return new Rotation[] {new Rotation(x, Mth.PI, Mth.PI), new Rotation(x, Mth.PI, Mth.PI)};
    }

    private static Rotation holding(Rotation arm) {
        return new Rotation(arm.x * 0.5F - Mth.PI / 10.0F, 0.0F, arm.z);
    }

    private static float quadraticArmUpdate(float value) {
        return -65.0F * value + value * value;
    }

    // Geo bones have X and Y flipped compared to vanilla model parts
    private static void rotateBone(BoneSnapshots snapshots, String bone, Rotation rotation) {
        snapshots.ifPresent(bone, snapshot -> snapshot.setRotation(
            snapshot.getRotX() - rotation.x,
            snapshot.getRotY() - rotation.y,
            snapshot.getRotZ() + rotation.z));
    }

    // Same timing as the CPM blink: two short blinks every 15 seconds, offset per Avali so they don't sync up
    private static boolean isBlinking(AvaliEntity avali) {
        int tick = Math.floorMod(avali.tickCount + avali.getId() * 97, BLINK_CYCLE);
        return (tick >= 10 && tick < 15) || (tick >= 128 && tick < 133);
    }

    private static void setVisible(BoneSnapshots snapshots, String bone, boolean visible) {
        snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(!visible));
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

    private record Rotation(float x, float y, float z) {

        Rotation lerpTo(Rotation target, float delta) {
            return new Rotation(
                Mth.rotLerpRad(delta, x, target.x),
                Mth.rotLerpRad(delta, y, target.y),
                Mth.rotLerpRad(delta, z, target.z));
        }
    }
}
