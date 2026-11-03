package com.linhagens.block;

import com.linhagens.Linhagens;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    private ModBlocks() {}

    public static final Block AURORA_CORE = reg("aurora_core",
            new AuroraCoreBlock(AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(3.0f, 6.0f)
                    .requiresTool().luminance(s -> 15).sounds(BlockSoundGroup.AMETHYST_BLOCK)));

    public static final Block BRACO_POCO = reg("braco_poco",
            new BracoPocoBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_GREEN).strength(2.0f, 6.0f)
                    .nonOpaque().sounds(BlockSoundGroup.BONE)));

    public static final Block ICE_DRAGON_EGG = reg("ice_dragon_egg",
            new IceDragonEggBlock(AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(3.0f, 9.0f)
                    .nonOpaque().luminance(s -> 6).sounds(BlockSoundGroup.GLASS)));

    public static final Block AGUA_RUBRA = reg("agua_rubra",
            new AguaRubraBlock(AbstractBlock.Settings.create().mapColor(MapColor.RED).noCollision().replaceable()
                    .dropsNothing().nonOpaque().strength(100.0f).luminance(s -> 2).pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block PIANO = reg("piano",
            new PianoBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(2.0f, 6.0f)
                    .nonOpaque().sounds(BlockSoundGroup.WOOD)));

    public static final Block PORTA_GELO = reg("porta_gelo",
            new DoorBlock(ModBlockSetTypes.GELO, AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE)
                    .strength(4.0f, 6.0f).nonOpaque().sounds(BlockSoundGroup.GLASS).pistonBehavior(PistonBehavior.DESTROY)));

    private static Block reg(String name, Block b) {
        return Registry.register(Registries.BLOCK, Linhagens.id(name), b);
    }

    public static void init() {
    }
}
