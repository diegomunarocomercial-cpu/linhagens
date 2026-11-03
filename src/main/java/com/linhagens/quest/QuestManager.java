package com.linhagens.quest;

import com.linhagens.Linhagens;
import com.linhagens.entity.ModEntities;
import com.linhagens.entity.OcultistaEntity;
import com.linhagens.entity.SamuelEntity;
import com.linhagens.item.ModItems;
import com.linhagens.net.AuroraBoostPayload;
import com.linhagens.server.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

/**
 * Missão do Samuel: 1) derrotar o Dragão Ancião de Gelo e trazer 5 Escamas; 2) ao entregar, 20 cultistas atacam a vila;
 * 3) derrotados todos, a aurora fica muito mais forte e raios caem por 2 minutos (10% de acertar o jogador e levá-lo
 * ao Reino Profano).
 */
public final class QuestManager {
    private QuestManager() {}

    public static final String EVENT_TAG = "evento_diablos";
    public static final int SCALES_NEEDED = 5;
    public static final int COINS_REWARD = 25;
    public static final int STORM_TICKS = 2400;
    public static final RegistryKey<World> REINO = RegistryKey.of(RegistryKeys.WORLD, Linhagens.id("reino_profano"));

    public static QuestData get(ServerPlayerEntity p) {
        return p.getAttachedOrElse(ModAttachments.QUEST, QuestData.NONE);
    }

    private static void set(ServerPlayerEntity p, QuestData d) {
        p.setAttached(ModAttachments.QUEST, d);
    }

    private static void say(ServerPlayerEntity p, String msg) {
        p.sendMessage(Text.literal("[Samuel] ").formatted(Formatting.GOLD).append(Text.literal(msg).formatted(Formatting.WHITE)), false);
    }

    // ------------------------------------------------------------------ conversa
    public static void talk(ServerPlayerEntity p, SamuelEntity samuel) {
        QuestData d = get(p);
        ServerWorld w = p.getServerWorld();
        switch (d.state()) {
            case 0 -> {
                say(p, "Forasteiro! Preciso de alguém corajoso. Há um Dragão Ancião de Gelo no castelo congelado... "
                        + "traga-me " + SCALES_NEEDED + " escamas dele e te pago em Tempus Coins de Ouro.");
                p.sendMessage(Text.literal("   [ACEITAR MISSÃO]").styled(s -> s.withColor(Formatting.GREEN).withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/missao aceitar"))), false);
            }
            case 1 -> {
                int have = countScales(p);
                if (have >= SCALES_NEEDED) {
                    deliver(p, w, samuel);
                } else {
                    say(p, "Você tem " + have + " de " + SCALES_NEEDED + " Escamas do Dragão Ancião de Gelo.");
                    if (d.hasPos()) say(p, "O castelo fica em X: " + d.cx() + "  Z: " + d.cz() + ".");
                    else tryLocate(p, w, d);
                }
            }
            case 2 -> say(p, "Os cultistas ainda estão na vila! Derrote todos os 20!");
            default -> say(p, "Cuidado com os raios...");
        }
    }

    public static void accept(ServerPlayerEntity p) {
        QuestData d = get(p);
        if (d.state() != 0) {
            p.sendMessage(Text.literal("Você já tem uma missão em andamento.").formatted(Formatting.RED), false);
            return;
        }
        ServerWorld w = p.getServerWorld();
        set(p, new QuestData(1, 0, 0, 0, 0, false));
        say(p, "Missão aceita! Derrote o Dragão Ancião de Gelo e traga " + SCALES_NEEDED + " escamas.");
        tryLocate(p, w, get(p));
    }

    private static void tryLocate(ServerPlayerEntity p, ServerWorld w, QuestData d) {
        TagKey<Structure> tag = TagKey.of(RegistryKeys.STRUCTURE, Linhagens.id("castelo_gelo"));
        BlockPos found = w.locateStructure(tag, p.getBlockPos(), 150, false);
        if (found == null) {
            say(p, "Hmm... não sei onde fica o castelo agora. Procure em terras geladas e volte a falar comigo.");
            return;
        }
        set(p, new QuestData(1, 0, found.getX(), found.getY(), found.getZ(), true));
        say(p, "O castelo de gelo fica em X: " + found.getX() + "  Z: " + found.getZ() + ".");
        p.sendMessage(Text.literal("Coordenadas do castelo: " + found.getX() + ", " + found.getZ()).formatted(Formatting.AQUA), false);
    }

    private static int countScales(ServerPlayerEntity p) {
        int n = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(ModItems.ESCAMA_DRAGAO)) n += s.getCount();
        }
        return n;
    }

    private static void deliver(ServerPlayerEntity p, ServerWorld w, SamuelEntity samuel) {
        int need = SCALES_NEEDED;
        for (int i = 0; i < p.getInventory().size() && need > 0; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(ModItems.ESCAMA_DRAGAO)) {
                int take = Math.min(s.getCount(), need);
                s.decrement(take);
                need -= take;
            }
        }
        p.giveItemStack(new ItemStack(ModItems.TEMPUS_COIN, COINS_REWARD));
        say(p, "Incrível! Aqui está seu pagamento: " + COINS_REWARD + " Tempus Coins de Ouro!");

