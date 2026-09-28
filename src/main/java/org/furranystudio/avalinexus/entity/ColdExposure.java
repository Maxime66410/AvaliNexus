/**
 * File: ColdExposure.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

// Players out in the polar taiga slowly freeze like in powder snow, leather armor keeps them warm
public final class ColdExposure {

    // Vanilla thaws 2 ticks every tick, so 3 then 2 nets +1 every other tick (about 14s to freeze)
    private static final int FREEZE_ODD_TICK = 3;
    private static final int FREEZE_EVEN_TICK = 2;
    // A little over the cap so the vanilla thaw still leaves the player fully frozen and taking damage
    private static final int CAP_MARGIN = 2;

    private ColdExposure() {
    }

    public static void tick(MinecraftServer server) {
        int step = server.getTickCount() % 2 == 0 ? FREEZE_EVEN_TICK : FREEZE_ODD_TICK;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isExposed(player)) {
                int cap = player.getTicksRequiredToFreeze() + CAP_MARGIN;
                player.setTicksFrozen(Math.min(cap, player.getTicksFrozen() + step));
            }
        }
    }

    private static boolean isExposed(ServerPlayer player) {
        if (player.getAbilities().invulnerable || !player.canFreeze() || player.isInWater()) {
            return false;
        }
        BlockPos pos = player.blockPosition();
        return player.level().getBiome(pos).is(ModBiomes.POLAR_TAIGA) && player.level().canSeeSky(pos.above());
    }
}
