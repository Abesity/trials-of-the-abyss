package monster;

import main.GamePanel;

public class MON_Bat extends Monster {
    public MON_Bat(GamePanel gp) {
        super(gp);
        name = "Bat";
        speed = 2;
        maxLife = 40; life = maxLife;
        maxMana = 20; mana = maxMana; manaRegen = 1;
        damage = 14; defense = 3;
        expDrop = 70; goldDrop = 12; scoreDrop = 20;
        solidArea.x = 6; solidArea.y = 10; solidArea.width = 36; solidArea.height = 34;
        solidAreaDefaultX = solidArea.x; solidAreaDefaultY = solidArea.y;
        monsterSkills = new Skill[] { new Skill("Wing Slash", 14, 5), new Skill("Screech", 18, 10) };
        getImage();
    }

    @Override
    public void getImage() {
        walkFrames = loadFrames("monster", "bat", 5, gp.tileSize);
        battleFrames = loadFrames("battle", "bat", 8, gp.tileSize * 4);
    }
}
