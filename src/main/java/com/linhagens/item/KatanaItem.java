package com.linhagens.item;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Katana do Espadachim Inexperiente.
 * Rápida e leve: dano 5, velocidade 2.2 (a mais rápida das 3). Cada golpe dá Velocidade por 2s.
 * Botão direito: "Iaijutsu" - avanço relâmpago que corta quem estiver no caminho (recarga 8s).
 */
public class KatanaItem extends SwordItem {
    public KatanaItem(Item.Settings settings) {
        super(ModToolMaterials.KATANA, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);

        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d look = player.getRotationVec(1f);
            player.setVelocity(look.x * 1.9, Math.max(look.y * 1.2, 0.12), look.z * 1.9);
            player.velocityModified = true;
            player.fallDistance = 0;

            Box box = player.getBoundingBox().stretch(look.multiply(7)).expand(1.0);
            DamageSource src = player.getDamageSources().playerAttack(player);
            for (LivingEntity t : sw.getEntitiesByClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                t.damage(src, 6f);
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, t.getX(), t.getBodyY(0.5), t.getZ(), 1, 0, 0, 0, 0);
            }
            sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getBodyY(0.5), player.getZ(), 25, 0.4, 0.5, 0.4, 0.1);
            sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8f, 1.4f);
            stack.damage(2, player, LivingEntityHand.slot(hand));
            player.getItemCooldownManager().set(this, 160);
        }
        player.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Leve e veloz. Cada golpe dá Velocidade.").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Botão direito: Iaijutsu (avanço cortante, 8s)").formatted(Formatting.DARK_GREEN));
    }
}
