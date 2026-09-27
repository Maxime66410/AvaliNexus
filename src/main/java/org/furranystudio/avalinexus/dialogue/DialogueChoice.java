/**
 * File: DialogueChoice.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.furranystudio.avalinexus.AvaliNexus;

import java.util.Locale;

public enum DialogueChoice {
    TALK("talk"),
    TRADE("shop"),
    LEAVE("close");

    private final Identifier icon;

    DialogueChoice(String icon) {
        this.icon = AvaliNexus.id("dialogue/" + icon);
    }

    public Component label() {
        return Component.translatable("avalinexus.dialogue.choice." + name().toLowerCase(Locale.ROOT));
    }

    public Identifier icon() {
        return icon;
    }
}
