package com.linhagens.entity;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Caçador de Marcados (boss raríssimo, só aparece ao lado de um piano).
 * Marca a presa (brilho + dano extra), se teletransporta atrás dela, dispara estilhaços e executa quem está ferido e marcado.
 */
public class CacadorMarcadosEntity extends BaseMonster {
    private static final DustParticleEffect MARK = new DustParticleEffect(new Vector3f(0.55f, 0.1f, 0.9f), 1.8f);
    private final ServerBossBar bossBar = new ServerBossBar(Text.literal("Caçador de Marcados"), BossBar.Color.PURPLE, BossBar.Style.NOTCHED_6);
    private UUID marked = null;
    private int markTicks = 0;
    private int markCd = 60, teleCd = 100, shardCd = 70;

    public CacadorMarcadosEntity(EntityType<? extends CacadorMarcadosEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 400;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 260.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living && target.getUuid().equals(marked)) {
            living.damage(this.getDamageSources().magic(), 5f);                     // dano extra na presa marcada
        }
        return hit;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!(this.getWorld() instanceof ServerWorld w)) return;
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (markCd > 0) markCd--;
        if (teleCd > 0) teleCd--;
        if (shardCd > 0) shardCd--;
        if (markTicks > 0 && --markTicks == 0) marked = null;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        double dist = this.distanceTo(target);

        // Marcar a presa
        if (markCd <= 0 && dist <= 24 && this.canSee(target)) {
            markCd = 240;
            marked = target.getUuid();
            markTicks = 400;
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 400, 0));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0));
            w.spawnParticles(MARK, target.getX(), target.getBodyY(0.6), target.getZ(), 25, 0.5, 0.8, 0.5, 0.0);
            w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, SoundCategory.HOSTILE, 1.5f, 1.4f);
        }
        // Teletransporte para trás da presa
        if (teleCd <= 0 && dist > 8) {
            teleCd = 140;
            Vec3d back = target.getRotationVec(1f).multiply(-2.2);
            double nx = target.getX() + back.x, nz = target.getZ() + back.z;
            w.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 30, 0.4, 0.8, 0.4, 0.3);
            this.requestTeleport(nx, target.getY(), nz);
            w.spawnParticles(ParticleTypes.PORTAL, nx, target.getY() + 1, nz, 30, 0.4, 0.8, 0.4, 0.3);
            w.playSound(null, nx, target.getY(), nz, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1f, 0.7f);
        }
        // Estilhaços à distância
        if (shardCd <= 0 && dist > 4 && dist <= 20 && this.canSee(target)) {
            shardCd = 90;
            Vec3d from = this.getEyePos(), to = target.getEyePos();
            for (int i = 0; i <= 16; i++) {
                Vec3d p = from.lerp(to, i / 16.0);
                w.spawnParticles(MARK, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            }
            float dmg = target.getUuid().equals(marked) ? 9f : 5f;
            // Execução: presa marcada e ferida
            if (target.getUuid().equals(marked) && target.getHealth() < target.getMaxHealth() * 0.3f) dmg = 16f;
            target.damage(this.getDamageSources().indirectMagic(this, this), dmg);
            w.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1f, 1.3f);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        bossBar.clearPlayers();
    }
}
