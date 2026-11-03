package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> clientes: a aurora fica muito mais forte pelos próximos N ticks. */
public record AuroraBoostPayload(int ticks) implements CustomPayload {
    public static final CustomPayload.Id<AuroraBoostPayload> ID = new CustomPayload.Id<>(Linhagens.id("aurora_boost"));
    public static final PacketCodec<RegistryByteBuf, AuroraBoostPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, AuroraBoostPayload::ticks, AuroraBoostPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
