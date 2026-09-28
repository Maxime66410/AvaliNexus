/**
 * File: HeaterZones.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block.heater;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Burning heaters check in every tick, a heater that stops ticking (burnt out, unloaded, broken) drops out on its own
public final class HeaterZones {

    public static final int RADIUS = 10;
    private static final long STALE_TICKS = 2;

    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> HEATERS = new HashMap<>();

    private HeaterZones() {
    }

    public static void mark(ServerLevel level, BlockPos pos) {
        HEATERS.computeIfAbsent(level.dimension(), key -> new HashMap<>()).put(pos.immutable(), level.getGameTime());
    }

    public static boolean isWarm(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Long> heaters = HEATERS.get(level.dimension());
        if (heaters == null) {
            return false;
        }
        long now = level.getGameTime();
        boolean warm = false;
        Iterator<Map.Entry<BlockPos, Long>> it = heaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Long> heater = it.next();
            if (now - heater.getValue() > STALE_TICKS) {
                it.remove();
            } else if (heater.getKey().closerThan(pos, RADIUS)) {
                warm = true;
            }
        }
        return warm;
    }
}
