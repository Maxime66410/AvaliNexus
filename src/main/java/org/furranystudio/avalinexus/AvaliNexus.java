/**
 * File: AvaliNexus.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

// Shared mod identity and init logic, called from each loader's own entrypoint
// (org.furranystudio.avalinexus.forge / .neoforge / .fabric).
public final class AvaliNexus {

    public static final String MODID = "avalinexus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AvaliNexus() {
    }

    // Common (non-loader-specific) init. Each loader's bootstrap calls this once, after
    // Platform.init() and Config.registerSettings().
    public static void commonSetup() {
        Config.load();
        LOGGER.info("[AvaliNexus] Common setup done.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
