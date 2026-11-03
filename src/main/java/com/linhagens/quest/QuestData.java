package com.linhagens.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Estado da missão do Samuel (salvo no jogador).
 * state: 0 = sem missão, 1 = caçar o dragão, 2 = defender a vila dos 20 cultistas, 3 = tempestade de raios.
 * (cx,cy,cz) = castelo (estado 1) ou centro da vila (estados 2 e 3). endTick = fim da tempestade.
 */
public record QuestData(int state, long endTick, int cx, int cy, int cz, boolean hasPos) {
    public static final QuestData NONE = new QuestData(0, 0, 0, 0, 0, false);

    public static final Codec<QuestData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("state", 0).forGetter(QuestData::state),
            Codec.LONG.optionalFieldOf("end_tick", 0L).forGetter(QuestData::endTick),
            Codec.INT.optionalFieldOf("cx", 0).forGetter(QuestData::cx),
            Codec.INT.optionalFieldOf("cy", 0).forGetter(QuestData::cy),
            Codec.INT.optionalFieldOf("cz", 0).forGetter(QuestData::cz),
            Codec.BOOL.optionalFieldOf("has_pos", false).forGetter(QuestData::hasPos)
    ).apply(i, QuestData::new));

    public QuestData withState(int s) {
        return new QuestData(s, endTick, cx, cy, cz, hasPos);
    }
}
