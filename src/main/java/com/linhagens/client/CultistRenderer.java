package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.OcultistaEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class CultistRenderer extends BipedEntityRenderer<OcultistaEntity, CultistModel> {
    private static final Identifier TEXTURE = Linhagens.id("textures/entity/ocultista_diablos.png");

    public CultistRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CultistModel(ctx.getPart(CultistModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(OcultistaEntity entity) {
        return TEXTURE;
    }
}
