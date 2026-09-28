/**
 * File: NexiteGear.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

// Nexite tools, weapons and armor, right between diamond and netherite, made by upgrading diamond gear
// The armor is in freeze_immune_wearables, so like leather it keeps the polar cold away
public final class NexiteGear {

    public static final TagKey<Item> TOOL_MATERIALS = TagKey.create(Registries.ITEM, AvaliNexus.id("nexite_tool_materials"));
    public static final TagKey<Item> REPAIRS_ARMOR = TagKey.create(Registries.ITEM, AvaliNexus.id("repairs_nexite_armor"));

    // Diamond 1561 / 8 / 3 / 10, netherite 2031 / 9 / 4 / 15
    public static final ToolMaterial TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1796, 8.5F, 3.5F, 12, TOOL_MATERIALS);

    // Diamond 33 / toughness 2 / no knockback resistance, netherite 37 / 3 / 0.1
    public static final ArmorMaterial ARMOR = new ArmorMaterial(35,
        Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3, ArmorType.BODY, 15),
        12, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.5F, 0.05F, REPAIRS_ARMOR,
        ResourceKey.create(EquipmentAssets.ROOT_ID, AvaliNexus.id("nexite")));

    public static final RegistryEntry<Item> SWORD = item("nexite_sword", p -> p.sword(TOOL, 3.0F, -2.4F));
    public static final RegistryEntry<Item> SHOVEL = item("nexite_shovel", p -> p.shovel(TOOL, 1.5F, -3.0F));
    public static final RegistryEntry<Item> PICKAXE = item("nexite_pickaxe", p -> p.pickaxe(TOOL, 1.0F, -2.8F));
    public static final RegistryEntry<Item> AXE = item("nexite_axe", p -> p.axe(TOOL, 5.0F, -3.0F));
    // Vanilla hoes cancel the material bonus so they always hit for 1
    public static final RegistryEntry<Item> HOE = item("nexite_hoe", p -> p.hoe(TOOL, -3.5F, 0.0F));
    // Halfway between the diamond and netherite spear settings
    public static final RegistryEntry<Item> SPEAR = item("nexite_spear", p -> p.spear(TOOL, 1.1F, 1.1375F, 0.45F, 2.75F, 9.5F, 6.0F, 5.1F, 9.375F, 4.6F));

    public static final RegistryEntry<Item> HELMET = item("nexite_helmet", p -> p.humanoidArmor(ARMOR, ArmorType.HELMET));
    public static final RegistryEntry<Item> CHESTPLATE = item("nexite_chestplate", p -> p.humanoidArmor(ARMOR, ArmorType.CHESTPLATE));
    public static final RegistryEntry<Item> LEGGINGS = item("nexite_leggings", p -> p.humanoidArmor(ARMOR, ArmorType.LEGGINGS));
    public static final RegistryEntry<Item> BOOTS = item("nexite_boots", p -> p.humanoidArmor(ARMOR, ArmorType.BOOTS));
    public static final RegistryEntry<Item> HORSE_ARMOR = item("nexite_horse_armor", p -> p.horseArmor(ARMOR));
    public static final RegistryEntry<Item> NAUTILUS_ARMOR = item("nexite_nautilus_armor", p -> p.nautilusArmor(ARMOR));

    public static final RegistryEntry<SmithingTemplateItem> UPGRADE_TEMPLATE = ModRegistry.register(Registries.ITEM, "nexite_upgrade_smithing_template",
        key -> new SmithingTemplateItem(
            translated("applies_to"), translated("ingredients"), translated("base_slot_description"), translated("additions_slot_description"),
            slots("helmet", "sword", "chestplate", "pickaxe", "leggings", "axe", "boots", "hoe", "shovel", "spear", "horse_armor", "nautilus_armor"),
            List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
            new Item.Properties().setId(key)));

    public static final List<RegistryEntry<? extends Item>> ALL = List.of(
        SWORD, SPEAR, SHOVEL, PICKAXE, AXE, HOE, HELMET, CHESTPLATE, LEGGINGS, BOOTS, HORSE_ARMOR, NAUTILUS_ARMOR, UPGRADE_TEMPLATE);

    private NexiteGear() {
    }

    public static void init() {
    }

    private static RegistryEntry<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        return ModRegistry.register(Registries.ITEM, name, key -> new Item(properties.apply(new Item.Properties().setId(key))));
    }

    private static Component translated(String line) {
        return Component.translatable("item.avalinexus.smithing_template.nexite_upgrade." + line);
    }

    // Empty slot icons cycled in the smithing table, same sprites as the netherite upgrade
    private static List<Identifier> slots(String... names) {
        return java.util.Arrays.stream(names).map(name -> Identifier.withDefaultNamespace("container/slot/" + name)).toList();
    }
}
