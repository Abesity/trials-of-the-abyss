package object;

import main.GamePanel;
import entity.Player;

public class PotionHP extends Item {

    GamePanel gp;

    public PotionHP(GamePanel gp) {
        this.gp = gp;
        name = "HP Potion";
        description = "[" + name + "]\nHeals minor wounds.";
        value = 25; // Heals 25 HP
        price = 10; // Example price
        image = setupImage("potion_red", gp);
    }

    @Override
    public boolean use(Player player) {
        // ... (use logic unchanged) ...
        gp.playSE(2);
        gp.ui.showMessage("Used " + name + "! Healed " + value + " HP.");
        player.setLife(player.getLife() + value);
        return true;
    }
}