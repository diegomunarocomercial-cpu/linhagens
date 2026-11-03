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

/**
 * Ocultista do Culto Diablos: capuz bordô, gola larga em coroa de espinhos (8 espinhos), camisola de riscas escuras com
 * ramos espinhosos enrolados (manchados de seiva/sangue), grande flor branca e rosada no peito, calças claras e pés descalços.
 */
public class CultistModel extends BipedEntityModel<OcultistaEntity> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(Linhagens.id("ocultista"), "main");

    public CultistModel(ModelPart root) {
        super(root);
    }

    public static TexturedModelData createModel() {
        ModelData md = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
        ModelPartData root = md.getRoot();

        root.getChild("head").addChild("hood",
                ModelPartBuilder.create().uv(64, 0).cuboid(-4f, -8f, -4f, 8f, 8f, 8f, new Dilation(0.6f)), ModelTransform.NONE);

        ModelPartData body = root.getChild("body");
        body.addChild("mantle", ModelPartBuilder.create().uv(64, 40).cuboid(-5.5f, -0.5f, -2.5f, 11f, 3f, 5f), ModelTransform.NONE);
        for (int k = 0; k < 8; k++) {                                     // coroa de espinhos em volta do pescoço
            double a = k * Math.PI / 4.0;
            body.addChild("spike" + k, ModelPartBuilder.create().uv(64, 16).cuboid(-1f, -5f, -1f, 2f, 5f, 2f),
                    ModelTransform.of((float) (Math.sin(a) * 5.0), 0f, (float) (Math.cos(a) * 5.0), 0.55f, (float) (a + Math.PI), 0f));
        }
        body.addChild("flower", ModelPartBuilder.create().uv(64, 24).cuboid(-3f, 2.5f, -3.4f, 6f, 6f, 1f), ModelTransform.NONE);
        body.addChild("vine1", ModelPartBuilder.create().uv(96, 0).cuboid(-4f, 5f, -2f, 8f, 1f, 4f, new Dilation(0.35f)), ModelTransform.NONE);
        body.addChild("vine2", ModelPartBuilder.create().uv(96, 0).cuboid(-4f, 9f, -2f, 8f, 1f, 4f, new Dilation(0.35f)), ModelTransform.NONE);
        root.getChild("right_arm").addChild("vine_a", ModelPartBuilder.create().uv(96, 0).cuboid(-3f, 3f, -2f, 4f, 1f, 4f, new Dilation(0.3f)), ModelTransform.NONE);
        root.getChild("left_arm").addChild("vine_b", ModelPartBuilder.create().uv(96, 0).cuboid(-1f, 6f, -2f, 4f, 1f, 4f, new Dilation(0.3f)), ModelTransform.NONE);

        return TexturedModelData.of(md, 128, 64);
    }
}
