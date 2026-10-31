package com.linhagens.server;

import com.linhagens.Linhagens;
import com.linhagens.race.RaceData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
    private ModAttachments() {}

    /** Raça/nível/XP do jogador: salvo no disco e mantido ao morrer. */
    public static final AttachmentType<RaceData> RACE_DATA = AttachmentRegistry.<RaceData>create(
            Linhagens.id("race_data"),
            builder -> builder.persistent(RaceData.CODEC).copyOnDeath());

    public static void init() {
        // Só força o carregamento da classe.
    }
}
