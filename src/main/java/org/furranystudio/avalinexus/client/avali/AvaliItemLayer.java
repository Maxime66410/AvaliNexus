/**
 * File: AvaliItemLayer.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-11-18
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.KineticWeapon;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

public class AvaliItemLayer<R extends LivingEntityRenderState & GeoRenderState> extends ItemInHandGeoLayer<AvaliEntity, Void, R> {

    private static final float ADULT_HAND_DROP = 9.0F / 16.0F;
    private static final DataTicket<Float> STAB = DataTicket.create("avalinexus_stab", Float.class);
    private static final DataTicket<Boolean> STAB_RIGHT = DataTicket.create("avalinexus_stab_right", Boolean.class);
    private static final DataTicket<Float> STAB_REACH = DataTicket.create("avalinexus_stab_reach", Float.class);

    public AvaliItemLayer(EntityRendererProvider.Context context, GeoRenderer<AvaliEntity, Void, R> renderer) {
        super(context, renderer, "right_arm", "left_arm");
    }

    @Override
    public void addRenderData(AvaliEntity avali, Void relatedObject, R state, float partialTick) {
        super.addRenderData(avali, relatedObject, state, partialTick);
        state.addGeckolibData(MAINHAND_SHIELD, false);
        state.addGeckolibData(OFFHAND_SHIELD, false);
        LivingEntity.SwingDescription swing = avali.getCurrentSwing();
        boolean stab = swing != null && swing.animation().type() == SwingAnimationType.STAB;
        KineticWeapon kinetic = avali.getMainHandItem().get(DataComponents.KINETIC_WEAPON);
        state.addGeckolibData(STAB, stab ? avali.getSwingAnimation(partialTick) : 0.0F);
        state.addGeckolibData(STAB_RIGHT, stab && swing.hand().asArm(avali.getMainArm()) == HumanoidArm.RIGHT);
        state.addGeckolibData(STAB_REACH, kinetic != null ? kinetic.forwardMovement() : 0.0F);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemState, ItemDisplayContext context,
                                         R state, SubmitNodeCollector collector, int light) {
        poseStack.translate(0.0F, -ADULT_HAND_DROP * (state.isBaby ? 0.5F : 1.0F), 0.0F);
        float stab = state.getOrDefaultGeckolibData(STAB, 0.0F);
        boolean rightHand = context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (stab <= 0.0F || rightHand != state.getOrDefaultGeckolibData(STAB_RIGHT, false)) {
            super.submitItemStackRender(poseStack, bone, itemState, context, state, collector, light);
            return;
        }
        float thrust = SpearMotion.attack(stab) - SpearMotion.retract(stab);
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.XN, 90.0F);
        poseStack.translate(0.0F, 0.125F, -0.0625F);
        poseStack.rotateAround(Axis.XN.rotationDegrees(70.0F * thrust), 0.0F, -0.125F, 0.125F);
        poseStack.translate(0.0F, state.getOrDefaultGeckolibData(STAB_REACH, 0.0F) * thrust, 0.0F);
        itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
