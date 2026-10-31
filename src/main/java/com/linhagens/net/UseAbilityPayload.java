package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: usar habilidade (slot 0 ou 1). */
public record UseAbilityPayload(int slot) implements CustomPayload {
    public static final CustomPayload.Id<UseAbilityPayload> ID = new CustomPayload.Id<>(Linhagens.id("use_ability"));
    public static final PacketCodec<RegistryByteBuf, UseAbilityPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, UseAbilityPayload::slot, UseAbilityPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
