package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.race.Race;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class RaceTextures {
    private RaceTextures() {}

    private static final Map<Race, Identifier> CACHE = new EnumMap<>(Race.class);

    public static Identifier of(Race r) {
        return CACHE.computeIfAbsent(r, k -> Linhagens.id("textures/entity/race/" + k.id + ".png"));
    }
}
