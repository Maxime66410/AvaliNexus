/**
 * File: AvaliClothes.java
 * Author: Maxime66410
 * Created: 2026-11-14
 * Last Modified: 2026-11-17
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.List;
import java.util.Map;

public final class AvaliClothes {

    public static final TagKey<Item> REPAIRS = TagKey.create(Registries.ITEM, AvaliNexus.id("repairs_avali_clothes"));
    public static final TagKey<DamageType> RESISTS = TagKey.create(Registries.DAMAGE_TYPE, AvaliNexus.id("avali_clothes_resist"));

    private static final int DURABILITY = 5;
    private static final int ENCHANTMENT_VALUE = 15;
    private static final float BLAST_PROTECTION = 2.0F;

    public static final RegistryEntry<Item> TRENCH_COAT = item("avali_trench_coat", ArmorType.CHESTPLATE);
    public static final RegistryEntry<Item> SNOW_BOOTS = item("avali_snow_boots", ArmorType.BOOTS);

    public static final List<RegistryEntry<Item>> ALL = List.of(TRENCH_COAT, SNOW_BOOTS);

    private AvaliClothes() {
    }

    public static void init() {
    }

    public static boolean isClothing(ItemStack stack) {
        for (RegistryEntry<Item> entry : ALL) {
            if (stack.is(entry.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean wearsTrenchCoat(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(TRENCH_COAT.get());
    }

    public static boolean wearsSnowBoots(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).is(SNOW_BOOTS.get());
    }

    public static float blastProtection(LivingEntity entity, DamageSource source) {
        if (!source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0.0F;
        }
        float protection = 0.0F;
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.isArmor() && isClothing(entity.getItemBySlot(slot))) {
                protection += BLAST_PROTECTION;
            }
        }
        return protection;
    }

    private static RegistryEntry<Item> item(String name, ArmorType type) {
        ArmorMaterial material = new ArmorMaterial(DURABILITY,
            Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 2, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1, ArmorType.BODY, 3),
            ENCHANTMENT_VALUE, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, REPAIRS,
            ResourceKey.create(EquipmentAssets.ROOT_ID, AvaliNexus.id(name)));
        return ModRegistry.register(Registries.ITEM, name, key -> new Item(new Item.Properties().setId(key).humanoidArmor(material, type)
            .delayedComponent(DataComponents.DAMAGE_RESISTANT, provider -> new DamageResistant(provider.getOrThrow(RESISTS)))));
    }
}
