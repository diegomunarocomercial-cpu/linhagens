package com.linhagens.client;

import com.linhagens.race.Race;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;

/** Camada extra do renderizador de jogadores: aplica a "skin de raça" por cima de qualquer skin. */
public class RaceLayer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    private final RaceModel classic;
    private final RaceModel slim;

    public RaceLayer(PlayerEntityRenderer renderer, EntityRendererFactory.Context ctx) {
        super(renderer);
        this.classic = new RaceModel(ctx.getModelLoader().getModelPart(RaceModel.CLASSIC));
        this.slim = new RaceModel(ctx.getModelLoader().getModelPart(RaceModel.SLIM));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vcp, int light, AbstractClientPlayerEntity player,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        Race race = RaceClient.raceFor(player);
        if (race == null || race == Race.HUMANO) return; // humano: skin original, sem alterações

        RaceModel model = player.getSkinTextures().model() == SkinTextures.Model.SLIM ? slim : classic;
        boolean helmet = !player.getEquippedStack(EquipmentSlot.HEAD).isEmpty();
        model.setup(race, getContextModel(), helmet, animationProgress, !player.isOnGround());

        int l = race == Race.ANJO ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light; // anjo brilha
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(RaceTextures.of(race)));
        model.render(matrices, vc, l, OverlayTexture.DEFAULT_UV);
    }
}
