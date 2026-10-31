package com.linhagens;

import com.linhagens.block.ModBlockEntities;
import com.linhagens.block.ModBlocks;
import com.linhagens.entity.ModEntities;
import com.linhagens.item.ModItemGroup;
import com.linhagens.item.ModItems;
import com.linhagens.net.ModNetworking;
import com.linhagens.server.LinhagemCommand;
import com.linhagens.server.ModAttachments;
import com.linhagens.server.RaceEvents;
import com.linhagens.server.WeaponEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

/** Ponto de entrada comum (servidor + cliente). */
public class Linhagens implements ModInitializer {
    public static final String MODID = "linhagens";

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    @Override
    public void onInitialize() {
        // Conteúdo
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModItems.init();
        ModItemGroup.init();

        // Sistema de raças
        ModAttachments.init();
        ModNetworking.registerPayloads();
        ModNetworking.registerServerReceivers();
        RaceEvents.register();
        WeaponEvents.register();
        LinhagemCommand.register();
    }
}
