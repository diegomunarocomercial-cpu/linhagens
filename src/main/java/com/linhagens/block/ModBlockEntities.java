package com.linhagens.block;

import com.linhagens.Linhagens;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final BlockEntityType<AuroraCoreBlockEntity> AURORA_CORE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, Linhagens.id("aurora_core"),
            FabricBlockEntityTypeBuilder.create(AuroraCoreBlockEntity::new, ModBlocks.AURORA_CORE).build());

    public static void init() {
    }
}
