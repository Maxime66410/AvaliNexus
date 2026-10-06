/**
 * File: MouseHandlerAimMixin.java
 * Author: Maxime66410
 * Created: 2026-10-07
 * Last Modified: 2026-10-07
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.client.MouseHandler;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Slows the mouse while aiming a rail gun, as much as the view zooms in, shared by the three loaders
@Mixin(MouseHandler.class)
public class MouseHandlerAimMixin {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void avalinexus$aimSensitivity(double movementTime, CallbackInfo ci) {
        double scale = RailGunClient.aimSensitivity();
        accumulatedDX *= scale;
        accumulatedDY *= scale;
    }
}
