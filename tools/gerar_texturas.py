#!/usr/bin/env python3
"""Gera as texturas 128x128 das raças (sem dependências). Rode: python3 tools/gerar_texturas.py
Layout: 0..63 x 0..63 = camada de tom de pele (mesmo UV da skin do jogador)
        resto = acessórios (chifres, auréola, orelhas, focinho, presas, asas, cauda)
Edite as cores/pixels aqui (ou pinte os PNGs à mão) para mudar o visual."""
import zlib, struct, random, math, os

W = H = 128
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                   "assets", "linhagens", "textures", "entity", "race")


def new():
    return [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]


def put(im, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        im[y][x] = c


def rect(im, x, y, w, h, c):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            put(im, xx, yy, c)


def save(im, name):
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in im)

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0)) \
          + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name + ".png"), "wb") as f:
        f.write(png)


def shade(c, k):
    return (int(c[0] * k), int(c[1] * k), int(c[2] * k), c[3])


def eyes(im, brow, eye):
    # rosto da cabeça: face frontal em (8..15, 8..15)
    for x in (9, 10, 13, 14):
        put(im, x, 11, brow)
        put(im, x, 12, eye)


def tint(im, c):
    rect(im, 0, 0, 64, 64, c)


def noise_rect(im, x, y, w, h, c, amt=14, seed=1):
    rnd = random.Random(seed)
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            d = rnd.randint(-amt, amt)
            put(im, xx, yy, (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), c[3]))


def wings(im, c_main, c_dark, c_tip):
    rect(im, 0, 64, 26, 15, c_main)                 # bordas
    rect(im, 1, 65, 12, 14, (0, 0, 0, 0))           # face frontal (vazia)
    rect(im, 14, 65, 12, 14, (0, 0, 0, 0))          # face traseira (vazia)
    for face_x, inner_right in ((14, True), (1, False)):
        for a in range(12):                         # a = distância da borda interna
            x = face_x + (11 - a if inner_right else a)
            top = int(a * 0.55)
            bot = 13 - int(a * 0.15) - (1 if a % 3 == 0 else 0)
            for y in range(top, bot + 1):
                t = y / 13.0
                col = c_main if (a // 2) % 2 == 0 else c_dark
                if t > 0.7:
                    col = c_tip
                put(im, x, 65 + y, col)


def demonio():
    im = new()
    tint(im, (170, 30, 35, 95))
    eyes(im, (110, 0, 0, 255), (255, 210, 40, 255))
    for y in range(21, 30):                          # runas no peito
        put(im, 24, y, (255, 90, 30, 190))
    rect(im, 21, 24, 7, 1, (255, 90, 30, 190))
    noise_rect(im, 64, 0, 8, 5, (45, 15, 20, 255), 8, 1)       # base dos chifres
    rect(im, 64, 8, 4, 3, (235, 215, 190, 255))                # ponta dos chifres
    rect(im, 32, 64, 12, 6, (150, 20, 30, 255))                # cauda
    rect(im, 48, 64, 8, 4, (215, 40, 50, 255))                 # ponta da cauda
    save(im, "demonio")


def anjo():
    im = new()
    tint(im, (255, 248, 225, 60))
    eyes(im, (255, 255, 255, 0), (140, 215, 255, 255))
    rnd = random.Random(7)
    for _ in range(40):                              # brilhos
        put(im, rnd.randint(16, 56), rnd.randint(16, 32), (255, 255, 200, 210))
    gold = (255, 225, 90, 255)
    rect(im, 64, 16, 22, 2, gold)                    # barras da auréola (10x1x1)
    rect(im, 64, 20, 18, 9, (255, 235, 130, 255))    # barras laterais (1x1x8)
    wings(im, (250, 250, 255, 255), (225, 230, 248, 255), (200, 215, 255, 255))
    save(im, "anjo")


def piglin():
    im = new()
    tint(im, (235, 140, 150, 90))
    eyes(im, (90, 40, 45, 255), (40, 20, 25, 255))
    noise_rect(im, 64, 32, 14, 8, (225, 120, 130, 255), 10, 3)   # orelhas
    rect(im, 67, 36, 3, 3, (150, 70, 80, 255))
    rect(im, 64, 48, 10, 4, (240, 165, 170, 255))                # focinho
    put(im, 65, 50, (110, 50, 60, 255))
    put(im, 68, 50, (110, 50, 60, 255))
    rect(im, 64, 56, 4, 3, (245, 240, 215, 255))                 # presas
    save(im, "piglin")


if __name__ == "__main__":
    demonio(); anjo(); piglin()
    print("Texturas geradas em", os.path.abspath(OUT))
