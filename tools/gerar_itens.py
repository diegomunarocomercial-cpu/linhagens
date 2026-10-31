#!/usr/bin/env python3
"""Gera texturas de itens, bloco, armadura e mobs (pixel art feita por código). Rode: python3 tools/gerar_itens.py
Pode editar as cores/pixels aqui ou substituir os PNGs por arte sua (mesmos nomes de arquivo)."""
import zlib, struct, random, math, os

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "linhagens", "textures")


def new(w, h):
    return [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]


def put(im, x, y, c):
    if 0 <= y < len(im) and 0 <= x < len(im[0]):
        im[y][x] = c


def rect(im, x, y, w, h, c):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            put(im, xx, yy, c)


def save(im, rel):
    h, w = len(im), len(im[0])
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in im)

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) \
        + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path = os.path.join(BASE, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def noise(im, x, y, w, h, c, amt, seed):
    r = random.Random(seed)
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            d = r.randint(-amt, amt)
            put(im, xx, yy, (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), 255))


def ascii_art(rows, pal):
    im = new(16, 16)
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row, len(row))
        for x, ch in enumerate(row):
            if ch in pal:
                put(im, x, y, pal[ch] + (255,))
    return im


# ------------------------------------------------------------------ ARMAS (16x16, ponta p/ cima-direita)
def katana():
    im = new(16, 16)
    K, W, D = (28, 28, 34, 255), (95, 98, 108, 255), (70, 74, 84, 255)   # cabo preto / amarração / aço escuro
    S, L = (150, 156, 168, 255), (215, 220, 230, 255)                     # aço / brilho
    G, GG = (70, 255, 120, 255), (30, 190, 90, 255)                       # fio verde
    # cabo (preto com amarração)
    for i, (x, y) in enumerate([(1, 14), (2, 13), (3, 12), (4, 11)]):
        put(im, x, y, K if i % 2 == 0 else W)
        put(im, x + 1, y, K)
    put(im, 0, 15, K)
    # guarda (tsuba) verde/prata, angulosa
    for (x, y, c) in [(4, 9, GG), (5, 9, (190, 195, 205, 255)), (5, 10, G), (6, 10, (190, 195, 205, 255)),
                      (6, 11, GG), (7, 11, (190, 195, 205, 255)), (4, 10, (190, 195, 205, 255)), (7, 12, GG),
                      (6, 12, K)]:
        put(im, x, y, c)
    # lâmina levemente curva
    for i in range(0, 9):
        t = i / 8.0
        x = 6 + int(round(8 * t))
        y = 9 - int(round(8 * t + 0.9 * math.sin(math.pi * t)))
        put(im, x, y, D)            # lombo
        put(im, x + 1, y, S)        # corpo
        put(im, x, y + 1, G if i > 0 else GG)   # fio verde brilhante
        if i % 3 == 1:
            put(im, x + 1, y - 1, L)
    put(im, 15, 0, S)
    put(im, 14, 0, D)
    return im


def lenhador():
    W_ = (120, 78, 40, 255)   # madeira
    W2 = (92, 58, 28, 255)
    im = new(16, 16)
    # cabo diagonal
    for i in range(0, 12):
        x, y = 2 + i, 14 - i
        put(im, x, y, W_)
        put(im, x + 1, y, W2)
    put(im, 1, 15, W2)
    # cabeça (aço envelhecido) - bloco robusto no alto
    M, M2, M3, EDGE = (128, 130, 135, 255), (96, 98, 104, 255), (70, 72, 78, 255), (190, 192, 198, 255)
    spans = {1: (9, 12), 2: (8, 13), 3: (8, 14), 4: (8, 14), 5: (8, 14), 6: (8, 13), 7: (8, 11)}
    r = random.Random(4)
    for y, (a, b) in spans.items():
        for x in range(a, b + 1):
            c = M
            if x == b:
                c = EDGE                       # fio da lâmina
            elif r.random() < 0.28:
                c = M2                         # desgaste
            elif r.random() < 0.1:
                c = M3
            put(im, x, y, c)
    put(im, 14, 3, (0, 0, 0, 0))               # lascas no fio
    put(im, 14, 5, (0, 0, 0, 0))
    put(im, 13, 4, M3)
    put(im, 10, 3, (150, 90, 50, 255))         # ferrugem
    put(im, 11, 5, (150, 90, 50, 255))
    return im


