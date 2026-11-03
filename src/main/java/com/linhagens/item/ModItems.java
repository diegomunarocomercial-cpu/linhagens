package com.linhagens.item;

import com.linhagens.Linhagens;
import com.linhagens.block.ModBlocks;
import com.linhagens.entity.ModEntities;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
    private ModItems() {}

    private static AttributeModifiersComponent weaponAttrs(double damage, double speed, double knockbackResist) {
        AttributeModifiersComponent.Builder b = AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(Identifier.ofVanilla("base_attack_damage"), damage, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(Identifier.ofVanilla("base_attack_speed"), speed, Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND);
        if (knockbackResist > 0) {
            b.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE,
                    new EntityAttributeModifier(Linhagens.id("lenhador_ombros"), knockbackResist, Operation.ADD_VALUE),
                    AttributeModifierSlot.MAINHAND);
        }
        return b.build();
    }

    // Dano total = 1 (mão) + valor.  Velocidade total = 4 + valor.
    public static final Item KATANA = register("katana_espadachim", new KatanaItem(
            new Item.Settings().maxCount(1).rarity(Rarity.RARE).attributeModifiers(weaponAttrs(4.0, -1.8, 0))));
    public static final Item MACHADO_LENHADOR = register("machado_lenhador", new LenhadorAxeItem(
            new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON).attributeModifiers(weaponAttrs(7.0, -3.0, 0.2))));
    public static final Item MACHADO_DESASTRE = register("machado_desastre", new DesastreAxeItem(
            new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof().attributeModifiers(weaponAttrs(10.0, -3.1, 0))));

    // materiais e moeda
    public static final Item AURORA_SHARD = register("aurora_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item ESCAMA_DRAGAO = register("escama_dragao_gelo", new Item(new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item TEMPUS_COIN = register("tempus_coin_ouro", new Item(new Item.Settings().rarity(Rarity.RARE)));

    // armadura da aurora
    public static final Item AURORA_HELMET = armor("aurora_helmet", ModArmorMaterials.AURORA, ArmorItem.Type.HELMET, 30, Rarity.RARE);
    public static final Item AURORA_CHESTPLATE = armor("aurora_chestplate", ModArmorMaterials.AURORA, ArmorItem.Type.CHESTPLATE, 30, Rarity.RARE);
    public static final Item AURORA_LEGGINGS = armor("aurora_leggings", ModArmorMaterials.AURORA, ArmorItem.Type.LEGGINGS, 30, Rarity.RARE);
    public static final Item AURORA_BOOTS = armor("aurora_boots", ModArmorMaterials.AURORA, ArmorItem.Type.BOOTS, 30, Rarity.RARE);

    // roupas (3 peças cada)
    public static final Item ESPADACHIM_CHESTPLATE = armor("espadachim_chestplate", ModArmorMaterials.ESPADACHIM, ArmorItem.Type.CHESTPLATE, 22, Rarity.UNCOMMON);
    public static final Item ESPADACHIM_LEGGINGS = armor("espadachim_leggings", ModArmorMaterials.ESPADACHIM, ArmorItem.Type.LEGGINGS, 22, Rarity.UNCOMMON);
    public static final Item ESPADACHIM_BOOTS = armor("espadachim_boots", ModArmorMaterials.ESPADACHIM, ArmorItem.Type.BOOTS, 22, Rarity.UNCOMMON);
    public static final Item LENHADOR_CHESTPLATE = armor("lenhador_chestplate", ModArmorMaterials.LENHADOR, ArmorItem.Type.CHESTPLATE, 26, Rarity.UNCOMMON);
    public static final Item LENHADOR_LEGGINGS = armor("lenhador_leggings", ModArmorMaterials.LENHADOR, ArmorItem.Type.LEGGINGS, 26, Rarity.UNCOMMON);
    public static final Item LENHADOR_BOOTS = armor("lenhador_boots", ModArmorMaterials.LENHADOR, ArmorItem.Type.BOOTS, 26, Rarity.UNCOMMON);
    public static final Item PIGLIN_DESASTRE_CHESTPLATE = armor("piglin_desastre_chestplate", ModArmorMaterials.PIGLIN_DESASTRE, ArmorItem.Type.CHESTPLATE, 34, Rarity.EPIC);
    public static final Item PIGLIN_DESASTRE_LEGGINGS = armor("piglin_desastre_leggings", ModArmorMaterials.PIGLIN_DESASTRE, ArmorItem.Type.LEGGINGS, 34, Rarity.EPIC);
    public static final Item PIGLIN_DESASTRE_BOOTS = armor("piglin_desastre_boots", ModArmorMaterials.PIGLIN_DESASTRE, ArmorItem.Type.BOOTS, 34, Rarity.EPIC);
    public static final Item CULTO_HELMET = armor("culto_helmet", ModArmorMaterials.CULTO, ArmorItem.Type.HELMET, 24, Rarity.RARE);
    public static final Item CULTO_CHESTPLATE = armor("culto_chestplate", ModArmorMaterials.CULTO, ArmorItem.Type.CHESTPLATE, 24, Rarity.RARE);
    public static final Item CULTO_LEGGINGS = armor("culto_leggings", ModArmorMaterials.CULTO, ArmorItem.Type.LEGGINGS, 24, Rarity.RARE);

    // blocos
    public static final Item AURORA_CORE = register("aurora_core", new BlockItem(ModBlocks.AURORA_CORE, new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item BRACO_POCO = register("braco_poco", new BlockItem(ModBlocks.BRACO_POCO, new Item.Settings().rarity(Rarity.RARE)));
    public static final Item ICE_DRAGON_EGG = register("ice_dragon_egg", new BlockItem(ModBlocks.ICE_DRAGON_EGG, new Item.Settings().rarity(Rarity.EPIC)));
    public static final Item PIANO = register("piano", new BlockItem(ModBlocks.PIANO, new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item PORTA_GELO = register("porta_gelo", new TallBlockItem(ModBlocks.PORTA_GELO, new Item.Settings()));
    public static final Item AGUA_RUBRA = register("agua_rubra", new BlockItem(ModBlocks.AGUA_RUBRA, new Item.Settings()));

    // ovos de criar mobs
    public static final Item DEMONIO_SPAWN_EGG = egg("demonio_monstro_spawn_egg", new SpawnEggItem(ModEntities.DEMONIO, 0x6E0A0A, 0xFF7A00, new Item.Settings()));
    public static final Item OCULTISTA_SPAWN_EGG = egg("ocultista_diablos_spawn_egg", new SpawnEggItem(ModEntities.OCULTISTA, 0x8E0F1B, 0xE0B040, new Item.Settings()));
    public static final Item SAMUEL_SPAWN_EGG = egg("samuel_spawn_egg", new SpawnEggItem(ModEntities.SAMUEL, 0x7A4A22, 0xF2E6C8, new Item.Settings()));
    public static final Item CACADOR_SPAWN_EGG = egg("cacador_marcados_spawn_egg", new SpawnEggItem(ModEntities.CACADOR, 0x15121C, 0xC8C8D8, new Item.Settings()));
    public static final Item ICE_DRAGON_SPAWN_EGG = egg("ice_dragon_spawn_egg", new SpawnEggItem(ModEntities.ICE_DRAGON, 0x9FD8F5, 0xFFFFFF, new Item.Settings()));
    public static final Item ICE_DRAGON_BABY_SPAWN_EGG = egg("ice_dragon_baby_spawn_egg", new SpawnEggItem(ModEntities.ICE_DRAGON_BABY, 0xBFE6F8, 0x4A9BD0, new Item.Settings()));

    private static Item armor(String name, RegistryEntry<ArmorMaterial> mat, ArmorItem.Type type, int durFactor, Rarity rarity) {
        return register(name, new ArmorItem(mat, type, new Item.Settings().maxDamage(type.getMaxDamage(durFactor)).rarity(rarity)));
    }

    private static Item egg(String name, Item item) {
        return register(name, item);
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Linhagens.id(name), item);
    }

    public static void init() {
        // força o carregamento
    }
}
