package com.linhagens.net;

import com.linhagens.Linhagens;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor -> Cliente: abra o menu de escolha de raça. */
public record OpenMenuPayload() implements CustomPayload {
    public static final OpenMenuPayload INSTANCE = new OpenMenuPayload();
    public static final CustomPayload.Id<OpenMenuPayload> ID = new CustomPayload.Id<>(Linhagens.id("open_menu"));
    public static final PacketCodec<RegistryByteBuf, OpenMenuPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
