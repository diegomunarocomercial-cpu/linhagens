#!/usr/bin/env python3
"""Gera a Vila Aurora (uma vila "normal": 6 casas simples, 5 médias, 1 grande do chefe, poço com o Braço, 5 plantações).
O Núcleo da Aurora fica ENTERRADO sob o poço: é ele que faz a aurora aparecer no céu. Rode: python3 tools/gerar_vila.py"""
import math, os, random, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from estruturas import *

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "data", "linhagens", "structure", "vila_aurora.nbt")
SX = SZ = 80
S = 3
SY = S + 21
W = World(SX, SY, SZ)
rnd = random.Random(1234)
occ = set()
C = 40  # centro

# ----------------------------------------------------------- terreno base
for x in range(SX):
    for z in range(SZ):
        for y in range(0, S):
            W.put(x, y, z, "dirt")
        W.put(x, S, z, "grass_block")
        for y in range(S + 1, SY):
            W.put(x, y, z, "air")

def road_block():
    r = rnd.random()
    return "gravel" if r < 0.12 else ("coarse_dirt" if r < 0.22 else "dirt_path")

for i in range(SX):
    for d in (-1, 0, 1):
        W.put(i, S, C + d, road_block()); occ.add((i, C + d))
        if i >= 23:
            W.put(C + d, S, i, road_block()); occ.add((C + d, i))

def path_v(x, z_from, z_to):
    step = 1 if z_to >= z_from else -1
    for z in range(z_from, z_to + step, step):
        W.put(x, S, z, "dirt_path"); occ.add((x, z))

def villager(x, z, y=None):
    W.entity(x + 0.5, S + 1 if y is None else y, z + 0.5, villager_nbt())

