package com.linhagens.server;

import com.linhagens.item.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Bônus de conjunto: Aurora (4 peças) e as roupas (todas as peças de cada uma). */
public final class SetBonuses {
    private SetBonuses() {}

    private static boolean wears(ServerPlayerEntity p, Item head, Item chest, Item legs, Item feet) {
        return (head == null || p.getEquippedStack(EquipmentSlot.HEAD).isOf(head))
                && (chest == null || p.getEquippedStack(EquipmentSlot.CHEST).isOf(chest))
                && (legs == null || p.getEquippedStack(EquipmentSlot.LEGS).isOf(legs))
                && (feet == null || p.getEquippedStack(EquipmentSlot.FEET).isOf(feet));
    }

    private static void eff(ServerPlayerEntity p, RegistryEntry<StatusEffect> e, int amp) {
        p.addStatusEffect(new StatusEffectInstance(e, 60, amp, true, false, true));
    }

    public static void tick(ServerPlayerEntity p, int tick) {
        if (tick % 20 != 0) return;
        ServerWorld w = p.getServerWorld();

        if (wears(p, ModItems.AURORA_HELMET, ModItems.AURORA_CHESTPLATE, ModItems.AURORA_LEGGINGS, ModItems.AURORA_BOOTS)) {
            eff(p, StatusEffects.SPEED, 0);
            if (w.isNight()) {
                eff(p, StatusEffects.REGENERATION, 0);
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, false));
            }
            w.spawnParticles(ParticleTypes.WITCH, p.getX(), p.getBodyY(0.6), p.getZ(), 10, 0.4, 0.8, 0.4, 0.02);
        }
        // Espadachim Inexperiente: leve e veloz
        if (wears(p, null, ModItems.ESPADACHIM_CHESTPLATE, ModItems.ESPADACHIM_LEGGINGS, ModItems.ESPADACHIM_BOOTS)) {
            eff(p, StatusEffects.SPEED, 0);
            eff(p, StatusEffects.JUMP_BOOST, 0);
        }
        // Lenhador: braço forte e disposição
        if (wears(p, null, ModItems.LENHADOR_CHESTPLATE, ModItems.LENHADOR_LEGGINGS, ModItems.LENHADOR_BOOTS)) {
            eff(p, StatusEffects.HASTE, 0);
            eff(p, StatusEffects.RESISTANCE, 0);
        }
        // Piglin do Desastre: força bruta
        if (wears(p, null, ModItems.PIGLIN_DESASTRE_CHESTPLATE, ModItems.PIGLIN_DESASTRE_LEGGINGS, ModItems.PIGLIN_DESASTRE_BOOTS)) {
            eff(p, StatusEffects.STRENGTH, 0);
            eff(p, StatusEffects.FIRE_RESISTANCE, 0);
        }
        // Culto Diablos: enxerga no escuro e resiste ao fogo
        if (wears(p, ModItems.CULTO_HELMET, ModItems.CULTO_CHESTPLATE, ModItems.CULTO_LEGGINGS, null)) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, true, false, false));
            eff(p, StatusEffects.FIRE_RESISTANCE, 0);
            w.spawnParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getBodyY(0.5), p.getZ(), 4, 0.4, 0.8, 0.4, 0.0);
        }
    }
}
