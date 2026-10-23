/**
 * File: LivingEntityDrownMixin.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import org.furranystudio.avalinexus.fluid.FluidParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public class LivingEntityDrownMixin {

    @ModifyArg(method = "makeDrownParticles", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 0)
    private ParticleOptions avalinexus$tintDrown(ParticleOptions options) {
        return FluidParticles.drown((LivingEntity) (Object) this, options);
    }
}
