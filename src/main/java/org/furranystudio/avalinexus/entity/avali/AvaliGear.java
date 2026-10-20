/**
 * File: AvaliGear.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.entity.avali;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.furranystudio.avalinexus.item.weapon.AerotechHiltItem;
import org.furranystudio.avalinexus.item.weapon.AerotechWeapons;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.sound.ModSounds;

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
            List<ItemStack> weapons = weapons(data.isRanged(stack));
            if (weapons.size() < kept(stack)) {
                return backpack.size() < SIZE;
            }
            return !weapons.isEmpty() && data.weaponScore(stack) > data.weaponScore(worst(weapons));
        }
        return (data.isFood(stack) || data.isReforge(stack)) && roomFor(stack) > 0;
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
            List<ItemStack> weapons = weapons(data.isRanged(stack));
            if (weapons.size() >= kept(stack)) {
                ItemStack worst = worst(weapons);
                if (worst == avali.getMainHandItem()) {
                    avali.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    backpack.remove(worst);
                }
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
        if (entity.getOwner() instanceof ServerPlayer player) {
            AvaliReputation.meet(player, avali.getPack());
            AvaliReputation.change(player, avali.getPack(), AvaliReputation.Change.GIFT);
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

    public int foodCount() {
        return backpack.stream().filter(data()::isFood).mapToInt(ItemStack::getCount).sum();
    }

    public void consumeFood(int count) {
        int left = count;
        for (ItemStack stack : backpack) {
            if (left > 0 && data().isFood(stack)) {
                int eaten = Math.min(left, stack.getCount());
                stack.shrink(eaten);
                left -= eaten;
            }
        }
        backpack.removeIf(ItemStack::isEmpty);
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
        return best(weapons(false));
    }

    public ItemStack bestRanged() {
        return best(weapons(true));
    }

    public ItemStack strongestWeapon() {
        List<ItemStack> all = weapons(false);
        all.addAll(weapons(true));
        return best(all);
    }

    public boolean isShield(ItemStack stack) {
        return data().isShield(stack);
    }

    public boolean hasWeapon() {
        return hasMelee() || hasRanged();
    }

    public boolean hasMelee() {
        return !weapons(false).isEmpty();
    }

    public boolean hasRanged() {
        return !weapons(true).isEmpty();
    }

    public void drawWeapon() {
        draw(bestWeapon());
    }

    public void drawRanged() {
        draw(bestRanged());
    }

    public void putAwayHilt() {
        ItemStack hand = avali.getMainHandItem();
        if (hand.getItem() instanceof AerotechHiltItem) {
            avali.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            stash(hand);
            drawWeapon();
        }
    }

    public void tickHilts() {
        boolean reforge = backpack.stream().anyMatch(data()::isReforge);
        for (int i = 0; i < backpack.size(); i++) {
            ItemStack hilt = backpack.get(i);
            if (!(hilt.getItem() instanceof AerotechHiltItem)) {
                continue;
            }
            int seconds = hilt.getOrDefault(AerotechWeapons.REFORGE.get(), 0) + 1;
            if (!reforge && seconds < AerotechHiltItem.REFORGE_SECONDS) {
                hilt.set(AerotechWeapons.REFORGE.get(), seconds);
                continue;
            }
            backpack.set(i, AerotechHiltItem.reforge(hilt));
            if (reforge) {
                takeReforge();
            }
            if (avali.getTarget() != null && !data().isRanged(avali.getMainHandItem())) {
                drawWeapon();
            }
            ModSounds.play(avali.level(), avali, ModSounds.AEROTECH_REFORGE.get(), 0.6F, 0.05F);
            return;
        }
    }

    private void takeReforge() {
        AvaliEquipment.Data data = data();
        for (ItemStack stack : backpack) {
            if (data.isReforge(stack)) {
                stack.shrink(1);
                backpack.removeIf(ItemStack::isEmpty);
                return;
            }
        }
    }

    private void draw(ItemStack best) {
        AvaliEquipment.Data data = data();
        ItemStack hand = avali.getMainHandItem();
        if (best.isEmpty() || best == hand) {
            return;
        }
        backpack.remove(best);
        avali.setItemSlot(EquipmentSlot.MAINHAND, best);
        if (data.isWeapon(hand)) {
            backpack.add(hand);
        } else {
            stash(hand);
        }
    }

    public void stowWeapon() {
        ItemStack hand = avali.getMainHandItem();
        if (data().isWeapon(hand)) {
            avali.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            stash(hand);
        }
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
        if (random.nextFloat() < spawn.rangedChance()) {
            add(spawn.ranged(), random, 1);
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

    private List<ItemStack> weapons(boolean ranged) {
        AvaliEquipment.Data data = data();
        List<ItemStack> weapons = new ArrayList<>(backpack.stream()
            .filter(stack -> data.isWeapon(stack) && data.isRanged(stack) == ranged).toList());
        ItemStack hand = avali.getMainHandItem();
        if (data.isWeapon(hand) && data.isRanged(hand) == ranged) {
            weapons.add(hand);
        }
        return weapons;
    }

    private int kept(ItemStack stack) {
        AvaliEquipment.Data data = data();
        return data.isRanged(stack) ? data.rangedKept() : data.weaponsKept();
    }

    private ItemStack best(List<ItemStack> weapons) {
        AvaliEquipment.Data data = data();
        return weapons.stream().max(Comparator.comparingDouble(data::weaponScore)).orElse(ItemStack.EMPTY);
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
