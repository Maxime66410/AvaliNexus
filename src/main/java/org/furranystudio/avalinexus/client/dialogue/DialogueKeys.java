/**
 * File: DialogueKeys.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.client.dialogue;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class DialogueKeys {

    public static final KeyMapping CURSOR = new KeyMapping("key.avalinexus.dialogue_cursor", InputConstants.KEY_LALT, KeyMapping.Category.MISC);

    private DialogueKeys() {
    }
}
