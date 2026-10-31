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
                .entries((ctx, entries) -> {
                    entries.add(ModItems.KATANA);
                    entries.add(ModItems.MACHADO_LENHADOR);
                    entries.add(ModItems.MACHADO_DESASTRE);
                    entries.add(ModItems.AURORA_SHARD);
                    entries.add(ModItems.AURORA_HELMET);
                    entries.add(ModItems.AURORA_CHESTPLATE);
                    entries.add(ModItems.AURORA_LEGGINGS);
                    entries.add(ModItems.AURORA_BOOTS);
                    entries.add(ModItems.AURORA_CORE);
                    entries.add(ModItems.IMP_SPAWN_EGG);
                    entries.add(ModItems.SHADOW_SPAWN_EGG);
                    entries.add(ModItems.GUARDIAN_SPAWN_EGG);
                })
                .build());
    }
}
