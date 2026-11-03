package com.linhagens.server;

import com.linhagens.Linhagens;
import com.linhagens.quest.QuestData;
import com.linhagens.race.RaceData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
    private ModAttachments() {}

    /** Raça/nível/XP/habilidades do jogador: salvo no disco e mantido ao morrer. */
    public static final AttachmentType<RaceData> RACE_DATA = AttachmentRegistry.<RaceData>create(
            Linhagens.id("race_data"),
            builder -> builder.persistent(RaceData.CODEC).copyOnDeath());

    /** Estado da missão do Samuel. */
    public static final AttachmentType<QuestData> QUEST = AttachmentRegistry.<QuestData>create(
            Linhagens.id("quest"),
            builder -> builder.persistent(QuestData.CODEC).copyOnDeath());

    public static void init() {
    }
}
