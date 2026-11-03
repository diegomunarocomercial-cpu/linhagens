package com.linhagens.item;

import com.linhagens.Linhagens;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

/** A aba "Linhagens" no menu do modo criativo. */
public final class ModItemGroup {
    private ModItemGroup() {}

    public static final RegistryKey<ItemGroup> KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Linhagens.id("linhagens"));

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, KEY, FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModItems.KATANA))
                .displayName(Text.translatable("itemGroup.linhagens"))
                .entries((ctx, e) -> {
                    e.add(ModItems.KATANA); e.add(ModItems.MACHADO_LENHADOR); e.add(ModItems.MACHADO_DESASTRE);
                    e.add(ModItems.AURORA_SHARD); e.add(ModItems.ESCAMA_DRAGAO); e.add(ModItems.TEMPUS_COIN);
                    e.add(ModItems.AURORA_HELMET); e.add(ModItems.AURORA_CHESTPLATE); e.add(ModItems.AURORA_LEGGINGS); e.add(ModItems.AURORA_BOOTS);
                    e.add(ModItems.ESPADACHIM_CHESTPLATE); e.add(ModItems.ESPADACHIM_LEGGINGS); e.add(ModItems.ESPADACHIM_BOOTS);
                    e.add(ModItems.LENHADOR_CHESTPLATE); e.add(ModItems.LENHADOR_LEGGINGS); e.add(ModItems.LENHADOR_BOOTS);
                    e.add(ModItems.PIGLIN_DESASTRE_CHESTPLATE); e.add(ModItems.PIGLIN_DESASTRE_LEGGINGS); e.add(ModItems.PIGLIN_DESASTRE_BOOTS);
                    e.add(ModItems.CULTO_HELMET); e.add(ModItems.CULTO_CHESTPLATE); e.add(ModItems.CULTO_LEGGINGS);
                    e.add(ModItems.AURORA_CORE); e.add(ModItems.BRACO_POCO); e.add(ModItems.ICE_DRAGON_EGG);
                    e.add(ModItems.PIANO); e.add(ModItems.PORTA_GELO); e.add(ModItems.AGUA_RUBRA);
                    e.add(ModItems.DEMONIO_SPAWN_EGG); e.add(ModItems.OCULTISTA_SPAWN_EGG); e.add(ModItems.SAMUEL_SPAWN_EGG);
                    e.add(ModItems.CACADOR_SPAWN_EGG); e.add(ModItems.ICE_DRAGON_SPAWN_EGG); e.add(ModItems.ICE_DRAGON_BABY_SPAWN_EGG);
                })
                .build());
    }
}
