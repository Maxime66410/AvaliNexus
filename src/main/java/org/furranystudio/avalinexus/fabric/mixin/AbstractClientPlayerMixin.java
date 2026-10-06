/**
 * File: AbstractClientPlayerMixin.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.fabric.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fabric API has no field of view event, this zooms while aiming a rail gun
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void avalinexus$zoom(boolean firstPerson, float effectScale, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(RailGunClient.fovModifier((AbstractClientPlayer) (Object) this, cir.getReturnValue()));
    }
}
