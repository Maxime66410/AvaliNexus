/**
 * File: AvaliGesture.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

import com.geckolib.animation.RawAnimation;

// Animation names are the gesture names from the CPM project, faces are the bones CPM shows for them
public enum AvaliGesture {
    WAG_TAIL("Wag Tail", null, true),
    NOM_TAIL("Nom Tail", null, true),
    BLEP("Blep", AvaliFace.TONGUE, true),
    MLEM("Mlem", AvaliFace.TONGUE, true),
    SMUG("-w-", AvaliFace.SMUG, true),
    CONTENT("UvU", AvaliFace.CONTENT, true),
    ADORABLE(":D", AvaliFace.ADORABLE, true),
    EXCITED(">w<", AvaliFace.EXCITED, true),
    SHOCKED("o_o", AvaliFace.SHOCKED, true),
    ANGRY("D:<", AvaliFace.ANGRY, false),
    SAD("T^T", AvaliFace.SAD, false),
    ANNOYED("u", AvaliFace.ANNOYED, false);

    private final RawAnimation animation;
    private final AvaliFace face;
    private final boolean idle;

    AvaliGesture(String animation, AvaliFace face, boolean idle) {
        this.animation = RawAnimation.begin().thenLoop(animation);
        this.face = face;
        this.idle = idle;
    }

    public RawAnimation animation() {
        return animation;
    }

    public AvaliFace face() {
        return face;
    }

    public boolean playsWhenIdle() {
        return idle;
    }
}
