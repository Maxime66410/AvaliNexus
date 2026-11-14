/**
 * File: NeoForgeFluidTransfer.java
 * Author: Maxime66410
 * Created: 2026-11-12
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.neoforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.furranystudio.avalinexus.fluid.FluidBridge;
import org.furranystudio.avalinexus.fluid.FluidContent;
import org.furranystudio.avalinexus.fluid.FluidPort;

public final class NeoForgeFluidTransfer {

    private NeoForgeFluidTransfer() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        Block[] blocks = BuiltInRegistries.BLOCK.stream().filter(FluidBridge::exposes).toArray(Block[]::new);
        event.registerBlock(Capabilities.Fluid.BLOCK, (level, pos, state, blockEntity, side) ->
            FluidBridge.exposed(level, pos, state, side) == null ? null : new Exposed(level, pos.immutable(), side), blocks);
    }

    public static FluidPort foreign(Level level, BlockPos pos, Direction side, boolean anyFluid) {
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : new Foreign(handler, pos, anyFluid);
    }

    private static boolean usable(FluidResource resource) {
        return usable(resource, false);
    }

    private static boolean usable(FluidResource resource, boolean anyFluid) {
        return !resource.isEmpty() && resource.isComponentsPatchEmpty() && FluidBridge.allowed(resource.getFluid(), anyFluid);
    }

    private static boolean holds(FluidResource resource, Fluid fluid) {
        return !resource.isEmpty() && resource.isComponentsPatchEmpty() && resource.getFluid().isSame(fluid);
    }

    private static final class Exposed extends SnapshotJournal<FluidContent> implements ResourceHandler<FluidResource> {

        private final Level level;
        private final BlockPos pos;
        private final Direction side;

        private Exposed(Level level, BlockPos pos, Direction side) {
            this.level = level;
            this.pos = pos;
            this.side = side;
        }

        private FluidPort port() {
            return FluidBridge.exposed(level, pos, level.getBlockState(pos), side);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            FluidPort port = port();
            return port == null || port.fluid() == Fluids.EMPTY ? FluidResource.EMPTY : FluidResource.of(port.fluid());
        }

        @Override
        public long getAmountAsLong(int index) {
            FluidPort port = port();
            return port == null ? 0 : port.amount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            FluidPort port = port();
            return port == null ? 0 : port.capacity();
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            FluidPort port = port();
            return port != null && usable(resource) && port.fill(resource.getFluid(), 1, true) > 0;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            FluidPort port = port();
            if (index != 0 || port == null || amount <= 0 || !usable(resource)) {
                return 0;
            }
            int filled = port.fill(resource.getFluid(), amount, true);
            if (filled > 0) {
                updateSnapshots(transaction);
                filled = port.fill(resource.getFluid(), filled, false);
            }
            return filled;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            FluidPort port = port();
            if (index != 0 || port == null || amount <= 0 || !usable(resource)) {
                return 0;
            }
            int drained = port.drain(resource.getFluid(), amount, true);
            if (drained > 0) {
                updateSnapshots(transaction);
                drained = port.drain(resource.getFluid(), drained, false);
            }
            return drained;
        }

        @Override
        protected FluidContent createSnapshot() {
            FluidPort port = port();
            return port == null ? FluidContent.EMPTY : FluidContent.of(port.fluid(), port.amount());
        }

        @Override
        protected void revertToSnapshot(FluidContent snapshot) {
            FluidPort port = port();
            if (port != null) {
                port.restore(snapshot.fluid(), snapshot.amount());
            }
        }
    }

    private record Foreign(ResourceHandler<FluidResource> handler, BlockPos pos, boolean anyFluid) implements FluidPort {

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
            for (int i = 0; i < handler.size(); i++) {
                FluidResource resource = handler.getResource(i);
                if (usable(resource, anyFluid) && handler.getAmountAsLong(i) > 0) {
                    return resource.getFluid();
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
            for (int i = 0; i < handler.size(); i++) {
                if (holds(handler.getResource(i), fluid)) {
                    total += handler.getAmountAsLong(i);
                }
            }
            return (int) Math.min(Integer.MAX_VALUE, total);
        }

        @Override
        public int capacity() {
            Fluid fluid = fluid();
            long total = 0;
            for (int i = 0; i < handler.size(); i++) {
                FluidResource resource = handler.getResource(i);
                if (fluid == Fluids.EMPTY || resource.isEmpty() || holds(resource, fluid)) {
                    total += handler.getCapacityAsLong(i, fluid == Fluids.EMPTY ? resource : FluidResource.of(fluid));
                }
            }
            return (int) Math.min(Integer.MAX_VALUE, total);
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            if (amount <= 0 || !FluidBridge.allowed(fluid, anyFluid)) {
                return 0;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int filled = handler.insert(FluidResource.of(fluid), amount, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return filled;
            }
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            Fluid target = fluid == Fluids.EMPTY ? fluid() : fluid;
            if (amount <= 0 || !FluidBridge.allowed(target, anyFluid)) {
                return 0;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int drained = handler.extract(FluidResource.of(target), amount, transaction);
                if (!simulate) {
                    transaction.commit();
                }
                return drained;
            }
        }
    }
}
