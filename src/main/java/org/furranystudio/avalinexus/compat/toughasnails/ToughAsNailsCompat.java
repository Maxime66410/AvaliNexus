/**
 * File: ToughAsNailsCompat.java
 * Author: Maxime66410
 * Created: 2026-11-11
 * Last Modified: 2026-11-11
 */
package org.furranystudio.avalinexus.compat.toughasnails;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.furranystudio.avalinexus.entity.ColdExposure;
import org.furranystudio.avalinexus.worldgen.ModBiomes;
import toughasnails.api.temperature.ITemperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

public final class ToughAsNailsCompat {

    private static final int SHELTER_WARMTH = 1;
    private static final int LIQUID_COLD = 2;
    private static final int LIQUID_EXTRA_TICKS = 1;

    private static boolean registered;

    private ToughAsNailsCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        TemperatureHelper.registerPositionalTemperatureModifier(ToughAsNailsCompat::shelter);
        TemperatureHelper.registerPlayerTemperatureModifier((player, current) ->
            ColdExposure.isInFreezingLiquid(player) ? current.decrement(LIQUID_COLD) : current);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!ColdExposure.isInFreezingLiquid(player)) {
                continue;
            }
            ITemperature data = TemperatureHelper.getTemperatureData(player);
            data.setChangeDelayTicks(Math.max(0, data.getChangeDelayTicks() - LIQUID_EXTRA_TICKS));
            data.setExtremityDelayTicks(Math.max(0, data.getExtremityDelayTicks() - LIQUID_EXTRA_TICKS));
        }
    }

    private static TemperatureLevel shelter(Level level, BlockPos pos, TemperatureLevel current) {
        if (level.getBiome(pos).is(ModBiomes.POLAR_TAIGA) && !level.canSeeSky(pos.above())) {
            return current.increment(SHELTER_WARMTH);
        }
        return current;
    }
}
