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
import org.furranystudio.avalinexus.client.block.NanocanvasTints;
import org.furranystudio.avalinexus.client.cushion.AvaliCushionRenderer;
import org.furranystudio.avalinexus.client.tapestry.AvaliTapestryRenderer;
import org.furranystudio.avalinexus.client.dialogue.ClientDialogue;
import org.furranystudio.avalinexus.client.dialogue.DialogueHud;
import org.furranystudio.avalinexus.client.dialogue.DialogueKeys;
import org.furranystudio.avalinexus.entity.ModEntities;

public final class AvaliNexusFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricNetwork.registerClient();
        AvaliNexusClient.init();
        HudElementRegistry.addLast(AvaliNexus.id("dialogue"), DialogueHud::render);
        KeyMappingHelper.registerKeyMapping(DialogueKeys.CURSOR);
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientDialogue.tick());
        EntityRendererRegistry.register(ModEntities.AVALI.get(), AvaliRenderer::new);
        NanocanvasTints.register(BlockColorRegistry::register);
        EntityRendererRegistry.register(ModEntities.AVALI_CUSHION.get(), AvaliCushionRenderer::new);
        EntityRendererRegistry.register(ModEntities.AVALI_TAPESTRY.get(), AvaliTapestryRenderer::new);
    }
}
