/**
 * File: WeaponRecipe.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.workbench;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.item.weapon.AerotechWeapons;
import org.furranystudio.avalinexus.item.weapon.RailWeapons;

import java.util.List;
import java.util.function.Supplier;

public record WeaponRecipe(Supplier<? extends Item> result, int count, int duration, List<Cost> costs) {

    private static final int LIGHT = 100;
    private static final int SMALL = 200;
    private static final int SIMPLE = 300;
    private static final int ADVANCED = 400;
    private static final int UPGRADE = 500;

    public static final List<WeaponRecipe> ALL = List.of(
        new WeaponRecipe(RailWeapons.RAIL_PISTOL, 1, SIMPLE, List.of(new Cost(ModItems.NEXITE_INGOT, 1), new Cost(ModItems.GRAPHENE, 2))),
        new WeaponRecipe(RailWeapons.RAIL_CARBINE, 1, ADVANCED, List.of(new Cost(ModItems.NEXITE_INGOT, 2), new Cost(ModItems.NEXITE_SHARD, 1), new Cost(ModItems.GRAPHENE, 2))),
        new WeaponRecipe(RailWeapons.RAIL_ASSAULT_RIFLE, 1, ADVANCED, List.of(new Cost(ModItems.NEXITE_INGOT, 2), new Cost(ModItems.NEXITE_SHARD, 2), new Cost(ModItems.GRAPHENE, 2))),
        new WeaponRecipe(RailWeapons.RAIL_SHOTGUN, 1, ADVANCED, List.of(new Cost(ModItems.NEXITE_INGOT, 2), new Cost(ModItems.GRAPHENE, 4))),
        new WeaponRecipe(RailWeapons.RAIL_SNIPER, 1, UPGRADE, List.of(new Cost(RailWeapons.RAIL_CARBINE, 1), new Cost(ModItems.NEXITE_INGOT, 1))),
        new WeaponRecipe(AerotechWeapons.AEROTECH_DAGGER, 1, SMALL, List.of(new Cost(ModItems.AEROGEL, 1), new Cost(ModItems.NEXITE_SHARD, 1))),
        new WeaponRecipe(AerotechWeapons.AEROTECH_BLADE, 1, SIMPLE, List.of(new Cost(ModItems.AEROGEL, 2), new Cost(ModItems.NEXITE_SHARD, 1), new Cost(ModItems.GRAPHENE, 1))),
        new WeaponRecipe(AerotechWeapons.AEROTECH_LONGBLADE, 1, ADVANCED, List.of(new Cost(ModItems.AEROGEL, 1), new Cost(AerotechWeapons.AEROTECH_BLADE, 1))),
        new WeaponRecipe(AerotechWeapons.AEROTECH_AXE, 1, SIMPLE, List.of(new Cost(ModItems.AEROGEL, 3), new Cost(ModItems.NEXITE_INGOT, 1))),
        new WeaponRecipe(AerotechWeapons.AEROTECH_SPEAR, 1, SIMPLE, List.of(new Cost(ModItems.AEROGEL, 1), new Cost(ModItems.NEXITE_INGOT, 1))),
        new WeaponRecipe(AerotechWeapons.SINGING_BLADE, 1, UPGRADE, List.of(new Cost(AerotechWeapons.AEROTECH_BLADE, 1), new Cost(ModItems.NEXITE_INGOT, 1))),
        new WeaponRecipe(RailWeapons.RAIL_PISTOL_MAGAZINE, 1, LIGHT, List.of(new Cost(ModItems.GRAPHENE, 1), new Cost(ModItems.NEXITE_NUGGET, 1))),
        new WeaponRecipe(RailWeapons.RAIL_CARBINE_MAGAZINE, 1, LIGHT, List.of(new Cost(ModItems.GRAPHENE, 1), new Cost(ModItems.NEXITE_SHARD, 1))),
        new WeaponRecipe(RailWeapons.RAIL_ASSAULT_RIFLE_MAGAZINE, 1, SMALL, List.of(new Cost(ModItems.GRAPHENE, 1), new Cost(ModItems.NEXITE_INGOT, 1))),
        new WeaponRecipe(RailWeapons.RAIL_SNIPER_MAGAZINE, 1, SMALL, List.of(new Cost(ModItems.GRAPHENE, 3), new Cost(ModItems.NEXITE_INGOT, 2))),
        new WeaponRecipe(RailWeapons.NEXITE_QUILL, 24, LIGHT, List.of(new Cost(ModItems.NEXITE_NUGGET, 1), new Cost(ModItems.GRAPHENE, 1)))
    );

    public static WeaponRecipe get(int index) {
        return index >= 0 && index < ALL.size() ? ALL.get(index) : null;
    }

    public static boolean isIngredient(ItemStack stack) {
        for (WeaponRecipe recipe : ALL) {
            for (Cost cost : recipe.costs) {
                if (stack.is(cost.item().get())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int fuel() {
        return switch (duration) {
            case LIGHT -> 250;
            case SMALL -> 400;
            case SIMPLE -> 500;
            case ADVANCED -> 750;
            default -> 1000;
        };
    }

    public ItemStack output() {
        return new ItemStack(result.get(), count);
    }

    public record Cost(Supplier<? extends Item> item, int count) {
    }
}
