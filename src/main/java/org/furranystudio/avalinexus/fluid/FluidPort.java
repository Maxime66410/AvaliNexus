/**
 * File: FluidPort.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.fluid;

import net.minecraft.world.level.material.Fluid;

public interface FluidPort {

    enum Role {
        SOURCE,
        SINK,
        BUFFER
    }

    Role role();

    Object identity();

    Fluid fluid();

    int amount();

    int capacity();

    int fill(Fluid fluid, int amount, boolean simulate);

    int drain(Fluid fluid, int amount, boolean simulate);
}
