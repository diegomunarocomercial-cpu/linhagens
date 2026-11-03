package com.linhagens.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Água Rubra: "água" levemente avermelhada do poço da Vila Aurora. Translúcida, sem colisão; quem entra
 * flutua devagar de volta à superfície (então não afoga e dá para sair do poço).
 */
public class AguaRubraBlock extends Block {
    public static final MapCodec<AguaRubraBlock> CODEC = createCodec(AguaRubraBlock::new);

    public AguaRubraBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Vec3d v = entity.getVelocity();
        entity.setVelocity(v.x * 0.88, Math.min(v.y + 0.06, 0.14), v.z * 0.88);
        entity.fallDistance = 0;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(14) == 0) {
            world.addParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0, 0.05, 0);
        }
    }
}
