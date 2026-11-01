package com.linhagens.race;

/** Um nó da árvore de habilidades. col 0..2 e row 0..4 definem a posição na tela. */
public record Skill(String id, String nome, String desc, String parent, int col, int row, String icon,
                    Perk[] perks, Race.ModSpec[] mods) {}
