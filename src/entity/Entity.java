package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.UtilityTool;

// Made the class abstract
public abstract class Entity {

    protected GamePanel gp; // Changed to protected
    protected int worldX, worldY; // Changed to protected
    protected int speed; // Changed to protected

    protected BufferedImage down1, down2, down3, down4, left1, left2, left3, left4, right1, right2, right3, right4, up1, up2, up3, up4, battleSprite;
    protected String direction = "down"; 
    
    protected int standCounter = 0;
    protected int spriteCounter = 0;
    protected int spriteNumber = 1; 
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); 
                                                             
    public int solidAreaDefaultX; // Keep public
    public int solidAreaDefaultY; // Keep public
    public boolean collisionOn = false; // Keep public, modified by CollisionChecker
    protected int actionLockCounter = 0; // Changed to protected
    protected String[] dialogues = new String[20]; // Changed to protected
    protected int dialogueIndex = 0; // Changed to protected
    // Public images used by UI, consider making private with getters if needed
    // elsewhere
    public BufferedImage image, image2, image3;
    protected String name; // Changed to protected
    public boolean collision = false; // Keep public, used by CollisionChecker

    // CHARACTER STATUS - Changed to protected
    protected int maxLife;
    protected int life;
    protected int maxMana;
    protected int mana;
    protected int manaRegen = 1;
    // sprite Image field removed as it wasn't used consistently, battleSprite used
    // instead
    protected int damage;
    protected int defense;

    // CHARACTER DROPS - Changed to protected
    protected int expDrop;
    protected int goldDrop;
    protected int scoreDrop;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    // Abstract method - subclasses MUST implement this
    public abstract void getImage();

    // Default implementation, can be overridden
    public void setAction() {
    }

    public void speak() {
        if (dialogues[dialogueIndex] == null) {
            dialogueIndex = 0; // Reset index if end is reached
        }
        gp.ui.currentDialogue = dialogues[dialogueIndex];
        dialogueIndex++;

        // Make NPC face the player
        switch (gp.player.getDirection()) { // Use getter for player direction
            case "up":
                direction = "down";
                break;
            case "down":
                direction = "up";
                break;
            case "left":
                direction = "right";
                break;
            case "right":
                direction = "left";
                break;
        }
    }

    public void update() {
        setAction();

        collisionOn = false;
        gp.cChecker.checkTile(this);
        gp.cChecker.checkObject(this, false);
        gp.cChecker.checkEntity(this, gp.npc);
        gp.cChecker.checkEntity(this, gp.monster);
        boolean playerCollision = gp.cChecker.checkPlayer(this); // Check returns boolean now

        if (!collisionOn && !playerCollision) { // Only move if no collision with tiles/objects/entities/player
            switch (direction) {
                case "up":
                    worldY -= speed;
                    break;
                case "down":
                    worldY += speed;
                    break;
                case "left":
                    worldX -= speed;
                    break;
                case "right":
                    worldX += speed;
                    break;
            }
        } else { // This block executes when NO movement keys are pressed
            standCounter++;
            if (standCounter >= 20) {
                // This line toggles the sprite between 1 and 2 for idle animation
                spriteNumber = (spriteNumber == 1) ? 2 : 1;
                standCounter = 0;
            }
        }

        // Sprite animation
        spriteCounter++;
        if (spriteCounter > 30) { // Adjusted for potentially smoother animation
            spriteNumber = (spriteNumber % 4) + 1; // Cycle through 1, 2, 3, 4
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage imageToDraw = null;
        // Use getters for player position
        int playerScreenX = gp.player.getScreenX();
        int playerScreenY = gp.player.getScreenY();
        int playerWorldX = gp.player.getWorldX();
        int playerWorldY = gp.player.getWorldY();

        // Calculate entity's screen position based on its world pos and player's
        // world/screen pos
        int screenX = getWorldX() - playerWorldX + playerScreenX; // Use getter for entity's worldX
        int screenY = getWorldY() - playerWorldY + playerScreenY; // Use getter for entity's worldY

        // --- Culling Check: Only draw if within screen bounds (Use getters) ---
        if (getWorldX() + gp.tileSize > playerWorldX - playerScreenX &&
                getWorldX() - gp.tileSize < playerWorldX + playerScreenX &&
                getWorldY() + gp.tileSize > playerWorldY - playerScreenY &&
                getWorldY() - gp.tileSize < playerWorldY + playerScreenY) {

            // --- Select the correct sprite based on direction and spriteNumber ---
            // This logic now handles entities with 2 or 4 frames per direction.
            switch (direction) {
                case "up":
                    if (spriteNumber == 1)
                        imageToDraw = up1;
                    if (spriteNumber == 2)
                        imageToDraw = up2;
                    // If spriteNumber is 3, use up3 if available, otherwise reuse up1
                    if (spriteNumber == 3)
                        imageToDraw = (up3 != null) ? up3 : up1;
                    // If spriteNumber is 4, use up4 if available, otherwise reuse up2
                    if (spriteNumber == 4)
                        imageToDraw = (up4 != null) ? up4 : up2;
                    break;
                case "down":
                    if (spriteNumber == 1)
                        imageToDraw = down1;
                    if (spriteNumber == 2)
                        imageToDraw = down2;
                    // If spriteNumber is 3, use down3 if available, otherwise reuse down1
                    if (spriteNumber == 3)
                        imageToDraw = (down3 != null) ? down3 : down1;
                    // If spriteNumber is 4, use down4 if available, otherwise reuse down2
                    if (spriteNumber == 4)
                        imageToDraw = (down4 != null) ? down4 : down2;
                    break;
                case "left":
                    if (spriteNumber == 1)
                        imageToDraw = left1;
                    if (spriteNumber == 2)
                        imageToDraw = left2;
                    // If spriteNumber is 3, use left3 if available, otherwise reuse left1
                    if (spriteNumber == 3)
                        imageToDraw = (left3 != null) ? left3 : left1;
                    // If spriteNumber is 4, use left4 if available, otherwise reuse left2
                    if (spriteNumber == 4)
                        imageToDraw = (left4 != null) ? left4 : left2;
                    break;
                case "right":
                    if (spriteNumber == 1)
                        imageToDraw = right1;
                    if (spriteNumber == 2)
                        imageToDraw = right2;
                    // If spriteNumber is 3, use right3 if available, otherwise reuse right1
                    if (spriteNumber == 3)
                        imageToDraw = (right3 != null) ? right3 : right1;
                    // If spriteNumber is 4, use right4 if available, otherwise reuse right2
                    if (spriteNumber == 4)
                        imageToDraw = (right4 != null) ? right4 : right2;
                    break;
            }

            if (imageToDraw == null) {
                if (this.image != null) {
                    imageToDraw = this.image;
                } else {
                    imageToDraw = down1; // Last resort
                }
            }

            // --- Draw the selected image ---
            if (imageToDraw != null) {
                g2.drawImage(imageToDraw, screenX, screenY, gp.tileSize, gp.tileSize, null);
            }
        }
    }

    // Renamed from setUp for clarity
    /** Loads an image and scales it (nearest-neighbour, so pixel art stays crisp). */
    protected BufferedImage setupImage(String imageName, int width, int height) {
        try (java.io.InputStream is = getClass().getResourceAsStream(imageName + ".png")) {
            if (is == null) {
                System.err.println("Missing image: " + imageName + ".png");
                return null;
            }
            return new UtilityTool().scaledImage(ImageIO.read(is), width, height);
        } catch (IOException e) {
            System.err.println("Could not read image: " + imageName);
            return null;
        }
    }

    protected BufferedImage setupImage(String imageName) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage scaledImage = null;
        try {
            BufferedImage original = ImageIO.read(getClass().getResourceAsStream(imageName + ".png"));
            if (original != null) {
                scaledImage = uTool.scaledImage(original, gp.tileSize, gp.tileSize);
            } else {
                System.err.println("Error: Could not load image " + imageName + ".png");
                // Return a placeholder or throw an exception if critical
            }
        } catch (IOException e) {
            System.err.println("IOException loading image: " + imageName);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException: Image resource not found: " + imageName + ".png");
            e.printStackTrace();
        }
        return scaledImage;
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public int getWorldX() {
        return worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public int getSpeed() {
        return speed;
    }

    public String getDirection() {
        return direction;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getLife() {
        return life;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getMana() {
        return mana;
    }

    public int getManaRegen() {
        return manaRegen;
    }

    public int getDamage() {
        return damage;
    }

    // getDefense() is potentially overridden by Player, use that one when needed.
    // This base getter returns the base defense value.
    public int getBaseDefense() {
        return defense;
    }

    public int getExpDrop() {
        return expDrop;
    }

    public int getGoldDrop() {
        return goldDrop;
    }

    public int getScoreDrop() {
        return scoreDrop;
    }

    public BufferedImage getBattleSprite() {
        return battleSprite;
    }

    // --- Setters ---
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, this.maxLife));
    } // Ensure life stays within 0 and maxLife

    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, this.maxMana));
    } // Ensure mana stays within 0 and maxMana
      // Setter for base defense

    public void setBaseDefense(int defense) {
        this.defense = Math.max(0, defense);
    }

    // Inner Skill class (remains largely the same)
    public static class Skill {
        protected String name; // protected
        protected int damage; // protected
        protected int manaCost;// protected

        public Skill(String name, int damage, int manaCost) {
            this.name = name;
            this.damage = damage;
            this.manaCost = manaCost;
        }

        public String getName() {
            return name;
        }

        public int getDamage() {
            return damage;
        }

        public int getManaCost() {
            return manaCost;
        }
    }
}