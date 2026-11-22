package monster;

import main.GamePanel;

/** The boss guarding the exit of the third world. */
public class MON_KingSlime extends Monster {
    public MON_KingSlime(GamePanel gp) {
        super(gp);
        name = "King Slime";
        speed = 0; // holds its ground
        boss = true;
        maxLife = 130; life = maxLife;
        maxMana = 30; mana = maxMana; manaRegen = 1;
        damage = 18; defense = 6;
        expDrop = 300; goldDrop = 100; scoreDrop = 500;
        solidArea.x = -12; solidArea.y = -12; solidArea.width = 72; solidArea.height = 60;
        solidAreaDefaultX = solidArea.x; solidAreaDefaultY = solidArea.y;
        monsterSkills = new Skill[] { new Skill("Royal Crush", 26, 10), new Skill("Slime Rain", 20, 8) };
        getImage();
    }

    @Override
    public void getImage() {
        walkFrames = loadFrames("monster", "kingslime", 4, gp.tileSize * 2);
        battleFrames = loadFrames("battle", "kingslime", 8, gp.tileSize * 4);
    }
}
