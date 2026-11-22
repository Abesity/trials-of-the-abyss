package harness;
import main.*;
import entity.Entity;
import java.awt.event.KeyEvent;
public class SaveShopTest {
    static GamePanel gp;
    static void press(int c){ gp.keyH.keyPressed(new KeyEvent(gp,KeyEvent.KEY_PRESSED,0,0,c,KeyEvent.CHAR_UNDEFINED)); gp.update(); gp.keyH.keyReleased(new KeyEvent(gp,KeyEvent.KEY_RELEASED,0,0,c,KeyEvent.CHAR_UNDEFINED)); gp.update(); }
    public static void main(String[] a) throws Exception {
        gp = new GamePanel(); gp.setUpGame(); gp.newGame();
        // go to world 2, open the first door, take some damage, then save
        gp.changeWorld(2, true);
        int doorsBefore = 0; for (Entity o : gp.obj) if (o instanceof object.OBJ_Door) doorsBefore++;
        gp.obj[0] = null; gp.player.setLife(55); gp.player.setGold(77); gp.player.addItem("Key", 2);
        gp.eHandler.powerUpTaken = true;
        int x = gp.player.getWorldX(), y = gp.player.getWorldY();
        gp.saveGame(3);
        gp.newGame();
        System.out.println("after new game: world=" + gp.currentWorld + " keys=" + gp.player.getInventory().get("Key"));
        gp.loadGame(3);
        int doorsAfter = 0; for (Entity o : gp.obj) if (o instanceof object.OBJ_Door) doorsAfter++;
        boolean ok = gp.currentWorld == 2 && gp.player.getWorldX() == x && gp.player.getWorldY() == y && gp.player.getLife() == 55
            && gp.player.getGold() == 77 && gp.player.getInventory().getOrDefault("Key",0) == 3 && doorsAfter == doorsBefore - 1 && gp.eHandler.powerUpTaken;
        System.out.println("save/load: world=" + gp.currentWorld + " pos ok=" + (gp.player.getWorldX()==x && gp.player.getWorldY()==y) + " life=" + gp.player.getLife() + " gold=" + gp.player.getGold() + " keys=" + gp.player.getInventory().get("Key") + " doors " + doorsBefore + "->" + doorsAfter + " state=" + gp.gameState + " => " + (ok ? "PASS" : "FAIL"));
        // shop: stand below the counter in world 1, walk into it holding ENTER
        gp.newGame(); gp.changeWorld(1, true);
        int[] shop = WorldData.get(1).shop[0];
        gp.player.setGold(50);
        gp.player.setWorldX(shop[0]*48 - 2); gp.player.setWorldY((shop[1]+1)*48 - 10);
        gp.keyH.upPressed = true; gp.keyH.enterPressed = true;
        for (int i=0;i<20 && gp.gameState==GamePanel.playState;i++) gp.update();
        gp.keyH.upPressed = false; gp.keyH.enterPressed = false;
        System.out.println("shop opened: " + (gp.gameState == GamePanel.shopState));
        int hp = gp.player.getInventory().getOrDefault("HP Potion",0);
        press(KeyEvent.VK_ENTER); // buy first item (HP Potion)
        System.out.println("bought: HP potions " + hp + " -> " + gp.player.getInventory().getOrDefault("HP Potion",0) + ", gold 50 -> " + gp.player.getGold() + " msg=" + gp.ui.message);
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(768,576,1); gp.paintComponent(img.createGraphics());
        javax.imageio.ImageIO.write(img,"png",new java.io.File(a[0]+"/shop.png"));
        press(KeyEvent.VK_ESCAPE);
        // fountain in world 3
        gp.changeWorld(3, true); int[] f = WorldData.get(3).fountainsRed[0];
        gp.player.setLife(10); gp.player.setWorldX(f[0]*48-2); gp.player.setWorldY((f[1]+1)*48-10); gp.player.setDirection("up");
        gp.keyH.enterPressed = true; gp.update(); gp.keyH.enterPressed = false;
        System.out.println("fountain: life " + gp.player.getLife() + "/" + gp.player.getMaxLife() + " state=" + gp.gameState);
        // boss gate: end teleporter before boss defeated
        int[] e = WorldData.get(3).end[0]; press(KeyEvent.VK_ENTER);
        gp.player.setWorldX(e[0]*48-2); gp.player.setWorldY(e[1]*48-10); gp.eHandler.loadWorld(3);
        gp.player.setWorldX(e[0]*48-2+200); gp.update(); gp.player.setWorldX(e[0]*48-2); gp.update();
        System.out.println("exit before boss: state=" + gp.gameState + " msg=" + gp.ui.message);
        System.exit(0);
    }
}
