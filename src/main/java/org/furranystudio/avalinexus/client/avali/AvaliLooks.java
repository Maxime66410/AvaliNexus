/**
 * File: AvaliLooks.java
 * Author: Maxime66410
 * Created: 2026-10-17
 * Last Modified: 2026-10-17
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
    public static final DataTicket<Identifier> PATTERN = DataTicket.create("avalinexus_pattern", Identifier.class);
    public static final DataTicket<Identifier> FIXED = DataTicket.create("avalinexus_fixed", Identifier.class);

    public static final Identifier ORIGINAL = AvaliNexus.id("textures/entity/avali/rare/original.png");
    private static final Identifier BASE = AvaliNexus.id("textures/entity/avali/base.png");
    private static final Identifier PATTERN_FEMALE = AvaliNexus.id("textures/entity/avali/pattern_female.png");
    private static final Identifier PATTERN_MALE = AvaliNexus.id("textures/entity/avali/pattern_male.png");
    private static final Identifier FIXED_TEXTURE = AvaliNexus.id("textures/entity/avali/fixed.png");

    private static ResourceManager checkedWith;
    private static final Map<Identifier, Boolean> EXISTS = new HashMap<>();

    private AvaliLooks() {
    }

    public static Identifier texture(AvaliEntity avali) {
        Identifier skin = Identifier.tryParse(avali.getSkin());
        if (skin != null && exists(skin)) {
            return skin;
        }
        return exists(BASE) ? BASE : ORIGINAL;
    }

    public static boolean tinted(AvaliEntity avali) {
        return texture(avali) == BASE;
    }

    public static Identifier pattern(AvaliEntity avali) {
        if (!tinted(avali)) {
            return null;
        }
        Identifier pattern = avali.isFemale() ? PATTERN_FEMALE : PATTERN_MALE;
        return exists(pattern) ? pattern : null;
    }

    public static Identifier fixed(AvaliEntity avali) {
        return tinted(avali) && exists(FIXED_TEXTURE) ? FIXED_TEXTURE : null;
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
