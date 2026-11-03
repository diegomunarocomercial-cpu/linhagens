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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Modelo desenhado POR CIMA da skin do jogador: "tom de pele" translúcido + acessórios de cada raça.
 *  Humano: faixa de cabeça dourada e capa de viajante.   Demônio: chifres de 3 segmentos, asas de morcego e cauda de 3 segmentos com ponta.
 *  Anjo: auréola octogonal e duas camadas de asas de penas.   Piglin: focinho, orelhas grandes com argola de ouro, presas e juba.
 * Textura 128x128 (ver tools/gerar_texturas.py).
 */
public class RaceModel {
    public static final EntityModelLayer CLASSIC = new EntityModelLayer(Linhagens.id("race_classic"), "main");
    public static final EntityModelLayer SLIM = new EntityModelLayer(Linhagens.id("race_slim"), "main");

    private final ModelPart root, head, body, rArm, lArm, rLeg, lLeg;
    private final ModelPart headband, hornL, hornR, halo, earL, earR, snout, tusks;
    private final ModelPart cape, wingL, wingL2, wingR, wingR2, batL, batR, tail, tail2, tail3, mane;

    public RaceModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rArm = root.getChild("right_arm");
        lArm = root.getChild("left_arm");
        rLeg = root.getChild("right_leg");
        lLeg = root.getChild("left_leg");
        headband = head.getChild("headband");
        hornL = head.getChild("horn_l");
        hornR = head.getChild("horn_r");
        halo = head.getChild("halo");
        earL = head.getChild("ear_l");
        earR = head.getChild("ear_r");
        snout = head.getChild("snout");
        tusks = head.getChild("tusks");
        cape = body.getChild("cape");
        wingL = body.getChild("wing_l");
        wingL2 = body.getChild("wing_l2");
        wingR = body.getChild("wing_r");
        wingR2 = body.getChild("wing_r2");
        batL = body.getChild("bat_l");
        batR = body.getChild("bat_r");
        tail = body.getChild("tail");
        tail2 = tail.getChild("t2");
        tail3 = tail2.getChild("t3");
        mane = body.getChild("mane");
    }

    private static ModelPartData horn(ModelPartData head, String name, float side) {
        ModelPartBuilder base = ModelPartBuilder.create().uv(64, 0).cuboid(-1.5f, -4f, -1.5f, 3f, 4f, 3f);
        if (side < 0) base = ModelPartBuilder.create().mirrored().uv(64, 0).cuboid(-1.5f, -4f, -1.5f, 3f, 4f, 3f);
        ModelPartData h = head.addChild(name, base, ModelTransform.of(side * 3f, -7.5f, -0.5f, 0.15f, 0f, side * 0.28f));
        ModelPartData mid = h.addChild("mid", ModelPartBuilder.create().uv(64, 8).cuboid(-1f, -4f, -1f, 2f, 4f, 2f),
                ModelTransform.of(0f, -3.6f, 0f, 0.25f, 0f, side * 0.35f));
        mid.addChild("tip", ModelPartBuilder.create().uv(64, 16).cuboid(-0.5f, -3f, -0.5f, 1f, 3f, 1f),
                ModelTransform.of(0f, -3.6f, 0f, 0.35f, 0f, side * 0.45f));
        return h;
    }

    public static TexturedModelData createModel(boolean slim) {
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();
        Dilation skin = new Dilation(0.3f);

        // ---- cabeça + acessórios
        ModelPartData head = r.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8f, 8f, 8f, new Dilation(0.55f)), ModelTransform.NONE);
        head.addChild("headband", ModelPartBuilder.create().uv(96, 0).cuboid(-4f, -6.5f, -4f, 8f, 2f, 8f, new Dilation(0.62f)), ModelTransform.NONE);
        horn(head, "horn_l", 1f);
        horn(head, "horn_r", -1f);

        ModelPartData halo = head.addChild("halo", ModelPartBuilder.create(), ModelTransform.pivot(0f, -12f, 0f));
        for (int k = 0; k < 8; k++) {                                  // auréola octogonal: 8 barras
            double a = k * Math.PI / 4.0;
            halo.addChild("b" + k, ModelPartBuilder.create().uv(80, 0).cuboid(-2.5f, 0f, -0.5f, 5f, 1f, 1f),
                    ModelTransform.of((float) (Math.sin(a) * 5.6), 0f, (float) (Math.cos(a) * 5.6), 0f, (float) a, 0f));
        }

        head.addChild("ear_l", ModelPartBuilder.create().uv(64, 24).cuboid(0f, -3f, -0.5f, 4f, 6f, 1f)
                        .uv(76, 24).cuboid(3f, 2f, -0.5f, 1f, 1f, 1f),
                ModelTransform.of(4f, -4f, 0f, 0f, -0.3f, -0.45f));
        head.addChild("ear_r", ModelPartBuilder.create().mirrored().uv(64, 24).cuboid(-4f, -3f, -0.5f, 4f, 6f, 1f)
                        .uv(76, 24).cuboid(-4f, 2f, -0.5f, 1f, 1f, 1f),
                ModelTransform.of(-4f, -4f, 0f, 0f, 0.3f, 0.45f));
        head.addChild("snout", ModelPartBuilder.create().uv(64, 32).cuboid(-3f, -3.2f, -6f, 6f, 4f, 2f), ModelTransform.NONE);
        head.addChild("tusks", ModelPartBuilder.create().uv(84, 32).cuboid(-2.8f, -0.6f, -5.6f, 1f, 2f, 1f)
                        .uv(84, 32).cuboid(1.8f, -0.6f, -5.6f, 1f, 2f, 1f), ModelTransform.NONE);

        // ---- corpo + acessórios
        ModelPartData body = r.addChild("body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4f, 0f, -2f, 8f, 12f, 4f, skin), ModelTransform.NONE);
        body.addChild("cape", ModelPartBuilder.create().uv(96, 16).cuboid(-4f, 0f, 0f, 8f, 14f, 1f),
                ModelTransform.pivot(0f, 0.5f, 2.5f));
        body.addChild("mane", ModelPartBuilder.create().uv(64, 40).cuboid(-5.5f, -1f, -3f, 11f, 3f, 6f), ModelTransform.NONE);
        // asas de anjo: 2 camadas por lado
        body.addChild("wing_l", ModelPartBuilder.create().uv(0, 64).cuboid(0f, -12f, 0f, 14f, 16f, 1f), ModelTransform.pivot(1f, 2f, 2.5f));
        body.addChild("wing_l2", ModelPartBuilder.create().uv(32, 64).cuboid(0f, -2f, 0f, 11f, 12f, 1f), ModelTransform.pivot(1f, 4f, 2.9f));
        body.addChild("wing_r", ModelPartBuilder.create().mirrored().uv(0, 64).cuboid(-14f, -12f, 0f, 14f, 16f, 1f), ModelTransform.pivot(-1f, 2f, 2.5f));
        body.addChild("wing_r2", ModelPartBuilder.create().mirrored().uv(32, 64).cuboid(-11f, -2f, 0f, 11f, 12f, 1f), ModelTransform.pivot(-1f, 4f, 2.9f));
        // asas de morcego
        body.addChild("bat_l", ModelPartBuilder.create().uv(32, 84).cuboid(0f, -13f, 0f, 13f, 14f, 1f), ModelTransform.pivot(2f, 3f, 2.5f));
        body.addChild("bat_r", ModelPartBuilder.create().mirrored().uv(32, 84).cuboid(-13f, -13f, 0f, 13f, 14f, 1f), ModelTransform.pivot(-2f, 3f, 2.5f));
        // cauda de 3 segmentos com ponta de flecha
        ModelPartData t1 = body.addChild("tail", ModelPartBuilder.create().uv(0, 100).cuboid(-1f, -1f, 0f, 2f, 2f, 5f),
                ModelTransform.of(0f, 11f, 2.4f, 0.5f, 0f, 0f));
        ModelPartData t2 = t1.addChild("t2", ModelPartBuilder.create().uv(16, 100).cuboid(-0.5f, -0.5f, 0f, 1f, 1f, 5f),
                ModelTransform.of(0f, 0f, 5f, -0.5f, 0f, 0f));
        t2.addChild("t3", ModelPartBuilder.create().uv(16, 100).cuboid(-0.5f, -0.5f, 0f, 1f, 1f, 4f)
                        .uv(32, 100).cuboid(-1.5f, -1.5f, 4f, 3f, 3f, 1f),
                ModelTransform.of(0f, 0f, 5f, -0.4f, 0f, 0f));

        // ---- braços e pernas
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

        boolean tint = race != Race.HUMANO;                       // humano: só acessórios, pele original
        rArm.visible = lArm.visible = rLeg.visible = lLeg.visible = tint;
        body.visible = true;
        // o corpo/cabeça "tom de pele" some no humano, mas os acessórios (filhos) continuam
        // (para isso desenhamos a textura do humano com a região de pele totalmente transparente)

        headband.visible = race == Race.HUMANO;
        cape.visible = race == Race.HUMANO;
        hornL.visible = hornR.visible = race == Race.DEMONIO && !helmet;
        batL.visible = batR.visible = race == Race.DEMONIO;
        tail.visible = race == Race.DEMONIO;
        halo.visible = race == Race.ANJO;
        wingL.visible = wingL2.visible = wingR.visible = wingR2.visible = race == Race.ANJO;
        earL.visible = earR.visible = snout.visible = tusks.visible = race == Race.PIGLIN && !helmet;
        mane.visible = race == Race.PIGLIN;

        halo.pivotY = -12f + MathHelper.sin(age * 0.1f) * 0.6f;
        halo.yaw = age * 0.04f;

        float flap = airborne ? MathHelper.sin(age * 0.6f) * 0.6f : MathHelper.sin(age * 0.08f) * 0.08f;
        wingL.roll = 0.15f + flap;       wingR.roll = -(0.15f + flap);
        wingL2.roll = 0.45f + flap * 0.8f; wingR2.roll = -(0.45f + flap * 0.8f);
        batL.roll = 0.25f + flap;        batR.roll = -(0.25f + flap);

        tail.yaw = MathHelper.sin(age * 0.12f) * 0.25f;
        tail2.yaw = MathHelper.sin(age * 0.12f - 0.6f) * 0.3f;
        tail3.yaw = MathHelper.sin(age * 0.12f - 1.2f) * 0.35f;

        cape.pitch = 0.12f + MathHelper.sin(age * 0.07f) * 0.04f + (airborne ? 0.3f : 0f);
        earL.roll = -0.45f + MathHelper.sin(age * 0.09f) * 0.05f;
        earR.roll = 0.45f - MathHelper.sin(age * 0.09f) * 0.05f;
    }

    public void render(MatrixStack matrices, VertexConsumer vc, int light, int overlay) {
        root.render(matrices, vc, light, overlay);
    }
}
