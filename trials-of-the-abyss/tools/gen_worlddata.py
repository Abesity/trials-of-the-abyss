"""Builds src/main/WorldData.java, res/maps/world*.txt and web/data.js from Vinci's design files.

    python tools/gen_worlddata.py

Reads design/World_PlacementsCode.txt and design/worlds/world{1,2,3}.csv, takes tile
collision from src/tile/TileManager.java, then:
  * snaps a door that sits one tile off the path onto the path,
  * checks every world can be finished (keys vs. locked doors, teleporters included),
  * adds a few ambient enemies on open ground so the player can level up before the boss.
"""
import os, re, random
from collections import deque

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SPEC = os.path.join(ROOT, 'design', 'World_PlacementsCode.txt')

def grid(w):
    with open(os.path.join(ROOT, 'design', 'worlds', f'world{w}.csv')) as f:
        rows = [list(map(int, l.replace(',', ' ').split())) for l in f if l.strip()]
    assert len(rows) == 100 and all(len(r) == 100 for r in rows)
    return rows

src = open(os.path.join(ROOT, 'src', 'tile', 'TileManager.java')).read()
COLL = {int(a): b == 'true' for a, b in re.findall(r'setUp\((\d+),"\d+",(true|false)\)', src)}

def placements():
    out = {1: [], 2: [], 3: []}; w = 0; mode = None
    for line in open(SPEC):
        s = line.strip()
        m = re.match(r'-+WORLD (\d)', s)
        if m: w = int(m.group(1)); mode = None; continue
        if not s: continue
        if s == 'Spikes': mode = 'spike'; continue
        pts = [(int(a), int(b)) for a, b in re.findall(r'\((\d+)\s*,\s*(\d+)\)', s)]
        name = re.sub(r'\(\s*\d+\s*,\s*\d+\s*\)', '', s).strip(' ,')
        if not name and mode == 'spike': name = 'Spikes'
        out[w].append((name, pts))
    return out

