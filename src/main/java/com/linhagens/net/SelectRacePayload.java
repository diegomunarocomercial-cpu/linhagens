package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: jogador escolheu uma raça no menu. */
public record SelectRacePayload(String raceId) implements CustomPayload {
    public static final CustomPayload.Id<SelectRacePayload> ID = new CustomPayload.Id<>(Linhagens.id("select_race"));
    public static final PacketCodec<RegistryByteBuf, SelectRacePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, SelectRacePayload::raceId, SelectRacePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
