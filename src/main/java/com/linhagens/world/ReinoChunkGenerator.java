package com.linhagens.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

/**
 * Gerador do REINO PROFANO: terreno do mundo normal, mas tudo vira tons de vermelho
 * (areia vermelha, pedra de netherrack, troncos de crimson, folhas de wart; grama e água ganham cor pelo bioma).
 */
public class ReinoChunkGenerator extends NoiseChunkGenerator {
    public static final MapCodec<ReinoChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(g -> g.getSettings())
    ).apply(i, i.stable(ReinoChunkGenerator::new)));

    public ReinoChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        super.generateFeatures(world, chunk, structureAccessor);
        recolor(chunk);
        int cx = chunk.getPos().x, cz = chunk.getPos().z;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                try {
                    recolor(world.getChunk(cx + dx, cz + dz));
                } catch (Exception ignored) {
                    // chunk vizinho ainda indisponível: será tratado quando for gerado
                }
            }
        }
    }

    private static boolean needs(BlockState s) {
        return s.isOf(Blocks.STONE) || s.isOf(Blocks.ANDESITE) || s.isOf(Blocks.DIORITE) || s.isOf(Blocks.GRANITE)
                || s.isOf(Blocks.DIRT) || s.isOf(Blocks.COARSE_DIRT) || s.isOf(Blocks.GRAVEL)
                || s.isOf(Blocks.SAND) || s.isOf(Blocks.SANDSTONE) || s.isOf(Blocks.CLAY)
                || s.isIn(BlockTags.LOGS) || s.isIn(BlockTags.LEAVES);
    }

    private static BlockState map(BlockState s) {
        if (s.isOf(Blocks.STONE) || s.isOf(Blocks.ANDESITE) || s.isOf(Blocks.DIORITE) || s.isOf(Blocks.GRANITE)) return Blocks.NETHERRACK.getDefaultState();
        if (s.isOf(Blocks.DIRT) || s.isOf(Blocks.COARSE_DIRT) || s.isOf(Blocks.CLAY)) return Blocks.RED_TERRACOTTA.getDefaultState();
        if (s.isOf(Blocks.GRAVEL)) return Blocks.RED_NETHER_BRICKS.getDefaultState();
        if (s.isOf(Blocks.SAND)) return Blocks.RED_SAND.getDefaultState();
        if (s.isOf(Blocks.SANDSTONE)) return Blocks.RED_SANDSTONE.getDefaultState();
        if (s.isIn(BlockTags.LOGS)) {
            BlockState stem = Blocks.CRIMSON_STEM.getDefaultState();
            return s.contains(PillarBlock.AXIS) ? stem.with(PillarBlock.AXIS, s.get(PillarBlock.AXIS)) : stem;
        }
        if (s.isIn(BlockTags.LEAVES)) return Blocks.NETHER_WART_BLOCK.getDefaultState();
        return s;
    }

    private static void recolor(Chunk chunk) {
        for (ChunkSection section : chunk.getSectionArray()) {
            if (section == null || section.isEmpty() || !section.hasAny(ReinoChunkGenerator::needs)) continue;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState s = section.getBlockState(x, y, z);
                        if (!needs(s)) continue;
                        BlockState n = map(s);
                        if (n != s) section.setBlockState(x, y, z, n, false);
                    }
                }
            }
        }
    }
}
