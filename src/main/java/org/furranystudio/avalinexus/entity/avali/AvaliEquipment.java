/**
 * File: AvaliEquipment.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.item.weapon.RailGunItem;
import org.furranystudio.avalinexus.item.weapon.RailStats;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public final class AvaliEquipment {

    private static final Identifier FILE = AvaliNexus.id("avali/equipment/equipment.json");
    private static final double BASE_DAMAGE = 1.0;
    private static final double BASE_ATTACK_SPEED = 4.0;
    private static final double TICKS_PER_SECOND = 20.0;

    public record Spawn(float weaponChance, List<Item> weapons, float rangedChance, List<Item> ranged, float armorChance, Map<EquipmentSlot, List<Item>> armor,
                        float shieldChance, List<Item> shields, float foodChance, int foodMin, int foodMax, List<Item> food) {
    }

    public record Data(Spawn spawn, Predicate<ItemStack> weapons, Predicate<ItemStack> shields, Predicate<ItemStack> armorExcluded,
                       Predicate<ItemStack> foodExcluded, Predicate<ItemStack> reforge, float modBonus, int weaponsKept, int rangedKept,
                       Predicate<ItemStack> weaponsExcluded) {

        public boolean isWeapon(ItemStack stack) {
            return weapons.test(stack) && !weaponsExcluded.test(stack);
        }

        public boolean isRanged(ItemStack stack) {
            return stack.getItem() instanceof RailGunItem && isWeapon(stack);
        }

        public boolean isReforge(ItemStack stack) {
            return reforge.test(stack);
        }

        public boolean isShield(ItemStack stack) {
            return shields.test(stack);
        }

        public boolean isFood(ItemStack stack) {
            return stack.has(DataComponents.FOOD) && !foodExcluded.test(stack);
        }

        public EquipmentSlot armorSlot(ItemStack stack) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || !equippable.slot().isArmor() || armorExcluded.test(stack)
                    || armorScore(stack, equippable.slot()) <= 0) {
                return null;
            }
            return equippable.slot();
        }

        public double weaponScore(ItemStack stack) {
            boolean fromMod = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(AvaliNexus.MODID);
            return weaponDamage(stack) * attacksPerSecond(stack) * (fromMod ? modBonus : 1.0);
        }
    }

    public static double weaponDamage(ItemStack stack) {
        if (stack.getItem() instanceof RailGunItem gun) {
            RailStats stats = gun.stats();
            return stats.damage() * stats.pellets();
        }
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .compute(Attributes.ATTACK_DAMAGE, BASE_DAMAGE, EquipmentSlot.MAINHAND);
    }

    public static double attacksPerSecond(ItemStack stack) {
        if (stack.getItem() instanceof RailGunItem gun) {
            return TICKS_PER_SECOND / gun.stats().interval();
        }
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .compute(Attributes.ATTACK_SPEED, BASE_ATTACK_SPEED, EquipmentSlot.MAINHAND);
    }

    public static double armorScore(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return modifiers.compute(Attributes.ARMOR, 0, slot) + modifiers.compute(Attributes.ARMOR_TOUGHNESS, 0, slot);
    }

    private static final Predicate<ItemStack> NONE = stack -> false;
    private static final Data FALLBACK = new Data(
        new Spawn(0, List.of(), 0, List.of(), 0, Map.of(), 0, List.of(), 0, 0, 0, List.of()),
        NONE, stack -> stack.is(Items.SHIELD), NONE, NONE, NONE, 1.25F, 2, 1, NONE);

    private static ResourceManager loadedFrom;
    private static Data data = FALLBACK;

    private AvaliEquipment() {
    }

    public static Data get(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        if (manager != loadedFrom) {
            loadedFrom = manager;
            data = load(manager);
        }
        return data;
    }

    public static void reload(MinecraftServer server) {
        loadedFrom = null;
        get(server);
    }

    public static Item pick(List<Item> items, RandomSource random) {
        return items.isEmpty() ? Items.AIR : items.get(random.nextInt(items.size()));
    }

    private static Data load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Missing equipment file {}", FILE);
            return FALLBACK;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject spawn = root.getAsJsonObject("spawn");
            JsonObject weapon = spawn.getAsJsonObject("weapon");
            JsonObject ranged = spawn.getAsJsonObject("ranged");
            JsonObject armor = spawn.getAsJsonObject("armor");
            JsonObject shield = spawn.getAsJsonObject("shield");
            JsonObject food = spawn.getAsJsonObject("food");
            Map<EquipmentSlot, List<Item>> armorItems = new EnumMap<>(EquipmentSlot.class);
            armorItems.put(EquipmentSlot.HEAD, items(armor.getAsJsonArray("head")));
            armorItems.put(EquipmentSlot.CHEST, items(armor.getAsJsonArray("chest")));
            armorItems.put(EquipmentSlot.LEGS, items(armor.getAsJsonArray("legs")));
            armorItems.put(EquipmentSlot.FEET, items(armor.getAsJsonArray("feet")));
            JsonObject useful = root.getAsJsonObject("useful");
            JsonObject weapons = root.getAsJsonObject("weapons");
            return new Data(
                new Spawn(weapon.get("chance").getAsFloat(), items(weapon.getAsJsonArray("items")),
                    ranged.get("chance").getAsFloat(), items(ranged.getAsJsonArray("items")),
                    armor.get("chance").getAsFloat(), armorItems,
                    shield.get("chance").getAsFloat(), items(shield.getAsJsonArray("items")),
                    food.get("chance").getAsFloat(), food.get("min").getAsInt(), food.get("max").getAsInt(),
                    items(food.getAsJsonArray("items"))),
                matcher(useful.getAsJsonArray("weapons")), matcher(useful.getAsJsonArray("shields")),
                matcher(useful.getAsJsonArray("armor_excluded")), matcher(useful.getAsJsonArray("food_excluded")),
                matcher(useful.getAsJsonArray("reforge")), weapons.get("mod_bonus").getAsFloat(), weapons.get("kept").getAsInt(),
                weapons.get("ranged_kept").getAsInt(), matcher(weapons.getAsJsonArray("excluded")));
        } catch (Exception e) {
            AvaliNexus.LOGGER.error("[AvaliNexus] Couldn't read equipment file {}", FILE, e);
            return FALLBACK;
        }
    }

    private static List<Item> items(JsonArray array) {
        List<Item> result = new ArrayList<>();
        for (JsonElement element : array) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString()));
            if (item != Items.AIR) {
                result.add(item);
            }
        }
        return result;
    }

    private static Predicate<ItemStack> matcher(JsonArray array) {
        Predicate<ItemStack> result = NONE;
        for (JsonElement element : array) {
            String entry = element.getAsString();
            Predicate<ItemStack> test;
            if (entry.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(entry.substring(1)));
                test = stack -> stack.is(tag);
            } else {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(entry));
                test = stack -> stack.is(item);
            }
            result = result.or(test);
        }
        return result;
    }
}
