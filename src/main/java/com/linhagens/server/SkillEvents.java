package com.linhagens.server;

import com.linhagens.race.Perk;
import com.linhagens.race.Race;
import com.linhagens.race.RaceData;
import com.linhagens.race.SkillEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Efeitos de combate das habilidades da árvore (roubo de vida, fogo, golpe sagrado, segunda chance). */
public final class SkillEvents {
    private SkillEvents() {}

    private static boolean busy = false;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (blocked || busy || damageTaken <= 0) return;
            if (!(source.getAttacker() instanceof ServerPlayerEntity p) || source.getSource() != p) return;
            RaceData d = RaceManager.get(p);
            if (d.race() == null || d.skills().isEmpty()) return;
            SkillEffects eff = SkillEffects.of(d);

            if (eff.has(Perk.LIFESTEAL)) p.heal(damageTaken * 0.15f);
            if (eff.has(Perk.FLAME_STRIKE)) entity.setOnFireFor(4);
            if (eff.has(Perk.HOLY_STRIKE) && isEvil(entity)) {
                busy = true;
                try {
                    entity.damage(p.getDamageSources().playerAttack(p), 4f);
                    if (entity.getWorld() instanceof ServerWorld sw) {
                        sw.spawnParticles(ParticleTypes.END_ROD, entity.getX(), entity.getBodyY(0.6), entity.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
                    }
                } finally {
                    busy = false;
                }
            }
        });

        // Segunda Chance (Humano): sobrevive a um golpe fatal.
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity p)) return true;
            RaceData d = RaceManager.get(p);
            if (d.race() == null || !SkillEffects.of(d).has(Perk.SECOND_WIND)) return true;
            RaceManager.Runtime rt = RaceManager.rt(p);
            long now = p.getServerWorld().getTime();
            if (now < rt.secondWindReady) return true;
            rt.secondWindReady = now + 6000;
            p.setHealth(6f);
            p.clearStatusEffects();
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1));
            p.getServerWorld().spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getBodyY(0.5), p.getZ(), 40, 0.5, 0.8, 0.5, 0.3);
            p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1f, 1f);
            p.sendMessage(Text.literal("Segunda Chance!").formatted(Formatting.GOLD), true);
            return false;
        });
    }

    private static boolean isEvil(LivingEntity e) {
        return e.getType().isIn(EntityTypeTags.UNDEAD)
                || (e instanceof ServerPlayerEntity sp && RaceManager.get(sp).race() == Race.DEMONIO);
    }
}
