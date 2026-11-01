package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: gastar 1 ponto numa habilidade. */
public record UnlockSkillPayload(String id) implements CustomPayload {
    public static final CustomPayload.Id<UnlockSkillPayload> ID = new CustomPayload.Id<>(Linhagens.id("unlock_skill"));
    public static final PacketCodec<RegistryByteBuf, UnlockSkillPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, UnlockSkillPayload::id, UnlockSkillPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
