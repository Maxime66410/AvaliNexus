/**
 * File: MachineSide.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.machine;

import net.minecraft.core.Direction;

public enum MachineSide {
    FRONT,
    BACK,
    SIDE,
    TOP,
    BOTTOM;

    public static MachineSide of(Direction facing, Direction direction) {
        if (direction == Direction.UP) {
            return TOP;
        }
        if (direction == Direction.DOWN) {
            return BOTTOM;
        }
        if (direction == facing) {
            return FRONT;
        }
        return direction == facing.getOpposite() ? BACK : SIDE;
    }
}
