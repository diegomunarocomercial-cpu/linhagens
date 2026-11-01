#!/usr/bin/env python3
"""Gera texturas de itens, blocos, armadura e mobs. Rode: python3 tools/gerar_itens.py
Armas/armaduras são desenhadas em alta resolução (supersampling) e reduzidas para 32x32 com contorno, bisel e brilho.
Você pode editar as cores aqui ou substituir qualquer PNG por arte sua (mesmo nome de arquivo)."""
import math, os, random, struct, zlib

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "linhagens", "textures")


# ---------------------------------------------------------------- PNG
def save_png(px, rel):
    h, w = len(px), len(px[0])
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in px)

    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) \
        + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path = os.path.join(BASE, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def new(w, h):
    return [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]


def put(im, x, y, c):
    if len(c) == 3:
        c = (c[0], c[1], c[2], 255)
    if 0 <= y < len(im) and 0 <= x < len(im[0]):
        im[y][x] = c


def rect(im, x, y, w, h, c):
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            put(im, xx, yy, c)


def noise(im, x, y, w, h, c, amt, seed):
    r = random.Random(seed)
    for yy in range(y, y + h):
        for xx in range(x, x + w):
            d = r.randint(-amt, amt)
            put(im, xx, yy, (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), 255))


def shade(c, k):
    return (max(0, min(255, int(c[0] * k))), max(0, min(255, int(c[1] * k))), max(0, min(255, int(c[2] * k))))


def mix(a, b, t):
    t = max(0.0, min(1.0, t))
    return (int(a[0] + (b[0] - a[0]) * t), int(a[1] + (b[1] - a[1]) * t), int(a[2] + (b[2] - a[2]) * t))


