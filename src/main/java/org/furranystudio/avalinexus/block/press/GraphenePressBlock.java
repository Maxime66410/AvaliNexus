/**
 * File: GraphenePressBlock.java
 * Author: Maxime66410
 * Created: 2026-10-16
 * Last Modified: 2026-10-16
 */
package org.furranystudio.avalinexus.block.press;

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

public class GraphenePressBlock extends MachineBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0, 0, 0, 16, 2, 16),
        Block.box(1, 2, 1, 15, 3, 15),
        Block.box(1.5, 2.5, 2, 12.5, 3.5, 14),
        Block.box(13, 3, 1, 15, 16, 15),
        Block.box(1, 15, 1, 13, 16, 15),
        Block.box(5, 10, 6, 9, 15, 10),
        Block.box(4, 9, 5, 10, 10, 11),
        Block.box(14, 5, 5, 16, 11, 11)
    );

    public GraphenePressBlock(Properties properties) {
        super(properties, Set.of(MachineSide.BACK), SHAPE);
    }

    @Override
    protected Supplier<? extends BlockEntityType<? extends MachineBlockEntity>> type() {
        return ModBlockEntities.GRAPHENE_PRESS::get;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraphenePressBlockEntity(pos, state);
    }
}
