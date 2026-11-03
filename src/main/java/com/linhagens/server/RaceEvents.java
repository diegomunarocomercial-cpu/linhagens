package com.linhagens.server;

import com.linhagens.net.OpenMenuPayload;
import com.linhagens.race.Race;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RaceEvents {
    private RaceEvents() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.player;
            RaceManager.applyModifiers(p);
            RaceManager.sendAllTo(p);
            RaceManager.broadcast(p);
            RaceManager.sendState(p, true);
            SkillManager.sendSkills(p);
            if (RaceManager.get(p).race() == null) {
                ServerPlayNetworking.send(p, OpenMenuPayload.INSTANCE);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                RaceManager.forget(handler.player.getUuid()));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            RaceManager.applyModifiers(newPlayer);
            RaceManager.broadcast(newPlayer);
            RaceManager.sendAllTo(newPlayer);
            RaceManager.sendState(newPlayer, true);
            SkillManager.sendSkills(newPlayer);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            com.linhagens.quest.QuestManager.tick(server);
            com.linhagens.quest.PianoSummon.tick(server);
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                RaceManager.tick(p, server);
                SetBonuses.tick(p, server.getTicks());
            }
        });

        // Matar criaturas dá XP de linhagem.
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
            if (entity instanceof ServerPlayerEntity p) {
                Race r = RaceManager.get(p).race();
                if (r != null) {
                    int gain = Math.max(3, (int) (killed.getMaxHealth() / 2f));
                    RaceManager.addRaceXp(p, gain);
                    if (com.linhagens.race.SkillEffects.of(RaceManager.get(p)).has(com.linhagens.race.Perk.LOOTER)) {
                        killed.dropStack(new net.minecraft.item.ItemStack(net.minecraft.item.Items.GOLD_NUGGET, 1 + world.random.nextInt(3)));
                    }
                }
            }
        });
    }
}
