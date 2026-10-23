/**
 * File: FluidParticleProvider.java
 * Author: Maxime66410
 * Created: 2026-10-24
 * Last Modified: 2026-10-24
 */
package org.furranystudio.avalinexus.client.fluid;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class FluidParticleProvider implements ParticleProvider<ColorParticleOption> {

    private final ParticleProvider<SimpleParticleType> vanilla;
    private final SimpleParticleType type;

    private FluidParticleProvider(ParticleProvider<SimpleParticleType> vanilla, SimpleParticleType type) {
        this.vanilla = vanilla;
        this.type = type;
    }

    public static FluidParticleProvider splash(SpriteSet sprites) {
        return new FluidParticleProvider(new SplashParticle.Provider(sprites), ParticleTypes.SPLASH);
    }

    public static FluidParticleProvider bubble(SpriteSet sprites) {
        return new FluidParticleProvider(new BubbleParticle.Provider(sprites), ParticleTypes.BUBBLE);
    }

    @Override
    public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
        Particle particle = vanilla.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed, random);
        if (particle instanceof SingleQuadParticle quad) {
            quad.setColor(options.getRed(), options.getGreen(), options.getBlue());
        }
        return particle;
    }
}
