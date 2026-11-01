package com.linhagens.race;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;

/**
 * Árvore de habilidades de cada raça: 1 raiz + 3 ramos de 4 nós (Ataque, Defesa, Maestria).
 * Cada nó custa 1 ponto; ganha-se 1 ponto por nível. O último nó de cada ramo é um poder exclusivo da raça.
 */
public final class SkillTrees {
    private SkillTrees() {}

    private static final Map<Race, List<Skill>> TREES = new EnumMap<>(Race.class);

    // ordem dos nomes: raiz, a1..a4, b1..b4, c1..c4
    private static final Map<Race, String[]> NAMES = new EnumMap<>(Race.class);
    private static final Map<Race, String[]> CAP_DESC = new EnumMap<>(Race.class);
    private static final Map<Race, String[]> CAP_ICON = new EnumMap<>(Race.class);
    private static final Map<Race, Perk[]> CAPS = new EnumMap<>(Race.class);

    static {
        NAMES.put(Race.HUMANO, new String[]{"Despertar", "Golpe Firme", "Foco Total", "Mãos Ágeis", "Aprendiz Prodigioso",
                "Vigor", "Pele Grossa", "Muralha", "Segunda Chance", "Reserva Arcana", "Fluxo Rápido", "Economia", "Maestria Total"});
        CAPS.put(Race.HUMANO, new Perk[]{Perk.XP_BOOST, Perk.SECOND_WIND, Perk.MASTERY});
        CAP_DESC.put(Race.HUMANO, new String[]{"+25% de XP extra (total +40%).",
                "Ao receber dano fatal, sobrevive com 3 corações (recarga de 5 min).", "-25% de recarga em todas as habilidades."});
        CAP_ICON.put(Race.HUMANO, new String[]{"minecraft:experience_bottle", "minecraft:totem_of_undying", "minecraft:nether_star"});

        NAMES.put(Race.DEMONIO, new String[]{"Chama Interior", "Garras Afiadas", "Ódio Concentrado", "Frenesi", "Chamas Eternas",
                "Sangue Quente", "Couro Infernal", "Inabalável", "Sede de Sangue", "Brasas Arcanas", "Pacto Veloz", "Barganha Sombria", "Fúria Infernal"});
        CAPS.put(Race.DEMONIO, new Perk[]{Perk.FLAME_STRIKE, Perk.LIFESTEAL, Perk.POWER30});
        CAP_DESC.put(Race.DEMONIO, new String[]{"Seus golpes incendeiam o alvo por 4s.",
                "Cura 15% do dano que você causa em combate corpo a corpo.", "+30% de poder nas habilidades."});
        CAP_ICON.put(Race.DEMONIO, new String[]{"minecraft:fire_charge", "minecraft:redstone", "minecraft:nether_star"});

        NAMES.put(Race.ANJO, new String[]{"Luz Interior", "Lâmina Radiante", "Foco Celeste", "Golpe Alado", "Lâmina Sagrada",
                "Alma Pura", "Manto de Luz", "Graça Divina", "Escudo Celestial", "Fonte de Luz", "Fluxo Divino", "Humildade", "Comunhão Celestial"});
        CAPS.put(Race.ANJO, new Perk[]{Perk.HOLY_STRIKE, Perk.HALO_SHIELD, Perk.MANA_REGEN2});
        CAP_DESC.put(Race.ANJO, new String[]{"+4 de dano contra mortos-vivos e demônios.",
                "Ganha 2 corações de absorção a cada 30s.", "A mana regenera o dobro da velocidade."});
        CAP_ICON.put(Race.ANJO, new String[]{"minecraft:glowstone_dust", "minecraft:golden_apple", "minecraft:nether_star"});

        NAMES.put(Race.PIGLIN, new String[]{"Instinto de Ouro", "Presas Afiadas", "Fúria de Guerra", "Investida", "Saque de Ouro",
                "Couro Grosso", "Armadura de Ouro", "Teimosia", "Pele Dourada", "Tesouro Arcano", "Barganha Rápida", "Mão Fechada", "Rei do Ouro"});
        CAPS.put(Race.PIGLIN, new Perk[]{Perk.LOOTER, Perk.GOLDEN_SKIN, Perk.ALLY_AURA});
        CAP_DESC.put(Race.PIGLIN, new String[]{"Criaturas que você mata largam pepitas de ouro.",
                "Resistência permanente a danos.", "Aliados num raio de 10 blocos ganham Força I."});
        CAP_ICON.put(Race.PIGLIN, new String[]{"minecraft:gold_nugget", "minecraft:gold_ingot", "minecraft:nether_star"});

        for (Race r : Race.values()) TREES.put(r, build(r));
    }

