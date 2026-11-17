/**
 * File: EnchantmentHelperMixin.java
 * Author: Maxime66410
 * Created: 2026-11-17
 * Last Modified: 2026-11-17
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.furranystudio.avalinexus.item.AvaliClothes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getDamageProtection", at = @At("RETURN"), cancellable = true)
    private static void avalinexus$clothesBlastProtection(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Float> cir) {
        float protection = AvaliClothes.blastProtection(entity, source);
        if (protection > 0.0F) {
            cir.setReturnValue(cir.getReturnValueF() + protection);
        }
    }
}
