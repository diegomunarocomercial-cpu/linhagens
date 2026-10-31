package com.linhagens.net;

import com.linhagens.race.Race;
import com.linhagens.server.RaceManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {}

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SelectRacePayload.ID, SelectRacePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UseAbilityPayload.ID, UseAbilityPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RaceSyncPayload.ID, RaceSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StatePayload.ID, StatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenMenuPayload.ID, OpenMenuPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SelectRacePayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> {
                    Race r = Race.byId(payload.raceId());
                    if (r != null) RaceManager.setRace(ctx.player(), r, false);
                }));

        ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> RaceManager.useAbility(ctx.player(), payload.slot())));
    }
}
