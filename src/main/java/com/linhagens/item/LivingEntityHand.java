package com.linhagens.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Hand;

final class LivingEntityHand {
    private LivingEntityHand() {}

    static EquipmentSlot slot(Hand hand) {
        return hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
}
