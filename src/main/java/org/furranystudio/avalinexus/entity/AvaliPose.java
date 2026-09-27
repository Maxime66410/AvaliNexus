/**
 * File: AvaliPose.java
 * Author: Maxime66410
 * Created: 2026-09-27
 * Last Modified: 2026-09-27
 */
package org.furranystudio.avalinexus.entity;

import com.geckolib.animation.RawAnimation;

public enum AvaliPose {
    LOUNGE("LOUNGE"),
    SIT_BACK("SIT BACK"),
    SIT_UP("SIT UP"),
    LAY_BELLY("LAY BELLY");

    private final RawAnimation animation;

    AvaliPose(String animation) {
        this.animation = RawAnimation.begin().thenLoop(animation);
    }

    public RawAnimation animation() {
        return animation;
    }
}
