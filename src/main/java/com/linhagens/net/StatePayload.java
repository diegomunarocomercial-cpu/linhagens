package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> o próprio jogador: mana, recargas (em segundos), nível e XP. */
public record StatePayload(int mana, int maxMana, int cd0, int cd1, int level, int xp) implements CustomPayload {
    public static final CustomPayload.Id<StatePayload> ID = new CustomPayload.Id<>(Linhagens.id("state"));
    public static final PacketCodec<RegistryByteBuf, StatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StatePayload::mana,
            PacketCodecs.VAR_INT, StatePayload::maxMana,
            PacketCodecs.VAR_INT, StatePayload::cd0,
            PacketCodecs.VAR_INT, StatePayload::cd1,
            PacketCodecs.VAR_INT, StatePayload::level,
            PacketCodecs.VAR_INT, StatePayload::xp,
            StatePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
