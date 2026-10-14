/**
 * File: TankFluidPort.java
 * Author: Maxime66410
 * Created: 2026-10-14
 * Last Modified: 2026-10-14
 */
package org.furranystudio.avalinexus.block.tank;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.fluid.FluidPort;

public record TankFluidPort(TankStructure structure) implements FluidPort {

    @Override
    public Role role() {
        return Role.BUFFER;
    }

    @Override
    public Object identity() {
        return structure.anchor();
    }

    @Override
    public Fluid fluid() {
        return structure.fluid();
    }

    @Override
    public int amount() {
        return structure.amount();
    }

    @Override
    public int capacity() {
        return structure.capacity();
    }

    @Override
    public int fill(Fluid fluid, int amount, boolean simulate) {
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return 0;
        }
        return structure.insert(fluid, amount, simulate);
    }

    @Override
    public int drain(Fluid fluid, int amount, boolean simulate) {
        Fluid held = structure.fluid();
        if (held == Fluids.EMPTY || amount <= 0 || fluid != Fluids.EMPTY && !held.isSame(fluid)) {
            return 0;
        }
        return structure.extract(amount, simulate);
    }
}
