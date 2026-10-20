/**
 * File: BondData.java
 * Author: Maxime66410
 * Created: 2026-10-20
 * Last Modified: 2026-10-20
 */
package org.furranystudio.avalinexus.entity.avali;

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

public class BondData extends SavedData {

    public record Bond(UUID partner, String name) {

        private static final Codec<Bond> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("partner").forGetter(Bond::partner),
            Codec.STRING.fieldOf("name").forGetter(Bond::name)
        ).apply(instance, Bond::new));
    }

    private static final Codec<BondData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Bond.CODEC)
        .xmap(BondData::new, data -> data.bonds);
    private static final SavedDataType<BondData> TYPE = new SavedDataType<>(AvaliNexus.id("bonds"), BondData::new,
        CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, Bond> bonds = new HashMap<>();

    public BondData() {
    }

    private BondData(Map<UUID, Bond> saved) {
        bonds.putAll(saved);
    }

    public static BondData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public Bond bond(UUID avali) {
        return bonds.get(avali);
    }

    public void link(AvaliEntity first, AvaliEntity second) {
        bonds.put(first.getUUID(), new Bond(second.getUUID(), second.getName().getString()));
        bonds.put(second.getUUID(), new Bond(first.getUUID(), first.getName().getString()));
        setDirty();
    }

    public void remove(UUID avali) {
        Bond bond = bonds.remove(avali);
        if (bond != null) {
            bonds.remove(bond.partner());
            setDirty();
        }
    }
}
