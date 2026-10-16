/**
 * File: ModSounds.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

public final class ModSounds {

    public static final RegistryEntry<SoundEvent> AVALI_HURT = register("entity.avali.hurt");
    public static final RegistryEntry<SoundEvent> AVALI_DEATH = register("entity.avali.death");
    public static final RegistryEntry<SoundEvent> AVALI_NOISE = register("entity.avali.noise");
    public static final RegistryEntry<SoundEvent> UI_CLICK = register("ui.click");
    public static final RegistryEntry<SoundEvent> UI_HOVER = register("ui.hover");

    public static final RegistryEntry<SoundEvent> RAIL_PISTOL_FIRE = register("weapon.rail_pistol.fire");
    public static final RegistryEntry<SoundEvent> RAIL_ASSAULT_RIFLE_FIRE = register("weapon.rail_assault_rifle.fire");
    public static final RegistryEntry<SoundEvent> RAIL_CARBINE_FIRE = register("weapon.rail_carbine.fire");
    public static final RegistryEntry<SoundEvent> RAIL_SHOTGUN_FIRE = register("weapon.rail_shotgun.fire");
    public static final RegistryEntry<SoundEvent> RAIL_SNIPER_FIRE = register("weapon.rail_sniper.fire");
    public static final RegistryEntry<SoundEvent> RAIL_EMPTY = register("weapon.rail.empty");
    public static final RegistryEntry<SoundEvent> RAIL_RELOAD_START = register("weapon.rail.reload_start");
    public static final RegistryEntry<SoundEvent> RAIL_RELOAD_END = register("weapon.rail.reload_end");
    public static final RegistryEntry<SoundEvent> RAIL_SHELL_INSERT = register("weapon.rail.shell_insert");
    public static final RegistryEntry<SoundEvent> RAIL_SHOTGUN_PUMP = register("weapon.rail.shotgun_pump");
    public static final RegistryEntry<SoundEvent> NEXITE_QUILL_IMPACT = register("weapon.nexite_quill.impact");
    public static final RegistryEntry<SoundEvent> AEROTECH_SHATTER = register("weapon.aerotech.shatter");
    public static final RegistryEntry<SoundEvent> AEROTECH_REFORGE = register("weapon.aerotech.reforge");

    public static final RegistryEntry<SoundEvent> REFINERY_WORKING = register("block.ammonia_refinery.working");
    public static final RegistryEntry<SoundEvent> REFINERY_DONE = register("block.ammonia_refinery.done");
    public static final RegistryEntry<SoundEvent> REFINERY_IGNITE = register("block.ammonia_refinery.ignite");
    public static final RegistryEntry<SoundEvent> FREEZER_WORKING = register("block.cryo_freezer.working");
    public static final RegistryEntry<SoundEvent> FREEZER_DONE = register("block.cryo_freezer.done");
    public static final RegistryEntry<SoundEvent> FREEZER_POWDER_SNOW = register("block.cryo_freezer.powder_snow");
    public static final RegistryEntry<SoundEvent> PRESS_EXTEND = register("block.graphene_press.extend");
    public static final RegistryEntry<SoundEvent> PRESS_CONTRACT = register("block.graphene_press.contract");
    public static final RegistryEntry<SoundEvent> PRESS_DONE = register("block.graphene_press.done");
    public static final RegistryEntry<SoundEvent> WORKBENCH_WORKING = register("block.weapon_workbench.working");
    public static final RegistryEntry<SoundEvent> WORKBENCH_DONE = register("block.weapon_workbench.done");

    private ModSounds() {
    }

    public static void init() {
    }

    public static void play(Level level, Entity source, SoundEvent sound, float volume, float pitchSpread) {
        float pitch = 1.0F + (level.getRandom().nextFloat() * 2.0F - 1.0F) * pitchSpread;
        level.playSound(null, source.getX(), source.getY(), source.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static RegistryEntry<SoundEvent> register(String name) {
        return ModRegistry.register(Registries.SOUND_EVENT, name, key -> SoundEvent.createVariableRangeEvent(key.identifier()));
    }
}
