/**
 * File: AvaliDataProvider.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.reputation.AvaliReputation;
import org.furranystudio.avalinexus.reputation.ReputationData;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.StreamServerDataProvider;

public enum AvaliDataProvider implements StreamServerDataProvider<EntityAccessor, AvaliDataProvider.Reputation> {
    INSTANCE;

    static final Identifier UID = AvaliNexus.id("avali");

    public record Reputation(int value, int tier, boolean met) {

        static final StreamCodec<RegistryFriendlyByteBuf, Reputation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Reputation::value,
            ByteBufCodecs.VAR_INT, Reputation::tier,
            ByteBufCodecs.BOOL, Reputation::met,
            Reputation::new);
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public Reputation streamData(EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof AvaliEntity avali) || avali.getPack().isEmpty() || !(accessor.getPlayer() instanceof ServerPlayer player)) {
            return null;
        }
        ReputationData.Standing standing = AvaliReputation.standing(player, avali.getPack());
        return new Reputation(standing.value(), AvaliReputation.tier(player, avali.getPack()).ordinal(), standing.met());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Reputation> streamCodec() {
        return Reputation.STREAM_CODEC;
    }
}
