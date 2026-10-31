package com.linhagens.client;

import com.linhagens.race.Race;
import com.linhagens.race.RaceData;
import com.linhagens.net.StatePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/** HUD no canto superior esquerdo: raça, nível, XP, mana e habilidades. */
public final class RaceHud {
    private RaceHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden || mc.currentScreen instanceof RaceSelectScreen) return;
        Race r = RaceClient.local();
        StatePayload s = RaceClient.state;
        if (r == null || s == null) return;

        TextRenderer tr = mc.textRenderer;
        int x = 6, y = 6, bw = 100;

        ctx.drawTextWithShadow(tr, Text.literal(r.nome + "  Nv. " + s.level()).styled(st -> st.withColor(r.color)), x, y, 0xFFFFFF);

        // XP
        float xpFrac = s.level() >= RaceData.MAX_LEVEL ? 1f : s.xp() / (float) RaceData.xpNeeded(s.level());
        ctx.fill(x, y + 11, x + bw, y + 14, 0xAA000000);
        ctx.fill(x, y + 11, x + (int) (bw * Math.min(1f, xpFrac)), y + 14, 0xFFFFD34D);

        // Mana
        float mf = s.maxMana() <= 0 ? 0 : s.mana() / (float) s.maxMana();
        ctx.fill(x, y + 17, x + bw, y + 25, 0xAA000000);
        ctx.fill(x + 1, y + 18, x + 1 + (int) ((bw - 2) * Math.min(1f, mf)), y + 24, 0xFF3A7BFF);
        ctx.drawTextWithShadow(tr, Text.literal(s.mana() + "/" + s.maxMana()), x + bw + 4, y + 17, 0xA8C8FF);

        // Habilidades
        for (int slot = 0; slot < 2; slot++) {
            Race.Ability ab = r.abilities[slot];
            int cd = slot == 0 ? s.cd0() : s.cd1();
            var key = (slot == 0 ? LinhagensClient.ABILITY_1 : LinhagensClient.ABILITY_2).getBoundKeyLocalizedText();
            boolean ready = cd <= 0 && s.mana() >= ab.mana();
            Text line = Text.literal("[").append(key).append("] " + ab.name()
                    + (cd > 0 ? " (" + cd + "s)" : " - " + ab.mana()));
            ctx.drawTextWithShadow(tr, line, x, y + 29 + slot * 10, ready ? 0xFFFFFF : 0x888888);
        }
    }
}
