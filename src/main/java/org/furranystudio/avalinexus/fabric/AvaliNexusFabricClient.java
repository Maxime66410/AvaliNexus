/**
 * File: AvaliNexusFabricClient.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.furranystudio.avalinexus.client.ModKeyCategory;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.client.AvaliNexusClient;
import org.furranystudio.avalinexus.client.avali.AvaliRenderer;
import org.furranystudio.avalinexus.client.reputation.ReputationClient;
import org.furranystudio.avalinexus.client.heater.HeaterScreen;
import org.furranystudio.avalinexus.client.machine.AmmoniaRefineryScreen;
import org.furranystudio.avalinexus.client.machine.CryoFreezerScreen;
import org.furranystudio.avalinexus.client.machine.GraphenePressScreen;
import org.furranystudio.avalinexus.client.machine.WeaponWorkbenchScreen;
import org.furranystudio.avalinexus.client.loom.AvaliLoomScreen;
import org.furranystudio.avalinexus.client.tank.GrapheneTankRenderer;
import org.furranystudio.avalinexus.inventory.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import java.lang.reflect.Method;
import org.furranystudio.avalinexus.client.block.ModBlockTints;
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
        KeyMapping.Category.register(ModKeyCategory.AVALI_NEXUS.id());
        KeyMappingHelper.registerKeyMapping(DialogueKeys.CURSOR);
        KeyMappingHelper.registerKeyMapping(ReputationClient.OPEN);
        ClientTickEvents.START_CLIENT_TICK.register(client -> ReputationClient.tick());
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientDialogue.tick());
        EntityRendererRegistry.register(ModEntities.AVALI.get(), AvaliRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.GRAPHENE_TANK.get(), GrapheneTankRenderer::new);
        registerScreen(ModMenus.HEATER.get(), (menu, inventory, title) -> new HeaterScreen((org.furranystudio.avalinexus.block.heater.HeaterMenu) menu, inventory, title));
        registerScreen(ModMenus.AVALI_LOOM.get(), (menu, inventory, title) -> new AvaliLoomScreen((org.furranystudio.avalinexus.block.loom.AvaliLoomMenu) menu, inventory, title));
        registerScreen(ModMenus.AMMONIA_REFINERY.get(), (menu, inventory, title) -> new AmmoniaRefineryScreen((org.furranystudio.avalinexus.block.refinery.AmmoniaRefineryMenu) menu, inventory, title));
        registerScreen(ModMenus.CRYO_FREEZER.get(), (menu, inventory, title) -> new CryoFreezerScreen((org.furranystudio.avalinexus.block.freezer.CryoFreezerMenu) menu, inventory, title));
        registerScreen(ModMenus.GRAPHENE_PRESS.get(), (menu, inventory, title) -> new GraphenePressScreen((org.furranystudio.avalinexus.block.press.GraphenePressMenu) menu, inventory, title));
        registerScreen(ModMenus.WEAPON_WORKBENCH.get(), (menu, inventory, title) -> new WeaponWorkbenchScreen((org.furranystudio.avalinexus.block.workbench.WeaponWorkbenchMenu) menu, inventory, title));
        ModBlockTints.register(BlockColorRegistry::register);
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
