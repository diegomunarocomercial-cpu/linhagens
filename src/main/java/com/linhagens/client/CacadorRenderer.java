package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.CacadorMarcadosEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class CacadorRenderer extends BipedEntityRenderer<CacadorMarcadosEntity, CacadorModel> {
    private static final Identifier TEXTURE = Linhagens.id("textures/entity/cacador_marcados.png");

    public CacadorRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CacadorModel(ctx.getPart(CacadorModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(CacadorMarcadosEntity entity) {
        return TEXTURE;
    }
}
