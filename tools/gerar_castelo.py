#!/usr/bin/env python3
"""Gera o Castelo de Gelo (só gelo que NÃO derrete: gelo compactado, gelo azul, neve e lanternas-do-mar).
Pátio -> Salão Principal (2 filhotes de dragão atacam aqui) -> arco -> Sala do Chefe (Dragão de Gelo Ancestral).
Rode: python3 tools/gerar_castelo.py"""
import os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from estruturas import *

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "data", "linhagens", "structure", "castelo_gelo.nbt")
SX = SZ = 65
S = 3
SY = S + 34
W = World(SX, SY, SZ)
rnd = random.Random(99)
PI, BI, SN, SL, GL = "packed_ice", "blue_ice", "snow_block", "sea_lantern", "light_blue_stained_glass"

def wall_block():
    r = rnd.random()
    return BI if r < 0.12 else PI

# ------------------------------------------------------------ terreno
for x in range(SX):
    for z in range(SZ):
        for y in range(0, S):
            W.put(x, y, z, SN)
        W.put(x, S, z, SN if (x + z) % 7 else PI)
        for y in range(S + 1, SY):
            W.put(x, y, z, "air")

def box(x1, y1, z1, x2, y2, z2, fn):
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            for z in range(z1, z2 + 1):
                W.put(x, y, z, fn())

def hollow(x1, y1, z1, x2, y2, z2, fn=wall_block):
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            for z in range(z1, z2 + 1):
                if x in (x1, x2) or z in (z1, z2) or y in (y1, y2):
                    W.put(x, y, z, fn())
                else:
                    W.put(x, y, z, "air")

def air(x1, y1, z1, x2, y2, z2):
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            for z in range(z1, z2 + 1):
                W.put(x, y, z, "air")

def crenel(x1, z1, x2, z2, y):
    for x in range(x1, x2 + 1):
        for z in range(z1, z2 + 1):
            if x in (x1, x2) or z in (z1, z2):
                if (x + z) % 2 == 0:
                    W.put(x, y, z, PI)

# ------------------------------------------------------------ muralha externa (x,z 2..62), altura 9
box(2, S + 1, 2, 62, S + 9, 4, wall_block);   box(2, S + 1, 60, 62, S + 9, 62, wall_block)
box(2, S + 1, 2, 4, S + 9, 62, wall_block);   box(60, S + 1, 2, 62, S + 9, 62, wall_block)
for x in range(2, 63):                                          # base de gelo azul
    for z in (2, 3, 4, 60, 61, 62):
        W.put(x, S + 1, z, BI)
for z in range(2, 63):
    for x in (2, 3, 4, 60, 61, 62):
        W.put(x, S + 1, z, BI)
# interior do corredor de patrulha
air(3, S + 7, 3, 61, S + 9, 3); air(3, S + 7, 61, 61, S + 9, 61); air(3, S + 7, 3, 3, S + 9, 61); air(61, S + 7, 3, 61, S + 9, 61)
for x in range(3, 62):
    for z in (3, 61):
        W.put(x, S + 6, z, PI)
for z in range(3, 62):
    for x in (3, 61):
        W.put(x, S + 6, z, PI)
crenel(2, 2, 62, 62, S + 10)
# portão sul (7 de largura), arco
air(29, S + 1, 60, 35, S + 7, 62)
air(30, S + 8, 60, 34, S + 8, 62)
for x in range(28, 37):
    W.put(x, S + 9, 61, BI)

