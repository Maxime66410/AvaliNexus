/**
 * File: MinecraftAttackMixin.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.fabric.mixin;

import net.minecraft.client.Minecraft;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Left click fires a held rail gun, so it must not hit or mine, Forge and NeoForge cancel their attack event instead
@Mixin(Minecraft.class)
public class MinecraftAttackMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void avalinexus$startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (RailGunClient.blocksAttack()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void avalinexus$continueAttack(boolean leftClick, CallbackInfo ci) {
        if (RailGunClient.blocksAttack()) {
            ci.cancel();
        }
    }
}
