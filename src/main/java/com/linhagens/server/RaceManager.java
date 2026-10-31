package com.linhagens.server;

import com.linhagens.net.OpenMenuPayload;
import com.linhagens.net.RaceSyncPayload;
import com.linhagens.net.StatePayload;
import com.linhagens.race.Race;
import com.linhagens.race.RaceData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/** Toda a lógica do lado do servidor: trocar raça, passivas, mana, recarga, XP. */
public final class RaceManager {
    private RaceManager() {}

    /** Tempo mínimo entre trocas de raça (5 min). Admins ignoram com /linhagem set. */
    public static final long CHANGE_COOLDOWN_TICKS = 6000L;

    public static final class Runtime {
        public float mana = 100;
        public final long[] cdEnd = new long[2];
        public int lastXp = -1;
        public float xpFrac = 0;
        public StatePayload lastSent = null;
    }

    private static final Map<UUID, Runtime> RUNTIME = new HashMap<>();

    public static Runtime rt(ServerPlayerEntity p) {
        return RUNTIME.computeIfAbsent(p.getUuid(), k -> new Runtime());
    }

    public static void forget(UUID id) {
        RUNTIME.remove(id);
    }

    public static RaceData get(ServerPlayerEntity p) {
        return p.getAttachedOrElse(ModAttachments.RACE_DATA, RaceData.NONE);
    }

    // ------------------------------------------------------------ trocar raça

