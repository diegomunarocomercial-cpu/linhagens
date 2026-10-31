package com.linhagens.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.util.math.random.Random;
import com.linhagens.item.ModItems;

/** Espadachim Sombrio: só aparece no escuro, rápido, empunha a Katana (pequena chance de largá-la). */
public class ShadowSwordsmanEntity extends BaseMonster {
    public ShadowSwordsmanEntity(EntityType<? extends ShadowSwordsmanEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 28.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.33)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KATANA));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f); // a katana cai pela tabela de loot (3%)
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
        if (spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION) {
            return world.getLightLevel(this.getBlockPos()) <= 7 && super.canSpawn(world, spawnReason);
        }
        return super.canSpawn(world, spawnReason);
    }
}
