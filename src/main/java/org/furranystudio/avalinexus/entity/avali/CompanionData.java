/**
 * File: CompanionData.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.entity.avali;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CompanionData extends SavedData {

    private static final Codec<CompanionData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.CODEC.listOf())
        .xmap(CompanionData::new, data -> data.companions);
    private static final SavedDataType<CompanionData> TYPE = new SavedDataType<>(AvaliNexus.id("companions"), CompanionData::new,
        CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, List<UUID>> companions = new HashMap<>();

    public CompanionData() {
    }

    private CompanionData(Map<UUID, List<UUID>> saved) {
        saved.forEach((owner, list) -> companions.put(owner, new ArrayList<>(list)));
    }

    public static CompanionData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public int count(UUID owner) {
        return companions.getOrDefault(owner, List.of()).size();
    }

    public void add(UUID owner, UUID companion) {
        List<UUID> list = companions.computeIfAbsent(owner, key -> new ArrayList<>());
        if (!list.contains(companion)) {
            list.add(companion);
            setDirty();
        }
    }

    public void remove(UUID owner, UUID companion) {
        List<UUID> list = companions.get(owner);
        if (list != null && list.remove(companion)) {
            if (list.isEmpty()) {
                companions.remove(owner);
            }
            setDirty();
        }
    }
}
