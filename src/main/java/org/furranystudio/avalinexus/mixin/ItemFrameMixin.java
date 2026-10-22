/**
 * File: ItemFrameMixin.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-22
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.reputation.VillageWatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrame.class)
public class ItemFrameMixin {

    @Inject(method = "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Z)V", at = @At("HEAD"))
    private void avalinexus$watchVillage(ServerLevel level, Entity entity, boolean dropSelf, CallbackInfo ci) {
        ItemFrame frame = (ItemFrame) (Object) this;
        if (entity instanceof ServerPlayer player && !frame.getItem().isEmpty() && frame.removeTag(VillageWatch.FRAME_TAG)) {
            VillageWatch.crime(player, frame.getPos(), AvaliReputation.Change.STEAL);
        }
    }
}
