package object;

import main.GamePanel;
import entity.Player;

public class PotionMP extends Item {

    GamePanel gp;

    public PotionMP(GamePanel gp) {
        this.gp = gp;
        name = "MP Potion";
        description = "[" + name + "]\nRestores some mana.";
        value = 15; // Restores 15 MP
        price = 15; // Example price
        image = setupImage("potion_blue", gp);
    }

    @Override
    public boolean use(Player player) {
        // ... (use logic unchanged) ...
         gp.playSE(2);
         gp.ui.showMessage("Used " + name + "! Restored " + value + " MP.");
         player.setMana(player.getMana() + value);
         return true;
    }
}