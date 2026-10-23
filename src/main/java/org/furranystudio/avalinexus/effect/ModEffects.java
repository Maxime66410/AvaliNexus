/**
 * File: ModEffects.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModEffects {

    private static final int TONIC_COLOR = 0xE8572A;
    private static final int TONIC_TICKS = 1200;
    private static final int WARMTH_COLOR = 0xFF8C1A;

    public static final RegistryEntry<MobEffect> NEXITE_TONIC = ModRegistry.register(Registries.MOB_EFFECT, "nexite_tonic",
        key -> new NexiteTonicEffect(TONIC_COLOR));

    public static final RegistryEntry<MobEffect> WARMTH = ModRegistry.register(Registries.MOB_EFFECT, "warmth",
        key -> new WarmthEffect(WARMTH_COLOR));

    public static final RegistryEntry<Potion> NEXITE_TONIC_POTION = ModRegistry.register(Registries.POTION, "nexite_tonic",
        key -> new Potion("nexite_tonic", new MobEffectInstance(tonic(), TONIC_TICKS)));

    private ModEffects() {
    }

    public static void init() {
    }

    public static Holder<MobEffect> tonic() {
        return BuiltInRegistries.MOB_EFFECT.getOrThrow(ResourceKey.create(Registries.MOB_EFFECT, NEXITE_TONIC.id()));
    }

    public static Holder<MobEffect> warmth() {
        return BuiltInRegistries.MOB_EFFECT.getOrThrow(ResourceKey.create(Registries.MOB_EFFECT, WARMTH.id()));
    }

    public static Holder<Potion> tonicPotion() {
        return BuiltInRegistries.POTION.getOrThrow(ResourceKey.create(Registries.POTION, NEXITE_TONIC_POTION.id()));
    }
}
