package com.linhagens.server;

import com.linhagens.net.SkillsPayload;
import com.linhagens.race.Race;
import com.linhagens.race.RaceData;
import com.linhagens.race.Skill;
import com.linhagens.race.SkillTrees;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Gasta/devolve pontos da árvore de habilidades (sempre validado no servidor). */
public final class SkillManager {
    private SkillManager() {}

    public static boolean unlock(ServerPlayerEntity p, String id) {
        RaceData d = RaceManager.get(p);
        Race r = d.race();
        if (r == null) return false;
        Skill s = SkillTrees.find(r, id);
        if (s == null || d.skills().contains(id)) return false;
        if (d.points() <= 0) {
            p.sendMessage(Text.literal("Sem pontos de habilidade. Suba de nível!").formatted(Formatting.RED), true);
            return false;
        }
        if (s.parent() != null && !d.skills().contains(s.parent())) {
            p.sendMessage(Text.literal("Desbloqueie a habilidade anterior primeiro.").formatted(Formatting.RED), true);
            return false;
        }
        List<String> n = new ArrayList<>(d.skills());
        n.add(id);
        p.setAttached(ModAttachments.RACE_DATA, d.withSkills(n));
        RaceManager.applyModifiers(p);
        sendSkills(p);
        RaceManager.sendState(p, true);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS, 1f, 1.2f);
        p.sendMessage(Text.literal("Habilidade desbloqueada: " + s.nome()).formatted(Formatting.GREEN), true);
        return true;
    }

    public static void reset(ServerPlayerEntity p) {
        RaceData d = RaceManager.get(p);
        if (d.race() == null || d.skills().isEmpty()) return;
        p.setAttached(ModAttachments.RACE_DATA, d.withSkills(List.of()));
        RaceManager.applyModifiers(p);
        sendSkills(p);
        RaceManager.sendState(p, true);
        p.sendMessage(Text.literal("Pontos de habilidade devolvidos.").formatted(Formatting.YELLOW), true);
    }

    public static void sendSkills(ServerPlayerEntity p) {
        ServerPlayNetworking.send(p, new SkillsPayload(String.join(",", RaceManager.get(p).skills())));
    }
}
