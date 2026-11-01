package com.linhagens.client;

import com.linhagens.net.ResetSkillsPayload;
import com.linhagens.net.UnlockSkillPayload;
import com.linhagens.race.Race;
import com.linhagens.race.RaceData;
import com.linhagens.race.Skill;
import com.linhagens.race.SkillTrees;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/** Árvore de habilidades (tecla K): 1 ponto por nível, 3 ramos (Ataque, Defesa, Maestria) a partir de uma raiz. */
public class SkillTreeScreen extends Screen {
    private static final int SZ = 24;
    private static final int COL_GAP = 78;
    private static final int ROW_GAP = 34;

    public SkillTreeScreen() {
        super(Text.literal("Árvore de Habilidades"));
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Redefinir pontos"),
                b -> ClientPlayNetworking.send(ResetSkillsPayload.INSTANCE))
                .dimensions(width / 2 - 100, height - 26, 95, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), b -> close())
                .dimensions(width / 2 + 5, height - 26, 95, 20).build());
    }

    private int nx(Skill s) {
        return width / 2 + (s.col() - 1) * COL_GAP - SZ / 2;
    }

    private int ny(Skill s) {
        return 38 + s.row() * ROW_GAP;
    }

    private int points() {
        if (RaceClient.state == null) return 0;
        return RaceData.pointsFor(RaceClient.state.level(), RaceClient.skills.size());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);

        Race race = RaceClient.local();
        if (race == null || RaceClient.state == null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Escolha uma raça primeiro (tecla R)."),
                    width / 2, height / 2, 0xAAAAAA);
            return;
        }

        int pts = points();
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(race.nome + "  -  Nível " + RaceClient.state.level() + "  -  Pontos: " + pts)
                        .styled(s -> s.withColor(race.color)), width / 2, 20, 0xFFFFFF);

        List<Skill> tree = SkillTrees.get(race);

        // ligações
        for (Skill s : tree) {
            if (s.parent() == null) continue;
            Skill p = SkillTrees.find(race, s.parent());
            int px = nx(p) + SZ / 2, py = ny(p) + SZ / 2;
            int cx = nx(s) + SZ / 2, cy = ny(s) + SZ / 2;
            int color = RaceClient.skills.contains(s.id()) ? 0xFF55FF77 : 0xFF555566;
            if (px == cx) {
                ctx.fill(px - 1, py, px + 1, cy, color);
            } else {
                ctx.fill(Math.min(px, cx), py - 1, Math.max(px, cx) + 1, py + 1, color);
                ctx.fill(cx - 1, py, cx + 1, cy, color);
            }
        }

        Skill hover = null;
        for (Skill s : tree) {
            int x = nx(s), y = ny(s);
            boolean unlocked = RaceClient.skills.contains(s.id());
            boolean available = !unlocked && pts > 0 && (s.parent() == null || RaceClient.skills.contains(s.parent()));
            boolean over = mx >= x && mx < x + SZ && my >= y && my < y + SZ;
            if (over) hover = s;

            int border = unlocked ? 0xFF55FF77 : (available ? 0xFFFFD34D : 0xFF555566);
            ctx.fill(x - 1, y - 1, x + SZ + 1, y + SZ + 1, border);
            ctx.fill(x, y, x + SZ, y + SZ, unlocked ? 0xFF1E3A26 : (available ? 0xFF3A3220 : 0xFF1A1A22));
            Item icon = Registries.ITEM.get(Identifier.of(s.icon()));
            ctx.drawItem(new ItemStack(icon), x + 4, y + 4);
            if (!unlocked && !available) ctx.fill(x, y, x + SZ, y + SZ, 0x88000000);
        }

        if (hover != null) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal(hover.nome()).formatted(Formatting.BOLD, Formatting.GOLD));
            lines.add(Text.literal(hover.desc()).formatted(Formatting.GRAY));
            if (RaceClient.skills.contains(hover.id())) {
                lines.add(Text.literal("Desbloqueada").formatted(Formatting.GREEN));
            } else if (hover.parent() != null && !RaceClient.skills.contains(hover.parent())) {
                Skill p = SkillTrees.find(race, hover.parent());
                lines.add(Text.literal("Requer: " + p.nome()).formatted(Formatting.RED));
            } else {
                lines.add(Text.literal("Custo: 1 ponto" + (pts > 0 ? " (clique para desbloquear)" : " (sem pontos)"))
                        .formatted(pts > 0 ? Formatting.YELLOW : Formatting.RED));
            }
            ctx.drawTooltip(textRenderer, lines, mx, my);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        Race race = RaceClient.local();
        if (button == 0 && race != null) {
            for (Skill s : SkillTrees.get(race)) {
                int x = nx(s), y = ny(s);
                if (mx >= x && mx < x + SZ && my >= y && my < y + SZ) {
                    ClientPlayNetworking.send(new UnlockSkillPayload(s.id()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
