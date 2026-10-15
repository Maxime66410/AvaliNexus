/**
 * File: CryoFreezerBlock.java
 * Author: Maxime66410
 * Created: 2026-10-15
 * Last Modified: 2026-10-15
 */
package org.furranystudio.avalinexus.block.freezer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.furranystudio.avalinexus.block.ModBlockEntities;
import org.furranystudio.avalinexus.block.machine.MachineBlock;
import org.furranystudio.avalinexus.block.machine.MachineBlockEntity;
import org.furranystudio.avalinexus.block.machine.MachineSide;

import java.util.Set;
import java.util.function.Supplier;

public class CryoFreezerBlock extends MachineBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 2, 16),
        Block.box(1, 2, 1, 15, 16, 15),
        Block.box(0.8, 3, 2, 1.8, 15, 14),
        Block.box(5, 5, 0, 11, 11, 2),
        Block.box(5, 5, 14, 11, 11, 16),
        Block.box(14, 5, 5, 16, 11, 11)
    );

    public CryoFreezerBlock(Properties properties) {
        super(properties, Set.of(MachineSide.BACK, MachineSide.SIDE), SHAPE);
    }

    @Override
    protected Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type() {
        return ModBlockEntities.CRYO_FREEZER::get;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CryoFreezerBlockEntity(pos, state);
    }
}
