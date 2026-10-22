/**
 * File: ServerPlayerMenuMixin.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.furranystudio.avalinexus.reputation.VillageWatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMenuMixin {

    @Inject(method = "initMenu", at = @At("TAIL"))
    private void avalinexus$watchVillage(AbstractContainerMenu menu, CallbackInfo ci) {
        VillageWatch.watch((ServerPlayer) (Object) this, menu);
    }
}
