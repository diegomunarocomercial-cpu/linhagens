package com.linhagens.quest;

/** Intensidade extra da aurora (em ticks restantes). Escrito pelo cliente ao receber o pacote do servidor. */
public final class AuroraState {
    private AuroraState() {}

    public static volatile int boost = 0;
}
