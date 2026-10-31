package com.linhagens.server;

import com.linhagens.item.ModItems;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/** Efeitos passivos das armas: Katana (velocidade), Desastre (golpe em área) e Lenhador (derrubar árvores). */
public final class WeaponEvents {
    private WeaponEvents() {}

    private static boolean inCleave = false;

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (blocked || inCleave || damageTaken <= 0) return;
            if (!(source.getAttacker() instanceof LivingEntity attacker) || source.getSource() != attacker) return;
            ItemStack held = attacker.getMainHandStack();

            if (held.isOf(ModItems.KATANA)) {
                attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0));
            } else if (held.isOf(ModItems.MACHADO_DESASTRE) && entity.getWorld() instanceof ServerWorld sw) {
                inCleave = true;
                try {
                    DamageSource src = attacker instanceof PlayerEntity p
                            ? attacker.getDamageSources().playerAttack(p)
                            : attacker.getDamageSources().mobAttack(attacker);
                    for (LivingEntity t : sw.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox().expand(2.5),
                            e -> e != attacker && e != entity && e.isAlive()
                                    && !(e instanceof TameableEntity tn && tn.getOwner() == attacker))) {
                        t.damage(src, damageTaken * 0.5f);
                        t.setOnFireFor(3);
                        sw.spawnParticles(ParticleTypes.CRIT, t.getX(), t.getBodyY(0.5), t.getZ(), 8, 0.3, 0.4, 0.3, 0.2);
                    }
                    sw.spawnParticles(ParticleTypes.LAVA, entity.getX(), entity.getBodyY(0.5), entity.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
                } finally {
                    inCleave = false;
                }
            }
        });

        // Machado de Lenhador: derruba a árvore inteira (agachado = só um tronco).
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity sp)) return;
            ItemStack tool = sp.getMainHandStack();
            if (!tool.isOf(ModItems.MACHADO_LENHADOR) || sp.isSneaking() || !state.isIn(BlockTags.LOGS)) return;

            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> seen = new HashSet<>();
            queue.add(pos);
            seen.add(pos);
            int broken = 0;
            while (!queue.isEmpty() && broken < 40) {
                BlockPos c = queue.poll();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos n = c.add(dx, dy, dz);
                            if (Math.abs(n.getX() - pos.getX()) > 6 || Math.abs(n.getZ() - pos.getZ()) > 6) continue;
                            if (!seen.add(n)) continue;
                            if (world.getBlockState(n).isIn(BlockTags.LOGS)) {
                                world.breakBlock(n, true, sp);
                                broken++;
                                tool.damage(1, sp, EquipmentSlot.MAINHAND);
                                queue.add(n);
                                if (tool.isEmpty() || broken >= 40) return;
                            }
                        }
                    }
                }
            }
        });
    }
}
