package com.linhagens.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Braço do Poço: um braço humano ensanguentado no fundo do poço da Vila Aurora.
 * Segredo: entregue uma esmeralda a ele e ele devolve uma recompensa.
 */
public class BracoPocoBlock extends Block {
    public static final MapCodec<BracoPocoBlock> CODEC = createCodec(BracoPocoBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(3, 0, 3, 13, 16, 13);
    private static final DustParticleEffect BLOOD = new DustParticleEffect(new Vector3f(0.65f, 0.02f, 0.04f), 1.2f);

    public BracoPocoBlock(Settings settings) {
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
        if (random.nextInt(4) == 0) {
            world.addParticle(BLOOD, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4, pos.getY() + 0.9,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4, 0, 0.03, 0);
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(Items.EMERALD)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!world.isClient && world instanceof ServerWorld sw) {
            stack.decrementUnlessCreative(1, player);
            int r = world.random.nextInt(100);
            Item reward;
            int count = 1;
            if (r < 30) { reward = com.linhagens.item.ModItems.AURORA_SHARD; count = 1 + world.random.nextInt(2); }
            else if (r < 50) { reward = Items.GOLD_INGOT; count = 1 + world.random.nextInt(3); }
            else if (r < 68) { reward = Items.IRON_INGOT; count = 2 + world.random.nextInt(3); }
            else if (r < 82) { reward = Items.AMETHYST_SHARD; count = 2 + world.random.nextInt(4); }
            else if (r < 92) { reward = Items.BREAD; count = 3; }
            else if (r < 98) { reward = Items.GOLDEN_APPLE; }
            else { reward = Items.DIAMOND; }
            Block.dropStack(world, pos.up(), new ItemStack(reward, count));
            sw.spawnParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 25, 0.3, 0.4, 0.3, 0.5);
            sw.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.BLOCKS, 1f, 0.7f);
        }
        return ItemActionResult.success(world.isClient);
    }
}
