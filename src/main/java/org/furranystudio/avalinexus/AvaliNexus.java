/**
 * File: AvaliNexus.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.slf4j.Logger;

public final class AvaliNexus {

    public static final String MODID = "avalinexus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AvaliNexus() {
    }

    public static void registerContent() {
        ModEntities.init();
    }

    public static void commonSetup() {
        Config.load();
        LOGGER.info("[AvaliNexus] Common setup done.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
