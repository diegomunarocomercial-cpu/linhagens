package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.AuroraGuardianEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.util.Identifier;

public class AuroraGuardianRenderer extends MobEntityRenderer<AuroraGuardianEntity, IronGolemEntityModel<AuroraGuardianEntity>> {
    private static final Identifier TEXTURE = Linhagens.id("textures/entity/aurora_guardian.png");

    public AuroraGuardianRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IronGolemEntityModel<>(ctx.getPart(EntityModelLayers.IRON_GOLEM)), 0.7f);
    }

    @Override
    public Identifier getTexture(AuroraGuardianEntity entity) {
        return TEXTURE;
    }
}
