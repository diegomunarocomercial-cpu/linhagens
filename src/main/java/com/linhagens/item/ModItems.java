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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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
            new Item.Settings().maxCount(1).rarity(Rarity.RARE).attributeModifiers(weaponAttrs(4.0, -1.8, 0))));      // 5 dano, 2.2 vel
    public static final Item MACHADO_LENHADOR = register("machado_lenhador", new LenhadorAxeItem(
            new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON).attributeModifiers(weaponAttrs(7.0, -3.0, 0.2)))); // 8 dano, 1.0 vel
    public static final Item MACHADO_DESASTRE = register("machado_desastre", new DesastreAxeItem(
            new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof().attributeModifiers(weaponAttrs(10.0, -3.1, 0)))); // 11 dano, 0.9 vel

    public static final Item AURORA_SHARD = register("aurora_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

    public static final Item AURORA_HELMET = register("aurora_helmet", new ArmorItem(ModArmorMaterials.AURORA, ArmorItem.Type.HELMET,
            new Item.Settings().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(30)).rarity(Rarity.RARE)));
    public static final Item AURORA_CHESTPLATE = register("aurora_chestplate", new ArmorItem(ModArmorMaterials.AURORA, ArmorItem.Type.CHESTPLATE,
            new Item.Settings().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(30)).rarity(Rarity.RARE)));
    public static final Item AURORA_LEGGINGS = register("aurora_leggings", new ArmorItem(ModArmorMaterials.AURORA, ArmorItem.Type.LEGGINGS,
            new Item.Settings().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(30)).rarity(Rarity.RARE)));
    public static final Item AURORA_BOOTS = register("aurora_boots", new ArmorItem(ModArmorMaterials.AURORA, ArmorItem.Type.BOOTS,
            new Item.Settings().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(30)).rarity(Rarity.RARE)));

    public static final Item AURORA_CORE = register("aurora_core", new BlockItem(ModBlocks.AURORA_CORE, new Item.Settings().rarity(Rarity.EPIC)));

    public static final Item IMP_SPAWN_EGG = register("imp_infernal_spawn_egg",
            new SpawnEggItem(ModEntities.IMP, 0xB02A2A, 0xFFB000, new Item.Settings()));
    public static final Item SHADOW_SPAWN_EGG = register("espadachim_sombrio_spawn_egg",
            new SpawnEggItem(ModEntities.SHADOW_SWORDSMAN, 0x1B1B22, 0x3CFF7A, new Item.Settings()));
    public static final Item GUARDIAN_SPAWN_EGG = register("aurora_guardian_spawn_egg",
            new SpawnEggItem(ModEntities.AURORA_GUARDIAN, 0x6B3FA0, 0xE0B0FF, new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Linhagens.id(name), item);
    }

    public static void init() {
        // força o carregamento
    }
}
