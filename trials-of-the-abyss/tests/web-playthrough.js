// Test-only bot for the web build (mirrors harness/Bot.java). Runs inside the page via Playwright.
async (opts) => {
  const T = window.__toa, gp = T.gp, p = T.player, S = T.S, B = T.B, battle = T.battle;
  const log = []; const note = s => log.push(`[w${gp.world} f${gp.frame}] ${s}`);
  const shots = []; const shot = n => { T.render(); shots.push([n, document.getElementById('game').toDataURL()]); };
  const tick = n => { for (let i = 0; i < n; i++) T.update(); };
  const press = c => { T.keyDown(c); tick(2); T.keyUp(c); tick(2); };
  const col = () => Math.floor((p.x + 26) / 48), row = () => Math.floor((p.y + 34) / 48);
  const D = () => window.TOA_DATA.worlds[gp.world];
  const walk = (c, r) => c >= 0 && r >= 0 && c < 100 && r < 100 && window.TOA_DATA.collision[gp.map[r * 100 + c]] === 0;
  const objAt = (c, r) => gp.obj.find(o => o && Math.floor(o.x / 48) === c && Math.floor(o.y / 48) === r);
  const keys = () => p.inventory.get('Key') || 0;
  let battles = 0, lastWorld = 0, lastTile = -1, lastTileFrame = 0;
  shot('title'); T.newGame();
  function goals() {
    const g = [];
    for (const o of gp.obj) { if (!o) continue; if (o.kind === 'key') g.push([o.x / 48, o.y / 48, 0]); if (o.kind === 'chest' && !o.opened) g.push([o.x / 48, o.y / 48, 1]); }
    const bossNext = gp.world === 3 && !T.events.bossDefeated;
    const needHeal = gp.world === 3 && (p.life < p.maxLife * 0.95 || p.mana < p.maxMana * 0.95);
    for (const m of gp.monster) if (m && (m.dropsKey || (m.boss && !needHeal) || p.level < 4)) g.push([Math.floor((m.x + 24) / 48), Math.floor((m.y + 24) / 48), 2]);
    if (keys() > 0) for (const o of gp.obj) if (o && o.kind === 'door') g.push([o.x / 48, o.y / 48, 1]);
    if (p.life < p.maxLife * (bossNext ? 0.95 : 0.5)) for (const f of D().fountainsRed) g.unshift([f[0], f[1], 1]);
    if (p.mana < p.maxMana * (bossNext ? 0.95 : 0.4)) for (const f of D().fountainsBlue) g.unshift([f[0], f[1], 1]);
    for (const e of D().end) g.push([e[0], e[1], 3]);
    return g;
  }
  function push(t, bump) {
    const wantX = t[0] * 48 - 2, wantY = t[1] * 48 - 10, c0 = col(), r0 = row(), k = T.keys;
    for (let i = 0; i < 40 && gp.state === S.PLAY; i++) {
      if (Math.abs(col() - c0) + Math.abs(row() - r0) > 2) break;
      const dx = wantX - p.x, dy = wantY - p.y, sc = col(), sr = row();
      k.up = k.down = k.left = k.right = false;
      const horiz = t[1] === sr && t[0] !== sc, vert = t[0] === sc && t[1] !== sr;
      if (bump || horiz || vert) {
        if (horiz || (bump && t[1] === sr)) { const ay = sr * 48 - 10 - p.y; if (Math.abs(ay) > p.speed) { if (ay < 0) k.up = true; else k.down = true; } else if (dx < 0) k.left = true; else k.right = true; }
        else { const ax = sc * 48 - 2 - p.x; if (Math.abs(ax) > p.speed) { if (ax < 0) k.left = true; else k.right = true; } else if (dy < 0) k.up = true; else k.down = true; }
      } else if (Math.abs(dx) > p.speed) { if (dx < 0) k.left = true; else k.right = true; }
      else if (Math.abs(dy) > p.speed) { if (dy < 0) k.up = true; else k.down = true; } else break;
      if (bump) k.enter = true;
      T.update();
      if (!bump && col() === t[0] && row() === t[1] && Math.abs(wantX - p.x) <= p.speed + 4 && Math.abs(wantY - p.y) <= p.speed + 4) break;
    }
    k.up = k.down = k.left = k.right = k.enter = false;
  }
  function step() {
    const sc = col(), sr = row();
    if (sc * 100 + sr !== lastTile) { lastTile = sc * 100 + sr; lastTileFrame = gp.frame; }
    if (gp.frame - lastTileFrame > 3000) { note('same tile too long at ' + sc + ',' + sr); return false; }
    const gl = goals(), prev = new Map(), seen = new Set([sr * 100 + sc]), q = [[sc, sr]];
    let found = null;
    while (q.length && !found) {
      const cur = q.shift();
      for (const g of gl) {
        const adj = Math.abs(g[0] - cur[0]) + Math.abs(g[1] - cur[1]) === 1, on = g[0] === cur[0] && g[1] === cur[1];
        if ((g[2] === 1 && adj) || ((g[2] === 0 || g[2] === 3) && on) || (g[2] === 2 && (on || adj))) {
          if (g[2] === 3 && gp.world === 3 && !T.events.bossDefeated) continue; found = cur; break;
        }
      }
      if (found) break;
      const nb = [[cur[0] + 1, cur[1]], [cur[0] - 1, cur[1]], [cur[0], cur[1] + 1], [cur[0], cur[1] - 1]];
      for (const t of D().teleporters) { if (t[0] === cur[0] && t[1] === cur[1]) nb.push([t[2], t[3]]); if (t[2] === cur[0] && t[3] === cur[1]) nb.push([t[0], t[1]]); }
      for (const n of nb) {
        const id = n[1] * 100 + n[0]; if (seen.has(id) || !walk(n[0], n[1])) continue;
        const o = objAt(n[0], n[1]); if (o && o.collision) continue;
        seen.add(id); prev.set(id, cur); q.push(n);
      }
    }
    if (!found) return false;
    const path = []; let c = found;
    while (c && !(c[0] === sc && c[1] === sr)) { path.unshift(c); c = prev.get(c[1] * 100 + c[0]); }
    if (!path.length) { for (const g of gl) if (Math.abs(g[0] - sc) + Math.abs(g[1] - sr) <= 1) { push([g[0], g[1]], true); return true; } return true; }
    const t = path[0];
    if (Math.abs(t[0] - sc) + Math.abs(t[1] - sr) > 1) {
      for (const d of [[0, 1], [0, -1], [1, 0], [-1, 0]]) if (walk(sc + d[0], sr + d[1]) && walk(sc + 2 * d[0], sr + 2 * d[1])) {
        push([sc + d[0], sr + d[1]]); push([sc + 2 * d[0], sr + 2 * d[1]]); push([sc + d[0], sr + d[1]]); push([sc, sr]); break; }
      return true;
    }
    push(t, false); return true;
  }
  function fight() {
    const e = battle.enemy; if (!e) { tick(1); return; }
    if (battle.counter === 0 && battle.state === B.CHOICE && !battle._seen) {
      battle._seen = true; battles++; note(`battle ${battles} vs ${e.name} hp=${p.life}/${p.maxLife}`);
      if (opts.shots && (battles === 1 || e.name === 'King Slime' || (e.name === 'Bat' && battles < 8))) { tick(20); shot('battle_' + e.name.replace(' ', '')); }
    }
    const pick = (n) => { T.keyDown('Enter'); };
    if (p.life < p.maxLife * 0.4 && (p.inventory.get('HP Potion') || 0) > 0) { battle.choice(1); battle.item(p.usableItems().indexOf('HP Potion')); }
    else if (p.mana < 12 && (p.inventory.get('MP Potion') || 0) > 0 && e.name === 'King Slime') { battle.choice(1); battle.item(p.usableItems().indexOf('MP Potion')); }
    else if (e.name === 'King Slime' && battle.counter % 4 === 1 && p.mana >= 10) { battle.choice(2); battle.skill(1); }
    else if (p.mana >= 15) { battle.choice(2); battle.skill(2); }
    else if (p.mana >= 5) { battle.choice(2); battle.skill(0); }
    else battle.choice(0);
    tick(3);
    if (gp.state === S.BATTLE2) press('Escape');
  }
  let guard = 0;
  while (gp.state !== S.ENDING && guard++ < 200000) {
    if (gp.world !== lastWorld) { lastWorld = gp.world; note(`entered world ${gp.world} keys=${keys()} lvl=${p.level}`); tick(5); if (opts.shots) shot('world' + gp.world); }
    if (gp.state === S.BATTLE1 || gp.state === S.BATTLE2) { fight(); continue; }
    if (gp.state === S.VICTORY) { if (opts.shots && battles === 1) shot('victory'); battle._seen = false; press('Enter'); continue; }
    if (gp.state === S.DEFEAT) { note('DEFEATED ' + battle.text); shot('defeat'); return { ok: false, log, shots }; }
    if (gp.state === S.DIALOGUE) { press('Enter'); continue; }
    if (gp.state !== S.PLAY) { note('unexpected state ' + gp.state); press('Escape'); continue; }
    if (!step()) { note('STUCK at ' + col() + ',' + row() + ' keys=' + keys()); shot('stuck'); return { ok: false, log, shots }; }
  }
  note(`ending: lvl=${p.level} battles=${battles}`); tick(3); shot('ending');
  return { ok: gp.state === S.ENDING, log, shots };
}
