/**
 * File: AvaliNexus.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.block.ModBlocks;
import org.furranystudio.avalinexus.block.NanocanvasBlocks;
import org.furranystudio.avalinexus.fluid.ModFluids;
import org.furranystudio.avalinexus.entity.ModEntities;
import org.furranystudio.avalinexus.item.ModCreativeTabs;
import org.furranystudio.avalinexus.item.ModItems;
import org.furranystudio.avalinexus.item.NexiteGear;
import org.furranystudio.avalinexus.inventory.ModMenus;
import org.furranystudio.avalinexus.item.TapestryItems;
import org.furranystudio.avalinexus.network.ModPackets;
import org.furranystudio.avalinexus.sound.ModSounds;
import org.furranystudio.avalinexus.worldgen.ModFeatures;
import org.slf4j.Logger;

public final class AvaliNexus {

    public static final String MODID = "avalinexus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AvaliNexus() {
    }

    public static void registerContent() {
        ModSounds.init();
        ModBlocks.init();
        NanocanvasBlocks.init();
        ModFluids.init();
        ModEntities.init();
        ModItems.init();
        NexiteGear.init();
        org.furranystudio.avalinexus.item.weapon.AerotechWeapons.init();
        TapestryItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModCreativeTabs.init();
        ModFeatures.init();
        ModPackets.init();
    }

    public static void commonSetup() {
        Config.load();
        LOGGER.info("[AvaliNexus] Common setup done.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
