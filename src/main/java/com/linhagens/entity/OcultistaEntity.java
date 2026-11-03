package com.linhagens.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Ocultista do Culto Diablos: mago sombrio de capa vermelha.
 * Lança a "Maldição Escarlate" à distância (dano + Wither) e, ferido, invoca 2 Imps Infernais.
 */
public class OcultistaEntity extends BaseMonster {
    private static final DustParticleEffect BLOOD = new DustParticleEffect(new Vector3f(0.85f, 0.05f, 0.08f), 1.6f);
    private int castCooldown = 60;
    private boolean summoned = false;

    public OcultistaEntity(EntityType<? extends OcultistaEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BLAZE_ROD));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!(this.getWorld() instanceof ServerWorld w)) return;
        if (castCooldown > 0) castCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && castCooldown <= 0 && this.canSee(target) && this.distanceTo(target) <= 16.0) {
            castCooldown = 80;
            Vec3d from = this.getEyePos();
            Vec3d to = target.getEyePos();
            for (int i = 0; i <= 14; i++) {
                Vec3d p = from.lerp(to, i / 14.0);
                w.spawnParticles(BLOOD, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
                if (i % 4 == 0) w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
            target.damage(this.getDamageSources().indirectMagic(this, this), 6.0f);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
            w.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1f, 0.7f);
        }

        if (!summoned && this.getHealth() < this.getMaxHealth() * 0.5f) {
            summoned = true;
            DemonioMonstroEntity d = new DemonioMonstroEntity(ModEntities.DEMONIO, w);
            d.refreshPositionAndAngles(this.getX() + 1.5, this.getY(), this.getZ(), this.getYaw(), 0);
            w.spawnEntity(d);
            w.spawnParticles(ParticleTypes.LARGE_SMOKE, d.getX(), d.getY() + 1, d.getZ(), 20, 0.4, 0.8, 0.4, 0.02);
            w.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.HOSTILE, 1f, 0.8f);
        }
    }
}
