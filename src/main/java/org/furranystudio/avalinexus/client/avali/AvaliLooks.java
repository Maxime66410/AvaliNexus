/**
 * File: AvaliLooks.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-25
 */
package org.furranystudio.avalinexus.client.avali;

import com.geckolib.constant.dataticket.DataTicket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;

import java.util.HashMap;
import java.util.Map;

public final class AvaliLooks {

    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("avalinexus_texture", Identifier.class);
    public static final DataTicket<Look> COMMON_PATTERN = DataTicket.create("avalinexus_common_pattern", Look.class);
    public static final DataTicket<Look> PATTERN = DataTicket.create("avalinexus_pattern", Look.class);
    public static final DataTicket<Look> FEATHERS = DataTicket.create("avalinexus_feathers", Look.class);
    public static final DataTicket<Look> FIXED = DataTicket.create("avalinexus_fixed", Look.class);

    public static final Identifier ORIGINAL = AvaliNexus.id("textures/entity/avali/rare/original.png");
    private static final Identifier BASE = AvaliNexus.id("textures/entity/avali/base.png");
    private static final Identifier COMMON_PATTERN_TEXTURE = AvaliNexus.id("textures/entity/avali/pattern.png");
    private static final Identifier PATTERN_FEMALE = AvaliNexus.id("textures/entity/avali/pattern_female.png");
    private static final Identifier PATTERN_MALE = AvaliNexus.id("textures/entity/avali/pattern_male.png");
    private static final Identifier FEATHERS_TEXTURE = AvaliNexus.id("textures/entity/avali/feathers.png");
    private static final Identifier FIXED_TEXTURE = AvaliNexus.id("textures/entity/avali/fixed.png");
    private static final int WHITE = 0xFFFFFF;

    public record Look(Identifier texture, int color) {
    }

    private static ResourceManager checkedWith;
    private static final Map<Identifier, Boolean> EXISTS = new HashMap<>();

    private AvaliLooks() {
    }

    public static Identifier texture(AvaliEntity avali) {
        Identifier skin = avali.getSkin().isEmpty() ? null : Identifier.tryParse(avali.getSkin());
        if (skin != null && exists(skin)) {
            return skin;
        }
        return exists(BASE) ? BASE : ORIGINAL;
    }

    public static boolean tinted(AvaliEntity avali) {
        return texture(avali) == BASE;
    }

    public static Look commonPattern(AvaliEntity avali) {
        return layer(avali, COMMON_PATTERN_TEXTURE, avali.getColors().pattern());
    }

    public static Look pattern(AvaliEntity avali) {
        return layer(avali, avali.isFemale() ? PATTERN_FEMALE : PATTERN_MALE, avali.getColors().pattern());
    }

    public static Look feathers(AvaliEntity avali) {
        return layer(avali, FEATHERS_TEXTURE, avali.getColors().feathers());
    }

    public static Look fixed(AvaliEntity avali) {
        return layer(avali, FIXED_TEXTURE, WHITE);
    }

    private static Look layer(AvaliEntity avali, Identifier texture, int color) {
        return tinted(avali) && exists(texture) ? new Look(texture, color) : null;
    }

    private static boolean exists(Identifier texture) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (manager != checkedWith) {
            checkedWith = manager;
            EXISTS.clear();
        }
        return EXISTS.computeIfAbsent(texture, id -> manager.getResource(id).isPresent());
    }
}
