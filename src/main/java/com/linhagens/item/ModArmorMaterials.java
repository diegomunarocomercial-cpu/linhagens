package com.linhagens.item;

import com.linhagens.Linhagens;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    /** Armadura da Aurora: quase tão forte quanto diamante, mas com bônus de conjunto (ver AuroraSetBonus). */
    public static final RegistryEntry<ArmorMaterial> AURORA = registerAurora();

    private static RegistryEntry<ArmorMaterial> registerAurora() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.BODY, 11);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Linhagens.id("aurora")));
        ArmorMaterial material = new ArmorMaterial(defense, 22, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
                () -> Ingredient.ofItems(ModItems.AURORA_SHARD), layers, 1.5f, 0.05f);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Linhagens.id("aurora"), material);
    }
}
