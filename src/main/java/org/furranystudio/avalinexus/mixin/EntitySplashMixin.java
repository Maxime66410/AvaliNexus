/**
 * File: EntitySplashMixin.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.avalinexus.mixin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import org.furranystudio.avalinexus.fluid.FluidParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class EntitySplashMixin {

    @ModifyArg(method = "doWaterSplashEffect", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"), index = 0)
    private ParticleOptions avalinexus$tintSplash(ParticleOptions options) {
        return FluidParticles.splash((Entity) (Object) this, options);
    }
}
