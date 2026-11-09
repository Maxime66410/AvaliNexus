/**
 * File: DialogueKeys.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-11-09
 */
package org.furranystudio.avalinexus.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.furranystudio.avalinexus.client.ModKeyCategory;

import java.util.Arrays;

public final class DialogueKeys {

    public static final KeyMapping CURSOR = new KeyMapping("key.avalinexus.dialogue_cursor", InputConstants.KEY_LALT, ModKeyCategory.AVALI_NEXUS);
    public static final KeyMapping PICK = new KeyMapping("key.avalinexus.dialogue_pick", InputConstants.KEY_E, ModKeyCategory.AVALI_NEXUS);
    public static final KeyMapping[] CHOICES = {
        choice(1, InputConstants.KEY_1), choice(2, InputConstants.KEY_2), choice(3, InputConstants.KEY_3), choice(4, InputConstants.KEY_4)
    };

    private DialogueKeys() {
    }

    public static void drain() {
        consume(PICK);
        for (KeyMapping choice : CHOICES) {
            consume(choice);
        }
    }

    public static void blockShared(KeyMapping key) {
        if (key == CURSOR || key == PICK || Arrays.asList(CHOICES).contains(key)) {
            return;
        }
        if (key.same(PICK) || Arrays.stream(CHOICES).anyMatch(key::same)) {
            consume(key);
        }
    }

    private static void consume(KeyMapping key) {
        while (key.consumeClick()) {
            key.setDown(false);
        }
    }

    private static KeyMapping choice(int number, int key) {
        return new KeyMapping("key.avalinexus.dialogue_choice_" + number, key, ModKeyCategory.AVALI_NEXUS);
    }
}
