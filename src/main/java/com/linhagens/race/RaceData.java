package com.linhagens.race;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** Dados persistentes de RPG do jogador (salvos junto com ele). */
public record RaceData(String raceId, int level, int xp, long lastChange, List<String> skills) {
    public static final int MAX_LEVEL = 20;
    public static final RaceData NONE = new RaceData("", 1, 0, -1_000_000L, List.of());

    public static final Codec<RaceData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("race").forGetter(RaceData::raceId),
            Codec.INT.fieldOf("level").forGetter(RaceData::level),
            Codec.INT.fieldOf("xp").forGetter(RaceData::xp),
            Codec.LONG.fieldOf("last_change").forGetter(RaceData::lastChange),
            Codec.STRING.listOf().optionalFieldOf("skills", List.of()).forGetter(RaceData::skills)
    ).apply(i, RaceData::new));

    public RaceData(String raceId, int level, int xp, long lastChange) {
        this(raceId, level, xp, lastChange, List.of());
    }

    /** Raça atual ou null se o jogador ainda não escolheu. */
    public Race race() {
        return Race.byId(raceId);
    }

    public RaceData withSkills(List<String> newSkills) {
        return new RaceData(raceId, level, xp, lastChange, List.copyOf(newSkills));
    }

    /** 1 ponto por nível (a partir do nível 2), menos os já gastos. */
    public int points() {
        return Math.max(0, level - 1 - skills.size());
    }

    public static int pointsFor(int level, int unlockedCount) {
        return Math.max(0, level - 1 - unlockedCount);
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