def desastre():
    im = new(16, 16)
    GO, GL, GD = (240, 195, 60, 255), (255, 235, 140, 255), (170, 120, 20, 255)   # dourado
    H, H2 = (70, 42, 28, 255), (48, 28, 18, 255)                                    # haste marrom-escura
    BL = (150, 10, 15, 255)                                                          # sangue
    for i in range(0, 11):                                                           # haste
        x, y = 1 + i, 14 - i
        put(im, x, y, H)
        put(im, x + 1, y, H2)
    for (x, y) in [(2, 13), (5, 10), (9, 6)]:                                        # anéis dourados
        put(im, x, y, GO); put(im, x + 1, y, GD); put(im, x, y + 1, GD)
    put(im, 0, 15, GO); put(im, 1, 15, GD)                                           # contrapeso
    # ponta central perfurante
    for (x, y, c) in [(11, 4, GO), (12, 3, GO), (13, 2, GL), (14, 1, GL), (15, 0, GL), (12, 4, GD), (13, 3, GD), (14, 2, GD)]:
        put(im, x, y, c)
    # lâmina em lua crescente (cima/esquerda)
    for (x, y, c) in [(10, 4, GO), (9, 3, GO), (8, 2, GL), (7, 1, GL), (6, 1, GO), (5, 2, GL), (9, 4, GD), (8, 3, GD), (7, 2, GD),
                      (6, 3, GD), (5, 3, GD)]:
        put(im, x, y, c)
    # lâmina em lua crescente (baixo/direita)
    for (x, y, c) in [(12, 5, GO), (13, 6, GO), (14, 7, GL), (14, 8, GL), (13, 9, GO), (12, 9, GL), (12, 6, GD), (13, 7, GD), (13, 8, GD),
                      (11, 8, GD), (11, 9, GD)]:
        put(im, x, y, c)
    put(im, 10, 5, GD); put(im, 11, 5, GO); put(im, 11, 6, GD)                       # cubo da cabeça
    for (x, y) in [(7, 1), (14, 8), (14, 1)]:                                        # ensanguentado
        put(im, x, y, BL)
    put(im, 8, 2, BL); put(im, 13, 6, BL)
    return im


def shard():
    return ascii_art([
        "................",
        ".......kk.......",
        "......kLPk......",
        ".....kLPPpk.....",
        ".....kLPPpk.....",
        "....kLPPPppk....",
        "....kPPPPppk....",
        "....kPPPPppk....",
        "....kPPPPppk....",
        ".....kPPPpk.....",
        ".....kPPppk.....",
        "......kPpk......",
        ".......kk.......",
        "................",
        "................",
        "................"],
        {"k": (45, 20, 80), "p": (110, 60, 175), "P": (165, 105, 235), "L": (235, 205, 255)})


PAL = {"k": (35, 20, 60), "p": (100, 55, 160), "P": (165, 110, 230), "g": (245, 205, 95), "h": (225, 190, 255)}


def helmet():
    return ascii_art([
        "................",
        "................",
        "....kkkkkkkk....",
        "...kppppppppk...",
        "..kpPPPPPPPPpk..",
        "..kpPPhhPPPPpk..",
        "..kpPggggggPpk..",
        "..kpPkkkkkkPpk..",
        "..kpPk....kPpk..",
        "..kpPk....kPpk..",
        "..kkkk....kkkk..",
        "................",
        "................",
        "................",
        "................",
        "................"], PAL)


