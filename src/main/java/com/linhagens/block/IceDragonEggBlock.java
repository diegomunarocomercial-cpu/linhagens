package com.linhagens.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Ovo de Dragão de Gelo: ovo cristalino com espinhos de gelo, brilha e solta flocos de neve. */
public class IceDragonEggBlock extends Block {
    public static final MapCodec<IceDragonEggBlock> CODEC = createCodec(IceDragonEggBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 17, 14);

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

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0) {
            world.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.9,
                    pos.getY() + 0.2 + random.nextDouble() * 1.1, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.9,
                    0, 0.02, 0);
        }
        if (random.nextInt(18) == 0) {
            world.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0, 0.04, 0);
        }
    }
}
