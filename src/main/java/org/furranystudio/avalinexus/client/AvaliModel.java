/**
 * File: AvaliModel.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.AvaliEntity;

public class AvaliModel extends GeoModel<AvaliEntity> {

    private static final Identifier ADULT_MODEL = AvaliNexus.id("entity/avali");
    private static final Identifier CHILD_MODEL = AvaliNexus.id("entity/avali_child");
    private static final Identifier ANIMATIONS = AvaliNexus.id("entity/avali");
    private static final Identifier TEXTURE = AvaliNexus.id("textures/entity/avali.png");

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return state instanceof LivingEntityRenderState living && living.isBaby ? CHILD_MODEL : ADULT_MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(AvaliEntity avali) {
        return ANIMATIONS;
    }
}
