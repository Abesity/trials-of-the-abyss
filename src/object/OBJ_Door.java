package object;

import entity.Entity;
import main.GamePanel;

/** A locked door. Bumping into it with a Key in the inventory opens it. */
public class OBJ_Door extends Entity {
    private final boolean gate;

    public OBJ_Door(GamePanel gp, boolean gate) {
        super(gp);
        this.gate = gate;
        name = "Door";
        collision = true;
        getImage();
    }

    @Override
    public void getImage() {
        down1 = setupImage(gate ? "/objects/door_gate" : "/objects/door_wood");
        image = down1;
    }
}
