package com.linhagens.block;

import com.linhagens.Linhagens;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    private ModBlocks() {}

    public static final Block AURORA_CORE = Registry.register(Registries.BLOCK, Linhagens.id("aurora_core"),
            new AuroraCoreBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.PURPLE)
                    .strength(3.0f, 6.0f)
                    .requiresTool()
                    .luminance(state -> 15)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static void init() {
    }
}
