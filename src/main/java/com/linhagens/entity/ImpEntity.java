package com.linhagens.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.world.World;

/** Imp Infernal: demoninho do Nether, imune a fogo, incendeia quem acerta. */
public class ImpEntity extends BaseMonster {
    public ImpEntity(EntityType<? extends ImpEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity_createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 22.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    private static DefaultAttributeContainer.Builder HostileEntity_createHostileAttributes() {
        return net.minecraft.entity.mob.HostileEntity.createHostileAttributes();
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.setOnFireFor(4);
        }
        return hit;
    }
}
