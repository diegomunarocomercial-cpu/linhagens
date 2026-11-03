package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.CacadorMarcadosEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Caçador de Marcados: sobretudo longo preto com gola alta e lapelas pontudas (forro roxo), cabelo prateado desgrenhado. */
public class CacadorModel extends BipedEntityModel<CacadorMarcadosEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Linhagens.id("cacador_marcados"), "main");
    private final ModelPart tails;

    public CacadorModel(ModelPart root) {
        super(root);
        tails = this.body.getChild("coat_tail");
    }

    public static TexturedModelData createModel() {
        ModelData md = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData root = md.getRoot();
        ModelPartData body = root.getChild("body");
        body.addChild("coat", ModelPartBuilder.create().uv(64, 0).cuboid(-4.5f, 0f, -2.5f, 9f, 18f, 5f), ModelTransform.NONE);
        body.addChild("collar", ModelPartBuilder.create().uv(64, 24).cuboid(-4.5f, -3f, -2.5f, 9f, 3f, 5f, new Dilation(0.15f)), ModelTransform.NONE);
        body.addChild("lapel_l", ModelPartBuilder.create().uv(96, 0).cuboid(0f, -2f, -0.5f, 3f, 7f, 1f), ModelTransform.of(1f, 0f, -2.9f, -0.1f, 0f, -0.3f));
        body.addChild("lapel_r", ModelPartBuilder.create().mirrored().uv(96, 0).cuboid(-3f, -2f, -0.5f, 3f, 7f, 1f), ModelTransform.of(-1f, 0f, -2.9f, -0.1f, 0f, 0.3f));
        body.addChild("coat_tail", ModelPartBuilder.create().uv(96, 10).cuboid(-4.5f, 0f, 0f, 9f, 11f, 1f), ModelTransform.pivot(0f, 12f, 2.3f));
        return TexturedModelData.of(md, 128, 64);
    }

    @Override
    public void setAngles(CacadorMarcadosEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        tails.pitch = 0.05f + limbDistance * 0.7f + MathHelper.sin(animationProgress * 0.07f) * 0.04f;
    }
}
