package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.DemonioMonstroEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DemonioRenderer extends BipedEntityRenderer<DemonioMonstroEntity, DemonioModel> {
    private static final Identifier TEXTURE = Linhagens.id("textures/entity/demonio_monstro.png");

    public DemonioRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DemonioModel(ctx.getPart(DemonioModel.LAYER)), 0.7f);
    }

    @Override
    protected void scale(DemonioMonstroEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.3f, 1.3f, 1.3f);
    }

    @Override
    public Identifier getTexture(DemonioMonstroEntity entity) {
        return TEXTURE;
    }
}
