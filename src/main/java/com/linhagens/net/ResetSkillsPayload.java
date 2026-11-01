package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: devolver todos os pontos gastos. */
public record ResetSkillsPayload() implements CustomPayload {
    public static final ResetSkillsPayload INSTANCE = new ResetSkillsPayload();
    public static final CustomPayload.Id<ResetSkillsPayload> ID = new CustomPayload.Id<>(Linhagens.id("reset_skills"));
    public static final PacketCodec<RegistryByteBuf, ResetSkillsPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
