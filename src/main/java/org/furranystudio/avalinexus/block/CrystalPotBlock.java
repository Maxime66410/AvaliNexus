/**
 * File: CrystalPotBlock.java
 * Author: Maxime66410
 * Created: 2026-09-28
 * Last Modified: 2026-09-28
 */
package org.furranystudio.avalinexus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// Small pot with Nexite crystals growing out of it, the box follows the pot and the crystal cluster
public class CrystalPotBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(4, 0, 4, 12, 3, 12),
        Block.box(3, 3, 3, 13, 5, 13),
        Block.box(5, 5, 5, 11, 15, 11));

    public CrystalPotBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
