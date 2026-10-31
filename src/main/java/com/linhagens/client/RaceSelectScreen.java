package com.linhagens.client;

import com.linhagens.net.SelectRacePayload;
import com.linhagens.race.Race;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/** Menu de configuração da raça: 4 cartões com prévia 3D do seu personagem já com a aparência da raça. */
public class RaceSelectScreen extends Screen {
    private static final int CARD_Y = 24;
    private static final int CARD_H = 106;
    private static final int GAP = 8;

    private final Race[] races = Race.values();
    private int selected = -1;
    private ButtonWidget confirm;

    public RaceSelectScreen() {
        super(Text.literal("Escolha sua Linhagem"));
    }

    @Override
    protected void init() {
        confirm = addDrawableChild(ButtonWidget.builder(Text.literal("Confirmar"), b -> confirm())
                .dimensions(width / 2 - 95, height - 26, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancelar"), b -> close())
                .dimensions(width / 2 + 5, height - 26, 90, 20).build());
        confirm.active = selected >= 0;
    }

    private void confirm() {
        if (selected < 0) return;
        if (races[selected] != RaceClient.local()) {
            ClientPlayNetworking.send(new SelectRacePayload(races[selected].id));
        }
        close();
    }

    private int cardW() {
        return Math.max(60, Math.min(104, (width - 40 - 3 * GAP) / 4));
    }

    private int cardX(int i) {
        int total = 4 * cardW() + 3 * GAP;
        return (width - total) / 2 + i * (cardW() + GAP);
    }

    @Override
    public boolean shouldPause() {
        return false; // o personagem da prévia continua animado
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta); // fundo + botões
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);

        int hover = -1;
        Race current = RaceClient.local();
        int w = cardW();

        for (int i = 0; i < races.length; i++) {
            Race r = races[i];
            int x = cardX(i), y = CARD_Y;
            boolean hov = mx >= x && mx < x + w && my >= y && my < y + CARD_H;
            if (hov) hover = i;

            ctx.fill(x, y, x + w, y + CARD_H, selected == i ? 0xDD202040 : 0xAA101018);
            ctx.drawBorder(x, y, w, CARD_H, selected == i ? (0xFF000000 | r.color) : (hov ? 0xFFAAAAAA : 0xFF444455));

            RaceClient.previewActive = true;
            RaceClient.previewRace = (r == Race.HUMANO) ? null : r;
            try {
                InventoryScreen.drawEntity(ctx, x + 2, y + 2, x + w - 2, y + CARD_H - 26, 34, 0.0625f, mx, my, client.player);
            } finally {
                RaceClient.previewActive = false;
                RaceClient.previewRace = null;
            }

            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(r.nome).styled(s -> s.withColor(r.color)),
                    x + w / 2, y + CARD_H - 22, 0xFFFFFF);
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(r == current ? "(atual)" : r.titulo), x + w / 2, y + CARD_H - 11, 0xAAAAAA);
        }

        int py = CARD_Y + CARD_H + 6;
        int px = cardX(0);
        int pw = 4 * w + 3 * GAP;
        int ph = height - 32 - py;
        ctx.fill(px, py, px + pw, py + ph, 0xAA000000);
        ctx.drawBorder(px, py, pw, ph, 0xFF444455);

        Race info = hover >= 0 ? races[hover] : (selected >= 0 ? races[selected] : null);
        if (info == null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Passe o mouse ou clique numa raça para ver os detalhes."),
                    width / 2, py + ph / 2 - 4, 0x999999);
            return;
        }

        int colW = pw / 2 - 12;
        int lx = px + 6;
        int y = py + 5;
        y = wrap(ctx, Text.literal(info.nome + " — " + info.titulo).styled(s -> s.withColor(info.color)), lx, y, colW, 0xFFFFFF);
        y = wrap(ctx, Text.literal(info.descricao), lx, y, colW, 0xBBBBBB);
        for (String p : info.passivas) {
            y = wrap(ctx, Text.literal("+ " + p), lx, y, colW, 0x7CFC7C);
        }
        wrap(ctx, Text.literal("- " + info.fraqueza), lx, y, colW, 0xFF7C7C);

        int rx = px + pw / 2 + 6;
        int ry = py + 5;
        ry = wrap(ctx, Text.literal("Habilidades"), rx, ry, colW, 0xFFD34D);
        for (int slot = 0; slot < 2; slot++) {
            Race.Ability ab = info.abilities[slot];
            var key = (slot == 0 ? LinhagensClient.ABILITY_1 : LinhagensClient.ABILITY_2).getBoundKeyLocalizedText();
            Text head = Text.literal("[").append(key).append("] " + ab.name() + "  (" + ab.mana() + " mana, " + ab.cooldownSec() + "s)");
            ry = wrap(ctx, head, rx, ry, colW, 0xFFFFFF);
            ry = wrap(ctx, Text.literal(ab.desc()), rx, ry, colW, 0xAAAAAA);
        }
    }

    private int wrap(DrawContext ctx, Text t, int x, int y, int w, int color) {
        for (OrderedText line : textRenderer.wrapLines(t, w)) {
            ctx.drawText(textRenderer, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int i = 0; i < races.length; i++) {
                int x = cardX(i);
                if (mx >= x && mx < x + cardW() && my >= CARD_Y && my < CARD_Y + CARD_H) {
                    selected = i;
                    confirm.active = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
