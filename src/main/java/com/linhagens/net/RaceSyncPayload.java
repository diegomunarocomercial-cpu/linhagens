package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> Clientes: qual raça tal jogador tem (raceId vazio = nenhuma). */
public record RaceSyncPayload(String uuid, String raceId) implements CustomPayload {
    public static final CustomPayload.Id<RaceSyncPayload> ID = new CustomPayload.Id<>(Linhagens.id("race_sync"));
    public static final PacketCodec<RegistryByteBuf, RaceSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, RaceSyncPayload::uuid,
            PacketCodecs.STRING, RaceSyncPayload::raceId,
            RaceSyncPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
