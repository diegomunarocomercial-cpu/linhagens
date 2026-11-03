package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.DemonioMonstroEntity;
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

/** Monstro Demônio: corpo musculoso com chifres, asas de morcego, cauda e espinhos nos ombros. */
public class DemonioModel extends BipedEntityModel<DemonioMonstroEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Linhagens.id("demonio_monstro"), "main");
    private final ModelPart batL, batR, tail;

    public DemonioModel(ModelPart root) {
        super(root);
        batL = this.body.getChild("bat_l");
        batR = this.body.getChild("bat_r");
        tail = this.body.getChild("tail");
    }

    public static TexturedModelData createModel() {
        ModelData md = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData root = md.getRoot();
        ModelPartData head = root.getChild("head");
        head.addChild("horn_l", ModelPartBuilder.create().uv(64, 0).cuboid(-1.5f, -5f, -1.5f, 3f, 5f, 3f)
                        .uv(64, 10).cuboid(-1f, -9f, -1f, 2f, 4f, 2f), ModelTransform.of(3f, -7f, 0f, 0.2f, 0f, 0.35f));
        head.addChild("horn_r", ModelPartBuilder.create().mirrored().uv(64, 0).cuboid(-1.5f, -5f, -1.5f, 3f, 5f, 3f)
                        .uv(64, 10).cuboid(-1f, -9f, -1f, 2f, 4f, 2f), ModelTransform.of(-3f, -7f, 0f, 0.2f, 0f, -0.35f));
        ModelPartData body = root.getChild("body");
        body.addChild("bat_l", ModelPartBuilder.create().uv(64, 18).cuboid(0f, -13f, 0f, 13f, 14f, 1f), ModelTransform.of(2f, 3f, 2.5f, 0f, 0f, 0.35f));
        body.addChild("bat_r", ModelPartBuilder.create().mirrored().uv(64, 18).cuboid(-13f, -13f, 0f, 13f, 14f, 1f), ModelTransform.of(-2f, 3f, 2.5f, 0f, 0f, -0.35f));
        body.addChild("tail", ModelPartBuilder.create().uv(64, 36).cuboid(-1f, -1f, 0f, 2f, 2f, 6f)
                        .uv(96, 36).cuboid(-1.5f, -1.5f, 6f, 3f, 3f, 1f), ModelTransform.of(0f, 11f, 2f, 0.6f, 0f, 0f));
        for (int s : new int[]{-1, 1}) {
            body.addChild(s < 0 ? "spike_r" : "spike_l", ModelPartBuilder.create().uv(64, 10).cuboid(-1f, -4f, -1f, 2f, 4f, 2f),
                    ModelTransform.of(s * 5.5f, 0f, 0f, 0f, 0f, s * 0.5f));
        }
        return TexturedModelData.of(md, 128, 64);
    }

    @Override
    public void setAngles(DemonioMonstroEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        float flap = MathHelper.sin(animationProgress * 0.12f) * 0.08f;
        batL.roll = 0.35f + flap;
        batR.roll = -0.35f - flap;
        tail.yaw = MathHelper.sin(animationProgress * 0.1f) * 0.3f;
    }
}
