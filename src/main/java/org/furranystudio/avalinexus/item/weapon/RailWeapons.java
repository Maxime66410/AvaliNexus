/**
 * File: RailWeapons.java
 * Author: Maxime66410
 * Created: 2026-09-29
 * Last Modified: 2026-09-29
 */
package org.furranystudio.avalinexus.item.weapon;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.UseEffects;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;
import org.furranystudio.avalinexus.sound.ModSounds;

import java.util.List;

// Avali rail weapons: magnetic launchers that fire Nexite quills
public final class RailWeapons {

    // Nexite quills loaded in the magazine
    public static final RegistryEntry<DataComponentType<Integer>> AMMO = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "rail_ammo",
        key -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    // Game time the running reload ends at, absent when not reloading
    public static final RegistryEntry<DataComponentType<Long>> RELOAD_END = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "rail_reload_end",
        key -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());
    // Game time the running reload started at, the HUD bar and the shell steps count from it
    public static final RegistryEntry<DataComponentType<Long>> RELOAD_START = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "rail_reload_start",
        key -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG).build());
    // Shells a shell by shell reload will insert, and how many are already in
    public static final RegistryEntry<DataComponentType<Integer>> RELOAD_SHELLS = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "rail_reload_shells",
        key -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final RegistryEntry<DataComponentType<Integer>> RELOAD_LOADED = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "rail_reload_loaded",
        key -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    // Automatic, light and quick to reload
    public static final RailStats PISTOL = new RailStats(6.0F, 4, 16, 30, 4.5F, 2.0F, 0.5F, 0.85F, 0.6F, true, 1.8F, 1, false);
    // Automatic with a big magazine, loses its aim on long bursts
    public static final RailStats ASSAULT_RIFLE = new RailStats(7.0F, 3, 30, 50, 5.5F, 2.5F, 0.6F, 0.7F, 0.8F, true, 1.6F, 1, false);
    // Semi automatic, precise when aimed
    public static final RailStats CARBINE = new RailStats(11.0F, 8, 10, 45, 6.0F, 1.5F, 0.2F, 0.6F, 1.2F, false, 1.4F, 1, false);
    // A wide spray of darts, deadly up close and weak at range, loaded one Nexite quill at a time
    public static final RailStats SHOTGUN = new RailStats(4.0F, 16, 6, 60, 3.5F, 7.0F, 5.0F, 0.9F, 3.5F, false, 0.9F, 8, true);
    // One heavy shot, useless from the hip, dead on through the scope
    public static final RailStats SNIPER = new RailStats(30.0F, 30, 4, 60, 9.0F, 4.0F, 0.0F, 0.2F, 3.0F, false, 0.8F, 1, false);

    public static final RegistryEntry<RailGunItem> RAIL_PISTOL = gun("rail_pistol", PISTOL, ModSounds.RAIL_PISTOL_FIRE);
    public static final RegistryEntry<RailGunItem> RAIL_ASSAULT_RIFLE = gun("rail_assault_rifle", ASSAULT_RIFLE, ModSounds.RAIL_ASSAULT_RIFLE_FIRE);
    public static final RegistryEntry<RailGunItem> RAIL_CARBINE = gun("rail_carbine", CARBINE, ModSounds.RAIL_CARBINE_FIRE);
    public static final RegistryEntry<RailGunItem> RAIL_SHOTGUN = gun("rail_shotgun", SHOTGUN, ModSounds.RAIL_SHOTGUN_FIRE);
    public static final RegistryEntry<RailGunItem> RAIL_SNIPER = gun("rail_sniper", SNIPER, ModSounds.RAIL_SNIPER_FIRE);

    public static final RegistryEntry<Item> NEXITE_QUILL = ModRegistry.register(Registries.ITEM, "nexite_quill",
        key -> new Item(new Item.Properties().setId(key)));

    public static final List<RegistryEntry<? extends Item>> ALL = List.of(RAIL_PISTOL, RAIL_ASSAULT_RIFLE, RAIL_CARBINE, RAIL_SHOTGUN, RAIL_SNIPER, NEXITE_QUILL);

    private RailWeapons() {
    }

    public static void init() {
    }

    // Aiming slows down a bit instead of the heavy bow slowdown
    private static RegistryEntry<RailGunItem> gun(String name, RailStats stats, RegistryEntry<SoundEvent> fireSound) {
        return ModRegistry.register(Registries.ITEM, name, key -> new RailGunItem(stats, fireSound, new Item.Properties().setId(key).stacksTo(1)
            .component(DataComponents.USE_EFFECTS, new UseEffects(false, false, 0.6F))));
    }
}
