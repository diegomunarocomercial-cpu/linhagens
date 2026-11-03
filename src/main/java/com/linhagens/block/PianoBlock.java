package com.linhagens.block;


import com.linhagens.quest.PianoSummon;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Piano: clique nas teclas para tocar (7 teclas brancas; agache = sustenido; clique na metade de cima = oitava mais aguda).
 * Cuidado: o misterioso Caçador de Marcados só aparece ao lado de um piano...
 */
public class PianoBlock extends HorizontalFacingBlock {
    public static final MapCodec<PianoBlock> CODEC = createCodec(PianoBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    private static final SoundEvent HARP = SoundEvent.of(Identifier.ofVanilla("block.note_block.harp"));
    private static final int[] WHITE = {0, 2, 4, 5, 7, 9, 11};

    public PianoBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        Vec3d h = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        double f;
        switch (state.get(FACING)) {
            case NORTH -> f = 1 - h.x;
            case SOUTH -> f = h.x;
            case EAST -> f = 1 - h.z;
            default -> f = h.z;
        }
        f = Math.max(0, Math.min(0.999, f));
        int idx = (int) (f * 7);
        int semi = WHITE[idx];
        if (player.isSneaking() && semi != 4 && semi != 11) semi += 1;          // sustenido
        int note = semi + (h.y >= 0.5 ? 12 : 0);                                // oitava
        float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
        if (world instanceof ServerWorld sw) {
            sw.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HARP, SoundCategory.RECORDS, 3.0f, pitch);
            sw.spawnParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 0, note / 24.0, 0, 0, 1.0);
            PianoSummon.onPianoPlayed(sw, pos, player);
        }
        return ActionResult.SUCCESS;
    }
}