        BlockPos center = villageCenter(w, samuel.getBlockPos());
        w.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 4f, 0.6f);
        say(p, "Espere... o que é isso?! O Culto do Diablos está atacando a vila!");
        p.sendMessage(Text.literal("20 Cultistas do Diablos surgiram no centro da vila! Derrote todos!").formatted(Formatting.DARK_RED), false);
        for (int i = 0; i < 20; i++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            double r = 5 + w.random.nextDouble() * 9;
            int x = center.getX() + (int) Math.round(Math.cos(a) * r);
            int z = center.getZ() + (int) Math.round(Math.sin(a) * r);
            int y = w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            OcultistaEntity c = new OcultistaEntity(ModEntities.OCULTISTA, w);
            c.refreshPositionAndAngles(x + 0.5, y, z + 0.5, (float) (w.random.nextDouble() * 360), 0);
            c.initialize(w, w.getLocalDifficulty(c.getBlockPos()), SpawnReason.EVENT, null);
            c.addCommandTag(EVENT_TAG);
            c.setPersistent();
            w.spawnEntity(c);
            w.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, x + 0.5, y + 1, z + 0.5, 12, 0.3, 0.8, 0.3, 0.05);
        }
        set(p, new QuestData(2, 0, center.getX(), center.getY(), center.getZ(), true));
    }

    private static BlockPos villageCenter(ServerWorld w, BlockPos fallback) {
        try {
            Structure s = w.getRegistryManager().get(RegistryKeys.STRUCTURE).get(Linhagens.id("vila_aurora"));
            if (s != null) {
                var start = w.getStructureAccessor().getStructureAt(fallback, s);
                if (start.hasChildren()) return start.getBoundingBox().getCenter();
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    // ------------------------------------------------------------------ tick
    public static void tick(MinecraftServer server) {
        int t = server.getTicks();
        if (t % 20 != 0) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            QuestData d = get(p);
            if (d.state() < 2) continue;
            ServerWorld w = p.getServerWorld();
            long now = w.getTime();

            if (d.state() == 2) {
                Box box = Box.of(new Vec3d(d.cx(), d.cy(), d.cz()), 200, 200, 200);
                int alive = w.getEntitiesByClass(OcultistaEntity.class, box, e -> e.isAlive() && e.getCommandTags().contains(EVENT_TAG)).size();
                if (alive == 0) {
                    set(p, new QuestData(3, now + STORM_TICKS, d.cx(), d.cy(), d.cz(), true));
                    p.sendMessage(Text.literal("A vila está salva! A aurora ruge no céu... e os raios começam a cair!").formatted(Formatting.LIGHT_PURPLE), false);
                    for (ServerPlayerEntity o : w.getPlayers()) {
                        ServerPlayNetworking.send(o, new AuroraBoostPayload(STORM_TICKS));
                    }
                    w.setWeather(0, STORM_TICKS, true, true);
                }
            } else if (d.state() == 3) {
                if (now >= d.endTick()) {
                    set(p, QuestData.NONE);
                    p.sendMessage(Text.literal("A tempestade passou. Você sobreviveu!").formatted(Formatting.GREEN), false);
                    continue;
                }
                if (t % 40 == 0) strike(p, w);
            }
        }
    }

    private static void strike(ServerPlayerEntity p, ServerWorld w) {
        boolean hit = w.random.nextInt(100) < 10;            // 10% de chance a cada raio
        double x = p.getX(), z = p.getZ();
        if (!hit) {
            double a = w.random.nextDouble() * Math.PI * 2;
            double r = 8 + w.random.nextDouble() * 18;
            x += Math.cos(a) * r;
            z += Math.sin(a) * r;
        }
        double y = hit ? p.getY() : w.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(x, y, z);
            bolt.setCosmetic(true);
            w.spawnEntity(bolt);
        }
        if (hit) {
            set(p, QuestData.NONE);
            p.sendMessage(Text.literal("Um raio te atingiu!").formatted(Formatting.DARK_RED), false);
            sendToReino(p);
        }
    }

    // ------------------------------------------------------------------ Reino Profano
    public static void sendToReino(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        ServerWorld target = server.getWorld(REINO);
        if (target == null) {
            p.sendMessage(Text.literal("O Reino Profano não foi encontrado (pacote de dados do mod ausente).").formatted(Formatting.RED), false);
            return;
        }
        int x = p.getBlockX(), z = p.getBlockZ();
        target.getChunk(x >> 4, z >> 4);
        int y = target.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        p.teleport(target, x + 0.5, y + 1, z + 0.5, p.getYaw(), p.getPitch());
        p.sendMessage(Text.literal("Você foi levado ao REINO PROFANO. Use /reino sair para voltar.").formatted(Formatting.DARK_RED), false);
    }

    public static void leaveReino(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return;
        if (p.getServerWorld().getRegistryKey() != REINO) {
            p.sendMessage(Text.literal("Você não está no Reino Profano.").formatted(Formatting.GRAY), false);
            return;
        }
        ServerWorld ow = server.getOverworld();
        BlockPos spawn = ow.getSpawnPos();
        int y = ow.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        p.teleport(ow, spawn.getX() + 0.5, y + 1, spawn.getZ() + 0.5, p.getYaw(), p.getPitch());
        p.sendMessage(Text.literal("Você voltou ao mundo normal.").formatted(Formatting.GREEN), false);
    }
}
