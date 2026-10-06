/**
 * File: RailGunRenderer.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.client.weapon;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;

// Tells the animations whether the gun is held in first person, aimed or reloaded
public class RailGunRenderer extends GeoItemRenderer<RailGunItem> {

    public RailGunRenderer(RailGunItem gun) {
        super(new Model(BuiltInRegistries.ITEM.getKey(gun)));
    }

    @Override
    public void captureDefaultRenderState(RailGunItem gun, RenderData data, GeoRenderState state, float partialTick) {
        super.captureDefaultRenderState(gun, data, state, partialTick);
        boolean firstPerson = data.renderPerspective() != null && data.renderPerspective().firstPerson();
        boolean aiming = firstPerson && data.itemOwner() instanceof Player player && RailGunItem.isAiming(player);
        state.addGeckolibData(RailGunItem.FIRST_PERSON, firstPerson);
        state.addGeckolibData(RailGunItem.AIMING, aiming);
        state.addGeckolibData(RailGunItem.RELOADING, RailGunItem.isReloading(data.itemStack()));
        long now = data.level() != null ? data.level().getGameTime() : 0L;
        state.addGeckolibData(RailGunItem.RELOAD_STEP, RailGunItem.reloadStep(data.itemStack(), now));
    }

    // Creative copies share the gun id, so a gun out of a hand gets its own animation instance and never sees the shots
    @Override
    public long getInstanceId(RailGunItem gun, RenderData data) {
        long id = super.getInstanceId(gun, data);
        ItemDisplayContext context = data.renderPerspective();
        boolean held = context != null && (context.firstPerson()
            || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        return held ? id : ~id;
    }

    // geckolib/models/item/<name>.geo.json, geckolib/animations/item/<name>.animation.json, textures/item/<name>/<name>_base.png
    private static class Model extends GeoModel<RailGunItem> {

        private final Identifier model;
        private final Identifier texture;

        Model(Identifier id) {
            this.model = id.withPrefix("item/");
            this.texture = id.withPath(path -> "textures/item/" + path + "/" + path + "_base.png");
        }

        @Override
        public Identifier getModelResource(GeoRenderState state) {
            return model;
        }

        @Override
        public Identifier getTextureResource(GeoRenderState state) {
            return texture;
        }

        @Override
        public Identifier getAnimationResource(RailGunItem gun) {
            return model;
        }
    }
}
