package monster;

import main.GamePanel;

public class MON_Slime extends Monster {
    public MON_Slime(GamePanel gp) {
        super(gp);
        name = "Slime";
        speed = 1;
        maxLife = 30; life = maxLife;
        maxMana = 20; mana = maxMana; manaRegen = 1;
        damage = 5; defense = 0;
        expDrop = 30; goldDrop = 6; scoreDrop = 10;
        solidArea.x = 6; solidArea.y = 18; solidArea.width = 36; solidArea.height = 30;
        solidAreaDefaultX = solidArea.x; solidAreaDefaultY = solidArea.y;
        monsterSkills = new Skill[] { new Skill("Slime Tackle", 8, 5), new Skill("Toxic Ooze", 12, 10) };
        getImage();
    }

    @Override
    public void getImage() {
        walkFrames = loadFrames("monster", "slime", 4, gp.tileSize);
        battleFrames = loadFrames("battle", "slime", 8, gp.tileSize * 4);
    }
}
