/**
 * File: AvaliFace.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity.avali.expression;

// Eye and mouth names match the bone suffixes in the model, "Eye Left Angry", "Mouth Front Upset"...
public enum AvaliFace {
    NEUTRAL("Normal", "Normal", false, false),
    HAPPY("Normal", "Smile", false, false),
    SCARED("Shock", "Scared", false, false),
    ANGRY("Angry", "Upset", false, false),
    SMUG("Smug", "Normal", true, false),
    CONTENT("Closed", "Grin", false, false),
    ADORABLE("Adorbe", "Smile", false, false),
    EXCITED("Playful", "Excited", false, false),
    SHOCKED("Shock", "Grin", false, false),
    SAD("Sad", "Frown", false, false),
    ANNOYED("Annoyed", "Frown", false, false),
    TONGUE("Normal", "Normal", false, true),
    SLEEPING("Closed", "Normal", false, false);

    private final String eyes;
    private final String mouth;
    private final boolean blush;
    private final boolean tongue;

    AvaliFace(String eyes, String mouth, boolean blush, boolean tongue) {
        this.eyes = eyes;
        this.mouth = mouth;
        this.blush = blush;
        this.tongue = tongue;
    }

    public String eyes() {
        return eyes;
    }

    public String mouth() {
        return mouth;
    }

    public boolean blush() {
        return blush;
    }

    public boolean tongue() {
        return tongue;
    }
}
