package com.linhagens.world;

import com.linhagens.Linhagens;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

/**
 * Reino Profano: dimensão (definida em data/linhagens/dimension) que usa o bioma "the_void" recolorido aqui:
 * céu, neblina, água, grama e folhagem em tons de vermelho. (O bioma the_void só é usado nesta dimensão.)
 */
public final class ModWorld {
    private ModWorld() {}

    public static void init() {
        Registry.register(Registries.CHUNK_GENERATOR, Linhagens.id("reino_profano"), ReinoChunkGenerator.CODEC);

        BiomeModifications.create(Linhagens.id("reino_profano_cores"))
                .add(ModificationPhase.POST_PROCESSING, BiomeSelectors.includeByKey(BiomeKeys.THE_VOID), ctx -> {
                    ctx.getEffects().setSkyColor(0xC2181F);
                    ctx.getEffects().setFogColor(0x8A1215);
                    ctx.getEffects().setWaterColor(0x9A1414);
                    ctx.getEffects().setWaterFogColor(0x4A0A0A);
                    ctx.getEffects().setGrassColor(0xA3141A);
                    ctx.getEffects().setFoliageColor(0xB8222A);
                    ctx.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_PLAINS);
                    ctx.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_PLAIN);
                    ctx.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_PLAINS);
                    ctx.getGenerationSettings().addCarver(ConfiguredCarvers.CAVE);
                    ctx.getGenerationSettings().addCarver(ConfiguredCarvers.CANYON);
                });
    }
}
