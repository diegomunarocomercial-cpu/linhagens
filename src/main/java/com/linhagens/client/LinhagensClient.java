package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.ModEntities;
import com.linhagens.net.OpenMenuPayload;
import com.linhagens.net.RaceSyncPayload;
import com.linhagens.net.SkillsPayload;
import com.linhagens.net.StatePayload;
import com.linhagens.net.UseAbilityPayload;
import com.linhagens.race.Race;
import java.util.HashSet;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class LinhagensClient implements ClientModInitializer {
    public static KeyBinding OPEN_MENU;
    public static KeyBinding OPEN_SKILLS;
    public static KeyBinding ABILITY_1;
    public static KeyBinding ABILITY_2;

    @Override
    public void onInitializeClient() {
        OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.linhagens.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.linhagens"));
        OPEN_SKILLS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.linhagens.skills", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.linhagens"));
        ABILITY_1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.linhagens.ability1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.linhagens"));
        ABILITY_2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.linhagens.ability2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.linhagens"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (OPEN_MENU.wasPressed()) {
                if (mc.player != null && mc.currentScreen == null) mc.setScreen(new RaceSelectScreen());
            }
            while (OPEN_SKILLS.wasPressed()) {
                if (mc.player != null && mc.currentScreen == null) mc.setScreen(new SkillTreeScreen());
            }
            while (ABILITY_1.wasPressed()) {
                if (mc.player != null && mc.currentScreen == null) ClientPlayNetworking.send(new UseAbilityPayload(0));
            }
            while (ABILITY_2.wasPressed()) {
                if (mc.player != null && mc.currentScreen == null) ClientPlayNetworking.send(new UseAbilityPayload(1));
            }
        });

        // Pacotes do servidor
        ClientPlayNetworking.registerGlobalReceiver(RaceSyncPayload.ID, (payload, ctx) ->
                ctx.client().execute(() -> {
                    try {
                        RaceClient.set(UUID.fromString(payload.uuid()), Race.byId(payload.raceId()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }));
        ClientPlayNetworking.registerGlobalReceiver(StatePayload.ID, (payload, ctx) ->
                ctx.client().execute(() -> RaceClient.state = payload));
        ClientPlayNetworking.registerGlobalReceiver(SkillsPayload.ID, (payload, ctx) ->
                ctx.client().execute(() -> {
                    HashSet<String> set = new HashSet<>();
                    for (String id : payload.csv().split(",")) if (!id.isEmpty()) set.add(id);
                    RaceClient.skills = set;
                }));
        ClientPlayNetworking.registerGlobalReceiver(OpenMenuPayload.ID, (payload, ctx) ->
                ctx.client().execute(() -> ctx.client().setScreen(new RaceSelectScreen())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> RaceClient.clear());

        // HUD
        HudRenderCallback.EVENT.register(RaceHud::render);

        // Renderizadores dos mobs novos
        EntityRendererRegistry.register(ModEntities.IMP,
                ctx -> new BipedMobRenderer<>(ctx, Linhagens.id("textures/entity/imp_infernal.png")));
        EntityRendererRegistry.register(ModEntities.SHADOW_SWORDSMAN,
                ctx -> new BipedMobRenderer<>(ctx, Linhagens.id("textures/entity/espadachim_sombrio.png")));
        EntityRendererRegistry.register(ModEntities.AURORA_GUARDIAN, AuroraGuardianRenderer::new);

        EntityRendererRegistry.register(ModEntities.OCULTISTA, CultistRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICE_DRAGON, IceDragonRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICE_DRAGON_BABY, IceDragonRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(IceDragonModel.LAYER, IceDragonModel::createModel);
        EntityModelLayerRegistry.registerModelLayer(CultistModel.LAYER, CultistModel::createModel);

        // Skins das raças (camada extra no renderizador de jogadores)
        EntityModelLayerRegistry.registerModelLayer(RaceModel.CLASSIC, () -> RaceModel.createModel(false));
        EntityModelLayerRegistry.registerModelLayer(RaceModel.SLIM, () -> RaceModel.createModel(true));
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof PlayerEntityRenderer pr) {
                helper.register(new RaceLayer(pr, ctx));
            }
        });
    }
}
