package object;

import entity.Entity;
import main.GamePanel;

/** A chest that holds one key. Bump into it to open it. */
public class OBJ_Chest extends Entity {
    public boolean opened = false;

    public OBJ_Chest(GamePanel gp) {
        super(gp);
        name = "Chest";
        collision = true;
        getImage();
    }

    @Override
    public void getImage() {
        down1 = setupImage("/objects/chest");
        image = down1;
    }
}