# ------------------------------------------------------------ torres de canto (7x7, altura 20) com telhado cônico
def tower(x0, z0):
    hollow(x0, S + 1, z0, x0 + 6, S + 20, z0 + 6, wall_block)
    for y in range(S + 1, S + 20):
        for (cx, cz) in ((x0, z0), (x0 + 6, z0), (x0, z0 + 6), (x0 + 6, z0 + 6)):
            W.put(cx, y, cz, BI)
    for y in (S + 6, S + 12, S + 17):
        for (a, b) in ((x0 + 3, z0), (x0 + 3, z0 + 6), (x0, z0 + 3), (x0 + 6, z0 + 3)):
            W.put(a, y, b, GL); W.put(a, y + 1, b, GL)
    for y in (S + 6, S + 13):
        for x in range(x0 + 1, x0 + 6):
            for z in range(z0 + 1, z0 + 6):
                W.put(x, y, z, PI)
    W.put(x0 + 3, S + 5, z0 + 3, SL); W.put(x0 + 3, S + 12, z0 + 3, SL)
    for k in range(5):                                           # cone
        for x in range(x0 - 1 + k, x0 + 8 - k):
            for z in range(z0 - 1 + k, z0 + 8 - k):
                if x in (x0 - 1 + k, x0 + 7 - k) or z in (z0 - 1 + k, z0 + 7 - k):
                    W.put(x, S + 21 + k, z, BI)
                else:
                    W.put(x, S + 21 + k, z, PI)
    W.put(x0 + 3, S + 26, z0 + 3, BI); W.put(x0 + 3, S + 27, z0 + 3, SL)
    W.put(x0 + 3, S + 28, z0 + 3, "end_rod", facing="up")
    # porta da torre para o corredor da muralha
    W.put(x0 + 3, S + 1, z0 + 3, "air")

tower(0, 0); tower(58, 0); tower(0, 58); tower(58, 58)

# ------------------------------------------------------------ pátio: caminho, pilares de gelo e fonte congelada
for z in range(5, 60):
    for x in range(30, 35):
        W.put(x, S, z, BI if (x + z) % 2 else PI)
for (px, pz) in ((25, 52), (39, 52), (25, 46), (39, 46)):
    for y in range(S + 1, S + 7):
        W.put(px, y, pz, BI)
    W.put(px, S + 7, pz, SL)
for dx in range(-2, 3):
    for dz in range(-2, 3):
        if max(abs(dx), abs(dz)) == 2:
            W.put(32 + dx, S + 1, 52 + dz, BI)
        elif max(abs(dx), abs(dz)) <= 1:
            W.put(32 + dx, S + 1, 52 + dz, PI)
for y in range(S + 2, S + 6):
    W.put(32, y, 52, BI)
