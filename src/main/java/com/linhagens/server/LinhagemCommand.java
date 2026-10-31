package com.linhagens.server;

import com.linhagens.race.Race;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /linhagem set <jogador> <raca>   (troca sem recarga)
 * /linhagem reset <jogador>        (volta a não ter raça e reabre o menu)
 */
public final class LinhagemCommand {
    private LinhagemCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("linhagem")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("jogador", EntityArgumentType.player())
                                        .then(CommandManager.argument("raca", StringArgumentType.word())
                                                .suggests((c, b) -> CommandSource.suggestMatching(
                                                        Arrays.stream(Race.values()).map(r -> r.id).toList(), b))
                                                .executes(ctx -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "jogador");
                                                    Race r = Race.byId(StringArgumentType.getString(ctx, "raca"));
                                                    if (r == null) {
                                                        ctx.getSource().sendError(Text.literal("Raça desconhecida."));
                                                        return 0;
                                                    }
                                                    RaceManager.setRace(target, r, true);
                                                    ctx.getSource().sendFeedback(() ->
                                                            Text.literal(target.getName().getString() + " agora é " + r.nome), true);
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("reset")
                                .then(CommandManager.argument("jogador", EntityArgumentType.player())
                                        .executes(ctx -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "jogador");
                                            RaceManager.resetRace(target);
                                            ctx.getSource().sendFeedback(() ->
                                                    Text.literal("Raça de " + target.getName().getString() + " resetada."), true);
                                            return 1;
                                        })))));
    }
}