def chestplate():
    return ascii_art([
        "................",
        "...kkk....kkk...",
        "..kppkkkkkkppk..",
        ".kpPPpkggkpPPpk.",
        ".kpPPppkkppPPpk.",
        ".kkpPPPPPPPPpkk.",
        "..kpPPPggPPPpk..",
        "..kpPPhPPPPPpk..",
        "..kpPPPggPPPpk..",
        "..kpPPPPPPPPpk..",
        "..kppPPPPPPppk..",
        "...kppppppppk...",
        "....kkkkkkkk....",
        "................",
        "................",
        "................"], PAL)


def leggings():
    return ascii_art([
        "................",
        "...kkkkkkkkkk...",
        "...kpgggggPpk...",
        "...kpPPPPPPpk...",
        "...kpPPPPPPpk...",
        "...kpPPkkPPpk...",
        "...kpPPk.kPpk...",
        "...kpPk..kPpk...",
        "...kpPk..kPpk...",
        "...kpPk..kPpk...",
        "...kpPk..kPpk...",
        "...kppk..kppk...",
        "...kkkk..kkkk...",
        "................",
        "................",
        "................"], PAL)


def boots():
    return ascii_art([
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "................",
        "..kkkk....kkkk..",
        "..kppk....kppk..",
        "..kpPk....kPpk..",
        "..kpPk....kPpk..",
        ".kkpPkk..kkPpkk.",
        ".kppPPk..kPPppk.",
        ".kgPPPk..kPPPgk.",
        ".kkkkkk..kkkkkk."], PAL)


def aurora_core_block():
    im = new(16, 16)
    r = random.Random(11)
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            base = (95 + int(80 * (1 - d / 16)), 40 + int(60 * (1 - d / 16)), 170 + int(70 * (1 - d / 16)))
            j = r.randint(-8, 8)
            put(im, x, y, (min(255, base[0] + j), min(255, base[1] + j), min(255, base[2] + j), 255))
    for i in range(16):                                   # facetas diagonais
        put(im, i, i, (200, 150, 255, 255))
        put(im, 15 - i, i, (170, 110, 240, 255))
    for x in range(16):                                   # moldura escura
        for y in (0, 15):
            put(im, x, y, (45, 20, 85, 255))
            put(im, y, x, (45, 20, 85, 255))
    for (x, y) in [(7, 7), (8, 7), (7, 8), (8, 8)]:       # centro brilhante (verde-água)
        put(im, x, y, (190, 255, 230, 255))
    for (x, y) in [(6, 7), (9, 8), (7, 6), (8, 9)]:
        put(im, x, y, (140, 235, 200, 255))
    return im


# ------------------------------------------------------------------ ARMADURA (64x32)
def armor_layer(second):
    im = new(64, 32)
    r = random.Random(5 if second else 6)
    for y in range(32):
        for x in range(64):
            v = r.randint(-12, 12)
            put(im, x, y, (112 + v, 64 + v, 178 + v, 255))
    gold, dark, light = (245, 205, 95, 255), (40, 22, 70, 255), (200, 160, 250, 255)
    if not second:
        rect(im, 0, 6, 32, 2, gold)                   # faixa dourada do elmo
        rect(im, 8, 11, 8, 3, dark)                   # viseira (face frontal do elmo)
        rect(im, 12, 11, 1, 3, light)
        rect(im, 20, 20, 8, 1, gold)                  # peitoral
        rect(im, 23, 21, 2, 8, light)
        rect(im, 20, 29, 8, 2, dark)
        for x in (44, 52):                            # ombreiras
            rect(im, x, 16, 4, 3, gold)
        rect(im, 0, 26, 16, 2, dark)                  # botas
        rect(im, 0, 28, 16, 4, (80, 45, 135, 255))
    else:
        rect(im, 16, 16, 24, 2, gold)                 # cinto
        rect(im, 19, 17, 3, 3, light)
        rect(im, 0, 20, 16, 1, gold)
        rect(im, 0, 24, 16, 1, dark)
    return im


