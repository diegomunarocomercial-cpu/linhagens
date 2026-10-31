#!/usr/bin/env python3
"""Gera a estrutura "Vila Aurora" (data/linhagens/structure/vila_aurora.nbt). Rode: python3 tools/gerar_vila.py
Tudo é construído por código: edite os parâmetros de casas/torre/fazenda abaixo para mudar a vila."""
import gzip, math, os, random, struct

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "data", "linhagens", "structure", "vila_aurora.nbt")

# ---------------------------------------------------------------- NBT mínimo
class Int:
    def __init__(s, v): s.v = v
class Byte:
    def __init__(s, v): s.v = v
class Dbl:
    def __init__(s, v): s.v = v
class Str:
    def __init__(s, v): s.v = v
class Lst:
    def __init__(s, t, items): s.t, s.items = t, items
class Comp:
    def __init__(s, d): s.d = d

TID = {Byte: 1, Int: 3, Dbl: 6, Str: 8, Lst: 9, Comp: 10}

def enc_str(x):
    b = x.encode("utf-8")
    return struct.pack(">H", len(b)) + b

def enc(v):
    if isinstance(v, Int): return struct.pack(">i", v.v)
    if isinstance(v, Byte): return struct.pack(">b", v.v)
    if isinstance(v, Dbl): return struct.pack(">d", v.v)
    if isinstance(v, Str): return enc_str(v.v)
    if isinstance(v, Lst):
        return struct.pack(">bi", v.t, len(v.items)) + b"".join(enc(i) for i in v.items)
    if isinstance(v, Comp):
        out = b""
        for k, x in v.d.items():
            out += struct.pack(">b", TID[type(x)]) + enc_str(k) + enc(x)
        return out + b"\x00"
    raise TypeError(v)

# ---------------------------------------------------------------- mundo
SX = SZ = 45
S = 5            # camada da superfície (y do bloco de grama)
SY = S + 22
B = {}           # (x,y,z) -> (nome, props, nbt)
ENT = []         # entidades
rnd = random.Random(2026)
M = "minecraft:"


def put(x, y, z, name, nbt=None, **props):
    if not name.startswith(("minecraft:", "linhagens:")):
        name = M + name
    B[(x, y, z)] = (name, tuple(sorted((k, str(v).lower() if isinstance(v, bool) else str(v)) for k, v in props.items())), nbt)


def get(x, y, z):
    return B.get((x, y, z), (M + "air", (), None))[0]


# base: fundação + grama + ar (limpa árvores/relevo dentro da vila)
for x in range(SX):
    for z in range(SZ):
        for y in range(0, S):
            put(x, y, z, "dirt")
        put(x, S, z, "grass_block")
        for y in range(S + 1, SY):
            put(x, y, z, "air")

occ = set()   # colunas ocupadas (para espalhar flores só no que sobrou)
C = 22

# ---------------------------------------------------------------- estradas e praça
def road_block():
    r = rnd.random()
    return "gravel" if r < 0.18 else ("coarse_dirt" if r < 0.3 else "dirt_path")

for i in range(SX):
    for dz in (-1, 0, 1):
        put(i, S, C + dz, road_block()); occ.add((i, C + dz))
        put(C + dz, S, i, road_block()); occ.add((C + dz, i))

for x in range(SX):
    for z in range(SZ):
        d = math.hypot(x - C, z - C)
        if d <= 7.6:
            occ.add((x, z))
            if d > 6.0:
                put(x, S, z, rnd.choice(["stone_bricks", "stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks"]))
            elif d > 3.9:
                put(x, S, z, "polished_deepslate")
            else:
                put(x, S, z, "polished_andesite")

# fonte com o Núcleo da Aurora
for dx in range(-3, 4):
    for dz in range(-3, 4):
        m = max(abs(dx), abs(dz))
        if m == 3:
            put(C + dx, S + 1, C + dz, "polished_blackstone_bricks")
        elif m <= 2:
            put(C + dx, S, C + dz, "water", level=0)
put(C, S, C, "polished_blackstone_bricks")
put(C, S + 1, C, "polished_blackstone_bricks")
put(C, S + 2, C, "amethyst_block")
put(C, S + 3, C, "linhagens:aurora_core")
put(C, S + 4, C, "amethyst_block")
put(C, S + 5, C, "amethyst_cluster", facing="up", waterlogged=False)
put(C, S + 6, C, "end_rod", facing="up")
for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
    put(C + dx, S + 1, C + dz, "amethyst_cluster", facing="up", waterlogged=False)

