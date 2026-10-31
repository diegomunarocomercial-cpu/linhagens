package com.linhagens.server;

import com.linhagens.race.Race;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Habilidades ativas de cada raça (executadas no servidor). */
public final class Abilities {
    private Abilities() {}

    public static void cast(Race race, int slot, ServerPlayerEntity p, float power) {
        ServerWorld w = p.getServerWorld();
        switch (race) {
            case HUMANO -> {
                if (slot == 0) {
                    p.heal(8f * power);
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, (int) (100 * power), 0));
                    w.spawnParticles(ParticleTypes.HEART, p.getX(), p.getBodyY(0.8), p.getZ(), 8, 0.4, 0.4, 0.4, 0.02);
                    sound(w, p, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.5f);
                } else {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int) (200 * power), 1));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, (int) (200 * power), 0));
                    w.spawnParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getBodyY(0.5), p.getZ(), 20, 0.5, 0.8, 0.5, 0.05);
                    sound(w, p, SoundEvents.BLOCK_BEACON_POWER_SELECT, 1.2f);
                }
            }

            case DEMONIO -> {
                if (slot == 0) {
                    Vec3d look = p.getRotationVec(1f);
                    p.setVelocity(look.x * 1.8, Math.max(look.y * 1.4, 0.2), look.z * 1.8);
                    p.velocityModified = true;
                    p.fallDistance = 0;
                    Box box = p.getBoundingBox().stretch(look.multiply(7)).expand(1.0);
                    DamageSource src = p.getDamageSources().playerAttack(p);
                    for (LivingEntity t : w.getEntitiesByClass(LivingEntity.class, box, e -> e != p && e.isAlive())) {
                        t.damage(src, 5f * power);
                        t.setOnFireFor(4);
                    }
                    w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 30, 0.3, 0.5, 0.3, 0.08);
                    sound(w, p, SoundEvents.ENTITY_BLAZE_SHOOT, 0.8f);
                } else {
                    DamageSource src = p.getDamageSources().playerAttack(p);
                    for (LivingEntity t : nearby(w, p, 6)) {
                        t.damage(src, 6f * power);
                        t.setOnFireFor(6);
                    }
                    ring(w, p, 6, 28);
                    w.spawnParticles(ParticleTypes.LAVA, p.getX(), p.getY() + 0.5, p.getZ(), 15, 1.5, 0.3, 1.5, 0.1);
                    sound(w, p, SoundEvents.ENTITY_BLAZE_SHOOT, 0.5f);
                }
            }

            case ANJO -> {
                if (slot == 0) {
                    Vec3d look = p.getRotationVec(1f);
                    p.setVelocity(look.x * 0.6, 1.1, look.z * 0.6);
                    p.velocityModified = true;
                    p.fallDistance = 0;
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, (int) (200 * power), 0, true, false, false));
                    w.spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 25, 0.4, 0.1, 0.4, 0.05);
                    w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 0.5, p.getZ(), 15, 0.4, 0.3, 0.4, 0.05);
                    sound(w, p, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1.3f);
                } else {
                    DamageSource src = p.getDamageSources().playerAttack(p);
                    for (LivingEntity t : nearby(w, p, 7)) {
                        float dmg = 7f * power;
                        boolean evil = t.getType().isIn(EntityTypeTags.UNDEAD)
                                || (t instanceof ServerPlayerEntity sp && RaceManager.get(sp).race() == Race.DEMONIO);
                        if (evil) dmg *= 1.5f;
                        t.damage(src, dmg);
                        t.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
                        w.spawnParticles(ParticleTypes.END_ROD, t.getX(), t.getY() + 1, t.getZ(), 15, 0.2, 1.0, 0.2, 0.02);
                    }
                    ring(w, p, 7, 32);
                    sound(w, p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.5f);
                }
            }

            case PIGLIN -> {
                if (slot == 0) {
                    Box box = p.getBoundingBox().expand(12);
                    for (ServerPlayerEntity a : w.getEntitiesByClass(ServerPlayerEntity.class, box, e -> true)) {
                        a.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, (int) (300 * power), 1));
                        a.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 0));
                    }
                    ring(w, p, 3, 16);
                    w.spawnParticles(ParticleTypes.ANGRY_VILLAGER, p.getX(), p.getBodyY(1.0), p.getZ(), 10, 0.5, 0.3, 0.5, 0.0);
                    sound(w, p, SoundEvents.ENTITY_PIGLIN_ANGRY, 0.9f);
                } else {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 1));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int) (200 * power), 0));
                    p.extinguish();
                    w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getBodyY(0.5), p.getZ(), 25, 0.4, 0.8, 0.4, 0.2);
                    sound(w, p, SoundEvents.ENTITY_PIGLIN_CELEBRATE, 1.0f);
                }
            }
        }
    }

    private static List<LivingEntity> nearby(ServerWorld w, ServerPlayerEntity p, double radius) {
        double r2 = radius * radius;
        return w.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(radius),
                e -> e != p && e.isAlive() && e.squaredDistanceTo(p) <= r2);
    }

    private static void ring(ServerWorld w, ServerPlayerEntity p, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double a = 2 * Math.PI * i / points;
            double x = p.getX() + Math.cos(a) * radius;
            double z = p.getZ() + Math.sin(a) * radius;
            var particle = p.getServerWorld() == w && RaceManager.get(p).race() == Race.DEMONIO
                    ? ParticleTypes.FLAME : ParticleTypes.END_ROD;
            w.spawnParticles(particle, x, p.getY() + 0.2, z, 2, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private static void sound(ServerWorld w, ServerPlayerEntity p, SoundEvent e, float pitch) {
        w.playSound(null, p.getX(), p.getY(), p.getZ(), e, SoundCategory.PLAYERS, 1f, pitch);
    }
}
