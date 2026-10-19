/**
 * File: ReputationData.java
 * Author: Maxime66410
 * Created: 2026-10-19
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.reputation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationData extends SavedData {

    public record Standing(int value, boolean met, long decayDay, long capDay, int giftGained, int tradeGained) {

        public static final Codec<Standing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("value").forGetter(Standing::value),
            Codec.BOOL.fieldOf("met").forGetter(Standing::met),
            Codec.LONG.fieldOf("decay_day").forGetter(Standing::decayDay),
            Codec.LONG.fieldOf("cap_day").forGetter(Standing::capDay),
            Codec.INT.fieldOf("gift_gained").forGetter(Standing::giftGained),
            Codec.INT.fieldOf("trade_gained").forGetter(Standing::tradeGained)
        ).apply(instance, Standing::new));

        public static final Standing EMPTY = new Standing(0, false, 0, 0, 0, 0);
    }

    private static final Codec<ReputationData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,
            Codec.unboundedMap(Codec.STRING, Standing.CODEC))
        .xmap(ReputationData::new, data -> data.players);
    private static final SavedDataType<ReputationData> TYPE = new SavedDataType<>(AvaliNexus.id("reputation"), ReputationData::new,
        CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, Map<String, Standing>> players = new HashMap<>();

    public ReputationData() {
    }

    private ReputationData(Map<UUID, Map<String, Standing>> saved) {
        saved.forEach((player, packs) -> players.put(player, new HashMap<>(packs)));
    }

    public static ReputationData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public Standing standing(UUID player, String pack) {
        return players.getOrDefault(player, Map.of()).getOrDefault(pack, Standing.EMPTY);
    }

    public void set(UUID player, String pack, Standing standing) {
        players.computeIfAbsent(player, key -> new HashMap<>()).put(pack, standing);
        setDirty();
    }
}
