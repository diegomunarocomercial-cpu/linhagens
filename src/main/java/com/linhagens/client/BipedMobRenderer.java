package com.linhagens.client;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

/** Renderizador simples: corpo de zumbi/jogador com uma textura própria (a arma na mão é desenhada sozinha). */
public class BipedMobRenderer<T extends MobEntity> extends BipedEntityRenderer<T, BipedEntityModel<T>> {
    private final Identifier texture;

    public BipedMobRenderer(EntityRendererFactory.Context ctx, Identifier texture) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
        this.texture = texture;
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }
}
