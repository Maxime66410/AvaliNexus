/**
 * File: DialogueKeys.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-10-19
 */
package org.furranystudio.avalinexus.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.furranystudio.avalinexus.client.ModKeyCategory;

public final class DialogueKeys {

    public static final KeyMapping CURSOR = new KeyMapping("key.avalinexus.dialogue_cursor", InputConstants.KEY_LALT, ModKeyCategory.AVALI_NEXUS);

    private DialogueKeys() {
    }
}
