/**
 * File: MouseHandlerMixin.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.fabric.mixin;

import net.minecraft.client.MouseHandler;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fabric API has no scroll event, Forge and NeoForge use their own
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void avalinexus$onScroll(long window, double scrollX, double scrollY, CallbackInfo ci) {
        if (ClientDialogue.onScroll(scrollY)) {
            ci.cancel();
        }
    }
}
