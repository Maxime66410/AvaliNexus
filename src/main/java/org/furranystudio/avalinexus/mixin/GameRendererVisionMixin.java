/**
 * File: GameRendererVisionMixin.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.client.vision.AvaliVision;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererVisionMixin {

    @Shadow
    @Final
    private List<Identifier> requestedPostEffects;

    @Inject(method = "update", at = @At("TAIL"))
    private void avalinexus$requestAvaliVision(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (AvaliVision.prepare()) {
            requestedPostEffects.add(AvaliVision.EFFECT);
        }
    }
}