# ------------------------------------------------------------------ MOBS
def fill_skin(im, base, seed, amt=10):
    for (x, y, w, h) in [(0, 0, 32, 16), (16, 16, 24, 16), (40, 16, 16, 16), (0, 16, 16, 16), (32, 48, 16, 16), (16, 48, 16, 16)]:
        noise(im, x, y, w, h, base, amt, seed)


def imp():
    im = new(64, 64)
    fill_skin(im, (190, 45, 40), 3)
    dark, yel, white = (60, 10, 12, 255), (255, 210, 40, 255), (250, 245, 230, 255)
    rect(im, 8, 0, 8, 2, (120, 20, 20, 255))                    # topo da cabeça
    for x in (9, 10, 13, 14):
        put(im, x, 11, dark)                                    # sobrancelhas bravas
        put(im, x, 12, yel)                                     # olhos
    rect(im, 10, 14, 4, 1, dark)                                # boca
    put(im, 10, 14, white); put(im, 13, 14, white)              # presas
    rect(im, 20, 22, 8, 10, (215, 90, 70, 255))                 # barriga
    rect(im, 4, 20, 4, 5, (35, 20, 25, 255)); rect(im, 20, 52, 4, 5, (35, 20, 25, 255))   # tanga
    rect(im, 20, 29, 8, 2, (35, 20, 25, 255))
    for (x, y) in [(22, 24), (25, 26), (23, 28)]:
        put(im, x, y, (255, 150, 40, 255))                      # brasas na pele
    return im


def shadow():
    im = new(64, 64)
    fill_skin(im, (28, 28, 36), 8, 5)
    green, gd = (60, 255, 120, 255), (25, 140, 70, 255)
    rect(im, 8, 8, 8, 8, (22, 22, 28, 255))                      # capuz
    rect(im, 9, 12, 6, 1, (12, 12, 16, 255))
    for x in (9, 10, 13, 14):
        put(im, x, 12, green)                                    # olhos verdes brilhantes
    rect(im, 23, 20, 2, 12, gd)                                   # faixa verde no peito
    rect(im, 20, 29, 8, 1, green)                                 # cinto
    rect(im, 44, 26, 4, 1, gd); rect(im, 36, 58, 4, 1, gd)        # braçadeiras
    rect(im, 4, 29, 4, 1, gd); rect(im, 20, 61, 4, 1, gd)
    return im


def guardian():
    im = new(128, 128)
    r = random.Random(21)
    for y in range(128):
        for x in range(128):
            v = r.randint(-14, 14)
            put(im, x, y, (92 + v, 66 + v, 138 + v, 255))
    for _ in range(260):                                           # cristais claros
        x, y = r.randint(0, 127), r.randint(0, 127)
        put(im, x, y, (225, 190, 255, 255))
        if r.random() < 0.4:
            put(im, x + 1, y, (170, 120, 240, 255))
    for _ in range(90):                                            # rachaduras escuras
        x, y = r.randint(0, 127), r.randint(0, 127)
        for k in range(r.randint(2, 5)):
            put(im, x + k, y + (k // 2), (45, 28, 80, 255))
    return im


if __name__ == "__main__":
    save(katana(), "item/katana_espadachim.png")
    save(lenhador(), "item/machado_lenhador.png")
    save(desastre(), "item/machado_desastre.png")
    save(shard(), "item/aurora_shard.png")
    save(helmet(), "item/aurora_helmet.png")
    save(chestplate(), "item/aurora_chestplate.png")
    save(leggings(), "item/aurora_leggings.png")
    save(boots(), "item/aurora_boots.png")
    save(aurora_core_block(), "block/aurora_core.png")
    save(armor_layer(False), "models/armor/aurora_layer_1.png")
    save(armor_layer(True), "models/armor/aurora_layer_2.png")
    save(imp(), "entity/imp_infernal.png")
    save(shadow(), "entity/espadachim_sombrio.png")
    save(guardian(), "entity/aurora_guardian.png")
    print("texturas ok")
