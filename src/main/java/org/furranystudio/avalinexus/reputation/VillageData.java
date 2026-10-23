/**
 * File: VillageData.java
 * Author: Maxime66410
 * Created: 2026-10-22
 * Last Modified: 2026-10-23
 */
package org.furranystudio.avalinexus.reputation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VillageData extends SavedData {

    private static final Codec<VillageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.listOf().optionalFieldOf("broken", List.of()).forGetter(data -> List.copyOf(data.broken)),
        Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Identifier.CODEC, Codec.INT)).optionalFieldOf("shares", Map.of())
            .forGetter(data -> data.shares),
        Codec.LONG.listOf().optionalFieldOf("planted", List.of()).forGetter(data -> List.copyOf(data.planted))
    ).apply(instance, VillageData::new));
    private static final SavedDataType<VillageData> TYPE = new SavedDataType<>(AvaliNexus.id("village_watch"), VillageData::new,
        CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Set<Long> broken = new HashSet<>();
    private final Map<String, Map<Identifier, Integer>> shares = new HashMap<>();
    private final Set<Long> planted = new HashSet<>();

    public VillageData() {
    }

    private VillageData(List<Long> broken, Map<String, Map<Identifier, Integer>> shares, List<Long> planted) {
        this.broken.addAll(broken);
        shares.forEach((pos, share) -> this.shares.put(pos, new HashMap<>(share)));
        this.planted.addAll(planted);
    }

    public static VillageData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isBroken(BlockPos pos) {
        return broken.contains(pos.asLong());
    }

    public void markBroken(BlockPos pos) {
        if (broken.add(pos.asLong()) | shares.remove(key(pos)) != null) {
            setDirty();
        }
    }

    public boolean isPlanted(BlockPos pos) {
        return planted.contains(pos.asLong());
    }

    public void setPlanted(BlockPos pos, boolean byPlayer) {
        if (byPlayer ? planted.add(pos.asLong()) : planted.remove(pos.asLong())) {
            setDirty();
        }
    }

    public boolean hasShare(BlockPos pos) {
        return shares.containsKey(key(pos));
    }

    public void setShare(BlockPos pos, Map<Item, Integer> share) {
        Map<Identifier, Integer> saved = new HashMap<>();
        share.forEach((item, count) -> saved.put(BuiltInRegistries.ITEM.getKey(item), count));
        shares.put(key(pos), saved);
        setDirty();
    }

    public int take(Collection<BlockPos> positions, Item item, int amount) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        int taken = 0;
        for (BlockPos pos : positions) {
            Map<Identifier, Integer> share = shares.get(key(pos));
            int owned = share == null ? 0 : share.getOrDefault(id, 0);
            int part = Math.min(owned, amount - taken);
            if (part > 0) {
                share.put(id, owned - part);
                taken += part;
            }
        }
        if (taken > 0) {
            setDirty();
        }
        return taken;
    }

    private static String key(BlockPos pos) {
        return Long.toString(pos.asLong());
    }
}
