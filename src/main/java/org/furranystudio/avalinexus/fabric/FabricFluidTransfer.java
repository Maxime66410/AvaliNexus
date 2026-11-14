/**
 * File: FabricFluidTransfer.java
 * Author: Maxime66410
 * Created: 2026-11-12
 * Last Modified: 2026-11-14
 */
package org.furranystudio.avalinexus.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.furranystudio.avalinexus.fluid.FluidBridge;
import org.furranystudio.avalinexus.fluid.FluidContent;
import org.furranystudio.avalinexus.fluid.FluidPort;

public final class FabricFluidTransfer {

    private static final long DROPLETS = FluidBridge.DROPLETS_PER_MILLIBUCKET;

    private FabricFluidTransfer() {
    }

    public static void register() {
        Block[] blocks = BuiltInRegistries.BLOCK.stream().filter(FluidBridge::exposes).toArray(Block[]::new);
        FluidStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, side) ->
            FluidBridge.exposed(level, pos, state, side) == null ? null : new Exposed(level, pos.immutable(), side), blocks);
    }

    public static FluidPort foreign(Level level, BlockPos pos, Direction side, boolean anyFluid) {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, side);
        return storage == null ? null : new Foreign(storage, pos, anyFluid);
    }

    private static boolean usable(FluidVariant variant) {
        return usable(variant, false);
    }

    private static boolean usable(FluidVariant variant, boolean anyFluid) {
        return !variant.isBlank() && !variant.hasComponents() && FluidBridge.allowed(variant.getFluid(), anyFluid);
    }

    private static boolean holds(StorageView<FluidVariant> view, Fluid fluid) {
        return !view.isResourceBlank() && !view.getResource().hasComponents() && view.getResource().getFluid().isSame(fluid);
    }

    private static int toMillibuckets(long droplets) {
        return (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS);
    }

    private static final class Exposed extends SnapshotParticipant<FluidContent> implements SingleSlotStorage<FluidVariant> {

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
        public long insert(FluidVariant variant, long maxAmount, TransactionContext transaction) {
            FluidPort port = port();
            int amount = toMillibuckets(maxAmount);
            if (port == null || amount <= 0 || !usable(variant)) {
                return 0;
            }
            int filled = port.fill(variant.getFluid(), amount, true);
            if (filled > 0) {
                updateSnapshots(transaction);
                filled = port.fill(variant.getFluid(), filled, false);
            }
            return filled * DROPLETS;
        }

        @Override
        public long extract(FluidVariant variant, long maxAmount, TransactionContext transaction) {
            FluidPort port = port();
            int amount = toMillibuckets(maxAmount);
            if (port == null || amount <= 0 || !usable(variant)) {
                return 0;
            }
            int drained = port.drain(variant.getFluid(), amount, true);
            if (drained > 0) {
                updateSnapshots(transaction);
                drained = port.drain(variant.getFluid(), drained, false);
            }
            return drained * DROPLETS;
        }

        @Override
        public boolean isResourceBlank() {
            FluidPort port = port();
            return port == null || port.fluid() == Fluids.EMPTY;
        }

        @Override
        public FluidVariant getResource() {
            FluidPort port = port();
            return port == null || port.fluid() == Fluids.EMPTY ? FluidVariant.blank() : FluidVariant.of(port.fluid());
        }

        @Override
        public long getAmount() {
            FluidPort port = port();
            return port == null ? 0 : port.amount() * DROPLETS;
        }

        @Override
        public long getCapacity() {
            FluidPort port = port();
            return port == null ? 0 : port.capacity() * DROPLETS;
        }

        @Override
        protected FluidContent createSnapshot() {
            FluidPort port = port();
            return port == null ? FluidContent.EMPTY : FluidContent.of(port.fluid(), port.amount());
        }

        @Override
        protected void readSnapshot(FluidContent snapshot) {
            FluidPort port = port();
            if (port != null) {
                port.restore(snapshot.fluid(), snapshot.amount());
            }
        }
    }

    private record Foreign(Storage<FluidVariant> storage, BlockPos pos, boolean anyFluid) implements FluidPort {

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
            for (StorageView<FluidVariant> view : storage) {
                if (usable(view.getResource(), anyFluid) && view.getAmount() >= DROPLETS) {
                    return view.getResource().getFluid();
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
            for (StorageView<FluidVariant> view : storage) {
                if (holds(view, fluid)) {
                    total += view.getAmount();
                }
            }
            return toMillibuckets(total);
        }

        @Override
        public int capacity() {
            Fluid fluid = fluid();
            long total = 0;
            for (StorageView<FluidVariant> view : storage) {
                if (fluid == Fluids.EMPTY || view.isResourceBlank() || holds(view, fluid)) {
                    total += view.getCapacity();
                }
            }
            return toMillibuckets(total);
        }

        @Override
        public int fill(Fluid fluid, int amount, boolean simulate) {
            if (amount <= 0 || !FluidBridge.allowed(fluid, anyFluid)) {
                return 0;
            }
            int possible;
            try (Transaction transaction = Transaction.openOuter()) {
                possible = toMillibuckets(storage.insert(FluidVariant.of(fluid), amount * DROPLETS, transaction));
            }
            if (simulate || possible <= 0) {
                return possible;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(FluidVariant.of(fluid), possible * DROPLETS, transaction);
                if (inserted != possible * DROPLETS) {
                    return 0;
                }
                transaction.commit();
                return possible;
            }
        }

        @Override
        public int drain(Fluid fluid, int amount, boolean simulate) {
            Fluid target = fluid == Fluids.EMPTY ? fluid() : fluid;
            if (amount <= 0 || !FluidBridge.allowed(target, anyFluid)) {
                return 0;
            }
            int possible;
            try (Transaction transaction = Transaction.openOuter()) {
                possible = toMillibuckets(storage.extract(FluidVariant.of(target), amount * DROPLETS, transaction));
            }
            if (simulate || possible <= 0) {
                return possible;
            }
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = storage.extract(FluidVariant.of(target), possible * DROPLETS, transaction);
                if (extracted != possible * DROPLETS) {
                    return 0;
                }
                transaction.commit();
                return possible;
            }
        }
    }
}
