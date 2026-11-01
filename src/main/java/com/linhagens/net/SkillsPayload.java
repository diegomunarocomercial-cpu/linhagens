package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> jogador: ids das habilidades desbloqueadas, separados por vírgula. */
public record SkillsPayload(String csv) implements CustomPayload {
    public static final CustomPayload.Id<SkillsPayload> ID = new CustomPayload.Id<>(Linhagens.id("skills"));
    public static final PacketCodec<RegistryByteBuf, SkillsPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, SkillsPayload::csv, SkillsPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
