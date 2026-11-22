package harness;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import main.*;
import entity.Entity;
import monster.Monster;
import object.*;

/**
 * Automated playthrough test. Starts a new game headless, then walks all three worlds with the
 * real movement and collision code: collects keys, opens chests and doors, uses teleporters and
 * fountains, fights every battle it needs, beats the King Slime and checks that the ending screen
 * is reached. Saves a screenshot at each milestone.
 *
 *   ./tests/run_playthrough.sh      (prints RESULT: PASS or RESULT: FAIL)
 */
public class Bot {
    static GamePanel gp;
    static String outDir;
    static int shots = 0, battles = 0, potions = 0;
    static List<String> log = new ArrayList<>();

    public static void main(String[] a) throws Exception {
        outDir = a[0];
        gp = new GamePanel();
        gp.setUpGame();
        shot("title");
        gp.newGame();
        long start = System.currentTimeMillis();
        int lastWorld = 0;
        int guard = 0;
        while (gp.gameState != GamePanel.endingState && guard++ < 200000) {
            if (gp.currentWorld != lastWorld) {
                lastWorld = gp.currentWorld;
                note("entered world " + lastWorld + " keys=" + keys() + " lvl=" + gp.player.getLevel());
                tick(5); shot("world" + lastWorld + "_start");
            }
            if (inBattle()) { fight(); continue; }
            if (gp.gameState == GamePanel.victoryState || gp.gameState == GamePanel.defeatState) {
                if (gp.gameState == GamePanel.defeatState) { note("DEFEATED: " + gp.battleHandler.battleText); shot("defeat"); finish(false); return; }
                shot("victory_" + battles);
                press(KeyEvent.VK_ENTER); continue;
            }
            if (gp.gameState == GamePanel.dialogueState) { press(KeyEvent.VK_ENTER); continue; }
            if (gp.gameState != GamePanel.playState) { note("unexpected state " + gp.gameState); press(KeyEvent.VK_ESCAPE); continue; }
            if (!step()) { note("STUCK in world " + gp.currentWorld + " at " + col() + "," + row() + " keys=" + keys()); shot("stuck"); finish(false); return; }
        }
        note("ending reached: lvl=" + gp.player.getLevel() + " battles=" + battles + " potions used=" + potions + " ms=" + (System.currentTimeMillis() - start));
        tick(3); shot("ending");
        finish(gp.gameState == GamePanel.endingState);
    }

    static void finish(boolean ok) throws Exception {
        log.forEach(System.out::println);
        System.out.println(ok ? "RESULT: PASS" : "RESULT: FAIL");
        System.exit(0);
    }

    static void note(String s) { log.add("[w" + gp.currentWorld + " f" + gp.frameCount + "] " + s); }
    static int keys() { return gp.player.getInventory().getOrDefault("Key", 0); }
    static boolean inBattle() { return gp.gameState == GamePanel.battleState1 || gp.gameState == GamePanel.battleState2; }
    static int col() { return (gp.player.getWorldX() + 26) / gp.tileSize; }
    static int row() { return (gp.player.getWorldY() + 34) / gp.tileSize; }

    static void tick(int n) { for (int i = 0; i < n; i++) gp.update(); }

    static void press(int code) {
        KeyEvent e = new KeyEvent(gp, KeyEvent.KEY_PRESSED, 0, 0, code, KeyEvent.CHAR_UNDEFINED);
        gp.keyH.keyPressed(e);
        tick(2);
        gp.keyH.keyReleased(new KeyEvent(gp, KeyEvent.KEY_RELEASED, 0, 0, code, KeyEvent.CHAR_UNDEFINED));
        tick(2);
    }

