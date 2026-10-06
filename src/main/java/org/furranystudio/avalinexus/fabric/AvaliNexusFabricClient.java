/**
 * File: AvaliNexusFabricClient.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.client.heater.HeaterScreen;
import org.furranystudio.avalinexus.inventory.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import java.lang.reflect.Method;
import org.furranystudio.avalinexus.client.block.NanocanvasTints;
import org.furranystudio.avalinexus.client.cushion.AvaliCushionRenderer;
import org.furranystudio.avalinexus.client.tapestry.AvaliTapestryRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.client.weapon.RailGunClient;
import org.furranystudio.avalinexus.client.weapon.RailGunHud;
import org.furranystudio.avalinexus.client.weapon.NexiteQuillRenderer;
import org.furranystudio.avalinexus.entity.ModEntities;

public final class AvaliNexusFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetwork.registerClient();
        AvaliNexusClient.init();
        for (org.furranystudio.avalinexus.fluid.AvaliFluidKind kind : org.furranystudio.avalinexus.fluid.AvaliFluidKind.values()) {
            net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry.register(
                org.furranystudio.avalinexus.fluid.ModFluids.source(kind), org.furranystudio.avalinexus.fluid.ModFluids.flowing(kind),
                org.furranystudio.avalinexus.client.fluid.AvaliFluidModels.model(kind));
        }
        HudElementRegistry.addLast(AvaliNexus.id("dialogue"), DialogueHud::render);
        KeyMappingHelper.registerKeyMapping(DialogueKeys.CURSOR);
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientDialogue.tick());
        EntityRendererRegistry.register(ModEntities.AVALI.get(), AvaliRenderer::new);
        registerScreen(ModMenus.HEATER.get(), (menu, inventory, title) -> new HeaterScreen((org.furranystudio.avalinexus.block.heater.HeaterMenu) menu, inventory, title));
        NanocanvasTints.register(BlockColorRegistry::register);
        EntityRendererRegistry.register(ModEntities.AVALI_CUSHION.get(), AvaliCushionRenderer::new);
        EntityRendererRegistry.register(ModEntities.AVALI_TAPESTRY.get(), AvaliTapestryRenderer::new);
        EntityRendererRegistry.register(ModEntities.NEXITE_QUILL.get(), NexiteQuillRenderer::new);
        HudElementRegistry.addLast(AvaliNexus.id("rail_gun"), RailGunHud::render);
        KeyMappingHelper.registerKeyMapping(RailGunClient.RELOAD);
        ClientTickEvents.START_CLIENT_TICK.register(client -> RailGunClient.tick());
    }

    public interface ScreenFactory {
        net.minecraft.client.gui.screens.Screen create(net.minecraft.world.inventory.AbstractContainerMenu menu,
                                                       net.minecraft.world.entity.player.Inventory inventory,
                                                       net.minecraft.network.chat.Component title);
    }

    // MenuScreens.register is private and Fabric API only opens it to itself
    private static void registerScreen(MenuType<?> type, ScreenFactory factory) {
        try {
            Class<?> constructorClass = Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor");
            Object constructor = java.lang.reflect.Proxy.newProxyInstance(constructorClass.getClassLoader(), new Class<?>[] {constructorClass},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> "AvaliScreenConstructor";
                        };
                    }
                    return factory.create((net.minecraft.world.inventory.AbstractContainerMenu) args[0],
                        (net.minecraft.world.entity.player.Inventory) args[1], (net.minecraft.network.chat.Component) args[2]);
                });
            Method register = MenuScreens.class.getDeclaredMethod("register", MenuType.class, constructorClass);
            register.setAccessible(true);
            register.invoke(null, type, constructor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not register the screen of " + type, e);
        }
    }
}
