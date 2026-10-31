package com.linhagens.item;

import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
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
import org.joml.Vector3f;

/**
 * Machado do Desastre.
 * Brutal e lento: dano 11, velocidade 0.9. Cada golpe também racha os inimigos em volta (metade do dano + fogo).
 * Botão direito: "Cataclismo" - pancada no chão em cone (12 de dano + arremesso), recarga 15s.
 * Preço do poder: custa 2 corações do usuário (não usa se a vida estiver baixa).
 */
public class DesastreAxeItem extends AxeItem {
    public DesastreAxeItem(Item.Settings settings) {
        super(ModToolMaterials.DESASTRE, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        if (player.getHealth() <= 6f) {
            if (!world.isClient) player.sendMessage(Text.literal("Sangue insuficiente para o Cataclismo.").formatted(Formatting.DARK_RED), true);
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient && world instanceof ServerWorld sw) {
            player.damage(player.getDamageSources().magic(), 4f); // o preço do desastre

            Vec3d look = player.getRotationVec(1f);
            Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();
            Vec3d origin = player.getPos();
            DamageSource src = player.getDamageSources().playerAttack(player);
            Box box = player.getBoundingBox().expand(5.5, 1.5, 5.5);
            for (LivingEntity t : sw.getEntitiesByClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                Vec3d to = t.getPos().subtract(origin);
                Vec3d toFlat = new Vec3d(to.x, 0, to.z);
                if (toFlat.length() < 0.001 || toFlat.length() > 5.5) continue;
                if (toFlat.normalize().dotProduct(flat) < 0.35) continue; // só na frente (cone)
                t.damage(src, 12f);
                t.takeKnockback(1.2, -flat.x, -flat.z);
                t.setOnFireFor(3);
            }

            for (int i = 1; i <= 5; i++) {
                Vec3d p = origin.add(flat.multiply(i));
                sw.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DEEPSLATE.getDefaultState()),
                        p.x, p.y + 0.2, p.z, 14, 0.6, 0.2, 0.6, 0.15);
                sw.spawnParticles(new DustParticleEffect(new Vector3f(0.7f, 0.05f, 0.05f), 2.0f),
                        p.x, p.y + 0.4, p.z, 8, 0.5, 0.3, 0.5, 0.0);
                sw.spawnParticles(ParticleTypes.LAVA, p.x, p.y + 0.3, p.z, 2, 0.4, 0.1, 0.4, 0.0);
            }
            sw.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 0.9f, 0.6f);
            stack.damage(6, player, LivingEntityHand.slot(hand));
            player.getItemCooldownManager().set(this, 300);
        }
        player.swingHand(hand);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Cada golpe racha os inimigos em volta.").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Botão direito: Cataclismo (custa 2 corações, 15s)").formatted(Formatting.DARK_RED));
    }
}
