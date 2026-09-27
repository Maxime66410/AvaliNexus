/**
 * File: Platform.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import net.minecraft.world.item.CreativeModeTab;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class Platform {

    private static Path gameDir;
    private static Supplier<CreativeModeTab.Builder> tabBuilder;

    private Platform() {
    }

    public static void init(Path gameDir, Supplier<CreativeModeTab.Builder> tabBuilder) {
        Platform.gameDir = gameDir;
        Platform.tabBuilder = tabBuilder;
    }

    public static Path getGameDir() {
        return gameDir;
    }

    public static CreativeModeTab.Builder creativeTabBuilder() {
        return tabBuilder.get();
    }
}
