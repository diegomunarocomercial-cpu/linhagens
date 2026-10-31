package com.linhagens.server;

import com.linhagens.item.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Bênção da Aurora: com as 4 peças da armadura, ganha Velocidade, regeneração à noite e deixa um rastro de brilho. */
public final class AuroraSetBonus {
    private AuroraSetBonus() {}

    public static boolean hasFullSet(ServerPlayerEntity p) {
        return p.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.AURORA_HELMET)
                && p.getEquippedStack(EquipmentSlot.CHEST).isOf(ModItems.AURORA_CHESTPLATE)
                && p.getEquippedStack(EquipmentSlot.LEGS).isOf(ModItems.AURORA_LEGGINGS)
                && p.getEquippedStack(EquipmentSlot.FEET).isOf(ModItems.AURORA_BOOTS);
    }

    public static void tick(ServerPlayerEntity p, int tick) {
        if (tick % 20 != 0 || !hasFullSet(p)) return;
        ServerWorld w = p.getServerWorld();
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false, true));
        if (w.isNight()) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 0, true, false, true));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, false));
        }
        w.spawnParticles(ParticleTypes.WITCH, p.getX(), p.getBodyY(0.6), p.getZ(), 10, 0.4, 0.8, 0.4, 0.02);
    }
}
