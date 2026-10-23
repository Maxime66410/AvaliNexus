/**
 * File: ServerPlayerGameModeMixin.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.furranystudio.avalinexus.reputation.VillageWatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Unique
    private boolean avalinexus$watched;

    @Unique
    private boolean avalinexus$crop;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void avalinexus$checkVillage(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        avalinexus$watched = VillageWatch.isVillageBlock(level, pos);
        avalinexus$crop = VillageWatch.isFieldCrop(level, pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void avalinexus$watchVillage(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (avalinexus$watched && cir.getReturnValueZ()) {
            VillageWatch.broke(player, pos);
        }
        if (avalinexus$crop && cir.getReturnValueZ()) {
            VillageWatch.tookCrop(player, pos);
        }
        avalinexus$watched = false;
        avalinexus$crop = false;
    }
}