    private static Race.ModSpec mod(Race r, String id, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.attribute.EntityAttribute> attr,
                                    double v, Operation op) {
        return new Race.ModSpec(attr, "sk_" + r.id + "_" + id, v, op);
    }

    private static List<Skill> build(Race r) {
        String[] n = NAMES.get(r);
        Perk[] caps = CAPS.get(r);
        String[] cd = CAP_DESC.get(r);
        String[] ci = CAP_ICON.get(r);
        Perk[] none = new Perk[0];
        Race.ModSpec[] noMods = new Race.ModSpec[0];
        List<Skill> l = new ArrayList<>();

        l.add(new Skill("raiz", n[0], "+30 de mana máxima.", null, 1, 0, "minecraft:amethyst_shard", new Perk[]{Perk.MANA_PLUS}, noMods));
        // Ramo A (ataque)
        l.add(new Skill("a1", n[1], "+1 de dano de ataque.", "raiz", 0, 1, "minecraft:iron_sword", none,
                new Race.ModSpec[]{mod(r, "a1", EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0, Operation.ADD_VALUE)}));
        l.add(new Skill("a2", n[2], "+15% de poder nas habilidades.", "a1", 0, 2, "minecraft:blaze_powder", new Perk[]{Perk.POWER15}, noMods));
        l.add(new Skill("a3", n[3], "+0,25 de velocidade de ataque.", "a2", 0, 3, "minecraft:feather", none,
                new Race.ModSpec[]{mod(r, "a3", EntityAttributes.GENERIC_ATTACK_SPEED, 0.25, Operation.ADD_VALUE)}));
        l.add(new Skill("a4", n[4], cd[0], "a3", 0, 4, ci[0], new Perk[]{caps[0]}, noMods));
        // Ramo B (defesa)
        l.add(new Skill("b1", n[5], "+4 de vida máxima (2 corações).", "raiz", 1, 1, "minecraft:apple", none,
                new Race.ModSpec[]{mod(r, "b1", EntityAttributes.GENERIC_MAX_HEALTH, 4.0, Operation.ADD_VALUE)}));
        l.add(new Skill("b2", n[6], "+2 de armadura.", "b1", 1, 2, "minecraft:iron_chestplate", none,
                new Race.ModSpec[]{mod(r, "b2", EntityAttributes.GENERIC_ARMOR, 2.0, Operation.ADD_VALUE)}));
        l.add(new Skill("b3", n[7], "+1 de resistência da armadura e +15% de resistência a empurrão.", "b2", 1, 3, "minecraft:shield", none,
                new Race.ModSpec[]{mod(r, "b3t", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 1.0, Operation.ADD_VALUE),
                        mod(r, "b3k", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.15, Operation.ADD_VALUE)}));
        l.add(new Skill("b4", n[8], cd[1], "b3", 1, 4, ci[1], new Perk[]{caps[1]}, noMods));
        // Ramo C (maestria)
        l.add(new Skill("c1", n[9], "+30 de mana máxima.", "raiz", 2, 1, "minecraft:lapis_lazuli", new Perk[]{Perk.MANA_PLUS}, noMods));
        l.add(new Skill("c2", n[10], "-10% de recarga nas habilidades.", "c1", 2, 2, "minecraft:clock", new Perk[]{Perk.COOLDOWN10}, noMods));
        l.add(new Skill("c3", n[11], "-15% de custo de mana.", "c2", 2, 3, "minecraft:emerald", new Perk[]{Perk.COST15}, noMods));
        l.add(new Skill("c4", n[12], cd[2], "c3", 2, 4, ci[2], new Perk[]{caps[2]}, noMods));
        return l;
    }

    public static List<Skill> get(Race r) {
        return TREES.get(r);
    }

    public static Skill find(Race r, String id) {
        for (Skill s : TREES.get(r)) if (s.id().equals(id)) return s;
        return null;
    }

    /** Todos os modificadores de atributo de todas as árvores (usado para limpar antes de reaplicar). */
    public static List<Race.ModSpec> allMods() {
        List<Race.ModSpec> out = new ArrayList<>();
        for (List<Skill> l : TREES.values()) for (Skill s : l) for (Race.ModSpec m : s.mods()) out.add(m);
        return out;
    }
}
