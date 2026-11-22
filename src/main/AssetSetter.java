package main;

import entity.NPC_ShopKeeper;
import monster.MON_Bat;
import monster.MON_KingSlime;
import monster.MON_Slime;
import monster.Monster;
import object.OBJ_Chest;
import object.OBJ_Door;
import object.OBJ_Key;

/** Places the objects, shop and monsters for the current world (see WorldData). */
public class AssetSetter {
    GamePanel gp;
    public boolean monstersInitialized = false;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    private WorldData data() {
        return WorldData.get(gp.currentWorld);
    }

    public void setObject() {
        for (int i = 0; i < gp.obj.length; i++) gp.obj[i] = null;
        WorldData d = data();
        int i = 0;
        for (int[] p : d.doors) gp.obj[i++] = place(new OBJ_Door(gp, gp.currentWorld != 1), p);
        for (int[] p : d.keys) gp.obj[i++] = place(new OBJ_Key(gp), p);
        for (int[] p : d.chests) gp.obj[i++] = place(new OBJ_Chest(gp), p);
    }

    private entity.Entity place(entity.Entity e, int[] p) {
        e.setWorldX(p[0] * gp.tileSize);
        e.setWorldY(p[1] * gp.tileSize);
        return e;
    }

    public void setNPC() {
        for (int i = 0; i < gp.npc.length; i++) gp.npc[i] = null;
        int[][] shop = data().shop;
        if (shop.length > 0) gp.npc[0] = place(new NPC_ShopKeeper(gp), shop[0]);
    }

    public void setMonster() {
        for (int i = 0; i < gp.monster.length; i++) gp.monster[i] = null;
        int i = 0;
        for (int[] e : data().enemies) {
            Monster m;
            switch (e[0]) {
                case WorldData.BAT: m = new MON_Bat(gp); break;
                case WorldData.KING_SLIME: m = new MON_KingSlime(gp); break;
                default: m = new MON_Slime(gp);
            }
            m.placeAt(e[1], e[2]);
            m.setDropsKey(e[3] == 1);
            gp.monster[i++] = m;
        }
        monstersInitialized = true;
    }
}
