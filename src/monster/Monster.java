package monster;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import entity.Combatant;
import entity.Entity;
import main.GamePanel;

/**
 * Shared behaviour for every enemy: wandering on the overworld (kept near its
 * spawn point), frame animation, and the Combatant stats used in battle.
 * Subclasses only set stats, skills and which sprite frames to load.
 */
public abstract class Monster extends Entity implements Combatant {

    protected Skill[] monsterSkills = new Skill[0];
    protected BufferedImage[] walkFrames = new BufferedImage[0];
    protected BufferedImage[] battleFrames = new BufferedImage[0];
    protected final Random random = new Random();
    protected int homeX, homeY;
    protected int leash = 3;            // tiles it may wander from its spawn
    protected boolean dropsKey = false; // Vinci's placement file marks key holders
    protected boolean boss = false;
    private int animCounter = 0;
    private int animFrame = 0;

    protected Monster(GamePanel gp) {
        super(gp);
    }

    /** Loads frames named /<folder>/<prefix>_01.png .. _NN.png at the given size. */
    protected BufferedImage[] loadFrames(String folder, String prefix, int count, int size) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = setupImage(String.format("/%s/%s_%02d", folder, prefix, i + 1), size, size);
        }
        return frames;
    }

    public void placeAt(int col, int row) {
        worldX = col * gp.tileSize;
        worldY = row * gp.tileSize;
        homeX = worldX;
        homeY = worldY;
    }

    public void setDropsKey(boolean dropsKey) { this.dropsKey = dropsKey; }
    public boolean dropsKey() { return dropsKey; }
    public boolean isBoss() { return boss; }

    @Override
    public void setAction() {
        if (speed == 0) return;
        actionLockCounter++;
        if (actionLockCounter >= 120) {
            int i = random.nextInt(100);
            int range = leash * gp.tileSize;
            if (worldX - homeX > range) direction = "left";
            else if (homeX - worldX > range) direction = "right";
            else if (worldY - homeY > range) direction = "up";
            else if (homeY - worldY > range) direction = "down";
            else if (i < 25) direction = "up";
            else if (i < 50) direction = "down";
            else if (i < 75) direction = "left";
            else direction = "right";
            actionLockCounter = 0;
        }
    }

    @Override
    public void update() {
        super.update();
        if (getMana() < getMaxMana()) setMana(getMana() + getManaRegen());
        if (++animCounter >= 10) {
            animCounter = 0;
            animFrame = (animFrame + 1) % Math.max(1, walkFrames.length);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (walkFrames.length == 0) { super.draw(g2); return; }
        int screenX = worldX - gp.player.getWorldX() + gp.player.getScreenX();
        int screenY = worldY - gp.player.getWorldY() + gp.player.getScreenY();
        if (screenX > -gp.tileSize * 2 && screenX < gp.screenWidth + gp.tileSize
                && screenY > -gp.tileSize * 2 && screenY < gp.screenHeight + gp.tileSize) {
            BufferedImage img = walkFrames[animFrame % walkFrames.length];
            int w = img.getWidth(), h = img.getHeight();
            // bigger sprites (the king slime) stay anchored to the bottom-centre of their tile
            g2.drawImage(img, screenX + (gp.tileSize - w) / 2, screenY + gp.tileSize - h, null);
        }
    }

    @Override public Skill[] getSkills() { return monsterSkills; }
    @Override public String[] getSkillNames() {
        String[] names = new String[monsterSkills.length];
        for (int i = 0; i < monsterSkills.length; i++) names[i] = monsterSkills[i].getName();
        return names;
    }
    @Override public int getDefense() { return defense; }
    @Override public void setDefense(int defense) { setBaseDefense(defense); }
    @Override public BufferedImage getBattleSprite() { return battleFrames.length > 0 ? battleFrames[0] : null; }
    @Override public BufferedImage[] getBattleFrames() { return battleFrames; }
}
