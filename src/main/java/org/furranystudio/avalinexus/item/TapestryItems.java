/**
 * File: TapestryItems.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.furranystudio.avalinexus.entity.tapestry.TapestryPattern;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.util.EnumMap;
import java.util.Map;

// Avali tapestry in every pattern and dye color, only the background gets tinted, the orange bands never change
public final class TapestryItems {

    private static final Map<TapestryPattern, Map<DyeColor, RegistryEntry<TapestryItem>>> ITEMS = new EnumMap<>(TapestryPattern.class);

    static {
        for (TapestryPattern pattern : TapestryPattern.values()) {
            Map<DyeColor, RegistryEntry<TapestryItem>> items = new EnumMap<>(DyeColor.class);
            for (DyeColor color : DyeColor.values()) {
                items.put(color, ModRegistry.register(Registries.ITEM, pattern.itemName(color),
                    key -> new TapestryItem(pattern, color, new Item.Properties().setId(key))));
            }
            ITEMS.put(pattern, items);
        }
    }

    private TapestryItems() {
    }

    public static void init() {
    }

    public static Item get(TapestryPattern pattern, DyeColor color) {
        return ITEMS.get(pattern).get(color).get();
    }
}
