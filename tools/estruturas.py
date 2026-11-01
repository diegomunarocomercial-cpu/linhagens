"""Ferramentas compartilhadas para gerar estruturas (.nbt) do Minecraft por código."""
import gzip, os, struct

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

def _str(x):
    b = x.encode("utf-8")
    return struct.pack(">H", len(b)) + b

def enc(v):
    if isinstance(v, Int): return struct.pack(">i", v.v)
    if isinstance(v, Byte): return struct.pack(">b", v.v)
    if isinstance(v, Dbl): return struct.pack(">d", v.v)
    if isinstance(v, Str): return _str(v.v)
    if isinstance(v, Lst): return struct.pack(">bi", v.t, len(v.items)) + b"".join(enc(i) for i in v.items)
    if isinstance(v, Comp):
        out = b""
        for k, x in v.d.items():
            out += struct.pack(">b", TID[type(x)]) + _str(k) + enc(x)
        return out + b"\x00"
    raise TypeError(v)

SOLID = ("planks", "log", "bricks", "terracotta", "stone", "glass", "amethyst_block", "deepslate", "dirt", "grass_block",
         "bookshelf", "crafting_table", "smithing_table", "furnace", "hay_block", "composter", "ice", "snow_block", "sea_lantern",
         "quartz", "concrete", "wool")
NOT_SOLID = ("stairs", "slab", "pane", "fence", "gate", "cluster", "bars", "wall")
DIRS = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}


class World:
    def __init__(self, sx, sy, sz):
        self.sx, self.sy, self.sz = sx, sy, sz
        self.B = {}
        self.ENT = []

    def put(self, x, y, z, name, nbt=None, **props):
        if not name.startswith(("minecraft:", "linhagens:")):
            name = "minecraft:" + name
        if not (0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz):
            return
        self.B[(x, y, z)] = (name, tuple(sorted((k, str(v).lower() if isinstance(v, bool) else str(v)) for k, v in props.items())), nbt)

    def get(self, x, y, z):
        return self.B.get((x, y, z), ("minecraft:air", (), None))[0]

    def fill(self, x1, y1, z1, x2, y2, z2, name, **props):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.put(x, y, z, name, **props)

    def entity(self, x, y, z, nbt):
        self.ENT.append((x, y, z, nbt))

    def _solid(self, name):
        n = name.replace("minecraft:", "")
        return any(s in n for s in SOLID) and not any(s in n for s in NOT_SOLID)

    def connect(self):
        """Liga vidraças, cercas e barras de ferro às vizinhas (estruturas não atualizam isso sozinhas)."""
        for (x, y, z), (name, props, nbt) in list(self.B.items()):
            n = name.replace("minecraft:", "")
            kind = "pane" if n.endswith("_pane") else ("fence" if n.endswith("_fence") else ("bars" if n == "iron_bars" else None))
            if not kind:
                continue
            p = dict(props)
            for d, (dx, dz) in DIRS.items():
                nb = self.get(x + dx, y, z + dz)
                nn = nb.replace("minecraft:", "")
                ok = (kind in nn and "gate" not in nn) or (kind == "fence" and "fence_gate" in nn) or self._solid(nb)
                p[d] = str(ok).lower()
            p.setdefault("waterlogged", "false")
            self.B[(x, y, z)] = (name, tuple(sorted(p.items())), nbt)

    def save(self, path, data_version=3955):
        palette, pidx, blocks = [], {}, []
        for (x, y, z), (name, props, nbt) in sorted(self.B.items(), key=lambda kv: (kv[0][1], kv[0][0], kv[0][2])):
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
        ents = [Comp({"pos": Lst(6, [Dbl(x), Dbl(float(y)), Dbl(z)]),
                      "blockPos": Lst(3, [Int(int(x)), Int(int(y)), Int(int(z))]), "nbt": nbt}) for (x, y, z, nbt) in self.ENT]
        root = Comp({"DataVersion": Int(data_version), "size": Lst(3, [Int(self.sx), Int(self.sy), Int(self.sz)]),
                     "palette": Lst(10, palette), "blocks": Lst(10, blocks), "entities": Lst(10, ents)})
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with gzip.open(path, "wb") as f:
            f.write(struct.pack(">b", 10) + _str("") + enc(root))
        return len(blocks), len(palette), len(ents), os.path.getsize(path)


CHEST_VILA = Comp({"id": Str("minecraft:chest"), "LootTable": Str("linhagens:chests/vila_aurora")})
CHEST_CASTELO = Comp({"id": Str("minecraft:chest"), "LootTable": Str("linhagens:chests/castelo_gelo")})

def villager_nbt():
    return Comp({"id": Str("minecraft:villager"), "PersistenceRequired": Byte(1),
                 "VillagerData": Comp({"level": Int(1), "profession": Str("minecraft:none"), "type": Str("minecraft:plains")})})

def mob_nbt(entity_id):
    return Comp({"id": Str(entity_id), "PersistenceRequired": Byte(1)})