W.put(32, S + 6, 52, SL)
# estátuas de espinhos de gelo
for (sx, sz) in ((12, 52), (52, 52), (12, 30), (52, 30)):
    for k in range(6):
        h = max(1, 6 - k)
        for y in range(S + 1, S + 1 + h):
            W.put(sx + (k % 3) - 1, y, sz + (k // 3), BI if y % 2 else PI)

# ------------------------------------------------------------ fortaleza: Salão Principal (z 28..46) e Sala do Chefe (z 8..26)
KX1, KX2 = 14, 50
# Salão principal
hollow(KX1, S, 28, KX2, S + 14, 46, wall_block)
# Sala do chefe (grande, separada)
hollow(KX1, S, 6, KX2, S + 18, 27, wall_block)
# parede divisória: z 26..28 já são paredes; abre arco largo (7x9) entre as salas
air(29, S + 1, 26, 35, S + 9, 28)
air(30, S + 10, 26, 34, S + 10, 28)
# entrada do salão (arco no lado sul, z=46)
air(29, S + 1, 46, 35, S + 8, 46)
air(30, S + 9, 46, 34, S + 9, 46)
for x in range(28, 37):
    W.put(x, S + 10, 46, BI)

def interior_floor(x1, z1, x2, z2, checker):
    for x in range(x1 + 1, x2):
        for z in range(z1 + 1, z2):
            W.put(x, S, z, (BI if (x + z) % 2 == 0 else PI) if checker else PI)

interior_floor(KX1, 28, KX2, 46, False)
interior_floor(KX1, 6, KX2, 27, True)
# tapete de gelo azul do portão até o trono
for z in range(7, 46):
    for x in (31, 32, 33):
        if W.get(x, S + 1, z) == "minecraft:air":
            W.put(x, S, z, BI)

# pilares e lanternas no salão
for z in (31, 37, 43):
    for x in (18, 25, 39, 46):
        for y in range(S + 1, S + 14):
            W.put(x, y, z, BI)
        W.put(x, S + 3, z, SL)
        W.put(x, S + 9, z, SL)
# janelas altas do salão
for z in (32, 38, 42):
    for y in range(S + 4, S + 11):
        W.put(KX1, y, z, GL); W.put(KX2, y, z, GL)
# lustres (lanternas-do-mar penduradas por barras de gelo)
for x in (24, 32, 40):
    for z in (34, 40):
        for y in range(S + 11, S + 14):
            W.put(x, y, z, PI)
        W.put(x, S + 10, z, SL)

# Sala do chefe: pilares, janelas, estalactites e trono
for z in (10, 16, 22):
    for x in (18, 25, 39, 46):
        for y in range(S + 1, S + 18):
            W.put(x, y, z, BI)
        W.put(x, S + 4, z, SL); W.put(x, S + 11, z, SL)
for z in (10, 14, 20):
    for y in range(S + 5, S + 14):
        W.put(KX1, y, z, GL); W.put(KX2, y, z, GL)
for _ in range(110):                                              # estalactites
    x, z = rnd.randint(KX1 + 1, KX2 - 1), rnd.randint(7, 26)
    length = rnd.randint(2, 6)
    for k in range(length):
        W.put(x, S + 17 - k, z, PI if k < length - 1 else BI)
# trono de gelo azul no fundo
for x in range(28, 37):
    W.put(x, S + 1, 8, BI)
for x in range(29, 36):
    W.put(x, S + 1, 7, BI); W.put(x, S + 2, 7, BI)
for y in range(S + 1, S + 8):
    for x in (29, 35):
        W.put(x, y, 7, BI)
for y in range(S + 3, S + 10):
    W.put(32, y, 7, BI); W.put(31, y, 7, BI); W.put(33, y, 7, BI)
W.put(32, S + 10, 7, SL)
# baús de tesouro nos cantos do fundo
W.put(17, S + 1, 8, "chest", nbt=CHEST_CASTELO, facing="south", type="single", waterlogged=False)
W.put(47, S + 1, 8, "chest", nbt=CHEST_CASTELO, facing="south", type="single", waterlogged=False)
W.put(17, S + 1, 9, "chest", nbt=CHEST_CASTELO, facing="south", type="single", waterlogged=False)
W.put(47, S + 1, 9, "chest", nbt=CHEST_CASTELO, facing="south", type="single", waterlogged=False)
# baú extra no salão
W.put(16, S + 1, 36, "chest", nbt=CHEST_CASTELO, facing="east", type="single", waterlogged=False)

# telhados planos com ameias e espiral de gelo
for (z1, z2, h) in ((28, 46, S + 14), (6, 27, S + 18)):
    crenel(KX1, z1, KX2, z2, h + 1)
for (x, z, h) in ((32, 37, 9), (32, 16, 12)):
    base = S + (14 if z == 37 else 18)
    for k in range(h):
        for dx in range(-(h - k) // 3, (h - k) // 3 + 1):
            for dz in range(-(h - k) // 3, (h - k) // 3 + 1):
                W.put(x + dx, base + 1 + k, z + dz, BI if k % 2 else PI)
    W.put(x, base + h + 1, z, SL)

# ------------------------------------------------------------ criaturas
# 2 filhotes atacam no salão principal (antes da sala do chefe); o chefe espera sozinho na sala grande.
W.entity(24.5, S + 1, 37.5, mob_nbt("linhagens:ice_dragon_baby"))
W.entity(40.5, S + 1, 37.5, mob_nbt("linhagens:ice_dragon_baby"))
W.entity(32.5, S + 1, 14.5, mob_nbt("linhagens:ice_dragon"))

W.connect()
print("castelo:", W.save(OUT))