    public static boolean setRace(ServerPlayerEntity p, Race r, boolean force) {
        RaceData cur = get(p);
        long now = p.getServerWorld().getTime();
        if (cur.race() == r) return false;
        if (!force && cur.race() != null) {
            long left = CHANGE_COOLDOWN_TICKS - (now - cur.lastChange());
            if (left > 0) {
                p.sendMessage(Text.literal("Você só poderá trocar de raça em " + (left / 20 + 1) + "s.")
                        .formatted(Formatting.RED), true);
                return false;
            }
        }
        p.setAttached(ModAttachments.RACE_DATA, new RaceData(r.id, 1, 0, now));
        Runtime rt = rt(p);
        rt.mana = RaceData.maxMana(1);
        Arrays.fill(rt.cdEnd, 0);
        rt.lastXp = -1;
        rt.lastSent = null;
        applyModifiers(p);
        broadcast(p);
        sendState(p, true);
        p.sendMessage(Text.literal("Você agora é " + r.nome + " — " + r.titulo + "!")
                .styled(s -> s.withColor(r.color)), false);
        ServerWorld w = p.getServerWorld();
        w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getBodyY(0.5), p.getZ(), 40, 0.5, 0.8, 0.5, 0.05);
        w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 1.2f);
        return true;
    }

    public static void resetRace(ServerPlayerEntity p) {
        p.setAttached(ModAttachments.RACE_DATA, RaceData.NONE);
        Runtime rt = rt(p);
        Arrays.fill(rt.cdEnd, 0);
        rt.lastSent = null;
        applyModifiers(p);
        broadcast(p);
        ServerPlayNetworking.send(p, OpenMenuPayload.INSTANCE);
    }

    /** Remove os modificadores de atributo de todas as raças e aplica os da raça atual. */
    public static void applyModifiers(ServerPlayerEntity p) {
        for (Race r : Race.values()) {
            for (Race.ModSpec spec : r.mods) {
                EntityAttributeInstance inst = p.getAttributeInstance(spec.attribute());
                if (inst != null) inst.removeModifier(spec.id());
            }
        }
        Race cur = get(p).race();
        if (cur != null) {
            for (Race.ModSpec spec : cur.mods) {
                EntityAttributeInstance inst = p.getAttributeInstance(spec.attribute());
                if (inst != null) inst.addPersistentModifier(spec.build());
            }
        }
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    // ------------------------------------------------------------ sincronização

    public static void broadcast(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        RaceSyncPayload pl = new RaceSyncPayload(p.getUuidAsString(), get(p).raceId());
        for (ServerPlayerEntity o : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(o, pl);
        }
    }

    /** Manda para um jogador que acabou de entrar a raça de todos os outros. */
    public static void sendAllTo(ServerPlayerEntity target) {
        MinecraftServer server = target.getServer();
        if (server == null) return;
        for (ServerPlayerEntity o : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(target, new RaceSyncPayload(o.getUuidAsString(), get(o).raceId()));
        }
    }

    public static void sendState(ServerPlayerEntity p, boolean force) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        RaceData d = get(p);
        Runtime rt = rt(p);
        long t = server.getTicks();
        StatePayload pl = new StatePayload(
                (int) rt.mana, RaceData.maxMana(d.level()),
                secs(rt.cdEnd[0] - t), secs(rt.cdEnd[1] - t),
                d.level(), d.xp());
        if (force || !pl.equals(rt.lastSent)) {
            rt.lastSent = pl;
            ServerPlayNetworking.send(p, pl);
        }
    }

    private static int secs(long ticks) {
        return ticks <= 0 ? 0 : (int) Math.ceil(ticks / 20.0);
    }

    // ------------------------------------------------------------ habilidades

    public static void useAbility(ServerPlayerEntity p, int slot) {
        if (slot < 0 || slot > 1 || !p.isAlive() || p.isSpectator()) return;
        RaceData d = get(p);
        Race r = d.race();
        if (r == null) return;
        Runtime rt = rt(p);
        long t = p.getServer().getTicks();
        Race.Ability ab = r.abilities[slot];
        if (t < rt.cdEnd[slot]) {
            p.sendMessage(Text.literal(ab.name() + " em recarga (" + secs(rt.cdEnd[slot] - t) + "s)")
                    .formatted(Formatting.GRAY), true);
            return;
        }
        if (rt.mana < ab.mana()) {
            p.sendMessage(Text.literal("Mana insuficiente (" + ab.mana() + ")").formatted(Formatting.BLUE), true);
            return;
        }
        rt.mana -= ab.mana();
        rt.cdEnd[slot] = t + ab.cooldownSec() * 20L;
        Abilities.cast(r, slot, p, RaceData.power(d.level()));
        sendState(p, true);
    }

    // ------------------------------------------------------------ XP de raça

    public static void addRaceXp(ServerPlayerEntity p, int gain) {
        RaceData d = get(p);
        if (d.race() == null || d.level() >= RaceData.MAX_LEVEL) return;
        int level = d.level();
        int xp = d.xp() + gain;
        boolean up = false;
        while (level < RaceData.MAX_LEVEL && xp >= RaceData.xpNeeded(level)) {
            xp -= RaceData.xpNeeded(level);
            level++;
            up = true;
        }
        if (level >= RaceData.MAX_LEVEL) xp = 0;
        p.setAttached(ModAttachments.RACE_DATA, new RaceData(d.raceId(), level, xp, d.lastChange()));
        if (up) {
            p.sendMessage(Text.literal("Linhagem nível " + level + "!").formatted(Formatting.GOLD), true);
            ServerWorld w = p.getServerWorld();
            w.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 0.8f);
            w.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getBodyY(0.5), p.getZ(), 25, 0.4, 0.7, 0.4, 0.2);
        }
        sendState(p, false);
    }

    // ------------------------------------------------------------ tick (passivas)

    public static void tick(ServerPlayerEntity p, MinecraftServer server) {
        RaceData d = get(p);
        Race r = d.race();
        if (r == null || !p.isAlive()) return;
        Runtime rt = rt(p);
        int t = server.getTicks();
        ServerWorld w = p.getServerWorld();

        int maxMana = RaceData.maxMana(d.level());
        if (t % 10 == 0 && rt.mana < maxMana) rt.mana = Math.min(maxMana, rt.mana + 1);
        if (rt.mana > maxMana) rt.mana = maxMana;
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());

        boolean sec = (t % 20 == 0);
        switch (r) {
            case HUMANO -> xpBonus(p, rt);

            case DEMONIO -> {
                if (sec) {
                    effect(p, StatusEffects.FIRE_RESISTANCE, 60, 0);
                    if (p.getHealth() < p.getMaxHealth() * 0.4f) effect(p, StatusEffects.STRENGTH, 60, 0);
                    if (p.isTouchingWater()) p.damage(p.getDamageSources().magic(), 1f);
                }
            }

            case ANJO -> {
                if (sec) {
                    effect(p, StatusEffects.SLOW_FALLING, 60, 0);
                    if (w.getRegistryKey() != World.OVERWORLD) {
                        effect(p, StatusEffects.WEAKNESS, 60, 0);
                    }
                }
                if (t % 80 == 0 && w.isDay() && w.isSkyVisible(p.getBlockPos()) && p.getHealth() < p.getMaxHealth()) {
                    p.heal(1f);
                }
            }

            case PIGLIN -> {
                if (sec) {
                    if (isGoldTool(p.getMainHandStack())) effect(p, StatusEffects.STRENGTH, 60, 0);
                    int gold = 0;
                    for (ItemStack a : p.getArmorItems()) if (isGoldArmor(a)) gold++;
                    if (gold >= 2) effect(p, StatusEffects.RESISTANCE, 60, 0);
                    if (w.getRegistryKey() == World.END) effect(p, StatusEffects.WEAKNESS, 60, 0);
                    p.addExhaustion(0.05f);
                }
                if (t % 10 == 0) pacifyPiglins(p, w);
            }
        }

        if (t % 2 == 0) sendState(p, false);
    }

    private static void effect(ServerPlayerEntity p, RegistryEntry<StatusEffect> e, int dur, int amp) {
        p.addStatusEffect(new StatusEffectInstance(e, dur, amp, true, false, false));
    }

    /** Humano: +15% de XP. Compara o XP total a cada tick e devolve a diferença. */
    private static void xpBonus(ServerPlayerEntity p, Runtime rt) {
        int total = p.totalExperience;
        if (rt.lastXp < 0) rt.lastXp = total;
        if (total > rt.lastXp) {
            float extra = (total - rt.lastXp) * 0.15f + rt.xpFrac;
            int give = (int) extra;
            rt.xpFrac = extra - give;
            if (give > 0) {
                p.addExperience(give);
                total = p.totalExperience;
            }
        }
        rt.lastXp = total;
    }

    private static void pacifyPiglins(ServerPlayerEntity p, ServerWorld w) {
        for (AbstractPiglinEntity pg : w.getEntitiesByClass(AbstractPiglinEntity.class,
                p.getBoundingBox().expand(24), x -> true)) {
            if (pg.getAttacker() == p) continue; // se você bateu nele, ele tem motivo
            Brain<?> b = pg.getBrain();
            if (b.getOptionalMemory(MemoryModuleType.ATTACK_TARGET).filter(x -> x == p).isPresent()) {
                b.forget(MemoryModuleType.ATTACK_TARGET);
            }
            if (b.getOptionalMemory(MemoryModuleType.ANGRY_AT).filter(u -> u.equals(p.getUuid())).isPresent()) {
                b.forget(MemoryModuleType.ANGRY_AT);
            }
        }
    }

    private static boolean isGoldTool(ItemStack s) {
        return s.isOf(Items.GOLDEN_SWORD) || s.isOf(Items.GOLDEN_AXE) || s.isOf(Items.GOLDEN_PICKAXE)
                || s.isOf(Items.GOLDEN_SHOVEL) || s.isOf(Items.GOLDEN_HOE);
    }

    private static boolean isGoldArmor(ItemStack s) {
        return s.isOf(Items.GOLDEN_HELMET) || s.isOf(Items.GOLDEN_CHESTPLATE)
                || s.isOf(Items.GOLDEN_LEGGINGS) || s.isOf(Items.GOLDEN_BOOTS);
    }
}
