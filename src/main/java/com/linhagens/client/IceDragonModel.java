package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.entity.IceDragonEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Modelo do Dragão de Gelo: corpo, pescoço de 2 segmentos, cabeça com mandíbula e chifres, cauda de 3 segmentos,
 * 2 asas e 4 patas. Textura 128x128 (ver tools/gerar_itens.py).
 */
public class IceDragonModel extends EntityModel<IceDragonEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Linhagens.id("ice_dragon"), "main");

    private final ModelPart root, neck1, neck2, head, jaw, tail1, tail2, tail3, wingL, wingR, legFL, legFR, legBL, legBR;

    public IceDragonModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        neck1 = body.getChild("neck1");
        neck2 = neck1.getChild("neck2");
        head = neck2.getChild("head");
        jaw = head.getChild("jaw");
        tail1 = body.getChild("tail1");
        tail2 = tail1.getChild("tail2");
        tail3 = tail2.getChild("tail3");
        wingL = body.getChild("wing_l");
        wingR = body.getChild("wing_r");
        legFL = root.getChild("leg_fl");
        legFR = root.getChild("leg_fr");
        legBL = root.getChild("leg_bl");
        legBR = root.getChild("leg_br");
    }

    public static TexturedModelData createModel() {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();

        ModelPartData body = r.addChild("body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-6f, -5f, -10f, 12f, 10f, 20f),
                ModelTransform.pivot(0f, 10f, 0f));
        body.addChild("spikes", ModelPartBuilder.create().uv(104, 60)
                        .cuboid(-1f, -8f, -8f, 2f, 3f, 2f).cuboid(-1f, -8f, -3f, 2f, 3f, 2f)
                        .cuboid(-1f, -8f, 2f, 2f, 3f, 2f).cuboid(-1f, -8f, 7f, 2f, 3f, 2f),
                ModelTransform.NONE);

        ModelPartData neck1 = body.addChild("neck1",
                ModelPartBuilder.create().uv(0, 32).cuboid(-3f, -3f, -6f, 6f, 6f, 6f),
                ModelTransform.of(0f, -2f, -9f, -0.55f, 0f, 0f));
        ModelPartData neck2 = neck1.addChild("neck2",
                ModelPartBuilder.create().uv(24, 32).cuboid(-2.5f, -2.5f, -6f, 5f, 5f, 6f),
                ModelTransform.of(0f, 0f, -5f, 0.45f, 0f, 0f));
        ModelPartData head = neck2.addChild("head",
                ModelPartBuilder.create().uv(48, 32).cuboid(-4f, -3.5f, -8f, 8f, 7f, 8f),
                ModelTransform.of(0f, 0f, -5f, 0.1f, 0f, 0f));
        head.addChild("snout", ModelPartBuilder.create().uv(0, 48).cuboid(-3f, -1.5f, -13f, 6f, 3f, 5f), ModelTransform.NONE);
        head.addChild("jaw", ModelPartBuilder.create().uv(24, 48).cuboid(-2.5f, 0f, -6f, 5f, 2f, 6f),
                ModelTransform.pivot(0f, 1.5f, -7f));
        head.addChild("horn_l", ModelPartBuilder.create().uv(48, 48).cuboid(-1f, -1f, 0f, 2f, 2f, 6f),
                ModelTransform.of(3f, -3f, -1f, 0.5f, 0.25f, 0f));
        head.addChild("horn_r", ModelPartBuilder.create().mirrored().uv(48, 48).cuboid(-1f, -1f, 0f, 2f, 2f, 6f),
                ModelTransform.of(-3f, -3f, -1f, 0.5f, -0.25f, 0f));

        ModelPartData tail1 = body.addChild("tail1",
                ModelPartBuilder.create().uv(24, 60).cuboid(-3f, -3f, 0f, 6f, 6f, 8f),
                ModelTransform.of(0f, 1f, 9.5f, -0.1f, 0f, 0f));
        ModelPartData tail2 = tail1.addChild("tail2",
                ModelPartBuilder.create().uv(56, 60).cuboid(-2f, -2f, 0f, 4f, 4f, 8f),
                ModelTransform.pivot(0f, 0f, 8f));
        tail2.addChild("tail3", ModelPartBuilder.create().uv(80, 60).cuboid(-1f, -1f, 0f, 2f, 2f, 8f)
                        .uv(104, 60).cuboid(-1f, -3f, 5f, 2f, 3f, 2f),
                ModelTransform.pivot(0f, 0f, 8f));

        body.addChild("wing_l", ModelPartBuilder.create().uv(0, 80).cuboid(0f, -1f, -1f, 12f, 2f, 2f)
                        .uv(32, 80).cuboid(1f, 0f, 1f, 11f, 1f, 10f),
                ModelTransform.pivot(6f, -4f, -3f));
        body.addChild("wing_r", ModelPartBuilder.create().mirrored().uv(0, 80).cuboid(-12f, -1f, -1f, 12f, 2f, 2f)
                        .uv(32, 80).cuboid(-12f, 0f, 1f, 11f, 1f, 10f),
                ModelTransform.pivot(-6f, -4f, -3f));

        ModelPartBuilder leg = ModelPartBuilder.create().uv(0, 60).cuboid(-2.5f, 0f, -2.5f, 5f, 9f, 5f);
        r.addChild("leg_fl", leg, ModelTransform.pivot(5.5f, 15f, -6f));
        r.addChild("leg_fr", leg, ModelTransform.pivot(-5.5f, 15f, -6f));
        r.addChild("leg_bl", leg, ModelTransform.pivot(5.5f, 15f, 6f));
        r.addChild("leg_br", leg, ModelTransform.pivot(-5.5f, 15f, 6f));

        return TexturedModelData.of(md, 128, 128);
    }

    @Override
    public void setAngles(IceDragonEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        float swing = limbAngle * 0.6662f;
        float amp = limbDistance * 1.2f;
        legFL.pitch = MathHelper.cos(swing) * amp;
        legBR.pitch = MathHelper.cos(swing) * amp;
        legFR.pitch = MathHelper.cos(swing + (float) Math.PI) * amp;
        legBL.pitch = MathHelper.cos(swing + (float) Math.PI) * amp;

        float yaw = headYaw * ((float) Math.PI / 180f);
        float pitch = headPitch * ((float) Math.PI / 180f);
        neck1.yaw = yaw * 0.35f;
        neck2.yaw = yaw * 0.3f;
        head.yaw = yaw * 0.35f;
        head.pitch = 0.1f + pitch * 0.5f;

        jaw.pitch = entity.isBreathing() ? 0.55f : 0.04f + MathHelper.sin(animationProgress * 0.07f) * 0.03f;

        tail1.yaw = MathHelper.sin(animationProgress * 0.09f) * 0.15f;
        tail2.yaw = MathHelper.sin(animationProgress * 0.09f - 0.6f) * 0.2f;
        tail3.yaw = MathHelper.sin(animationProgress * 0.09f - 1.2f) * 0.25f;

        float flap = MathHelper.sin(animationProgress * 0.08f) * 0.12f;
        wingL.roll = 0.35f + flap;
        wingR.roll = -0.35f - flap;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(matrices, vertices, light, overlay, color);
    }
}
