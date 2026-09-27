/**
 * File: ModSounds.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModSounds {

    public static final RegistryEntry<SoundEvent> AVALI_HURT = register("entity.avali.hurt");
    public static final RegistryEntry<SoundEvent> AVALI_DEATH = register("entity.avali.death");
    public static final RegistryEntry<SoundEvent> AVALI_NOISE = register("entity.avali.noise");

    private ModSounds() {
    }

    public static void init() {
    }

    private static RegistryEntry<SoundEvent> register(String name) {
        return ModRegistry.register(Registries.SOUND_EVENT, name, key -> SoundEvent.createVariableRangeEvent(key.identifier()));
    }
}
