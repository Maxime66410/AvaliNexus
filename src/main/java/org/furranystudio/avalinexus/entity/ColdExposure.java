/**
 * File: ColdExposure.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-10-27
 */
package org.furranystudio.avalinexus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.Config;
import org.furranystudio.avalinexus.block.heater.HeaterZones;
import org.furranystudio.avalinexus.effect.ModEffects;
import org.furranystudio.avalinexus.fluid.AvaliLiquidBlock;
import org.furranystudio.avalinexus.worldgen.ModBiomes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Players out in the polar taiga slowly freeze like in powder snow, leather armor keeps them warm
public final class ColdExposure {

    // Vanilla thaws 2 ticks every tick, at coldSpeed 100 we add half a tick more on average (about 14s to freeze)
    private static final int VANILLA_THAW = 2;
    // A little over the cap so the vanilla thaw still leaves the player fully frozen and taking damage
    private static final int CAP_MARGIN = 2;

    private static final Map<UUID, String> STATES = new HashMap<>();

    private ColdExposure() {
    }

    public static void tick(MinecraftServer server) {
        int speed = Config.COLD_SPEED.get();
        long time = server.getTickCount();
        int step = VANILLA_THAW + (int) ((time + 1) * speed / 200 - time * speed / 200);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String state = "out of the cold";
            if (HeaterZones.isWarm(player.level(), player.blockPosition()) || player.hasEffect(ModEffects.warmth())) {
                // On top of the vanilla thaw, so a frozen player warms up twice as fast
                player.setTicksFrozen(Math.max(0, player.getTicksFrozen() - VANILLA_THAW));
                state = player.hasEffect(ModEffects.warmth()) ? "warmed up by the spiced stew" : "warmed up by a heater";
            } else if (speed > 0 && isExposed(player)) {
                int cap = player.getTicksRequiredToFreeze() + CAP_MARGIN;
                player.setTicksFrozen(Math.min(cap, player.getTicksFrozen() + step));
                state = "freezing in the polar taiga";
            } else if (inFreezingLiquid(player)) {
                state = "freezing in a liquid";
            }
            if (Config.DEBUG_LOGGING.get() && !state.equals(STATES.put(player.getUUID(), state))) {
                AvaliNexus.debug("{}: {}", player.getName().getString(), state);
            }
        }
    }

    private static boolean inFreezingLiquid(ServerPlayer player) {
        if (Config.LIQUID_COLD_SPEED.get() <= 0 || !player.canFreeze()) {
            return false;
        }
        for (BlockPos pos : new BlockPos[] {player.blockPosition(), BlockPos.containing(player.getEyePosition())}) {
            if (player.level().getBlockState(pos).getBlock() instanceof AvaliLiquidBlock liquid && liquid.kind().freezing() > 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExposed(ServerPlayer player) {
        if (player.getAbilities().invulnerable || !player.canFreeze() || player.isInWater()) {
            return false;
        }
        BlockPos pos = player.blockPosition();
        return player.level().getBiome(pos).is(ModBiomes.POLAR_TAIGA) && player.level().canSeeSky(pos.above());
    }
}
