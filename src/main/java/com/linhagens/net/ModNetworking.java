package com.linhagens.net;

import com.linhagens.race.Race;
import com.linhagens.server.RaceManager;
import com.linhagens.server.SkillManager;
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
        PayloadTypeRegistry.playS2C().register(SkillsPayload.ID, SkillsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AuroraBoostPayload.ID, AuroraBoostPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UnlockSkillPayload.ID, UnlockSkillPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ResetSkillsPayload.ID, ResetSkillsPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SelectRacePayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> {
                    Race r = Race.byId(payload.raceId());
                    if (r != null) RaceManager.setRace(ctx.player(), r, false);
                }));

        ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> RaceManager.useAbility(ctx.player(), payload.slot())));

        ServerPlayNetworking.registerGlobalReceiver(UnlockSkillPayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> SkillManager.unlock(ctx.player(), payload.id())));

        ServerPlayNetworking.registerGlobalReceiver(ResetSkillsPayload.ID, (payload, ctx) ->
                ctx.server().execute(() -> SkillManager.reset(ctx.player())));
    }
}
