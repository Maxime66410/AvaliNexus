/**
 * File: AvaliGear.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-18
 */
package org.furranystudio.avalinexus.entity.avali;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AvaliGear {

    public static final int SIZE = 9;

    private final AvaliEntity avali;
    private final List<ItemStack> backpack = new ArrayList<>();
    private final List<ItemStack> toGive = new ArrayList<>();

    public AvaliGear(AvaliEntity avali) {
        this.avali = avali;
    }

    private AvaliEquipment.Data data() {
        return AvaliEquipment.get(avali.level().getServer());
    }

    public boolean wants(ItemStack stack) {
        AvaliEquipment.Data data = data();
        EquipmentSlot slot = data.armorSlot(stack);
        if (slot != null) {
            return AvaliEquipment.armorScore(stack, slot) > AvaliEquipment.armorScore(avali.getItemBySlot(slot), slot);
        }
        if (data.isShield(stack)) {
            return avali.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
        }
        if (data.isWeapon(stack)) {
            List<ItemStack> weapons = weapons();
            if (weapons.size() < data.weaponsKept()) {
                return backpack.size() < SIZE;
            }
            return !weapons.isEmpty() && data.weaponScore(stack) > data.weaponScore(worst(weapons));
        }
        return data.isFood(stack) && roomFor(stack) > 0;
    }

    public ItemStack accept(ItemStack stack) {
        AvaliEquipment.Data data = data();
        if (!wants(stack)) {
            return stack;
        }
        EquipmentSlot slot = data.armorSlot(stack);
        if (slot != null) {
            give(avali.getItemBySlot(slot));
            avali.setItemSlot(slot, stack.split(1));
            return stack;
        }
        if (data.isShield(stack)) {
            avali.setItemSlot(EquipmentSlot.OFFHAND, stack.split(1));
            return stack;
        }
        if (data.isWeapon(stack)) {
            List<ItemStack> weapons = weapons();
            if (weapons.size() >= data.weaponsKept()) {
                ItemStack worst = worst(weapons);
                backpack.remove(worst);
                give(worst);
            }
            backpack.add(stack.split(1));
            return stack;
        }
        store(stack);
        return stack;
    }

    public void takeFrom(ItemEntity entity) {
        ItemStack stack = entity.getItem().copy();
        int before = stack.getCount();
        ItemStack left = accept(stack);
        int taken = before - left.getCount();
        if (taken <= 0) {
            return;
        }
        avali.take(entity, taken);
        if (left.isEmpty()) {
            entity.discard();
        } else {
            entity.setItem(left);
        }
    }

    public boolean hasFood() {
        return backpack.stream().anyMatch(data()::isFood);
    }

    public ItemStack takeFood() {
        ItemStack food = backpack.stream().filter(data()::isFood)
            .max(Comparator.comparingInt(AvaliGear::nutrition)).orElse(ItemStack.EMPTY);
        backpack.remove(food);
        return food;
    }

    public static int nutrition(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? 0 : food.nutrition();
    }

    public void stash(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        store(stack);
        give(stack);
    }

    public ItemStack bestWeapon() {
        AvaliEquipment.Data data = data();
        return weapons().stream().max(Comparator.comparingDouble(data::weaponScore)).orElse(ItemStack.EMPTY);
    }

    public void give(ItemStack stack) {
        if (!stack.isEmpty()) {
            toGive.add(stack.copy());
            stack.setCount(0);
        }
    }

    public boolean hasToGive() {
        return !toGive.isEmpty();
    }

    public List<ItemStack> drainToGive() {
        List<ItemStack> result = new ArrayList<>(toGive);
        toGive.clear();
        return result;
    }

    public void dropAll(ServerLevel level) {
        for (ItemStack stack : backpack) {
            avali.spawnAtLocation(level, stack);
        }
        for (ItemStack stack : toGive) {
            avali.spawnAtLocation(level, stack);
        }
        backpack.clear();
        toGive.clear();
    }

    public void giveSpawnGear(RandomSource random) {
        AvaliEquipment.Spawn spawn = data().spawn();
        if (random.nextFloat() < spawn.weaponChance()) {
            add(spawn.weapons(), random, 1);
        }
        for (Map.Entry<EquipmentSlot, List<Item>> entry : spawn.armor().entrySet()) {
            Item item = AvaliEquipment.pick(entry.getValue(), random);
            if (random.nextFloat() < spawn.armorChance() && item != Items.AIR) {
                avali.setItemSlot(entry.getKey(), new ItemStack(item));
            }
        }
        Item shield = AvaliEquipment.pick(spawn.shields(), random);
        if (random.nextFloat() < spawn.shieldChance() && shield != Items.AIR) {
            avali.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(shield));
        }
        if (random.nextFloat() < spawn.foodChance()) {
            add(spawn.food(), random, Mth.nextInt(random, spawn.foodMin(), spawn.foodMax()));
        }
    }

    private void add(List<Item> items, RandomSource random, int count) {
        Item item = AvaliEquipment.pick(items, random);
        if (item != Items.AIR && backpack.size() < SIZE) {
            backpack.add(new ItemStack(item, count));
        }
    }

    private List<ItemStack> weapons() {
        AvaliEquipment.Data data = data();
        return backpack.stream().filter(data::isWeapon).toList();
    }

    private ItemStack worst(List<ItemStack> weapons) {
        AvaliEquipment.Data data = data();
        return weapons.stream().min(Comparator.comparingDouble(data::weaponScore)).orElse(ItemStack.EMPTY);
    }

    private int roomFor(ItemStack stack) {
        int room = (SIZE - backpack.size()) * stack.getMaxStackSize();
        for (ItemStack held : backpack) {
            if (ItemStack.isSameItemSameComponents(held, stack)) {
                room += held.getMaxStackSize() - held.getCount();
            }
        }
        return room;
    }

    private void store(ItemStack stack) {
        for (ItemStack held : backpack) {
            if (stack.isEmpty()) {
                return;
            }
            if (ItemStack.isSameItemSameComponents(held, stack)) {
                int moved = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
                held.grow(moved);
                stack.shrink(moved);
            }
        }
        while (!stack.isEmpty() && backpack.size() < SIZE) {
            backpack.add(stack.split(stack.getMaxStackSize()));
        }
    }

    public void save(ValueOutput output) {
        output.store("avalinexus_backpack", ItemStack.CODEC.listOf(), List.copyOf(backpack));
        output.store("avalinexus_to_give", ItemStack.CODEC.listOf(), List.copyOf(toGive));
    }

    public void load(ValueInput input) {
        backpack.clear();
        toGive.clear();
        input.read("avalinexus_backpack", ItemStack.CODEC.listOf()).ifPresent(backpack::addAll);
        input.read("avalinexus_to_give", ItemStack.CODEC.listOf()).ifPresent(toGive::addAll);
    }
}
