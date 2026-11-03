package com.linhagens.quest;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

/** /missao aceitar  e  /reino sair  (qualquer jogador). */
public final class QuestCommands {
    private QuestCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("missao")
                    .then(CommandManager.literal("aceitar").executes(ctx -> {
                        QuestManager.accept(ctx.getSource().getPlayerOrThrow());
                        return 1;
                    })));
            dispatcher.register(CommandManager.literal("reino")
                    .then(CommandManager.literal("sair").executes(ctx -> {
                        QuestManager.leaveReino(ctx.getSource().getPlayerOrThrow());
                        return 1;
                    })));
        });
    }
}
