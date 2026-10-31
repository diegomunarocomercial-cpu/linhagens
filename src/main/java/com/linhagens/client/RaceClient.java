package com.linhagens.client;

import com.linhagens.net.StatePayload;
import com.linhagens.race.Race;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

/** Estado do lado do cliente: raça de cada jogador, mana/recargas do jogador local e modo de prévia do menu. */
public final class RaceClient {
    private RaceClient() {}

    private static final Map<UUID, Race> RACES = new HashMap<>();
    public static StatePayload state = null;

    /** Usado pelo menu: faz o jogador local ser desenhado com a raça "prévia". */
    public static boolean previewActive = false;
    public static Race previewRace = null;

    public static void set(UUID id, Race r) {
        if (r == null) RACES.remove(id);
        else RACES.put(id, r);
    }

    public static Race raceFor(AbstractClientPlayerEntity p) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (previewActive && p == mc.player) return previewRace;
        return RACES.get(p.getUuid());
    }

    public static Race local() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.player == null ? null : RACES.get(mc.player.getUuid());
    }

    public static void clear() {
        RACES.clear();
        state = null;
    }
}
