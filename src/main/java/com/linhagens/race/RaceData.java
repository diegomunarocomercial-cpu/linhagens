package com.linhagens.race;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Dados persistentes de RPG do jogador (salvos junto com ele). */
public record RaceData(String raceId, int level, int xp, long lastChange) {
    public static final int MAX_LEVEL = 20;
    public static final RaceData NONE = new RaceData("", 1, 0, -1_000_000L);

    public static final Codec<RaceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("race").forGetter(RaceData::raceId),
            Codec.INT.fieldOf("level").forGetter(RaceData::level),
            Codec.INT.fieldOf("xp").forGetter(RaceData::xp),
            Codec.LONG.fieldOf("last_change").forGetter(RaceData::lastChange)
    ).apply(i, RaceData::new));

    /** Raça atual ou null se o jogador ainda não escolheu. */
    public Race race() {
        return Race.byId(raceId);
    }

    public static int xpNeeded(int level) {
        return 40 + level * 30;
    }

    public static int maxMana(int level) {
        return 100 + 10 * (level - 1);
    }

    /** Multiplicador de poder das habilidades por nível. */
    public static float power(int level) {
        return 1f + 0.08f * (level - 1);
    }
}