# ---------------------------------------------------------------- canvas supersampled
class Canvas:
    def __init__(self, n=32, ss=8):
        self.n, self.ss = n, ss
        self.N = n * ss
        self.px = [[None] * self.N for _ in range(self.N)]

    def _loop(self, x0, y0, x1, y1):
        ss = self.ss
        for j in range(max(0, int(y0 * ss)), min(self.N, int(y1 * ss) + 1)):
            for i in range(max(0, int(x0 * ss)), min(self.N, int(x1 * ss) + 1)):
                yield i, j, (i + 0.5) / ss, (j + 0.5) / ss

    def shape(self, bbox, fn):
        """fn(x,y)->(r,g,b) ou None; desenha por cima."""
        for i, j, x, y in self._loop(*bbox):
            c = fn(x, y)
            if c is not None:
                self.px[j][i] = c

    def poly(self, pts, fn):
        xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
        bbox = (min(xs), min(ys), max(xs), max(ys))

        def inside(x, y):
            ins = False
            n = len(pts)
            for k in range(n):
                x1, y1 = pts[k]; x2, y2 = pts[(k + 1) % n]
                if (y1 > y) != (y2 > y) and x < (x2 - x1) * (y - y1) / (y2 - y1 + 1e-12) + x1:
                    ins = not ins
            return ins

        def f(x, y):
            if not inside(x, y):
                return None
            # bisel: borda inferior-direita escura, superior-esquerda clara
            k = 1.0
            if not inside(x + 0.9, y + 0.9):
                k = 0.72
            elif not inside(x - 0.9, y - 0.9):
                k = 1.25
            c = fn(x, y)
            return None if c is None else shade(c, k)
        self.shape(bbox, f)

    def tube(self, pts, widths, fn):
        """Tubo de largura variável ao longo de uma polilinha. fn(t, s, x, y): t 0..1 ao longo, s -1..1 lateral."""
        segs = []
        total = 0.0
        for k in range(len(pts) - 1):
            L = math.dist(pts[k], pts[k + 1])
            segs.append((total, L))
            total += L
        for k in range(len(pts) - 1):
            (ax, ay), (bx, by) = pts[k], pts[k + 1]
            wa, wb = widths[k], widths[k + 1]
            dx, dy = bx - ax, by - ay
            L2 = dx * dx + dy * dy or 1e-9
            m = max(wa, wb) + 0.2
            bbox = (min(ax, bx) - m, min(ay, by) - m, max(ax, bx) + m, max(ay, by) + m)
            start, L = segs[k]
            for i, j, x, y in self._loop(*bbox):
                u = ((x - ax) * dx + (y - ay) * dy) / L2
                if u < 0 or u > 1:
                    continue
                px_, py_ = ax + u * dx, ay + u * dy
                d = math.hypot(x - px_, y - py_)
                w = wa + (wb - wa) * u
                if d <= w:
                    side = ((x - px_) * -dy + (y - py_) * dx) / (math.sqrt(L2) * max(w, 1e-6))
                    c = fn((start + u * L) / total, max(-1, min(1, side)), x, y)
                    if c is not None:
                        self.px[j][i] = c

    def render(self, outline=True, thresh=0.5):
        n, ss = self.n, self.ss
        out = new(n, n)
        for y in range(n):
            for x in range(n):
                cnt = 0; r = g = b = 0
                for j in range(y * ss, (y + 1) * ss):
                    row = self.px[j]
                    for i in range(x * ss, (x + 1) * ss):
                        c = row[i]
                        if c is not None:
                            cnt += 1; r += c[0]; g += c[1]; b += c[2]
                if cnt >= thresh * ss * ss:
                    out[y][x] = (r // cnt, g // cnt, b // cnt, 255)
        if outline:
            src = [row[:] for row in out]
            for y in range(n):
                for x in range(n):
                    if src[y][x][3] == 0:
                        best = None
                        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                            xx, yy = x + dx, y + dy
                            if 0 <= xx < n and 0 <= yy < n and src[yy][xx][3] == 255:
                                best = src[yy][xx]
                        if best:
                            c = shade(best, 0.28)
                            out[y][x] = (c[0], c[1], c[2], 255)
        return out


# ---------------------------------------------------------------- ARMAS
def katana():
    cv = Canvas(32)
    # cabo (preto, amarrado em losangos)
    def handle(t, s, x, y):
        band = (t * 7.5) % 1.0
        c = (28, 28, 34)
        if band < 0.20:
            c = (82, 86, 98)
        elif abs(s) > 0.72:
            c = (14, 14, 18)
        return c
    cv.tube([(10.6, 21.6), (3.3, 28.7)], [1.55, 1.55], handle)
    # remate (kashira)
    cv.tube([(3.6, 28.4), (2.2, 29.8)], [1.9, 1.9], lambda t, s, x, y: mix((120, 130, 140), (60, 255, 140), 0.25 if abs(s) < 0.3 else 0))

    # lâmina curva: o lombo (verde-escuro/aço) fica em cima, o fio verde brilhante embaixo
    A, B = (13.2, 19.0), (30.0, 2.4)
    dxy = (B[0] - A[0], B[1] - A[1]); L = math.hypot(*dxy)
    d = (dxy[0] / L, dxy[1] / L); nrm = (d[1], -d[0])          # normal apontando p/ cima-esquerda
    pts, widths = [], []
    N = 28
    for i in range(N + 1):
        t = i / N
        bow = 1.5 * math.sin(math.pi * t)
        pts.append((A[0] + dxy[0] * t - nrm[0] * bow * -1, A[1] + dxy[1] * t - nrm[1] * bow * -1))
        w = 1.75 * (1 - 0.28 * t)
        if t > 0.88:
            w *= max(0.12, (1 - t) / 0.12)
        widths.append(w)

    def blade(t, s, x, y):
        # s > 0: lado do fio (embaixo/direita)
        if s > 0.52:
            g = (s - 0.52) / 0.48
            return mix((40, 210, 110), (150, 255, 190), g)
        if s > 0.22:
            return mix((190, 200, 214), (236, 242, 248), 1 - abs(s - 0.36) * 6)
        if s > -0.15:
            return (160, 170, 186)
        return mix((92, 100, 116), (64, 70, 84), -s)
    cv.tube(pts, widths, blade)

    # guarda (tsuba): losango anguloso verde e prata
    C = (11.9, 20.3); p = (0.70, 0.71); q = (0.71, -0.70)
    def P(a, b): return (C[0] + p[0] * a + q[0] * b, C[1] + p[1] * a + q[1] * b)
    pts_t = [P(-5.0, 0), P(-2.6, 1.9), P(2.6, 1.9), P(5.0, 0), P(2.6, -1.9), P(-2.6, -1.9)]
    def tsuba(x, y):
        a = (x - C[0]) * p[0] + (y - C[1]) * p[1]
        b = (x - C[0]) * q[0] + (y - C[1]) * q[1]
        if abs(a) < 1.2 and abs(b) < 0.7:
            return (110, 255, 170)
        if b >= 0:
            return (196, 204, 214) if a < 0 else (150, 158, 170)
        return (34, 150, 96) if a < 0 else (22, 104, 70)
    cv.poly(pts_t, tsuba)
    return cv.render()


def lenhador():
    cv = Canvas(32)
    def wood(t, s, x, y):
        c = (134, 90, 48)
        if s < -0.45:
            c = (168, 118, 66)
        elif s > 0.5:
            c = (92, 60, 30)
        if int((x * 3 + y * 5)) % 4 == 0:
            c = shade(c, 0.9)
        if t > 0.70:                       # empunhadura envolta em couro
            c = (96, 62, 40) if int(t * 22) % 2 == 0 else (70, 44, 28)
        return c
    cv.tube([(4.2, 28.0), (22.4, 9.6)], [1.5, 1.4], wood)

    H = (22.8, 9.2); h = (0.707, -0.707); q = (-0.707, -0.707)
    def P(a, b): return (H[0] + h[0] * a + q[0] * b, H[1] + h[1] * a + q[1] * b)
    head = [P(-2.4, -2.4), P(2.4, -2.4), P(2.4, 2.0), P(4.2, 3.6), P(5.2, 6.4), P(4.8, 8.2), P(3.0, 9.4),
            P(0, 9.9), P(-3.0, 9.4), P(-4.8, 8.2), P(-5.2, 6.4), P(-4.2, 3.6), P(-2.4, 2.0)]
    rnd = random.Random(7)
    spots = [(rnd.uniform(-4, 4), rnd.uniform(2, 9), rnd.random()) for _ in range(14)]
    def metal(x, y):
        a = (x - H[0]) * h[0] + (y - H[1]) * h[1]
        b = (x - H[0]) * q[0] + (y - H[1]) * q[1]
        c = (126, 130, 138)
        if b > 8.4:                        # fio da lâmina (desgastado, mas afiado)
            c = (206, 210, 218)
        elif b > 7.0:
            c = (156, 160, 168)
        if b < 2.0:
            c = (88, 90, 98)               # nuca do machado
            if abs(a) < 0.8 and abs(b - 0.0) < 0.9:
                c = (40, 40, 46)           # olho do cabo
        for (sa, sb, v) in spots:
            if math.hypot(a - sa, b - sb) < 0.9:
                c = (148, 86, 44) if v < 0.5 else (74, 76, 84)     # ferrugem e manchas
        return c
    cv.poly(head, metal)
    # lascas no fio
    cv.shape((0, 0, 32, 32), lambda x, y: None)
    return cv.render()


def desastre():
    cv = Canvas(32)
    GOLD, GOLD_L, GOLD_D = (240, 192, 66), (255, 238, 150), (150, 100, 24)
    def shaft(t, s, x, y):
        c = (74, 46, 32) if s < 0.4 else (46, 28, 20)
        if s < -0.5:
            c = (104, 68, 46)
        for r in (0.10, 0.36, 0.62):       # anéis dourados
            if abs(t - r) < 0.03:
                return mix(GOLD_L, GOLD, (s + 1) / 2)
            if abs(t - r) < 0.045:
                return GOLD_D
        return c
    cv.tube([(2.6, 29.6), (23.4, 8.8)], [1.25, 1.2], shaft)
    cv.tube([(2.6, 29.6), (1.0, 31.2)], [1.6, 0.5], lambda t, s, x, y: GOLD)       # contrapeso

    H = (23.4, 8.6); h = (0.707, -0.707); q = (-0.707, -0.707)
    def loc(x, y):
        return ((x - H[0]) * h[0] + (y - H[1]) * h[1], (x - H[0]) * q[0] + (y - H[1]) * q[1])
    rnd = random.Random(13)
    blood = [(rnd.uniform(-5, 5), rnd.uniform(-8, 8)) for _ in range(9)]

    def crescent(x, y):
        a, b = loc(x, y)
        sb = abs(b)
        # lua crescente: dentro do círculo grande, fora do círculo pequeno deslocado (para cada lado)
        d_out = math.hypot(a, sb - 3.6)
        d_in = math.hypot(a - 2.2, sb - 1.4)
        if d_out > 6.7 or d_in < 4.9 or sb < 0.6:
            return None
        edge = 6.7 - d_out
        c = mix(GOLD_L, GOLD, edge / 1.6) if edge < 1.6 else mix(GOLD, GOLD_D, (edge - 1.6) / 3.0)
        if abs(d_out - 4.2) < 0.22:                      # gravura ramificada
            c = GOLD_D
        for (ba, bb) in blood:
            if math.hypot(a - ba, sb - abs(bb)) < 0.75:
                c = (150, 12, 18)
        return c
    cv.shape((H[0] - 11, H[1] - 11, H[0] + 11, H[1] + 11), crescent)

    # ponta central perfurante
    tip = [(H[0] + h[0] * 1.0 + q[0] * 1.6, H[1] + h[1] * 1.0 + q[1] * 1.6),
           (H[0] + h[0] * 8.6, H[1] + h[1] * 8.6),
           (H[0] + h[0] * 1.0 - q[0] * 1.6, H[1] + h[1] * 1.0 - q[1] * 1.6)]
    def spike(x, y):
        a, b = loc(x, y)
        c = mix(GOLD_L, GOLD, 0.5)
        if abs(b) < 0.35:
            c = GOLD_L
        if a > 5.6 and abs(b) < 0.8:
            c = (150, 12, 18)
        return c
    cv.poly(tip, spike)
    # cubo da cabeça (anel)
    cv.shape((H[0] - 3, H[1] - 3, H[0] + 3, H[1] + 3),
             lambda x, y: (mix(GOLD_L, GOLD_D, math.hypot(x - H[0], y - H[1]) / 2.4) if math.hypot(x - H[0], y - H[1]) < 2.3 else None))
    return cv.render()


# ---------------------------------------------------------------- ARMADURA (ícones 32x32)
PUR, PUR_L, PUR_D, GLD = (126, 76, 196), (186, 140, 246), (74, 40, 128), (246, 206, 98)

def armor_fill(x, y, top=6, bot=28):
    t = (y - top) / max(1, (bot - top))
    return mix(PUR_L, PUR_D, t * 0.9)

def helmet():
    cv = Canvas(32)
    dome = [(7, 16), (8, 9), (11, 5.5), (16, 4), (21, 5.5), (24, 9), (25, 16), (25, 25), (21, 25), (20.5, 18.5), (11.5, 18.5), (11, 25), (7, 25)]
    cv.poly(dome, lambda x, y: armor_fill(x, y, 4, 25))
    cv.poly([(14.6, 4.2), (17.4, 4.2), (18.5, 11), (13.5, 11)], lambda x, y: mix(GLD, (200, 150, 50), (y - 4) / 8))      # crista dourada
    cv.poly([(10.5, 13.2), (21.5, 13.2), (21.0, 16.2), (11.0, 16.2)], lambda x, y: (28, 18, 50))                          # viseira
    cv.poly([(13, 14), (14, 14), (14, 15.6), (13, 15.6)], lambda x, y: (150, 255, 230))
    cv.poly([(18, 14), (19, 14), (19, 15.6), (18, 15.6)], lambda x, y: (150, 255, 230))
    cv.poly([(8.4, 12.6), (23.6, 12.6), (23.6, 13.2), (8.4, 13.2)], lambda x, y: GLD)
    return cv.render()

def chestplate():
    cv = Canvas(32)
    body = [(9, 8), (13, 6), (16, 7.5), (19, 6), (23, 8), (27, 10), (27, 15), (23, 16), (23, 27), (9, 27), (9, 16), (5, 15), (5, 10)]
    cv.poly(body, lambda x, y: armor_fill(x, y, 6, 27))
    cv.poly([(5, 10), (9, 8), (10, 14), (5, 15)], lambda x, y: mix(PUR_L, PUR, (y - 8) / 8))     # ombreira esq
    cv.poly([(27, 10), (23, 8), (22, 14), (27, 15)], lambda x, y: mix(PUR_L, PUR, (y - 8) / 8))  # ombreira dir
    cv.poly([(11.5, 7.5), (20.5, 7.5), (16, 12.5)], lambda x, y: (30, 20, 54))                   # gola
    cv.poly([(9, 23), (23, 23), (23, 25), (9, 25)], lambda x, y: GLD)                               # cinto
    cv.poly([(14.5, 15), (16, 13), (17.5, 15), (16, 19)], lambda x, y: mix((120, 255, 220), (40, 190, 170), (y - 13) / 6))  # gema da aurora
    cv.poly([(9, 20), (23, 20), (23, 20.8), (9, 20.8)], lambda x, y: PUR_D)
    return cv.render()

def leggings():
    cv = Canvas(32)
    cv.poly([(8, 7), (24, 7), (25, 16), (25, 27), (18, 27), (16, 14), (14, 27), (7, 27), (7, 16)], lambda x, y: armor_fill(x, y, 7, 27))
    cv.poly([(8, 7), (24, 7), (24, 10), (8, 10)], lambda x, y: GLD)
    cv.poly([(14.8, 7.4), (17.2, 7.4), (17.2, 9.8), (14.8, 9.8)], lambda x, y: (120, 255, 220))
    cv.poly([(7.5, 19), (14, 19), (14, 20.6), (7.5, 20.6)], lambda x, y: PUR_D)
    cv.poly([(18.5, 19), (24.5, 19), (24.5, 20.6), (18.5, 20.6)], lambda x, y: PUR_D)
    return cv.render()

def boots():
    cv = Canvas(32)
    for ox in (0, 15.5):
        cv.poly([(5 + ox, 10), (12 + ox, 10), (12 + ox, 22), (14.5 + ox, 24), (14.5 + ox, 27), (3.5 + ox, 27), (3.5 + ox, 23), (5 + ox, 22)],
                lambda x, y: armor_fill(x, y, 10, 27))
        cv.poly([(5 + ox, 10), (12 + ox, 10), (12 + ox, 12.4), (5 + ox, 12.4)], lambda x, y: GLD)
        cv.poly([(3.5 + ox, 25), (14.5 + ox, 25), (14.5 + ox, 27), (3.5 + ox, 27)], lambda x, y: PUR_D)
    return cv.render()

def shard():
    cv = Canvas(32)
    def crystal(x, y):
        t = (y - 4) / 24
        c = mix((236, 210, 255), (112, 62, 190), t)
        if x > 17:
            c = shade(c, 0.78)
        if abs(x - 16) < 0.5:
            c = (255, 255, 255)
        return c
    cv.poly([(16, 3), (22, 10), (21, 22), (16, 29), (11, 22), (10, 10)], crystal)
    cv.poly([(24.5, 15), (27, 18), (26, 23), (24.5, 25), (23, 22), (23.2, 18)], lambda x, y: mix((200, 160, 255), (110, 70, 190), (y - 15) / 10))
    cv.poly([(6, 17), (8, 15), (9.5, 19), (8.6, 23), (6.6, 24), (5.2, 21)], lambda x, y: mix((200, 160, 255), (110, 70, 190), (y - 15) / 10))
    return cv.render()


# ---------------------------------------------------------------- BLOCOS 16x16
def aurora_core_block():
    im = new(16, 16)
    r = random.Random(11)
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            base = (95 + int(80 * (1 - d / 16)), 40 + int(60 * (1 - d / 16)), 170 + int(70 * (1 - d / 16)))
            j = r.randint(-8, 8)
            put(im, x, y, (min(255, base[0] + j), min(255, base[1] + j), min(255, base[2] + j), 255))
    for i in range(16):
        put(im, i, i, (200, 150, 255, 255)); put(im, 15 - i, i, (170, 110, 240, 255))
    for x in range(16):
        for y in (0, 15):
            put(im, x, y, (45, 20, 85, 255)); put(im, y, x, (45, 20, 85, 255))
    for (x, y) in [(7, 7), (8, 7), (7, 8), (8, 8)]:
        put(im, x, y, (190, 255, 230, 255))
    for (x, y) in [(6, 7), (9, 8), (7, 6), (8, 9)]:
        put(im, x, y, (140, 235, 200, 255))
    return im

def braco_poco_tex():
    im = new(16, 16)
    r = random.Random(3)
    for y in range(16):
        for x in range(16):
            v = r.randint(-9, 9)
            put(im, x, y, (176 + v, 190 + v, 182 + v, 255))      # pele pálida acinzentada
    for x in range(1, 15, 3):                                    # veias azuladas
        for y in range(2, 14):
            if (y + x) % 5 < 2:
                put(im, x, y, (118, 140, 168, 255))
    for x in range(0, 16):
        put(im, x, 0, (96, 110, 104, 255)); put(im, x, 15, (96, 110, 104, 255))
    for (x, y) in [(3, 10), (8, 4), (12, 11)]:
        put(im, x, y, (92, 60, 70, 255))                          # manchas
    return im

def ice_egg_tex():
    im = new(16, 16)
    r = random.Random(5)
    for y in range(16):
        for x in range(16):
            v = r.randint(-8, 8)
            put(im, x, y, (150 + v, 205 + v, 240 + v, 255))
    for (x, y, rad) in [(4, 4, 2), (10, 6, 3), (6, 11, 2), (12, 12, 2), (3, 8, 1)]:
        for yy in range(16):
            for xx in range(16):
                if math.hypot(xx - x, yy - y) <= rad:
                    put(im, xx, yy, (88, 150, 214, 255))          # manchas azuis
    for (x, y) in [(2, 2), (9, 3), (5, 9), (13, 9), (7, 14)]:
        put(im, x, y, (245, 252, 255, 255))                       # brilhos de gelo
    return im


# ---------------------------------------------------------------- ARMADURA vestida (64x32)
def armor_layer(second):
    im = new(64, 32)
    r = random.Random(5 if second else 6)
    for y in range(32):
        for x in range(64):
            v = r.randint(-9, 9)
            t = (y % 16) / 16.0
            put(im, x, y, (118 + v - int(14 * t), 70 + v - int(10 * t), 186 + v - int(14 * t), 255))
    gold, dark, light, glow = (246, 206, 98, 255), (40, 22, 70, 255), (206, 168, 252, 255), (130, 255, 224, 255)
    if not second:
        rect(im, 0, 6, 32, 2, gold)
        rect(im, 8, 11, 8, 3, dark); put(im, 10, 12, glow); put(im, 13, 12, glow)
        rect(im, 12, 0, 8, 1, light)
        rect(im, 20, 20, 8, 1, gold); rect(im, 23, 21, 2, 6, light)
        rect(im, 23, 23, 2, 2, glow)
        rect(im, 20, 29, 8, 2, dark)
        for x in (44, 52):
            rect(im, x, 16, 4, 3, gold); rect(im, x, 19, 4, 1, dark)
        rect(im, 0, 26, 16, 2, dark); rect(im, 0, 24, 16, 1, gold)
    else:
        rect(im, 16, 16, 24, 2, gold); rect(im, 19, 17, 3, 3, glow)
        rect(im, 0, 20, 16, 1, gold); rect(im, 0, 24, 16, 1, dark)
        rect(im, 4, 17, 1, 8, light)
    return im


# ---------------------------------------------------------------- MOBS
def fill_skin(im, base, seed, amt=10):
    for (x, y, w, h) in [(0, 0, 32, 16), (16, 16, 24, 16), (40, 16, 16, 16), (0, 16, 16, 16), (32, 48, 16, 16), (16, 48, 16, 16)]:
        noise(im, x, y, w, h, base, amt, seed)

def imp():
    im = new(64, 64)
    fill_skin(im, (190, 45, 40), 3)
    dark, yel, white = (60, 10, 12, 255), (255, 210, 40, 255), (250, 245, 230, 255)
    rect(im, 8, 0, 8, 2, (120, 20, 20, 255))
    for x in (9, 10, 13, 14):
        put(im, x, 11, dark); put(im, x, 12, yel)
    rect(im, 10, 14, 4, 1, dark); put(im, 10, 14, white); put(im, 13, 14, white)
    rect(im, 20, 22, 8, 10, (215, 90, 70, 255))
    rect(im, 4, 20, 4, 5, (35, 20, 25, 255)); rect(im, 20, 52, 4, 5, (35, 20, 25, 255)); rect(im, 20, 29, 8, 2, (35, 20, 25, 255))
    for (x, y) in [(22, 24), (25, 26), (23, 28)]:
        put(im, x, y, (255, 150, 40, 255))
    return im

def shadow():
    im = new(64, 64)
    fill_skin(im, (28, 28, 36), 8, 5)
    green, gd = (60, 255, 120, 255), (25, 140, 70, 255)
    rect(im, 8, 8, 8, 8, (22, 22, 28, 255)); rect(im, 9, 12, 6, 1, (12, 12, 16, 255))
    for x in (9, 10, 13, 14):
        put(im, x, 12, green)
    rect(im, 23, 20, 2, 12, gd); rect(im, 20, 29, 8, 1, green)
    rect(im, 44, 26, 4, 1, gd); rect(im, 36, 58, 4, 1, gd); rect(im, 4, 29, 4, 1, gd); rect(im, 20, 61, 4, 1, gd)
    return im

def guardian():
    im = new(128, 128)
    r = random.Random(21)
    for y in range(128):
        for x in range(128):
            v = r.randint(-14, 14)
            put(im, x, y, (92 + v, 66 + v, 138 + v, 255))
    for _ in range(260):
        x, y = r.randint(0, 127), r.randint(0, 127)
        put(im, x, y, (225, 190, 255, 255))
        if r.random() < 0.4:
            put(im, x + 1, y, (170, 120, 240, 255))
    for _ in range(90):
        x, y = r.randint(0, 127), r.randint(0, 127)
        for k in range(r.randint(2, 5)):
            put(im, x + k, y + (k // 2), (45, 28, 80, 255))
    return im

def ocultista():
    """128x64. Região 0..63: corpo (túnica negra com runas douradas). x>=64: capuz, capa, ombreiras, túnica longa."""
    im = new(128, 64)
    ROBE, ROBE_D = (30, 20, 44), (18, 12, 28)
    CRIM, CRIM_D, CRIM_L = (150, 16, 30), (96, 8, 20), (206, 44, 56)
    GOLD, GOLD_D = (240, 192, 70), (150, 104, 30)
    r = random.Random(33)

    def cloth(x0, y0, w, h, base, amt=6):
        for yy in range(y0, y0 + h):
            for xx in range(x0, x0 + w):
                v = r.randint(-amt, amt)
                put(im, xx, yy, (max(0, base[0] + v), max(0, base[1] + v), max(0, base[2] + v), 255))

    # base do corpo
    for (x, y, w, h) in [(0, 0, 32, 16), (16, 16, 24, 16), (40, 16, 16, 16), (0, 16, 16, 16), (32, 48, 16, 16), (16, 48, 16, 16)]:
        cloth(x, y, w, h, ROBE)
    # rosto vazio dentro do capuz, olhos vermelhos brilhantes
    rect(im, 8, 8, 8, 8, (8, 4, 12, 255))
    for x in (9, 10, 13, 14):
        put(im, x, 12, (255, 70, 50, 255))
    put(im, 9, 11, (255, 190, 90, 255)); put(im, 14, 11, (255, 190, 90, 255))
    # peito: símbolo dourado (triângulo invertido com olho)
    for k in range(5):
        rect(im, 22 + k, 22 + k, 5 - 2 * k if 5 - 2 * k > 0 else 1, 1, GOLD)
    put(im, 24, 23, GOLD_D); put(im, 25, 23, GOLD_D)
    rect(im, 20, 20, 8, 1, GOLD_D); rect(im, 20, 29, 8, 1, GOLD_D)
    # mangas com punhos dourados
    for (x, y) in ((44, 27), (36, 59)):
        rect(im, x, y, 4, 1, GOLD); rect(im, x, y + 1, 4, 1, GOLD_D)
    # pernas escondidas pela túnica: escuras
    cloth(0, 16, 16, 16, ROBE_D); cloth(16, 48, 16, 16, ROBE_D)

    # capuz (64..95, 0..15): vermelho com friso dourado
    cloth(64, 0, 32, 16, CRIM, 8)
    rect(im, 72, 8, 8, 1, GOLD_D)               # friso frontal
    rect(im, 72, 15, 8, 1, CRIM_D)
    for x in range(72, 80):
        put(im, x, 9, CRIM_D)
    rect(im, 72, 10, 8, 6, (0, 0, 0, 0))        # abertura do capuz: deixa ver o rosto negro (face frontal)
    # capa (64..86, 16..38): costas = face (76..85, 17..38)
    cloth(64, 16, 22, 23, CRIM, 8)
    for yy in range(17, 39):
        for xx in range(76, 86):
            t = (yy - 17) / 22.0
            put(im, xx, yy, mix_c(CRIM_L, CRIM_D, t))
    # símbolo: estrela de 5 pontas dentro de um círculo, dourado
    cx, cy, R = 80.5, 27.0, 4.6
    for yy in range(17, 39):
        for xx in range(76, 86):
            d = math.hypot(xx - cx, yy - cy)
            if abs(d - R) < 0.55:
                put(im, xx, yy, (*GOLD, 255))
    pts = [(cx + R * 0.95 * math.sin(2 * math.pi * k / 5), cy - R * 0.95 * math.cos(2 * math.pi * k / 5)) for k in range(5)]
    for a in range(5):
        p0, p1 = pts[a], pts[(a + 2) % 5]
        for s in range(0, 21):
            xx = int(round(p0[0] + (p1[0] - p0[0]) * s / 20)); yy = int(round(p0[1] + (p1[1] - p0[1]) * s / 20))
            put(im, xx, yy, (*GOLD, 255))
    put(im, int(cx), int(cy), (255, 240, 170, 255))
    rect(im, 76, 17, 10, 1, GOLD)                # barra superior
    rect(im, 76, 37, 10, 1, GOLD); rect(im, 76, 36, 10, 1, GOLD_D)   # bainha dourada
    for xx in range(76, 86, 2):
        put(im, xx, 38, GOLD_D)
    # forro (face interna 65..74, 17..38): vinho escuro
    cloth(65, 17, 10, 22, CRIM_D, 4)
    # ombreiras (64..95, 40..47)
    cloth(64, 40, 32, 8, CRIM, 6)
    rect(im, 64, 44, 32, 1, GOLD); rect(im, 64, 47, 32, 1, GOLD_D)
    for xx in range(66, 94, 4):
        put(im, xx, 45, GOLD)
    # túnica longa (96..125, 16..34): roxo-negro com runas
    cloth(96, 16, 30, 19, (36, 22, 52), 5)
    for yy in range(22, 35):
        for xx in (102, 105, 108, 111, 114, 117, 120, 123):
            pass
    for xx in range(102, 111):                   # frente da túnica: faixa dourada central + runas
        put(im, xx, 22 + (xx % 3), (*GOLD_D, 255))
    rect(im, 106, 22, 1, 13, GOLD)
    for yy in range(24, 34, 3):
        rect(im, 104, yy, 2, 1, GOLD); rect(im, 108, yy, 2, 1, GOLD)
    rect(im, 102, 34, 9, 1, GOLD)                # bainha
    return im


def mix_c(a, b, t):
    c = mix(a, b, t)
    return (c[0], c[1], c[2], 255)


def ice_dragon():
    """128x128. Escamas azul-gelo, barriga clara, olhos ciano, chifres de osso, membrana das asas translúcida."""
    im = new(128, 128)
    r = random.Random(77)
    for y in range(128):
        for x in range(128):
            cell = ((x // 3) + (y // 3) * 7) % 5
            base = [(96, 156, 208), (84, 144, 198), (110, 170, 218), (90, 150, 204), (102, 162, 214)][cell]
            v = r.randint(-7, 7)
            edge = (x % 3 == 2 or y % 3 == 2)
            k = 0.88 if edge else 1.0
            put(im, x, y, (int((base[0] + v) * k), int((base[1] + v) * k), int((base[2] + v) * k), 255))
    # barriga (face inferior do corpo): região 32..44 x 0..20
    for yy in range(0, 20):
        for xx in range(32, 44):
            v = r.randint(-6, 6)
            put(im, xx, yy, (214 + v, 236 + v, 248 + v, 255) if (yy % 4) else (190 + v, 220 + v, 240 + v, 255))
    # cabeça (uv 48,32; 8x7x8): olhos nas faces laterais perto da frente
    for (ex, ey) in ((51, 42), (50, 42), (61 + 8, 42), (62 + 8, 42)):
        pass
    for (x, y) in ((54, 42), (53, 42), (54, 43), (53, 43)):         # face direita (borda direita é a frente)
        put(im, x, y, (150, 255, 255, 255))
    put(im, 54, 42, (255, 255, 255, 255))
    for (x, y) in ((65, 42), (66, 42), (65, 43), (66, 43)):         # face esquerda (borda esquerda é a frente)
        put(im, x, y, (150, 255, 255, 255))
    put(im, 65, 42, (255, 255, 255, 255))
    # focinho (0..22, 48..56): narinas escuras
    for yy in range(48, 56):
        for xx in range(0, 22):
            v = r.randint(-5, 5)
            put(im, xx, yy, (140 + v, 190 + v, 228 + v, 255))
    put(im, 6, 51, (40, 70, 110, 255)); put(im, 9, 51, (40, 70, 110, 255))
    # mandíbula e dentes (24..46, 48..56)
    for yy in range(48, 56):
        for xx in range(24, 46):
            v = r.randint(-5, 5)
            put(im, xx, yy, (200 + v, 226 + v, 244 + v, 255))
    # chifres de osso (48..64, 48..56)
    for yy in range(48, 56):
        for xx in range(48, 64):
            v = r.randint(-6, 6)
            put(im, xx, yy, (232 + v, 236 + v, 226 + v, 255))
    # espinhos (104..112, 60..65) e ponta da cauda
    for yy in range(60, 66):
        for xx in range(104, 112):
            put(im, xx, yy, (235, 245, 255, 255))
    # membrana das asas (32..74, 80..91): azul claro com veias
    for yy in range(80, 92):
        for xx in range(32, 74):
            v = r.randint(-5, 5)
            vein = ((xx + yy * 2) % 7 == 0)
            put(im, xx, yy, ((110, 170, 220, 255) if vein else (176 + v, 218 + v, 244 + v, 255)))
    # osso do braço da asa (0..28, 80..84)
    for yy in range(80, 85):
        for xx in range(0, 28):
            v = r.randint(-5, 5)
            put(im, xx, yy, (82 + v, 132 + v, 188 + v, 255))
    return im


if __name__ == "__main__":
    save_png(katana(), "item/katana_espadachim.png")
    save_png(lenhador(), "item/machado_lenhador.png")
    save_png(desastre(), "item/machado_desastre.png")
    save_png(shard(), "item/aurora_shard.png")
    save_png(helmet(), "item/aurora_helmet.png")
    save_png(chestplate(), "item/aurora_chestplate.png")
    save_png(leggings(), "item/aurora_leggings.png")
    save_png(boots(), "item/aurora_boots.png")
    save_png(aurora_core_block(), "block/aurora_core.png")
    save_png(braco_poco_tex(), "block/braco_poco.png")
    save_png(ice_egg_tex(), "block/ice_dragon_egg.png")
    save_png(armor_layer(False), "models/armor/aurora_layer_1.png")
    save_png(armor_layer(True), "models/armor/aurora_layer_2.png")
    save_png(imp(), "entity/imp_infernal.png")
    save_png(shadow(), "entity/espadachim_sombrio.png")
    save_png(guardian(), "entity/aurora_guardian.png")
    save_png(ocultista(), "entity/ocultista_diablos.png")
    save_png(ice_dragon(), "entity/ice_dragon.png")
    print("texturas ok")
