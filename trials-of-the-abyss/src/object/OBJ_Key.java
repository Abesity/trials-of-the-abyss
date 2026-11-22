package object;

import entity.Entity;
import main.GamePanel;

/** A key lying on the ground. Walking over it adds a Key to the inventory. */
public class OBJ_Key extends Entity {
    public OBJ_Key(GamePanel gp) {
        super(gp);
        name = "Key";
        getImage();
    }

    @Override
    public void getImage() {
        down1 = setupImage("/objects/key");
        image = down1;
    }
}
