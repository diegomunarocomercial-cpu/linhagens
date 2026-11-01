package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.OcultistaEntity;
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

/** Ocultista: corpo humanoide + capuz, ombreiras, túnica longa e uma capa vermelha com símbolo dourado nas costas. */
public class CultistModel extends BipedEntityModel<OcultistaEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Linhagens.id("ocultista"), "main");

    private final ModelPart cape;

    public CultistModel(ModelPart root) {
        super(root);
        this.cape = this.body.getChild("cape");
    }

    public static TexturedModelData createModel() {
        ModelData md = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData root = md.getRoot();

        ModelPartData head = root.getChild("head");
        head.addChild("hood", ModelPartBuilder.create().uv(64, 0).cuboid(-4f, -8f, -4f, 8f, 8f, 8f, new Dilation(0.6f)),
                ModelTransform.NONE);

        ModelPartData body = root.getChild("body");
        body.addChild("shoulders", ModelPartBuilder.create().uv(64, 40).cuboid(-5.5f, -0.5f, -2.5f, 11f, 3f, 5f),
                ModelTransform.NONE);
        body.addChild("robe", ModelPartBuilder.create().uv(96, 16).cuboid(-4.5f, 10f, -3f, 9f, 13f, 6f),
                ModelTransform.NONE);
        body.addChild("cape", ModelPartBuilder.create().uv(64, 16).cuboid(-5f, 0f, 3.2f, 10f, 22f, 1f),
                ModelTransform.pivot(0f, 0.5f, 0f));

        return TexturedModelData.of(md, 128, 64);
    }

    @Override
    public void setAngles(OcultistaEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        this.cape.pitch = 0.08f + limbDistance * 0.6f + MathHelper.sin(animationProgress * 0.08f) * 0.03f;
    }
}
