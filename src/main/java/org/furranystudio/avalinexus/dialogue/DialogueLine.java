/**
 * File: DialogueLine.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.dialogue;

import com.google.gson.JsonObject;
import org.furranystudio.avalinexus.entity.avali.AvaliEntity;
import org.furranystudio.avalinexus.reputation.ReputationTier;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliFace;
import org.furranystudio.avalinexus.entity.avali.expression.AvaliGesture;

import java.util.Locale;

public record DialogueLine(String text, AvaliFace face, AvaliGesture gesture, String condition) {

    public static DialogueLine fromJson(JsonObject json) {
        String text = json.get("text").getAsString();
        AvaliFace face = json.has("face") ? AvaliFace.valueOf(json.get("face").getAsString().toUpperCase(Locale.ROOT)) : null;
        AvaliGesture gesture = json.has("gesture") ? AvaliGesture.valueOf(json.get("gesture").getAsString().toUpperCase(Locale.ROOT)) : null;
        String condition = json.has("condition") ? json.get("condition").getAsString() : null;
        return new DialogueLine(text, face, gesture, condition);
    }

    public boolean matches(AvaliEntity avali, ReputationTier tier) {
        if (condition == null) {
            return true;
        }
        for (ReputationTier candidate : ReputationTier.values()) {
            if (candidate.key().equals(condition)) {
                return candidate == tier;
            }
        }
        return switch (condition) {
            case "day" -> avali.level().isBrightOutside();
            case "night" -> avali.level().isDarkOutside();
            case "rain" -> avali.level().isRaining();
            case "baby" -> avali.isBaby();
            case "adult" -> !avali.isBaby();
            default -> true;
        };
    }
}
