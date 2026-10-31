package com.linhagens.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Desenha a AURORA: 3 faixas ondulantes de partículas roxas (com um toque de verde/ciano) a ~35 blocos acima do núcleo.
 * Roda só no cliente, e só quando há um jogador por perto (até 180 blocos), então é visível de longe.
 */
public class AuroraCoreBlockEntity extends BlockEntity {
    private static final DustParticleEffect GREEN = new DustParticleEffect(new Vector3f(0.25f, 1.0f, 0.6f), 3.0f);
    private static final DustParticleEffect VIOLET = new DustParticleEffect(new Vector3f(0.65f, 0.25f, 1.0f), 3.0f);

    public AuroraCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURORA_CORE, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, AuroraCoreBlockEntity be) {
        if (world.getClosestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, false) == null) return;

        Random rnd = world.random;
        long time = world.getTime();
        int perRibbon = world.isDay() ? 4 : 9;           // mais intensa à noite
        double cx = pos.getX() + 0.5, cz = pos.getZ() + 0.5;
        double baseY = pos.getY() + 34;

        for (int ribbon = 0; ribbon < 3; ribbon++) {
            double angle = ribbon * (Math.PI / 3.0) + 0.4;
            double cos = Math.cos(angle), sin = Math.sin(angle);
            for (int i = 0; i < perRibbon; i++) {
                double s = (rnd.nextDouble() * 2 - 1) * 75;                                   // posição ao longo da faixa
                double wave = 12 * Math.sin(s * 0.055 + time * 0.012 + ribbon * 2.1)          // ondulação lateral
                        + 5 * Math.sin(s * 0.13 - time * 0.02);
                double x = cx + cos * s - sin * wave;
                double z = cz + sin * s + cos * wave;
                double curtain = rnd.nextDouble() * 16;                                       // altura da "cortina"
                double y = baseY + 5 * Math.sin(s * 0.09 + time * 0.018) + curtain;

                ParticleEffect effect;
                float roll = rnd.nextFloat();
                if (roll < 0.55f) effect = ParticleTypes.DRAGON_BREATH;
                else if (roll < 0.75f) effect = ParticleTypes.WITCH;
                else if (roll < 0.9f) effect = VIOLET;
                else effect = curtain < 5 ? GREEN : ParticleTypes.END_ROD;

                world.addParticle(effect, true, x, y, z,
                        MathHelper.sin((float) (time * 0.05 + s)) * 0.01, 0.005, MathHelper.cos((float) (time * 0.05 + s)) * 0.01);
            }
        }
    }
}
