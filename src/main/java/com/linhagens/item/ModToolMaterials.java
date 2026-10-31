package com.linhagens.item;

import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/** Materiais das 3 armas. O dano de ataque real é definido nos atributos de cada arma (ver ModItems). */
public enum ModToolMaterials implements ToolMaterial {
    KATANA(900, 6.0f, 4.0f, BlockTags.INCORRECT_FOR_IRON_TOOL, 18, () -> Ingredient.ofItems(Items.IRON_INGOT, Items.EMERALD)),
    LENHADOR(800, 9.0f, 4.0f, BlockTags.INCORRECT_FOR_IRON_TOOL, 12, () -> Ingredient.ofItems(Items.IRON_INGOT)),
    DESASTRE(1800, 8.0f, 10.0f, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 16, () -> Ingredient.ofItems(Items.GOLD_INGOT, Items.NETHERITE_INGOT));

    private final int durability;
    private final float speed;
    private final float damage;
    private final TagKey<Block> inverseTag;
    private final int enchantability;
    private final Supplier<Ingredient> repair;

    ModToolMaterials(int durability, float speed, float damage, TagKey<Block> inverseTag, int enchantability, Supplier<Ingredient> repair) {
        this.durability = durability;
        this.speed = speed;
        this.damage = damage;
        this.inverseTag = inverseTag;
        this.enchantability = enchantability;
        this.repair = repair;
    }

    @Override public int getDurability() { return durability; }
    @Override public float getMiningSpeedMultiplier() { return speed; }
    @Override public float getAttackDamage() { return damage; }
    @Override public TagKey<Block> getInverseTag() { return inverseTag; }
    @Override public int getEnchantability() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
