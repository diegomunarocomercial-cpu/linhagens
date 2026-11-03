package com.linhagens.quest;

import com.linhagens.block.ModBlocks;
import com.linhagens.entity.CacadorMarcadosEntity;
import com.linhagens.entity.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** O Caçador de Marcados só aparece ao lado de um piano, e com chance baixíssima. */
public final class PianoSummon {
    private PianoSummon() {}

    /** Chance ao tocar uma nota: 1 em 400. */
    public static void onPianoPlayed(ServerWorld w, BlockPos pianoPos, PlayerEntity player) {
        if (w.random.nextInt(400) == 0) trySpawn(w, pianoPos);
    }

    /** Chance passiva: a cada segundo perto de um piano, de noite, 1 em 12000. */
    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            ServerWorld w = p.getServerWorld();
            if (!w.isNight() || w.random.nextInt(12000) != 0) continue;
            BlockPos piano = findPiano(w, p.getBlockPos(), 6);
            if (piano != null) trySpawn(w, piano);
        }
    }

    private static BlockPos findPiano(ServerWorld w, BlockPos center, int r) {
        for (BlockPos q : BlockPos.iterate(center.add(-r, -3, -r), center.add(r, 3, r))) {
            if (w.isChunkLoaded(q) && w.getBlockState(q).isOf(ModBlocks.PIANO)) return q.toImmutable();
        }
        return null;
    }

    private static void trySpawn(ServerWorld w, BlockPos piano) {
        if (!w.getEntitiesByClass(CacadorMarcadosEntity.class, new net.minecraft.util.math.Box(piano).expand(64), e -> e.isAlive()).isEmpty()) return;
        for (int attempt = 0; attempt < 30; attempt++) {
            int dx = w.random.nextInt(9) - 4, dz = w.random.nextInt(9) - 4;
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
            BlockPos p = piano.add(dx, 0, dz);
            if (!w.getBlockState(p).isAir() || !w.getBlockState(p.up()).isAir() || !w.getBlockState(p.down()).isSolid()) continue;
            CacadorMarcadosEntity c = new CacadorMarcadosEntity(ModEntities.CACADOR, w);
            c.refreshPositionAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, w.random.nextFloat() * 360f, 0);
            c.setPersistent();
            w.spawnEntity(c);
            w.playSound(null, p, SoundEvents.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 2f, 1.2f);
            for (ServerPlayerEntity pl : w.getPlayers(x -> x.squaredDistanceTo(piano.getX(), piano.getY(), piano.getZ()) < 40 * 40)) {
                pl.sendMessage(Text.literal("Uma presença sombria surge ao lado do piano... O Caçador de Marcados chegou.")
                        .formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), false);
            }
            return;
        }
    }
}
