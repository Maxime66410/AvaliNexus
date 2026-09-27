/**
 * File: AvaliMood.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

public enum AvaliMood {
    NEUTRAL(AvaliFace.NEUTRAL, false),
    HAPPY(AvaliFace.HAPPY, false),
    SCARED(AvaliFace.SCARED, true),
    ANGRY(AvaliFace.ANGRY, true);

    private final AvaliFace face;
    private final boolean earsDown;

    AvaliMood(AvaliFace face, boolean earsDown) {
        this.face = face;
        this.earsDown = earsDown;
    }

    public AvaliFace face() {
        return face;
    }

    public boolean earsDown() {
        return earsDown;
    }
}
