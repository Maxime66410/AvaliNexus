/**
 * File: PackHomeData.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PackHomeData extends SavedData {

    private static final Codec<PackHomeData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, BlockPos.CODEC)
        .xmap(PackHomeData::new, data -> data.homes);
    private static final SavedDataType<PackHomeData> TYPE = new SavedDataType<>(AvaliNexus.id("pack_homes"), PackHomeData::new,
        CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    private static final double MOVE_THRESHOLD_SQ = 16.0;

    private final Map<UUID, BlockPos> homes = new HashMap<>();

    public PackHomeData() {
    }

    private PackHomeData(Map<UUID, BlockPos> saved) {
        homes.putAll(saved);
    }

    public static PackHomeData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public BlockPos home(UUID pack) {
        return homes.get(pack);
    }

    public void update(UUID pack, BlockPos home) {
        BlockPos previous = homes.get(pack);
        if (previous == null || previous.distSqr(home) > MOVE_THRESHOLD_SQ) {
            homes.put(pack, home);
            setDirty();
        }
    }
}