# ----------------------------------------------------------- casas
def house(x0, z0, Wd, D, door, kind, roof="spruce", hw=4, chimney=False):
    x1, z1 = x0 + Wd - 1, z0 + D - 1
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            occ.add((x, z))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            W.put(x, S, z, "oak_planks")
    xd = x0 + Wd // 2
    zd = z1 if door == "S" else z0
    for y in range(S + 1, S + 1 + hw):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x not in (x0, x1) and z not in (z0, z1):
                    continue
                if x in (x0, x1) and z in (z0, z1):
                    W.put(x, y, z, "oak_log", axis="y")
                elif y == S + 1:
                    W.put(x, y, z, "cobblestone")
                else:
                    off = (x - x0) if z in (z0, z1) else (z - z0)
                    if y in (S + 2, S + 3) and off % 4 == 2 and not (x == xd and z == zd):
                        W.put(x, y, z, "glass_pane", waterlogged=False)
                    else:
                        W.put(x, y, z, "oak_planks")
    face = "south" if door == "S" else "north"
    W.put(xd, S + 1, zd, "oak_door", facing=face, half="lower", hinge="left", open=False, powered=False)
    W.put(xd, S + 2, zd, "oak_door", facing=face, half="upper", hinge="left", open=False, powered=False)
    top = S + 1 + hw
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            W.put(x, top, z, "oak_planks")
    R = D + 2
    for r in range(R):
        z = z0 - 1 + r
        k = min(r, R - 1 - r)
        y = top + k
        for x in range(x0, x1 + 1):
            if r < R - 1 - r:
                W.put(x, y, z, f"{roof}_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
            elif r > R - 1 - r:
                W.put(x, y, z, f"{roof}_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
            else:
                W.put(x, y, z, f"{roof}_planks")
    for z in range(z0, z1 + 1):
        r = z - (z0 - 1)
        k = min(r, R - 1 - r)
        for y in range(top + 1, top + k):
            for x in (x0, x1):
                W.put(x, y, z, "oak_planks")
    if chimney:
        cz = z0 + D // 2
        for y in range(S + 1, top + (R // 2) + 2):
            W.put(x1 + 1, y, cz, "cobblestone")
        W.put(x1 + 1, S + 1, cz, "cobblestone")
        W.put(x1 + 1, top + (R // 2) + 1, cz, "campfire", facing="north", lit=True, signal_fire=False, waterlogged=False)
    # caminho até a estrada
    if door == "S":
        path_v(xd, z1 + 1, C - 2)
    else:
        path_v(xd, z0 - 1, C + 2)
    # interior
    back_z = z0 + 1 if door == "S" else z1 - 1
    fd = 1 if door == "S" else -1
    fz = "south" if fd == 1 else "north"
    cx, cz2 = x0 + Wd // 2, z0 + D // 2
    W.put(cx, top - 1, cz2, "lantern", hanging=True, waterlogged=False)
    W.put(x0 + 1, S + 1, back_z, "red_bed", facing="east", occupied=False, part="foot")
    W.put(x0 + 2, S + 1, back_z, "red_bed", facing="east", occupied=False, part="head")
    W.put(x1 - 1, S + 1, back_z, "crafting_table")
    if kind == "medium":
        W.put(x1 - 2, S + 1, back_z, "chest", nbt=CHEST_VILA, facing=fz, type="single", waterlogged=False)
        W.put(x1 - 1, S + 1, back_z + fd, "furnace", facing=fz, lit=False)
        W.put(x1 - 1, S + 1, back_z + 2 * fd, "bookshelf")
        W.put(x1 - 1, S + 2, back_z + 2 * fd, "bookshelf")
        W.put(x0 + 1, S + 1, back_z + fd * 2, "white_carpet")
        W.put(cx, S + 1, cz2, "oak_fence")
        W.put(cx, S + 2, cz2, "oak_pressure_plate")
        villager(cx - 1, cz2)
    else:
        W.put(x1 - 1, S + 1, back_z + fd, "oak_fence")
        W.put(x1 - 1, S + 2, back_z + fd, "oak_pressure_plate")
        villager(cx, cz2)
    return xd

def medium(x0, z0, door):  # 8 x 7
    house(x0, z0, 8, 7, door, "medium", roof="spruce", chimney=True)

def simple(x0, z0, door):  # 5 x 6
    house(x0, z0, 5, 6, door, "simple", roof="oak")

# ----------------------------------------------------------- casa grande do chefe (2 andares)
def chief(x0, z0):
    Wd, D = 13, 11
    x1, z1 = x0 + Wd - 1, z0 + D - 1
    for x in range(x0 - 2, x1 + 3):
        for z in range(z0 - 2, z1 + 5):
            occ.add((x, z))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            W.put(x, S, z, "spruce_planks")
    xd = x0 + Wd // 2
    hw = 8
    for y in range(S + 1, S + 1 + hw):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x not in (x0, x1) and z not in (z0, z1):
                    continue
                if x in (x0, x1) and z in (z0, z1):
                    W.put(x, y, z, "stripped_oak_log" if y == S + 5 else "oak_log", axis="y")
                elif y == S + 1:
                    W.put(x, y, z, "stone_bricks")
                elif y == S + 5:
                    W.put(x, y, z, "oak_log", axis="x" if z in (z0, z1) else "z")
                else:
                    off = (x - x0) if z in (z0, z1) else (z - z0)
                    if y in (S + 3, S + 7) and off % 3 == 1 and 0 < off < (Wd - 1 if z in (z0, z1) else D - 1) and not (x == xd and z == z1):
                        W.put(x, y, z, "glass_pane", waterlogged=False)
                    else:
                        W.put(x, y, z, "oak_planks")
    # piso do 2º andar (com buraco para a escada) e teto
    for x in range(x0 + 1, x1):
        for z in range(z0 + 1, z1):
            if not (x == x0 + 1 and z == z0 + 1):
                W.put(x, S + 5, z, "spruce_planks")
            W.put(x, S + 9, z, "spruce_planks")
    for y in range(S + 1, S + 6):
        W.put(x0 + 1, y, z0 + 1, "ladder", facing="south", waterlogged=False)
    # porta principal
    W.put(xd, S + 1, z1, "dark_oak_door", facing="south", half="lower", hinge="left", open=False, powered=False)
    W.put(xd, S + 2, z1, "dark_oak_door", facing="south", half="upper", hinge="left", open=False, powered=False)
    W.put(xd - 1, S + 3, z1 + 1, "red_wall_banner", facing="south"); W.put(xd + 1, S + 3, z1 + 1, "red_wall_banner", facing="south")
    # sacada no 2º andar
    for x in range(xd - 2, xd + 3):
        for z in (z1 + 1, z1 + 2):
            W.put(x, S + 5, z, "spruce_planks")
        for z in (z1 + 1, z1 + 2):
            if x in (xd - 2, xd + 2) or z == z1 + 2:
                W.put(x, S + 6, z, "oak_fence")
    for x in (xd - 2, xd + 2):
        for y in range(S + 1, S + 5):
            W.put(x, y, z1 + 2, "oak_fence")
    W.put(xd, S + 6, z1, "dark_oak_door", facing="south", half="lower", hinge="left", open=False, powered=False)
    W.put(xd, S + 7, z1, "dark_oak_door", facing="south", half="upper", hinge="left", open=False, powered=False)
    # telhado
    top = S + 1 + hw + 1
    R = D + 4
    for r in range(R):
        z = z0 - 2 + r
        k = min(r, R - 1 - r)
        y = top + k
        for x in range(x0 - 1, x1 + 2):
            if r < R - 1 - r:
                W.put(x, y, z, "dark_oak_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
            elif r > R - 1 - r:
                W.put(x, y, z, "dark_oak_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
            else:
                W.put(x, y, z, "dark_oak_planks")
    for z in range(z0, z1 + 1):
        r = z - (z0 - 2)
        k = min(r, R - 1 - r)
        for y in range(top + 1, top + k):
            for x in (x0, x1):
                W.put(x, y, z, "spruce_planks")
    # chaminé
    for y in range(S + 1, top + 5):
        W.put(x1 + 1, y, z0 + 4, "stone_bricks")
    W.put(x1 + 1, top + 5, z0 + 4, "campfire", facing="north", lit=True, signal_fire=False, waterlogged=False)
    # --- térreo: salão do chefe
    W.put(x0 + 3, S + 4, z0 + 5, "lantern", hanging=True, waterlogged=False); W.put(x1 - 3, S + 4, z0 + 5, "lantern", hanging=True, waterlogged=False)
    for z in range(z0 + 2, z1):                                    # tapete vermelho até o fundo
        W.put(xd, S + 1, z, "red_carpet")
    for x in range(x0 + 1, x1):                                    # estantes no fundo
        W.put(x, S + 1, z0 + 1, "bookshelf"); W.put(x, S + 2, z0 + 1, "bookshelf")
    W.put(xd, S + 1, z0 + 1, "lectern", facing="south", has_book=False, powered=False)
    W.put(xd, S + 2, z0 + 1, "air")
    W.put(xd - 3, S + 1, z0 + 2, "oak_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)   # trono do chefe
    W.put(xd + 3, S + 1, z0 + 2, "oak_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    for dx in (-2, -1, 1, 2):                                      # mesa longa
        W.put(xd + dx, S + 1, z0 + 6, "oak_fence"); W.put(xd + dx, S + 2, z0 + 6, "oak_pressure_plate")
    W.put(x1 - 1, S + 1, z0 + 2, "chest", nbt=CHEST_VILA, facing="west", type="single", waterlogged=False)
    W.put(x1 - 1, S + 1, z0 + 3, "chest", nbt=CHEST_VILA, facing="west", type="single", waterlogged=False)
    W.put(x0 + 1, S + 1, z0 + 8, "crafting_table"); W.put(x0 + 1, S + 1, z0 + 9, "furnace", facing="east", lit=False)
    W.put(x1 - 1, S + 1, z1 - 1, "barrel", facing="up", open=False)
    # --- 2º andar: quartos
    W.put(x0 + 3, S + 6, z0 + 2, "red_bed", facing="east", occupied=False, part="foot"); W.put(x0 + 4, S + 6, z0 + 2, "red_bed", facing="east", occupied=False, part="head")
    W.put(x1 - 4, S + 6, z0 + 2, "red_bed", facing="west", occupied=False, part="foot"); W.put(x1 - 5, S + 6, z0 + 2, "red_bed", facing="west", occupied=False, part="head")
    W.put(xd, S + 6, z0 + 1, "chest", nbt=CHEST_VILA, facing="south", type="single", waterlogged=False)
    W.put(x0 + 4, S + 6, z0 + 6, "oak_fence"); W.put(x0 + 4, S + 7, z0 + 6, "oak_pressure_plate")
    W.put(x1 - 4, S + 6, z0 + 6, "oak_fence"); W.put(x1 - 4, S + 7, z0 + 6, "oak_pressure_plate")
    W.put(xd, S + 8, z0 + 5, "lantern", hanging=True, waterlogged=False)
    villager(xd, z0 + 4); villager(xd + 2, z0 + 4, S + 6)
    path_v(xd, z1 + 3, C - 2)

# ----------------------------------------------------------- plantações
def farm(x0, z0, Wd, D, gate_side, gate_x, seed):
    r = random.Random(seed)
    x1, z1 = x0 + Wd - 1, z0 + D - 1
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            occ.add((x, z))
    crops = ["wheat", "carrots", "potatoes", "beetroots"]
    r.shuffle(crops)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x in (x0, x1) or z in (z0, z1):
                W.put(x, S + 1, z, "oak_fence")
            else:
                W.put(x, S, z, "farmland", moisture=7)
                c = crops[(z - z0) // 2 % 4]
                W.put(x, S + 1, z, c, age=3 if c == "beetroots" else 7)
    wx, wz = x0 + Wd // 2, z0 + D // 2
    W.put(wx, S, wz, "water", level=0); W.put(wx, S + 1, wz, "air")
    gz = z0 if gate_side == "N" else z1
    W.put(gate_x, S + 1, gz, "oak_fence_gate", facing="north", in_wall=False, open=False, powered=False)
    W.put(x0 - 1, S + 1, z0 + 1, "composter", level=0)
    W.put(x0 - 1, S + 1, z0 + 3, "hay_block", axis="y")
    W.put(x1 + 1, S + 1, z1 - 1, "hay_block", axis="y")
    W.put(x1 + 1, S + 1, z1 - 2, "pumpkin")
    villager(x0 + 2, z0 - 1 if gate_side == "N" else z1 + 1)
    if gate_side == "N":
        path_v(gate_x, gz - 1, C + 2)
    else:
        path_v(gate_x, gz + 1, C - 2)

# ----------------------------------------------------------- layout
chief(34, 12)

simple(5, 30, "S"); medium(13, 29, "S"); medium(52, 29, "S"); simple(64, 30, "S")
simple(22, 14, "S"); simple(72, 13, "S")
simple(5, 45, "N"); medium(13, 45, "N"); medium(52, 45, "N"); simple(64, 45, "N")
medium(24, 62, "N")

farm(4, 58, 10, 8, "N", 11, 1)
farm(60, 58, 10, 8, "N", 69, 2)
farm(44, 58, 10, 8, "N", 46, 3)
farm(6, 5, 10, 8, "S", 10, 4)
farm(60, 4, 10, 8, "S", 61, 5)

# ----------------------------------------------------------- poço do centro (com o Braço e o Núcleo escondido)
for x in range(C - 3, C + 4):
    for z in range(C - 3, C + 4):
        occ.add((x, z))
for dx in range(-2, 3):
    for dz in range(-2, 3):
        m = max(abs(dx), abs(dz))
        x, z = C + dx, C + dz
        if m == 2:
            W.put(x, S, z, "cobblestone")
            W.put(x, S + 1, z, rnd.choice(["cobblestone", "mossy_cobblestone", "cobblestone"]))
        else:
            W.put(x, S, z, "water", level=0)
            W.put(x, S - 1, z, "water", level=0)
            W.put(x, S - 2, z, "stone_bricks")
            for y in range(S + 1, S + 4):
                W.put(x, y, z, "air")
W.put(C, S - 3, C, "linhagens:aurora_core")                 # a fonte da aurora, escondida debaixo do poço
W.put(C, S + 1, C, "linhagens:braco_poco")                   # a mão que sai da água
for dx, dz in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
    W.put(C + dx, S + 2, C + dz, "oak_fence"); W.put(C + dx, S + 3, C + dz, "oak_fence")
for dx in range(-2, 3):
    for dz in range(-2, 3):
        W.put(C + dx, S + 4, C + dz, "spruce_slab", type="bottom", waterlogged=False)
W.put(C, S + 3, C - 2, "lantern", hanging=True, waterlogged=False)
for dx, dz in ((-4, -1), (4, 1)):
    W.entity(C + dx + 0.5, S + 1, C + dz + 0.5, mob_nbt("linhagens:aurora_guardian"))
villager(C - 4, C + 4); villager(C + 4, C - 4); villager(C + 3, C + 5)

# ----------------------------------------------------------- postes de luz e decoração
for i in range(6, SX - 4, 10):
    for (x, z) in ((C - 2, i), (C + 2, i), (i, C - 2), (i, C + 2)):
        if (x, z) in occ and W.get(x, S, z) not in ("minecraft:grass_block",):
            continue
        if any(abs(x - C) <= 3 and abs(z - C) <= 3 for _ in [0]):
            continue
        for y in range(S + 1, S + 4):
            W.put(x, y, z, "oak_fence")
        W.put(x, S + 4, z, "lantern", hanging=False, waterlogged=False)
        occ.add((x, z))

FLOWERS = ["poppy", "dandelion", "cornflower", "allium", "azure_bluet", "oxeye_daisy"]
for x in range(SX):
    for z in range(SZ):
        if (x, z) in occ or W.get(x, S + 1, z) != "minecraft:air" or W.get(x, S, z) != "minecraft:grass_block":
            continue
        r = rnd.random()
        if r < 0.06:
            W.put(x, S + 1, z, rnd.choice(FLOWERS))
        elif r < 0.12:
            W.put(x, S + 1, z, "short_grass")

W.connect()
print("vila:", W.save(OUT))
