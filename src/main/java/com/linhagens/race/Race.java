package com.linhagens.race;

import com.linhagens.Linhagens;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/** As raças do mod. Para adicionar uma nova, basta criar mais uma constante aqui (e a lógica em RaceManager/Abilities). */
public enum Race {
    HUMANO("humano", "Humano", 0xF2D16B, "Adaptável",
            "Versátil e persistente. Nenhuma raça aprende tão rápido quanto a humana.",
            List.of("+15% de XP ao coletar experiência", "+1 de Sorte"),
            "Nenhum bônus natural de combate.",
            new Ability[]{
                    new Ability("Segundo Fôlego", 30, 60, "Cura 4 corações e regenera por alguns segundos."),
                    new Ability("Determinação", 40, 45, "Resistência II e Velocidade por 10 segundos.")
            },
            new ModSpec[]{
                    new ModSpec(EntityAttributes.GENERIC_LUCK, "luck", 1.0, Operation.ADD_VALUE)
            }),

    DEMONIO("demonio", "Demônio", 0xE03030, "Fúria Infernal",
            "Nascido nas chamas. Quanto mais ferido, mais perigoso fica.",
            List.of("Imune a fogo e lava", "+1 de dano de ataque, +5% de velocidade", "Força quando abaixo de 40% de vida"),
            "Água o machuca (1 de dano por segundo).",
            new Ability[]{
                    new Ability("Investida Infernal", 30, 12, "Avança numa investida e incendeia quem estiver no caminho."),
                    new Ability("Aura Infernal", 45, 30, "Explosão de chamas em volta: dano e fogo nos inimigos.")
            },
            new ModSpec[]{
                    new ModSpec(EntityAttributes.GENERIC_ATTACK_DAMAGE, "dmg", 1.0, Operation.ADD_VALUE),
                    new ModSpec(EntityAttributes.GENERIC_MOVEMENT_SPEED, "speed", 0.05, Operation.ADD_MULTIPLIED_BASE)
            }),

    ANJO("anjo", "Anjo", 0xFFF2B0, "Bênção Celeste",
            "Pairando acima do mundo. Luz, velocidade e quedas suaves.",
            List.of("Queda lenta permanente", "+10% de velocidade", "Regenera vida sob o sol, ao ar livre"),
            "-2 corações de vida máxima; enfraquecido no Nether e no End.",
            new Ability[]{
                    new Ability("Voo Sagrado", 25, 15, "Impulso para cima e para frente; depois plana suavemente."),
                    new Ability("Julgamento", 50, 35, "Luz em área: dano e brilho. Dobra contra mortos-vivos e demônios.")
            },
            new ModSpec[]{
                    new ModSpec(EntityAttributes.GENERIC_MAX_HEALTH, "health", -4.0, Operation.ADD_VALUE),
                    new ModSpec(EntityAttributes.GENERIC_MOVEMENT_SPEED, "speed", 0.10, Operation.ADD_MULTIPLIED_BASE)
            }),

    PIGLIN("piglin", "Piglin", 0xE8A0A0, "Sede de Ouro",
            "Fortes e vaidosos. Ouro é poder e piglins são família.",
            List.of("Piglins e brutos não te atacam (a menos que você os ataque)", "Armas de ouro dão Força; 2+ peças de armadura de ouro dão Resistência", "+2 de armadura, +20% resistência a empurrão"),
            "Fome mais rápida; enfraquecido no End.",
            new Ability[]{
                    new Ability("Grito de Guerra", 40, 45, "Aliados próximos (e você) ganham Força II e Velocidade."),
                    new Ability("Banho de Ouro", 35, 40, "Absorção, Resistência e apaga o fogo.")
            },
            new ModSpec[]{
                    new ModSpec(EntityAttributes.GENERIC_ARMOR, "armor", 2.0, Operation.ADD_VALUE),
                    new ModSpec(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "kb", 0.2, Operation.ADD_VALUE)
            });

    public record Ability(String name, int mana, int cooldownSec, String desc) {}

    public record ModSpec(RegistryEntry<EntityAttribute> attribute, String key, double value, Operation op) {
        public Identifier id() {
            return Linhagens.id("race_" + key);
        }

        public EntityAttributeModifier build() {
            return new EntityAttributeModifier(id(), value, op);
        }
    }

    public final String id;
    public final String nome;
    public final int color;
    public final String titulo;
    public final String descricao;
    public final List<String> passivas;
    public final String fraqueza;
    public final Ability[] abilities;
    public final ModSpec[] mods;

    Race(String id, String nome, int color, String titulo, String descricao, List<String> passivas,
         String fraqueza, Ability[] abilities, ModSpec[] mods) {
        this.id = id;
        this.nome = nome;
        this.color = color;
        this.titulo = titulo;
        this.descricao = descricao;
        this.passivas = passivas;
        this.fraqueza = fraqueza;
        this.abilities = abilities;
        this.mods = mods;
    }

    public static Race byId(String id) {
        if (id == null) return null;
        for (Race r : values()) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }
}
