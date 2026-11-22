package object;

import main.GamePanel;

/** Inventory entry for keys. Keys are used automatically on locked doors. */
public class ItemKey extends Item {
    public ItemKey(GamePanel gp) {
        name = "Key";
        description = "[Key]\nOpens one locked door.";
        price = 0;
        image = setupImage("key", gp);
    }
}
