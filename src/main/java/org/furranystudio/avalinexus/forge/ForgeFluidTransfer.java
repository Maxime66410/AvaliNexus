/**
 * File: ForgeFluidTransfer.java
 * Author: Maxime66410
 * Created: 2026-11-12
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.furranystudio.avalinexus.AvaliNexus;
import org.furranystudio.avalinexus.fluid.FluidBridge;
import org.furranystudio.avalinexus.fluid.FluidPort;

public final class ForgeFluidTransfer {

    private ForgeFluidTransfer() {
    }

    public static void attach(AttachCapabilitiesEvent.BlockEntities event) {
        BlockEntity blockEntity = event.getObject();
        if (FluidBridge.exposes(blockEntity.getBlockState().getBlock())) {
            event.addCapability(AvaliNexus.id("fluid_handler"), new Provider(blockEntity));
        }
    }

    public static FluidPort foreign(Level level, BlockPos pos, Direction side, boolean anyFluid) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).map(handler -> (FluidPort) new Foreign(handler, pos, anyFluid)).orElse(null);
    }

    private static boolean usable(FluidStack stack) {
        return usable(stack, false);
    }

    private static boolean usable(FluidStack stack, boolean anyFluid) {
        return !stack.isEmpty() && !stack.hasTag() && FluidBridge.allowed(stack.getFluid(), anyFluid);
    }

    private static boolean holds(FluidStack stack, Fluid fluid) {
        return !stack.isEmpty() && !stack.hasTag() && stack.getFluid().isSame(fluid);
    }

    private record Provider(BlockEntity blockEntity) implements ICapabilityProvider {

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            Level level = blockEntity.getLevel();
            if (capability != ForgeCapabilities.FLUID_HANDLER || level == null
                || FluidBridge.exposed(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), side) == null) {
                return LazyOptional.empty();
            }
            return LazyOptional.of(() -> (IFluidHandler) new Exposed(level, blockEntity.getBlockPos(), side)).cast();
        }
    }

    private record Exposed(Level level, BlockPos pos, Direction side) implements IFluidHandler {

        private FluidPort port() {
            return FluidBridge.exposed(level, pos, level.getBlockState(pos), side);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            FluidPort port = port();
            return port == null || port.fluid() == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(port.fluid(), port.amount());
        }

        @Override
        public int getTankCapacity(int tank) {
            FluidPort port = port();
            return port == null ? 0 : port.capacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            FluidPort port = port();
            return port != null && usable(stack) && port.fill(stack.getFluid(), 1, true) > 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            FluidPort port = port();
            if (port == null || !usable(resource)) {
                return 0;
            }
            return port.fill(resource.getFluid(), resource.getAmount(), action.simulate());
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            FluidPort port = port();
            if (port == null || !usable(resource)) {
                return FluidStack.EMPTY;
            }
            int drained = port.drain(resource.getFluid(), resource.getAmount(), action.simulate());
            return drained > 0 ? new FluidStack(resource.getFluid(), drained) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidPort port = port();
            if (port == null || port.fluid() == Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }
            Fluid fluid = port.fluid();
            int drained = port.drain(fluid, maxDrain, action.simulate());
            return drained > 0 ? new FluidStack(fluid, drained) : FluidStack.EMPTY;
        }
    }

    private record Foreign(IFluidHandler handler, BlockPos pos, boolean anyFluid) implements FluidPort {

        @Override
        public Role role() {
            return Role.BUFFER;
        }

        @Override
        public Object identity() {
            return pos;
        }

        @Override
        public Fluid fluid() {
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack stack = handler.getFluidInTank(i);
                if (usable(stack, anyFluid)) {
                    return stack.getFluid();
                }
            }
            return Fluids.EMPTY;
        }

        @Override
        public int amount() {
            Fluid fluid = fluid();
            if (fluid == Fluids.EMPTY) {
                return 0;
            }
            long total = 0;
            for (int i = 0; i < handler.getTanks(); i++) {
                if (holds(handler.getFluidInTank(i), fluid)) {
                    total += handler.getFluidInTank(i).getAmount();
                }
            }
            return (int) Math.min(Integer.MAX_VALUE, total);
        }

        @Override
        public int capacity() {
            Fluid fluid = fluid();
            long total = 0;
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack stack = handler.getFluidInTank(i);
                if (fluid == Fluids.EMPTY || stack.isEmpty() || holds(stack, fluid)) {
                    total += handler.getTankCapacity(i);
                }
            }
            return (int) Math.min(Integer.MAX_VALUE, total);
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            if (amount <= 0 || !FluidBridge.allowed(fluid, anyFluid)) {
                return 0;
            }
            return handler.fill(new FluidStack(fluid, amount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            Fluid target = fluid == Fluids.EMPTY ? fluid() : fluid;
            if (amount <= 0 || !FluidBridge.allowed(target, anyFluid)) {
                return 0;
            }
            return handler.drain(new FluidStack(target, amount), simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE).getAmount();
        }
    }
}
