package com.linhagens.item;

import java.util.List;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Machado de Lenhador.
 * Resistente e utilitário: dano 8, velocidade 1.0, dá +20% de resistência a empurrão na mão.
 * Derruba árvores inteiras de uma vez (até 40 troncos; agache para cortar um só). Ver WeaponEvents.
 */
public class LenhadorAxeItem extends AxeItem {
    public LenhadorAxeItem(Item.Settings settings) {
        super(ModToolMaterials.LENHADOR, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Derruba a árvore inteira (agache para cortar um tronco).").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Ombros largos: resiste a empurrões.").formatted(Formatting.GRAY));
    }
}
