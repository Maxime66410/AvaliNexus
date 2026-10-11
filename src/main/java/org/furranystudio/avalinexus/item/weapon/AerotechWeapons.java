/**
 * File: AerotechWeapons.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item.weapon;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.List;

public final class AerotechWeapons {

    public static final TagKey<Item> REPAIRS = TagKey.create(Registries.ITEM, AvaliNexus.id("repairs_aerotech_blades"));

    public static final RegistryEntry<DataComponentType<Identifier>> BLADE = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "aerotech_blade",
        key -> DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build());
    public static final RegistryEntry<DataComponentType<Integer>> REFORGE = ModRegistry.register(Registries.DATA_COMPONENT_TYPE, "aerotech_reforge",
        key -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    private static final ToolMaterial DAGGER = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 150, 6.0F, 2.0F, 14, REPAIRS);
    private static final ToolMaterial BLADE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 600, 6.0F, 2.5F, 14, REPAIRS);
    private static final ToolMaterial LONGBLADE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 750, 6.0F, 2.5F, 14, REPAIRS);
    private static final ToolMaterial AXE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 750, 8.0F, 2.0F, 14, REPAIRS);
    private static final ToolMaterial SINGING = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 8.0F, 3.25F, 15, REPAIRS);

    public static final RegistryEntry<AerotechBladeItem> AEROTECH_DAGGER = blade("aerotech_dagger", DAGGER, 2.0F, -1.6F);
    public static final RegistryEntry<AerotechBladeItem> AEROTECH_BLADE = blade("aerotech_blade", BLADE_MATERIAL, 3.0F, -2.4F);
    public static final RegistryEntry<AerotechBladeItem> AEROTECH_LONGBLADE = blade("aerotech_longblade", LONGBLADE_MATERIAL, 4.5F, -2.9F, 0.75F);
    public static final RegistryEntry<AerotechBladeItem> AEROTECH_AXE = ModRegistry.register(Registries.ITEM, "aerotech_axe", key ->
        new AerotechBladeItem(new Item.Properties().setId(key).axe(AXE_MATERIAL, 6.0F, -3.0F)
            .component(DataComponents.WEAPON, new Weapon(0, Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS))));
    public static final RegistryEntry<AerotechBladeItem> SINGING_BLADE = blade("singing_blade", SINGING, 3.0F, -2.2F);

    public static final RegistryEntry<AerotechHiltItem> AEROTECH_HILT = ModRegistry.register(Registries.ITEM, "aerotech_hilt",
        key -> new AerotechHiltItem(new Item.Properties().setId(key).stacksTo(1)));

    public static final List<RegistryEntry<? extends Item>> ALL = List.of(AEROTECH_DAGGER, AEROTECH_BLADE, AEROTECH_LONGBLADE, AEROTECH_AXE, SINGING_BLADE, AEROTECH_HILT);

    private AerotechWeapons() {
    }

    public static void init() {
    }

    // Vanilla would destroy the item at 0 durability, the blade shatters into its hilt instead
    private static RegistryEntry<AerotechBladeItem> blade(String name, ToolMaterial material, float damage, float speed) {
        return blade(name, material, damage, speed, 0.0F);
    }

    // The sword attributes are rebuilt to add the reach
    private static RegistryEntry<AerotechBladeItem> blade(String name, ToolMaterial material, float damage, float speed, float reach) {
        return ModRegistry.register(Registries.ITEM, name, key -> {
            Item.Properties properties = new Item.Properties().setId(key).sword(material, damage, speed);
            if (reach > 0.0F) {
                properties.attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage + material.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(AvaliNexus.id("longblade_reach"), reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
            }
            properties.component(DataComponents.WEAPON, new Weapon(0));
            properties.component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 0, false));
            return new AerotechBladeItem(properties);
        });
    }
}
