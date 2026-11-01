package com.linhagens.race;

import java.util.EnumSet;
import java.util.Set;

/** Soma os efeitos de todas as habilidades desbloqueadas de um jogador. */
public record SkillEffects(int manaBonus, double cdMult, double costMult, float powerMult, Set<Perk> perks) {
    public static SkillEffects of(RaceData d) {
        Race r = d.race();
        int mana = 0;
        double cd = 1.0, cost = 1.0;
        float power = 1.0f;
        Set<Perk> perks = EnumSet.noneOf(Perk.class);
        if (r != null) {
            for (String id : d.skills()) {
                Skill s = SkillTrees.find(r, id);
                if (s == null) continue;
                for (Perk p : s.perks()) {
                    perks.add(p);
                    switch (p) {
                        case MANA_PLUS -> mana += 30;
                        case COOLDOWN10 -> cd *= 0.9;
                        case COST15 -> cost *= 0.85;
                        case POWER15 -> power += 0.15f;
                        case MASTERY -> cd *= 0.75;
                        case POWER30 -> power += 0.30f;
                        default -> { }
                    }
                }
            }
        }
        return new SkillEffects(mana, cd, cost, power, perks);
    }

    public boolean has(Perk p) {
        return perks.contains(p);
    }
}
