/**
 * File: PowderSnowBlockMixin.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-17
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.furranystudio.avalinexus.item.AvaliClothes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {

    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true)
    private static void avalinexus$walkWithSnowBoots(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && entity instanceof LivingEntity living && AvaliClothes.wearsSnowBoots(living)) {
            cir.setReturnValue(true);
        }
    }
}
