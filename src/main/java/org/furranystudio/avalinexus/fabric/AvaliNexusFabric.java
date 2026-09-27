/**
 * File: AvaliNexusFabric.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.Platform;
import org.furranystudio.avalinexus.command.AvaliNexusCommand;

// Fabric entrypoint (declared as "main" in fabric.mod.json - runs on both client and dedicated
// server). See AvaliNexusFabricClient for the client-only wiring.
public final class AvaliNexusFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.init(FabricLoader.getInstance().getGameDir());
        Config.registerSettings();

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            AvaliNexusCommand.register(dispatcher));

        AvaliNexus.commonSetup();
    }
}
