package com.linhagens.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** Ovo de Dragão de Gelo: troféu decorativo (formato de ovo), brilha de leve. */
public class IceDragonEggBlock extends Block {
    public static final MapCodec<IceDragonEggBlock> CODEC = createCodec(IceDragonEggBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(3, 0, 3, 13, 16, 13);

    public IceDragonEggBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
