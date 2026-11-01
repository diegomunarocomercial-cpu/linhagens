package com.linhagens.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Desenha a AURORA BOREAL: 5 cortinas ondulantes enormes (até ~150 blocos de comprimento) a ~40 blocos de altura.
 * Cada cortina muda de cor com a altura: verde-água embaixo, ciano e violeta no meio, rosa no topo.
 * Roda só no cliente, e só quando há um jogador por perto (até 220 blocos), então é visível de longe.
 */
public class AuroraCoreBlockEntity extends BlockEntity {
    // cores (verde -> ciano -> violeta -> rosa) como pares "de -> para" para a partícula desbotar suavemente
    private static final Vector3f GREEN = new Vector3f(0.20f, 1.00f, 0.55f);
    private static final Vector3f TEAL = new Vector3f(0.15f, 0.90f, 0.95f);
    private static final Vector3f VIOLET = new Vector3f(0.60f, 0.30f, 1.00f);
    private static final Vector3f PINK = new Vector3f(1.00f, 0.35f, 0.85f);

    private static final ParticleEffect[] LOW = {
            new DustColorTransitionParticleEffect(GREEN, TEAL, 4.0f),
            new DustColorTransitionParticleEffect(TEAL, GREEN, 3.5f)};
    private static final ParticleEffect[] MID = {
            new DustColorTransitionParticleEffect(TEAL, VIOLET, 4.0f),
            new DustColorTransitionParticleEffect(VIOLET, TEAL, 3.5f)};
    private static final ParticleEffect[] HIGH = {
            new DustColorTransitionParticleEffect(VIOLET, PINK, 4.0f),
            new DustColorTransitionParticleEffect(PINK, VIOLET, 3.5f)};
    private static final ParticleEffect SPARK = new DustParticleEffect(new Vector3f(0.9f, 1.0f, 1.0f), 1.5f);

    public AuroraCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AURORA_CORE, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, AuroraCoreBlockEntity be) {
        if (world.getClosestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 220, false) == null) return;

        Random rnd = world.random;
        double time = world.getTime();
        int perRibbon = world.isDay() ? 6 : 16;           // bem mais intensa à noite
        double cx = pos.getX() + 0.5, cz = pos.getZ() + 0.5;
        double baseY = Math.max(pos.getY() + 38, 95);      // sempre alta no céu, mesmo se o núcleo estiver enterrado

        for (int ribbon = 0; ribbon < 5; ribbon++) {
            double angle = ribbon * (Math.PI / 5.0) + 0.3;
            double cos = Math.cos(angle), sin = Math.sin(angle);
            double offset = (ribbon - 2) * 22;             // cortinas paralelas, afastadas entre si
            for (int i = 0; i < perRibbon; i++) {
                double s = (rnd.nextDouble() * 2 - 1) * 90;
                double wave = 14 * Math.sin(s * 0.045 + time * 0.010 + ribbon * 1.7)
                        + 6 * Math.sin(s * 0.11 - time * 0.017 + ribbon)
                        + offset;
                double x = cx + cos * s - sin * wave;
                double z = cz + sin * s + cos * wave;
                double h = rnd.nextDouble();                                   // 0 = base da cortina, 1 = topo
                double y = baseY + 6 * Math.sin(s * 0.08 + time * 0.015 + ribbon) + h * 26;

                ParticleEffect[] set = h < 0.35 ? LOW : (h < 0.72 ? MID : HIGH);
                ParticleEffect effect = set[rnd.nextInt(set.length)];
                if (rnd.nextFloat() < 0.06f) effect = SPARK;
                else if (rnd.nextFloat() < 0.04f) effect = ParticleTypes.END_ROD;

                world.addParticle(effect, true, x, y, z,
                        Math.sin(time * 0.03 + s * 0.05) * 0.02, 0.012, Math.cos(time * 0.03 + s * 0.05) * 0.02);
            }
        }
    }
}
