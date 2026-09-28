/**
 * File: Platform.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus;

import net.minecraft.world.item.CreativeModeTab;
import org.furranystudio.avalinexus.fluid.AvaliFluid;
import org.furranystudio.avalinexus.fluid.AvaliFluidKind;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class Platform {

    private static Path gameDir;
    private static Supplier<CreativeModeTab.Builder> tabBuilder;

    // Forge and NeoForge swap this for fluids that also carry their fluid type
    public interface FluidFactory {
        AvaliFluid create(AvaliFluidKind kind, boolean source);
    }

    private static FluidFactory fluidFactory = (kind, source) -> source ? new AvaliFluid.Source(kind) : new AvaliFluid.Flowing(kind);

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

    public static void setFluidFactory(FluidFactory factory) {
        fluidFactory = factory;
    }

    public static AvaliFluid createFluid(AvaliFluidKind kind, boolean source) {
        return fluidFactory.create(kind, source);
    }
}
