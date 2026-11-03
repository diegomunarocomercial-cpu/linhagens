package com.linhagens.block;

import com.linhagens.Linhagens;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.minecraft.block.BlockSetType;

public final class ModBlockSetTypes {
    private ModBlockSetTypes() {}

    /** Porta de gelo: abre com a mão, como porta de madeira. */
    public static final BlockSetType GELO = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(Linhagens.id("gelo"));
}
