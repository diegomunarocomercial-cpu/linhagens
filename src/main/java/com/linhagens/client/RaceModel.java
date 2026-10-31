package com.linhagens.client;

import com.linhagens.Linhagens;
import com.linhagens.race.Race;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Modelo extra desenhado POR CIMA da skin do jogador: um "tom de pele" translúcido (cada parte do corpo
 * um pouco maior) + acessórios (chifres, auréola, asas, cauda, orelhas, focinho...).
 * Textura 128x128: skin em 0..63 x 0..63, acessórios no resto (ver tools/gerar_texturas.py).
 */
public class RaceModel {
    public static final EntityModelLayer CLASSIC = new EntityModelLayer(Linhagens.id("race_classic"), "main");
    public static final EntityModelLayer SLIM = new EntityModelLayer(Linhagens.id("race_slim"), "main");

    private final ModelPart root, head, body, rArm, lArm, rLeg, lLeg;
    private final ModelPart hornL, hornR, halo, earL, earR, snout, tusks, wingL, wingR, tail;

    public RaceModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rArm = root.getChild("right_arm");
        lArm = root.getChild("left_arm");
        rLeg = root.getChild("right_leg");
        lLeg = root.getChild("left_leg");
        hornL = head.getChild("horn_l");
        hornR = head.getChild("horn_r");
        halo = head.getChild("halo");
        earL = head.getChild("ear_l");
        earR = head.getChild("ear_r");
        snout = head.getChild("snout");
        tusks = head.getChild("tusks");
        wingL = body.getChild("wing_l");
        wingR = body.getChild("wing_r");
        tail = body.getChild("tail");
    }

    public static TexturedModelData createModel(boolean slim) {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();
        Dilation skin = new Dilation(0.3f);

        // ---- cabeça + acessórios
        ModelPartData head = r.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8f, 8f, 8f, new Dilation(0.55f)),
                ModelTransform.NONE);

        head.addChild("horn_l", ModelPartBuilder.create()
                        .uv(64, 0).cuboid(-1f, -3f, -1f, 2f, 3f, 2f)
                        .uv(64, 8).cuboid(-0.5f, -5f, -0.5f, 1f, 2f, 1f),
                ModelTransform.of(3f, -8f, 0f, 0f, 0f, 0.3f));
        head.addChild("horn_r", ModelPartBuilder.create().mirrored()
                        .uv(64, 0).cuboid(-1f, -3f, -1f, 2f, 3f, 2f)
                        .uv(64, 8).cuboid(-0.5f, -5f, -0.5f, 1f, 2f, 1f),
                ModelTransform.of(-3f, -8f, 0f, 0f, 0f, -0.3f));

        // Auréola quadrada feita de 4 barras de ouro, flutuando sobre a cabeça.
        head.addChild("halo", ModelPartBuilder.create()
                        .uv(64, 16).cuboid(-5f, 0f, -5f, 10f, 1f, 1f)
                        .uv(64, 16).cuboid(-5f, 0f, 4f, 10f, 1f, 1f)
                        .uv(64, 20).cuboid(-5f, 0f, -4f, 1f, 1f, 8f)
                        .uv(64, 20).cuboid(4f, 0f, -4f, 1f, 1f, 8f),
                ModelTransform.pivot(0f, -12f, 0f));

        head.addChild("ear_l", ModelPartBuilder.create().uv(64, 32).cuboid(0f, -2f, -2f, 3f, 4f, 4f),
                ModelTransform.of(4f, -4f, 0f, 0f, 0f, -0.35f));
        head.addChild("ear_r", ModelPartBuilder.create().mirrored().uv(64, 32).cuboid(-3f, -2f, -2f, 3f, 4f, 4f),
                ModelTransform.of(-4f, -4f, 0f, 0f, 0f, 0.35f));
        head.addChild("snout", ModelPartBuilder.create().uv(64, 48).cuboid(-2f, -3f, -5f, 4f, 3f, 1f),
                ModelTransform.NONE);
        head.addChild("tusks", ModelPartBuilder.create()
                        .uv(64, 56).cuboid(-2.5f, -1.5f, -4.6f, 1f, 2f, 1f)
                        .uv(64, 56).cuboid(1.5f, -1.5f, -4.6f, 1f, 2f, 1f),
                ModelTransform.NONE);

        // ---- corpo + acessórios
        ModelPartData body = r.addChild("body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4f, 0f, -2f, 8f, 12f, 4f, skin),
                ModelTransform.NONE);
        body.addChild("wing_l", ModelPartBuilder.create().uv(0, 64).cuboid(0f, -10f, 0f, 12f, 14f, 1f),
                ModelTransform.pivot(1f, 2f, 2.4f));
        body.addChild("wing_r", ModelPartBuilder.create().mirrored().uv(0, 64).cuboid(-12f, -10f, 0f, 12f, 14f, 1f),
                ModelTransform.pivot(-1f, 2f, 2.4f));
        body.addChild("tail", ModelPartBuilder.create()
                        .uv(32, 64).cuboid(-0.5f, -0.5f, 0f, 1f, 1f, 6f)
                        .uv(48, 64).cuboid(-1.5f, -1.5f, 6f, 3f, 3f, 1f),
                ModelTransform.of(0f, 11f, 2.4f, 0.3f, 0f, 0f));

        // ---- braços e pernas (clássico 4px ou slim 3px)
        if (slim) {
            r.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-2f, -2f, -2f, 3f, 12f, 4f, skin), ModelTransform.NONE);
            r.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1f, -2f, -2f, 3f, 12f, 4f, skin), ModelTransform.NONE);
        } else {
            r.addChild("right_arm", ModelPartBuilder.create().uv(40, 16).cuboid(-3f, -2f, -2f, 4f, 12f, 4f, skin), ModelTransform.NONE);
            r.addChild("left_arm", ModelPartBuilder.create().uv(32, 48).cuboid(-1f, -2f, -2f, 4f, 12f, 4f, skin), ModelTransform.NONE);
        }
        r.addChild("right_leg", ModelPartBuilder.create().uv(0, 16).cuboid(-2f, 0f, -2f, 4f, 12f, 4f, skin), ModelTransform.NONE);
        r.addChild("left_leg", ModelPartBuilder.create().uv(16, 48).cuboid(-2f, 0f, -2f, 4f, 12f, 4f, skin), ModelTransform.NONE);

        return TexturedModelData.of(md, 128, 128);
    }

    /** Copia a pose do modelo do jogador, liga/desliga os acessórios da raça e anima. */
    public void setup(Race race, PlayerEntityModel<AbstractClientPlayerEntity> src, boolean helmet, float age, boolean airborne) {
        head.copyTransform(src.head);
        body.copyTransform(src.body);
        rArm.copyTransform(src.rightArm);
        lArm.copyTransform(src.leftArm);
        rLeg.copyTransform(src.rightLeg);
        lLeg.copyTransform(src.leftLeg);

        hornL.visible = hornR.visible = race == Race.DEMONIO && !helmet;
        tail.visible = race == Race.DEMONIO;
        halo.visible = race == Race.ANJO;
        wingL.visible = wingR.visible = race == Race.ANJO;
        earL.visible = earR.visible = snout.visible = tusks.visible = race == Race.PIGLIN && !helmet;

        halo.pivotY = -12f + MathHelper.sin(age * 0.1f) * 0.6f;
        halo.yaw = age * 0.04f;

        float flap = airborne ? MathHelper.sin(age * 0.6f) * 0.55f : MathHelper.sin(age * 0.08f) * 0.08f;
        wingL.roll = 0.12f + flap;
        wingR.roll = -(0.12f + flap);

        tail.yaw = MathHelper.sin(age * 0.12f) * 0.25f;
    }

    public void render(MatrixStack matrices, VertexConsumer vc, int light, int overlay) {
        root.render(matrices, vc, light, overlay);
    }
}
