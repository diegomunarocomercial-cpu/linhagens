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

    public static final Block BRACO_POCO = Registry.register(Registries.BLOCK, Linhagens.id("braco_poco"),
            new BracoPocoBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.PALE_GREEN)
                    .strength(2.0f, 6.0f)
                    .nonOpaque()
                    .sounds(BlockSoundGroup.BONE)));

    public static final Block ICE_DRAGON_EGG = Registry.register(Registries.BLOCK, Linhagens.id("ice_dragon_egg"),
            new IceDragonEggBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .strength(3.0f, 9.0f)
                    .nonOpaque()
                    .luminance(state -> 4)
                    .sounds(BlockSoundGroup.GLASS)));

    public static void init() {
    }
}
