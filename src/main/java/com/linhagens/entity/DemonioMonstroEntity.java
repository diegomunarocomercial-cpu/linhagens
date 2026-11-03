package com.linhagens.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** Monstro Demônio: brutamontes do Reino Profano. Imune a fogo, incendeia quem acerta e pisoteia em área. */
public class DemonioMonstroEntity extends BaseMonster {
    private int stompCooldown = 60;

    public DemonioMonstroEntity(EntityType<? extends DemonioMonstroEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 30;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 70.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) living.setOnFireFor(5);
        return hit;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (stompCooldown > 0) stompCooldown--;
        LivingEntity t = this.getTarget();
        if (t != null && stompCooldown <= 0 && this.distanceTo(t) <= 4.5 && this.getWorld() instanceof ServerWorld w) {
            stompCooldown = 100;
            for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(4.5),
                    x -> x != this && !(x instanceof DemonioMonstroEntity) && x.isAlive())) {
                e.damage(this.getDamageSources().mobAttack(this), 6f);
                e.takeKnockback(1.0, this.getX() - e.getX(), this.getZ() - e.getZ());
                e.setOnFireFor(4);
            }
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI * 2 / 24;
                w.spawnParticles(ParticleTypes.FLAME, getX() + Math.cos(a) * 3, getY() + 0.2, getZ() + Math.sin(a) * 3, 2, 0.1, 0.1, 0.1, 0.03);
            }
            w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.HOSTILE, 1.5f, 0.5f);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RAVAGER_DEATH;
    }
}
