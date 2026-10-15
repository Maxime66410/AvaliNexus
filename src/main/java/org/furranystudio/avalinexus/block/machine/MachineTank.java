/**
 * File: MachineTank.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.furranystudio.avalinexus.fluid.AvaliLiquids;
import org.furranystudio.avalinexus.fluid.FluidContent;
import org.furranystudio.avalinexus.fluid.FluidPort;

import java.util.function.Predicate;

public final class MachineTank {

    private final String key;
    private final int capacity;
    private final Predicate<Fluid> accepts;
    private final Runnable changed;
    private FluidContent content = FluidContent.EMPTY;

    public MachineTank(String key, int capacity, Predicate<Fluid> accepts, Runnable changed) {
        this.key = key;
        this.capacity = capacity;
        this.accepts = accepts;
        this.changed = changed;
    }

    public Fluid fluid() {
        return content.fluid();
    }

    public int amount() {
        return content.amount();
    }

    public int capacity() {
        return capacity;
    }

    public boolean accepts(Fluid fluid) {
        return fluid != Fluids.EMPTY && accepts.test(fluid) && (content.isEmpty() || content.fluid().isSame(fluid));
    }

    public int fill(Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0 || !accepts(fluid)) {
            return 0;
        }
        int filled = Math.min(amount, capacity - content.amount());
        if (!simulate && filled > 0) {
            content = FluidContent.of(AvaliLiquids.source(fluid), content.amount() + filled);
            changed.run();
        }
        return filled;
    }

    public int drain(Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0 || content.isEmpty() || fluid != Fluids.EMPTY && !content.fluid().isSame(fluid)) {
            return 0;
        }
        int drained = Math.min(amount, content.amount());
        if (!simulate) {
            content = FluidContent.of(content.fluid(), content.amount() - drained);
            changed.run();
        }
        return drained;
    }

    public ItemStack useBucket(ItemStack stack, boolean output) {
        if (stack.getCount() != 1) {
            return stack;
        }
        if (output) {
            if (stack.is(Items.BUCKET) && drain(Fluids.EMPTY, AvaliLiquids.BUCKET, true) == AvaliLiquids.BUCKET) {
                Fluid fluid = fluid();
                drain(fluid, AvaliLiquids.BUCKET, false);
                return AvaliLiquids.filledBucket(fluid);
            }
            return stack;
        }
        Fluid fluid = AvaliLiquids.fromBucket(stack);
        if (fluid != Fluids.EMPTY && fill(fluid, AvaliLiquids.BUCKET, true) == AvaliLiquids.BUCKET) {
            fill(fluid, AvaliLiquids.BUCKET, false);
            return new ItemStack(Items.BUCKET);
        }
        return stack;
    }

    public FluidPort port(FluidPort.Role role) {
        return new TankPort(this, role);
    }

    public void save(ValueOutput output) {
        output.store(key, FluidContent.CODEC, content);
    }

    public void load(ValueInput input) {
        content = input.read(key, FluidContent.CODEC).orElse(FluidContent.EMPTY);
    }

    private record TankPort(MachineTank tank, Role role) implements FluidPort {

        @Override
        public Object identity() {
            return tank;
        }

        @Override
        public Fluid fluid() {
            return tank.amount() > 0 ? tank.fluid() : Fluids.EMPTY;
        }

        @Override
        public int amount() {
            return tank.amount();
        }

        @Override
        public int capacity() {
            return tank.capacity();
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            return role == Role.SINK ? tank.fill(fluid, amount, simulate) : 0;
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            return role == Role.SOURCE ? tank.drain(fluid, amount, simulate) : 0;
        }
    }
}
