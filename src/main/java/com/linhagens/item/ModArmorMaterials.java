package com.linhagens.item;

import com.linhagens.Linhagens;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    /** Armadura da Aurora: quase tão forte quanto diamante, com bônus de conjunto. */
    public static final RegistryEntry<ArmorMaterial> AURORA = make("aurora", 3, 6, 8, 3, 11, 22, 1.5f, 0.05f, true);
    /** Roupas leves (3 peças): Espadachim Inexperiente. */
    public static final RegistryEntry<ArmorMaterial> ESPADACHIM = make("espadachim", 0, 4, 6, 2, 9, 18, 0.0f, 0.0f, false);
    /** Roupa de Lenhador: resistente. */
    public static final RegistryEntry<ArmorMaterial> LENHADOR = make("lenhador", 0, 5, 6, 2, 10, 12, 0.0f, 0.10f, false);
    /** Roupa do Piglin do Desastre: pesada, de ouro. */
    public static final RegistryEntry<ArmorMaterial> PIGLIN_DESASTRE = make("piglin_desastre", 0, 6, 8, 3, 12, 25, 1.0f, 0.10f, false);
    /** Roupa do Culto Diablos. */
    public static final RegistryEntry<ArmorMaterial> CULTO = make("culto", 3, 4, 6, 0, 8, 20, 0.5f, 0.0f, false);

    private static RegistryEntry<ArmorMaterial> make(String name, int helmet, int legs, int chest, int boots, int body,
                                                     int ench, float tough, float kb, boolean shardRepair) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.BOOTS, boots);
        defense.put(ArmorItem.Type.LEGGINGS, legs);
        defense.put(ArmorItem.Type.CHESTPLATE, chest);
        defense.put(ArmorItem.Type.HELMET, helmet);
        defense.put(ArmorItem.Type.BODY, body);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Linhagens.id(name)));
        ArmorMaterial m = new ArmorMaterial(defense, ench, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                () -> shardRepair ? Ingredient.ofItems(ModItems.AURORA_SHARD) : Ingredient.ofItems(Items.LEATHER), layers, tough, kb);
        return Registry.registerReference(Registries.ARMOR_MATERIAL, Linhagens.id(name), m);
    }
}
