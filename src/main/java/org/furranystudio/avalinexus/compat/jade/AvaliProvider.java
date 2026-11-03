/**
 * File: AvaliProvider.java
 * Author: Maxime66410
 * Created: 2026-11-02
 * Last Modified: 2026-11-02
 */
package org.furranystudio.avalinexus.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.reputation.ReputationTier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

public enum AvaliProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return AvaliDataProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (!(accessor.getEntity() instanceof AvaliEntity avali) || avali.getPack().isEmpty()) {
            return;
        }
        Component pack = Component.translatable("avalinexus.pack." + avali.getPack());
        if (avali.isBaby() || avali.getProfession().isEmpty()) {
            tooltip.add(pack);
        } else {
            Component job = Component.translatable("avalinexus.profession." + avali.getProfession() + (avali.isFemale() ? ".female" : ""));
            tooltip.add(Component.translatable("jade.avalinexus.pack_job", pack, job));
        }
        if (avali.isCompanion()) {
            tooltip.add(Component.translatable("jade.avalinexus.companion", avali.getOwnerName()));
            Component role = Component.translatable("avalinexus.dialogue.choice." + avali.getRole().name().toLowerCase(Locale.ROOT));
            Component state = Component.translatable(avali.isWaiting() ? "jade.avalinexus.waiting" : "jade.avalinexus.following");
            tooltip.add(Component.translatable("jade.avalinexus.role", role, state));
        }
        AvaliDataProvider.INSTANCE.decodeFromData(accessor).ifPresent(reputation -> {
            if (!reputation.met()) {
                tooltip.add(Component.translatable("jade.avalinexus.reputation", Component.translatable("avalinexus.reputation.unknown")));
                return;
            }
            ReputationTier tier = ReputationTier.values()[reputation.tier()];
            Component name = Component.translatable("avalinexus.reputation.tier." + tier.name().toLowerCase(Locale.ROOT)).withColor(tier.color());
            tooltip.add(Component.translatable("jade.avalinexus.reputation", Component.translatable("jade.avalinexus.reputation_value", name, reputation.value())));
        });
    }
}