# postes de luz em volta da praça
for dx, dz in [(-6, -6), (6, -6), (-6, 6), (6, 6), (-10, 0), (10, 0), (0, -10), (0, 10)]:
    x, z = C + dx, C + dz
    for y in range(S + 1, S + 4):
        put(x, y, z, "spruce_fence")
    put(x, S + 4, z, "lantern", hanging=False, waterlogged=False)
    occ.add((x, z))

# ---------------------------------------------------------------- casas
CHEST = Comp({"id": Str("minecraft:chest"), "LootTable": Str("linhagens:chests/vila_aurora")})

def villager(x, z, y=None):
    y = S + 1 if y is None else y
    ENT.append((x + 0.5, y, z + 0.5, Comp({
        "id": Str("minecraft:villager"), "PersistenceRequired": Byte(1),
        "VillagerData": Comp({"level": Int(1), "profession": Str("minecraft:none"), "type": Str("minecraft:plains")})})))

def guardian(x, z):
    ENT.append((x + 0.5, S + 1, z + 0.5, Comp({"id": Str("linhagens:aurora_guardian"), "PersistenceRequired": Byte(1)})))


def house(x0, z0, W, D, door, kind):
    x1, z1 = x0 + W - 1, z0 + D - 1
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            occ.add((x, z))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            put(x, S, z, "spruce_planks")
    xd = x0 + W // 2
    zd = z1 if door == "S" else z0
    for y in range(S + 1, S + 5):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x not in (x0, x1) and z not in (z0, z1):
                    continue
                corner = x in (x0, x1) and z in (z0, z1)
                if corner:
                    put(x, y, z, "stripped_dark_oak_log", axis="y")
                elif y == S + 1:
                    put(x, y, z, "stone_bricks")
                else:
                    off = (x - x0) if z in (z0, z1) else (z - z0)
                    if y in (S + 2, S + 3) and off % 4 == 2 and not (x == xd and z == zd):
                        put(x, y, z, "purple_stained_glass_pane", waterlogged=False)
                    else:
                        put(x, y, z, "spruce_planks")
    face = "south" if door == "S" else "north"
    put(xd, S + 1, zd, "spruce_door", facing=face, half="lower", hinge="left", open=False, powered=False)
    put(xd, S + 2, zd, "spruce_door", facing=face, half="upper", hinge="left", open=False, powered=False)
    # teto
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            put(x, S + 5, z, "spruce_planks")
    R = D + 2
    for r in range(R):
        z = z0 - 1 + r
        k = min(r, R - 1 - r)
        y = S + 5 + k
        for x in range(x0 - 1, x1 + 2):
            if r < R - 1 - r:
                put(x, y, z, "dark_oak_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
            elif r > R - 1 - r:
                put(x, y, z, "dark_oak_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
            else:
                put(x, y, z, "dark_oak_planks")
    for z in range(z0, z1 + 1):
        r = z - (z0 - 1)
        k = min(r, R - 1 - r)
        for y in range(S + 6, S + 5 + k):
            for x in (x0, x1):
                put(x, y, z, "purple_terracotta")
    # caminho até a estrada
    if door == "S":
        for z in range(z1 + 1, C - 1):
            put(xd, S, z, "dirt_path"); occ.add((xd, z))
    else:
        for z in range(z0 - 1, C + 1, -1):
            put(xd, S, z, "dirt_path"); occ.add((xd, z))
    # interior
    back_z = z0 + 1 if door == "S" else z1 - 1
    fd = 1 if door == "S" else -1
    fz = "south" if fd == 1 else "north"
    cx, cz = x0 + W // 2, z0 + D // 2
    put(cx, S + 4, cz, "lantern", hanging=True, waterlogged=False)
    if kind in ("home", "home2"):
        bed = "purple_bed" if kind == "home" else "red_bed"
        put(x0 + 1, S + 1, back_z, bed, facing="east", occupied=False, part="foot")
        put(x0 + 2, S + 1, back_z, bed, facing="east", occupied=False, part="head")
        put(x1 - 1, S + 1, back_z, "chest", nbt=CHEST, facing=fz, type="single", waterlogged=False)
        put(x1 - 2, S + 1, back_z, "crafting_table")
        put(x1 - 1, S + 1, back_z + fd, "furnace", facing=fz, lit=False)
        for dx in (-1, 0, 1):
            put(cx + dx, S + 1, cz, "purple_carpet")
        villager(cx, cz + fd)
    elif kind == "library":
        for x in range(x0 + 1, x1):
            for y in (S + 1, S + 2):
                put(x, y, back_z, "bookshelf")
        put(cx, S + 1, back_z + fd, "lectern", facing=fz, has_book=False, powered=False)
        put(cx, S + 1, cz, "enchanting_table")
        put(x1 - 1, S + 1, cz + fd, "chest", nbt=CHEST, facing=fz, type="single", waterlogged=False)
        villager(cx - 1, cz + fd)
        villager(cx + 1, cz + fd)
    elif kind == "smithy":
        put(x0 + 1, S + 1, back_z, "blast_furnace", facing=fz, lit=False)
        put(x0 + 2, S + 1, back_z, "smithing_table")
        put(x0 + 3, S + 1, back_z, "grindstone", face="floor", facing=fz)
        put(x1 - 1, S + 1, back_z, "anvil", facing="east")
        put(x1 - 2, S + 1, back_z, "cauldron")
        put(x1 - 1, S + 1, back_z + fd, "chest", nbt=CHEST, facing=fz, type="single", waterlogged=False)
        villager(cx, cz + fd)


def tower(x0, z0):
    x1, z1 = x0 + 4, z0 + 4
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            occ.add((x, z))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            put(x, S, z, "stone_bricks")
    top = S + 12
    for y in range(S + 1, top + 1):
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x not in (x0, x1) and z not in (z0, z1):
                    continue
                corner = x in (x0, x1) and z in (z0, z1)
                if corner:
                    put(x, y, z, "deepslate_bricks")
                elif y % 4 == 2 and (x == x0 + 2 or z == z0 + 2) and y > S + 2:
                    put(x, y, z, "purple_stained_glass_pane", waterlogged=False)
                else:
                    put(x, y, z, "stone_bricks" if y % 5 else "mossy_stone_bricks")
    xd = x0 + 2
    put(xd, S + 1, z1, "air"); put(xd, S + 2, z1, "air")
    put(xd, S + 1, z1, "spruce_door", facing="south", half="lower", hinge="left", open=False, powered=False)
    put(xd, S + 2, z1, "spruce_door", facing="south", half="upper", hinge="left", open=False, powered=False)
    for z in range(z1 + 1, C - 1):
        put(xd, S, z, "dirt_path"); occ.add((xd, z))
    for y in range(S + 1, top + 1):                       # escada de mão
        put(xd, y, z0 + 1, "ladder", facing="south", waterlogged=False)
    for x in range(x0 + 1, x1):                           # piso do topo (com buraco para a escada)
        for z in range(z0 + 1, z1):
            if (x, z) != (xd, z0 + 1):
                put(x, top, z, "dark_oak_planks")
    put(x1 - 1, top + 1, z1 - 1, "chest", nbt=CHEST, facing="north", type="single", waterlogged=False)
    for y in (top + 1, top + 2):                          # sala de observação em vidro roxo
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                if x in (x0, x1) or z in (z0, z1):
                    put(x, y, z, "purple_stained_glass")
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            put(x, top + 3, z, "amethyst_block")
    put(xd, top + 4, z0 + 2, "end_rod", facing="up")
    put(xd, top + 2, z0 + 2, "lantern", hanging=False, waterlogged=False)
    put(xd, top + 1, z0 + 2, "air")


def farm(x0, z0):
    x1, z1 = x0 + 7, z0 + 7
    for x in range(x0 - 1, x1 + 2):
        for z in range(z0 - 1, z1 + 2):
            occ.add((x, z))
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if x in (x0, x1) or z in (z0, z1):
                put(x, S + 1, z, "spruce_fence")
            else:
                put(x, S, z, "farmland", moisture=7)
                crop = ["wheat", "carrots", "potatoes", "beetroots"][(z - z0) % 4]
                put(x, S + 1, z, crop, age=3 if crop == "beetroots" else 7)
    for x in (x0 + 3, x0 + 4):                            # água no meio
        put(x, S, z0 + 3, "water", level=0)
        put(x, S + 1, z0 + 3, "air")
    put(x0 + 3, S + 1, z0, "spruce_fence_gate", facing="north", in_wall=False, open=False, powered=False)
    put(x0 + 4, S + 1, z0, "spruce_fence_gate", facing="north", in_wall=False, open=False, powered=False)
    put(x0 - 1, S + 1, z0 + 1, "composter", level=0)
    put(x0 - 1, S + 1, z0 + 3, "hay_block", axis="y")
    put(x0 - 1, S + 2, z0 + 3, "hay_block", axis="y")
    villager(x0 + 2, z0 - 1)
    for z in range(z0 - 2, C + 1, -1):
        put(x0 + 3, S, z, "dirt_path"); occ.add((x0 + 3, z))


# ----- layout (norte = z pequeno). Estradas em x=21..23 e z=21..23
house(3, 4, 9, 7, "S", "home")
house(14, 3, 7, 8, "S", "library")
house(25, 4, 9, 7, "S", "home2")
tower(37, 4)
house(3, 30, 9, 8, "N", "smithy")
house(14, 31, 7, 7, "N", "home2")
house(25, 30, 9, 8, "N", "home")
farm(35, 31)

guardian(13, 22); guardian(31, 22)
villager(18, 18); villager(26, 26); villager(22, 17)

# ---------------------------------------------------------------- decoração
FLOWERS = ["allium", "cornflower", "allium", "lily_of_the_valley", "azure_bluet"]
for x in range(SX):
    for z in range(SZ):
        if (x, z) in occ or get(x, S + 1, z) != M + "air":
            continue
        r = rnd.random()
        if r < 0.07:
            put(x, S + 1, z, rnd.choice(FLOWERS))
        elif r < 0.085:
            put(x, S + 1, z, "amethyst_cluster", facing="up", waterlogged=False)

# ---------------------------------------------------------------- conexões de vidraças e cercas
SOLID = ("planks", "log", "bricks", "terracotta", "stone", "glass", "amethyst_block", "deepslate", "dirt", "grass_block",
         "bookshelf", "crafting_table", "smithing_table", "furnace", "hay_block", "composter")
NOT_SOLID = ("stairs", "slab", "pane", "fence", "gate", "cluster")

def solid(name):
    n = name.replace("minecraft:", "")
    return any(s in n for s in SOLID) and not any(s in n for s in NOT_SOLID)

DIRS = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}
for (x, y, z), (name, props, nbt) in list(B.items()):
    n = name.replace("minecraft:", "")
    kind = "pane" if n.endswith("_pane") else ("fence" if n.endswith("_fence") else None)
    if not kind:
        continue
    p = dict(props)
    for d, (dx, dz) in DIRS.items():
        nb = get(x + dx, y, z + dz)
        nn = nb.replace("minecraft:", "")
        p[d] = str((kind in nn and "gate" not in nn) or (kind == "fence" and "fence_gate" in nn) or solid(nb)).lower()
    p.setdefault("waterlogged", "false")
    B[(x, y, z)] = (name, tuple(sorted(p.items())), nbt)

# ---------------------------------------------------------------- gravar
palette, pidx = [], {}
blocks = []
for (x, y, z), (name, props, nbt) in sorted(B.items(), key=lambda kv: (kv[0][1], kv[0][0], kv[0][2])):
    key = (name, props)
    if key not in pidx:
        pidx[key] = len(palette)
        d = {"Name": Str(name)}
        if props:
            d["Properties"] = Comp({k: Str(v) for k, v in props})
        palette.append(Comp(d))
    e = {"pos": Lst(3, [Int(x), Int(y), Int(z)]), "state": Int(pidx[key])}
    if nbt is not None:
        e["nbt"] = nbt
    blocks.append(Comp(e))

entities = []
for (x, y, z, nbt) in ENT:
    entities.append(Comp({"pos": Lst(6, [Dbl(x), Dbl(float(y)), Dbl(z)]),
                          "blockPos": Lst(3, [Int(int(x)), Int(int(y)), Int(int(z))]), "nbt": nbt}))

root = Comp({"DataVersion": Int(3955), "size": Lst(3, [Int(SX), Int(SY), Int(SZ)]),
             "palette": Lst(10, palette), "blocks": Lst(10, blocks), "entities": Lst(10, entities)})
data = struct.pack(">b", 10) + enc_str("") + enc(root)
os.makedirs(os.path.dirname(OUT), exist_ok=True)
with gzip.open(OUT, "wb") as f:
    f.write(data)
print("blocos:", len(blocks), "paleta:", len(palette), "entidades:", len(entities), "bytes:", os.path.getsize(OUT))
