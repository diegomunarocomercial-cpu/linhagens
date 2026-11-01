package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.IceDragonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class IceDragonRenderer extends MobEntityRenderer<IceDragonEntity, IceDragonModel> {
    private static final Identifier TEXTURE = Linhagens.id("textures/entity/ice_dragon.png");

    public IceDragonRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IceDragonModel(ctx.getPart(IceDragonModel.LAYER)), 1.2f);
    }

    @Override
    protected void scale(IceDragonEntity entity, MatrixStack matrices, float amount) {
        float s = entity.isBoss() ? 2.3f : 0.8f;
        matrices.scale(s, s, s);
        this.shadowRadius = entity.isBoss() ? 2.6f : 0.9f;
    }

    @Override
    public Identifier getTexture(IceDragonEntity entity) {
        return TEXTURE;
    }
}