    static void shot(String name) {
        try {
            BufferedImage img = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_RGB);
            gp.paintComponent(img.createGraphics());
            ImageIO.write(img, "png", new File(outDir, String.format("%02d_%s.png", shots++, name)));
        } catch (Exception ex) { note("shot failed " + ex); }
    }

    // ---- battle policy ----
    static void fight() {
        var bh = gp.battleHandler;
        if (bh.getEnemy() == null) { tick(1); return; }
        String enemy = bh.getEnemy().getName();
        if (bh.battleCounter == 0 && bh.currentState == BattleHandler.BattleState.PLAYER_CHOICE) {
            battles++;
            note("battle " + battles + " vs " + enemy + " hp=" + gp.player.getLife() + "/" + gp.player.getMaxLife());
            if (battles <= 3 || enemy.equals("King Slime") || enemy.equals("Bat") && battles < 9) { tick(20); shot("battle_" + enemy.replace(" ", "")); }
        }
        var p = gp.player;
        int turns = bh.battleCounter;
        if (p.getLife() < p.getMaxLife() * 0.4 && p.getInventory().getOrDefault("HP Potion", 0) > 0) {
            bh.executePlayerChoice(1);
            int idx = p.getUsableItemNames().indexOf("HP Potion");
            bh.executePlayerItemUse(idx); potions++;
        } else if (p.getMana() < 12 && p.getInventory().getOrDefault("MP Potion", 0) > 0 && enemy.equals("King Slime")) {
            bh.executePlayerChoice(1);
            bh.executePlayerItemUse(p.getUsableItemNames().indexOf("MP Potion")); potions++;
        } else if (enemy.equals("King Slime") && turns % 4 == 1 && p.getMana() >= 10) {
            bh.executePlayerChoice(2); bh.executePlayerSkill(1); // Iron Guard
        } else if (p.getMana() >= 15) {
            bh.executePlayerChoice(2); bh.executePlayerSkill(2); // Heavy Blow
        } else if (p.getMana() >= 5) {
            bh.executePlayerChoice(2); bh.executePlayerSkill(0); // Sword Strike
        } else {
            bh.executePlayerChoice(0);
        }
        tick(3);
        if (gp.gameState == GamePanel.battleState2) { press(KeyEvent.VK_ESCAPE); }
        if (bh.battleCounter > 60) { note("battle too long"); bh.exitBattle(); }
    }

    // ---- movement ----
    static WorldData data() { return WorldData.get(gp.currentWorld); }

    static boolean walkable(int c, int r) {
        if (c < 0 || r < 0 || c >= 100 || r >= 100) return false;
        return !gp.tileM.tile[gp.tileM.mapTileNum[c][r]].collision;
    }

    static Entity objAt(int c, int r) {
        for (Entity o : gp.obj) if (o != null && o.getWorldX() / 48 == c && o.getWorldY() / 48 == r) return o;
        return null;
    }

    /** Picks the next goal tile list; returns targets in priority order. */
    static List<int[]> goals() {
        List<int[]> g = new ArrayList<>();
        for (Entity o : gp.obj) {
            if (o instanceof OBJ_Key) g.add(new int[]{o.getWorldX() / 48, o.getWorldY() / 48, 0});
            if (o instanceof OBJ_Chest && !((OBJ_Chest) o).opened) g.add(new int[]{o.getWorldX() / 48, o.getWorldY() / 48, 1});
        }
        boolean needHeal = gp.currentWorld == 3 && (gp.player.getLife() < gp.player.getMaxLife() * 0.95 || gp.player.getMana() < gp.player.getMaxMana() * 0.95);
        for (Entity m : gp.monster) if (m instanceof Monster && (((Monster) m).dropsKey() || (((Monster) m).isBoss() && !needHeal) || gp.player.getLevel() < 4))
            g.add(new int[]{(m.getWorldX() + 24) / 48, (m.getWorldY() + 24) / 48, 2});
        if (keys() > 0) for (Entity o : gp.obj) if (o instanceof OBJ_Door) g.add(new int[]{o.getWorldX() / 48, o.getWorldY() / 48, 1});
        var pl = gp.player;
        boolean bossNext = gp.currentWorld == 3 && !gp.eHandler.bossDefeated;
        if (pl.getLife() < pl.getMaxLife() * (bossNext ? 0.95 : 0.5)) for (int[] f : data().fountainsRed) g.add(0, new int[]{f[0], f[1], 1});
        if (pl.getMana() < pl.getMaxMana() * (bossNext ? 0.95 : 0.4)) for (int[] f : data().fountainsBlue) g.add(0, new int[]{f[0], f[1], 1});
        for (int[] e : data().end) g.add(new int[]{e[0], e[1], 3});
        return g;
    }

    static int lastTile = -1, lastTileFrame = 0;
    static int stuckFrames = 0, lastPos = -1, noProgress = 0;

    /** One movement step toward the nearest reachable goal; false if nothing reachable. */
    static boolean step() {
        int sc = col(), sr = row();
        if (sc * 100 + sr != lastTile) { lastTile = sc * 100 + sr; lastTileFrame = gp.frameCount; }
        List<int[]> goals = goals();
        if (gp.frameCount - lastTileFrame > 3000) { note("same tile too long at " + sc + "," + sr + " goals near: " + nearGoals(goals, sc, sr)); return false; }
        // BFS
        int[][] prev = new int[100 * 100][];
        boolean[] seen = new boolean[100 * 100];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sc, sr}); seen[sr * 100 + sc] = true;
        int[] found = null; int foundType = -1;
        while (!q.isEmpty() && found == null) {
            int[] cur = q.poll();
            for (int[] gl : goals) {
                boolean adj = Math.abs(gl[0] - cur[0]) + Math.abs(gl[1] - cur[1]) == 1;
                boolean on = gl[0] == cur[0] && gl[1] == cur[1];
                if ((gl[2] == 1 && adj) || ((gl[2] == 0 || gl[2] == 3) && on) || (gl[2] == 2 && (on || adj))) {
                    if (gl[2] == 3 && gp.currentWorld == 3 && !gp.eHandler.bossDefeated) continue;
                    found = cur; foundType = gl[2]; break;
                }
            }
            if (found != null) break;
            List<int[]> nb = new ArrayList<>(List.of(new int[]{cur[0] + 1, cur[1]}, new int[]{cur[0] - 1, cur[1]}, new int[]{cur[0], cur[1] + 1}, new int[]{cur[0], cur[1] - 1}));
            for (int[] t : data().teleporters) {
                if (t[0] == cur[0] && t[1] == cur[1]) nb.add(new int[]{t[2], t[3], 1});
                if (t[2] == cur[0] && t[3] == cur[1]) nb.add(new int[]{t[0], t[1], 1});
            }
            for (int[] n : nb) {
                if (n[0] < 0 || n[1] < 0 || n[0] >= 100 || n[1] >= 100 || seen[n[1] * 100 + n[0]]) continue;
                if (!walkable(n[0], n[1])) continue;
                Entity o = objAt(n[0], n[1]);
                if (o != null && o.collision) continue;
                seen[n[1] * 100 + n[0]] = true; prev[n[1] * 100 + n[0]] = cur; q.add(new int[]{n[0], n[1]});
            }
        }
        if (found == null) return false;
        // reconstruct first step
        List<int[]> path = new ArrayList<>();
        int[] c = found;
        while (c != null && !(c[0] == sc && c[1] == sr)) { path.add(0, c); c = prev[c[1] * 100 + c[0]]; }
        int[] target;
        if (path.isEmpty()) {
            // at the goal's neighbour: bump toward the goal
            for (int[] gl : goals) if (Math.abs(gl[0] - sc) + Math.abs(gl[1] - sr) <= 1) { target = new int[]{gl[0], gl[1]}; push(target, true); return true; }
            return true;
        }
        target = path.get(0);
        if (Math.abs(target[0] - sc) + Math.abs(target[1] - sr) > 1) { // teleport edge: wait on the pad
            gp.keyH.upPressed = gp.keyH.downPressed = gp.keyH.leftPressed = gp.keyH.rightPressed = false;
            // step off and back on to re-arm the pad
            // walk two tiles away (re-arms the pad) and come back
            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) if (walkable(sc + d[0], sr + d[1]) && walkable(sc + 2 * d[0], sr + 2 * d[1])) {
                push(new int[]{sc + d[0], sr + d[1]}, false); push(new int[]{sc + 2 * d[0], sr + 2 * d[1]}, false);
                push(new int[]{sc + d[0], sr + d[1]}, false); push(new int[]{sc, sr}, false);
                break;
            }
            return true;
        }
        push(target, false);
        int pos = gp.player.getWorldX() * 10007 + gp.player.getWorldY();
        if (pos == lastPos && inPlay()) { if (++noProgress > 400) { note("no progress toward " + target[0] + "," + target[1]); return false; } } else noProgress = 0;
        lastPos = pos;
        return true;
    }

    static String nearGoals(List<int[]> goals, int sc, int sr) {
        StringBuilder b = new StringBuilder();
        for (int[] g : goals) if (Math.abs(g[0]-sc)+Math.abs(g[1]-sr) <= 3) b.append(java.util.Arrays.toString(g));
        for (Entity m : gp.monster) if (m != null) b.append(" mon@" + m.getWorldX() + "," + m.getWorldY());
        b.append(" player@" + gp.player.getWorldX() + "," + gp.player.getWorldY() + " cd=" + gp.player.encounterCooldown);
        return b.toString();
    }

    static boolean inPlay() { return gp.gameState == GamePanel.playState; }

    /** Hold keys until the player stands in the target tile (or bumps it when bump=true). */
    static void push(int[] t, boolean bump) {
        int wantX = t[0] * 48 - 2, wantY = t[1] * 48 - 10;
        int c0 = col(), r0 = row();
        for (int i = 0; i < 40 && inPlay(); i++) {
            if (Math.abs(col() - c0) + Math.abs(row() - r0) > 2) break; // teleported
            int dx = wantX - gp.player.getWorldX(), dy = wantY - gp.player.getWorldY();
            var k = gp.keyH;
            k.upPressed = k.downPressed = k.leftPressed = k.rightPressed = false;
            int sc = col(), sr = row();
            boolean horizontal = t[1] == sr && t[0] != sc;
            boolean vertical = t[0] == sc && t[1] != sr;
            if (bump || horizontal || vertical) {
                // first centre on the perpendicular axis, then move
                if (horizontal || (bump && t[1] == sr)) {
                    int ay = sr * 48 - 10 - gp.player.getWorldY();
                    if (Math.abs(ay) > gp.player.getSpeed()) { if (ay < 0) k.upPressed = true; else k.downPressed = true; }
                    else if (dx < 0) k.leftPressed = true; else k.rightPressed = true;
                } else {
                    int ax = sc * 48 - 2 - gp.player.getWorldX();
                    if (Math.abs(ax) > gp.player.getSpeed()) { if (ax < 0) k.leftPressed = true; else k.rightPressed = true; }
                    else if (dy < 0) k.upPressed = true; else k.downPressed = true;
                }
            } else if (Math.abs(dx) > gp.player.getSpeed()) { if (dx < 0) k.leftPressed = true; else k.rightPressed = true; }
            else if (Math.abs(dy) > gp.player.getSpeed()) { if (dy < 0) k.upPressed = true; else k.downPressed = true; }
            else break;
            if (bump) k.enterPressed = true;
            gp.update();
            if (!bump && col() == t[0] && row() == t[1]
                    && Math.abs(wantX - gp.player.getWorldX()) <= gp.player.getSpeed() + 4
                    && Math.abs(wantY - gp.player.getWorldY()) <= gp.player.getSpeed() + 4) break;
        }
        var k = gp.keyH;
        k.upPressed = k.downPressed = k.leftPressed = k.rightPressed = k.enterPressed = false;
    }
}