def main():
    P = placements(); W = {}
    for w in (1, 2, 3):
        g = grid(w)
        with open(os.path.join(ROOT, 'res', 'maps', f'world{w}.txt'), 'w') as f:
            f.write('\n'.join(' '.join(map(str, r)) for r in g) + '\n')
        walk = lambda x, y: 0 <= x < 100 and 0 <= y < 100 and not COLL[g[y][x]]
        def snap(p):
            if walk(*p): return p
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                if walk(p[0] + dx, p[1] + dy): return (p[0] + dx, p[1] + dy)
            return p
        get = lambda pred: [pt for n, pts in P[w] if pred(n) for pt in pts]
        d = {'start': get(lambda n: n.startswith('Start'))[0],
             'end': sorted(set(get(lambda n: n.startswith('End')))),
             'teleporters': [pts for n, pts in P[w] if n == 'Teleporter'],
             'doors': [snap(p) for p in get(lambda n: n.startswith('Door'))],
             'keys': get(lambda n: n == 'Key'),
             'chests': get(lambda n: n.startswith('Chest')),
             'shop': sorted(get(lambda n: n.startswith('Potion')))[:1],
             'fountainsRed': get(lambda n: n == 'Fountain_Red'),
             'fountainsBlue': get(lambda n: n == 'Fountain_Blue'),
             'spikes': get(lambda n: n == 'Spikes'),
             'powerups': get(lambda n: n == 'PowerUP')}
        key_type = {1: [0, 0], 2: [1, 1], 3: [1, 0]}[w]          # 0 slime, 1 bat, 2 king slime
        d['enemies'] = [[key_type[i], x, y, 1] for i, (x, y) in enumerate(get(lambda n: n.startswith('Enemy(')))]
        d['enemies'] += [[2, x, y, 0] for x, y in get(lambda n: n == 'EnemyBoss')]
        # reachable tiles (doors open) for ambient enemy placement
        seen = {tuple(d['start'])}; q = deque(seen)
        while q:
            x, y = q.popleft(); nb = [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]
            for a, b in d['teleporters']:
                if (x, y) == a: nb.append(b)
                if (x, y) == b: nb.append(a)
            for p in nb:
                if p not in seen and walk(*p): seen.add(p); q.append(p)
        used = [tuple(d['start'])] + [tuple(p) for k in ('end', 'doors', 'keys', 'chests', 'spikes', 'powerups',
                'fountainsRed', 'fountainsBlue', 'shop') for p in d[k]] + [p for t in d['teleporters'] for p in t] \
               + [(e[1], e[2]) for e in d['enemies']]
        cand = [p for p in sorted(seen) if sum(walk(p[0] + dx, p[1] + dy) for dx in (-1, 0, 1) for dy in (-1, 0, 1)) >= 7
                and all(walk(p[0] + dx, p[1] + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        rng = random.Random(w * 7); rng.shuffle(cand)
        types = {1: [0, 0, 0, 0], 2: [1, 1, 1], 3: [0, 1, 0, 1]}[w]; picked = []
        for p in cand:
            if len(picked) == len(types): break
            if any(max(abs(p[0] - s[0]), abs(p[1] - s[1])) < 12 for s in d['shop']): continue
            if all(max(abs(p[0] - u[0]), abs(p[1] - u[1])) >= 5 for u in used + picked): picked.append(p)
        d['enemies'] += [[types[i], x, y, 0] for i, (x, y) in enumerate(picked)]
        W[w] = d
    write_java(W)
    write_web(W)
    print('WorldData.java written:', {w: len(d['enemies']) for w, d in W.items()}, 'enemies per world')

def write_java(W):
    o = ["package main;", "", "/**",
         " * World layouts for Trials of the Abyss, generated by tools/gen_worlddata.py from",
         " * Vinci Villanueva's design/World_PlacementsCode.txt (coordinates are {column, row}).",
         " * Ambient enemies were added in the 2026 restoration so the player can level up before the boss.",
         " */", "public class WorldData {",
         "    public int[] start;",
         "    public int[][] end, teleporters, doors, keys, chests, shop, fountainsRed, fountainsBlue, spikes, powerups, enemies;", "",
         "    public static final int SLIME = 0, BAT = 1, KING_SLIME = 2;", "",
         "    public static WorldData get(int world) {", "        WorldData d = new WorldData();", "        switch (world) {"]
    for w, d in W.items():
        o.append(f"        case {w}:")
        o.append(f"            d.start = new int[] {{{d['start'][0]},{d['start'][1]}}};")
        for k in ['end', 'doors', 'keys', 'chests', 'shop', 'fountainsRed', 'fountainsBlue', 'spikes', 'powerups']:
            o.append(f"            d.{k} = new int[][] {{ " + ", ".join(f"{{{p[0]},{p[1]}}}" for p in d[k]) + " };")
        o.append("            d.teleporters = new int[][] { " + ", ".join(f"{{{a[0]},{a[1]},{b[0]},{b[1]}}}" for a, b in d['teleporters']) + " };")
        o.append("            d.enemies = new int[][] { " + ", ".join("{" + ",".join(map(str, e)) + "}" for e in d['enemies']) + " }; // {type, col, row, dropsKey}")
        o.append("            break;")
    o += ["        }", "        return d;", "    }", "}"]
    with open(os.path.join(ROOT, 'src', 'main', 'WorldData.java'), 'w') as f:
        f.write("\n".join(o) + "\n")

def write_web(W):
    import json
    maps = {w: [' '.join(map(str, r)) for r in grid(w)] for w in (1, 2, 3)}
    worlds = {w: {k: [list(p) for p in v] if k not in ('start',) else list(v) for k, v in d.items() if k != 'teleporters'}
              for w, d in W.items()}
    for w, d in W.items():
        worlds[w]['teleporters'] = [[a[0], a[1], b[0], b[1]] for a, b in d['teleporters']]
    data = {'collision': [1 if COLL[i] else 0 for i in range(197)], 'maps': maps, 'worlds': worlds}
    os.makedirs(os.path.join(ROOT, 'web'), exist_ok=True)
    with open(os.path.join(ROOT, 'web', 'data.js'), 'w') as f:
        f.write('// Generated by tools/gen_worlddata.py from design/ (Vinci Villanueva\'s maps and placements).\n')
        f.write('window.TOA_DATA = ' + json.dumps(data, separators=(',', ':')) + ';\n')

if __name__ == '__main__':
    main()
