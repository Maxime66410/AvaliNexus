/**
 * File: ModMenus.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.inventory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.furranystudio.avalinexus.block.heater.HeaterMenu;
import org.furranystudio.avalinexus.registry.ModRegistry;
import org.furranystudio.avalinexus.registry.RegistryEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;

public final class ModMenus {

    public static final RegistryEntry<MenuType<HeaterMenu>> HEATER = ModRegistry.register(Registries.MENU, "heater",
        key -> create(HeaterMenu::new));

    private ModMenus() {
    }

    public static void init() {
    }

    // Forge and NeoForge open the MenuType constructor but Fabric keeps it private, reflection works the same everywhere
    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu> MenuType<T> create(BiFunction<Integer, Inventory, T> factory) {
        try {
            Class<?> supplierClass = Class.forName("net.minecraft.world.inventory.MenuType$MenuSupplier");
            Object supplier = Proxy.newProxyInstance(supplierClass.getClassLoader(), new Class<?>[] {supplierClass}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> "HeaterMenuSupplier";
                    };
                }
                return factory.apply((Integer) args[0], (Inventory) args[1]);
            });
            Constructor<?> constructor = MenuType.class.getDeclaredConstructor(supplierClass, FeatureFlagSet.class);
            constructor.setAccessible(true);
            return (MenuType<T>) constructor.newInstance(supplier, FeatureFlags.VANILLA_SET);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create a menu type", e);
        }
    }
}
