package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List; 
import java.util.stream.Collectors; 
import main.GamePanel;
import main.KeyHandler;
import object.Item;

public class Player extends Entity implements Combatant {

    private KeyHandler keyH;
    public final int screenX;
    public final int screenY;
    private Skill[] playerSkills = new Skill[4];
    private int exp;
    private int level;
    private int gold;
    private int score;
    private int[] expToNextLevel = new int[100];

    private HashMap<String, Integer> inventory = new HashMap<>();
    public final int maxInventorySize = 20;

    // Battle animation (Vinci's 64x64 battle frames: idle, sword attack, shield block)
    private BufferedImage[] idleFrames = new BufferedImage[0];
    private BufferedImage[] attackFrames = new BufferedImage[0];
    private BufferedImage[] defendFrames = new BufferedImage[0];
    private String battleAnim = "idle";
    private int battleAnimStart = 0;
    public boolean guarding = false;      // set by Iron Guard: halves the next hit
    public int encounterCooldown = 0;     // frames after running away before a new battle can start
    public int hurtCooldown = 0;          // frames of spike immunity after being hurt

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.name = "Knight";
        this.keyH = keyH;
        screenX = gp.screenWidth / 2 - (gp.tileSize / 2);
        screenY = gp.screenHeight / 2 - (gp.tileSize / 2);
        solidArea = new Rectangle(10, 18, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        setDefaultValues();
        getImage();
        initializeSkills();
        initializeInventory(); 
    }
    
    public void setDefaultValues() {
        worldX = gp.tileSize * 23;
        worldY = gp.tileSize * 21;
        speed = 3; // Default speed
        direction = "down";
        maxLife = 100;
        life = maxLife;
        maxMana = 50;
        mana = maxMana;
        manaRegen = 2;
        defense = 10; // Base defense
        damage = 5;  // Base damage/attack
        exp = 0;
        level = 1;
        gold = 0;
        score = 0;
        for (int i = 0; i < expToNextLevel.length; i++) {
            expToNextLevel[i] = (i * i * 100) / 2 + 50;
        }
        guarding = false;
        encounterCooldown = 0;
        hurtCooldown = 0;
        initializeSkills(); // Ensure skills are set after stats
        inventory.clear(); // Clear inventory on reset
        initializeInventory(); // Add starting items again
    }

    
    private void initializeInventory() {
    	addItem("HP Potion", 3);
        addItem("MP Potion", 3);
    }
    
    public boolean useItem(String itemName) {
        Item item = gp.itemManager.getItem(itemName); // Get item details
        if (item != null) {
            boolean used = item.use(this); // Call the item's specific use method
            if (used) {
                removeItem(itemName, 1); // Remove one quantity if use was successful
                return true;
            } else {
                // Optional: Message if item couldn't be used (e.g., full health)
                // gp.ui.showMessage("Cannot use " + itemName + " right now.");
            }
        } else {
             gp.ui.showMessage("Unknown item: " + itemName); // Should not happen if inventory is correct
        }
        return false; // Item not found or use failed/not consumed
    }

    public void gainRewards(int exp, int gold, int score) {
        this.exp += exp;
        this.gold += gold;
        this.score += score;
        gp.ui.showMessage("Gained " + exp + " EXP, " + gold + " Gold!");
        gp.ui.updateExpBar();
        checkLevelUp();
    }

     private void checkLevelUp() {
        while (level < expToNextLevel.length -1 && exp >= expToNextLevel[level]) {
            level++;
            int lifeIncrease = 10;
            int manaIncrease = 5;
            int damageIncrease = 2; // Attack increase
            int defenseIncrease = 1;
            // int speedIncrease = 0; // Optionally increase speed

            maxLife += lifeIncrease;
            maxMana += manaIncrease;
            damage += damageIncrease; // Increase base damage
            defense += defenseIncrease; // Increase base defense
            // speed += speedIncrease; // Increase speed

            setLife(maxLife);
            setMana(maxMana);
            String levelUpMsg = String.format(
                "Level Up! Reached Level %d! HP+%d, MP+%d, ATK+%d, DEF+%d", // Add speed if included
                 level, lifeIncrease, manaIncrease, damageIncrease, defenseIncrease);
            gp.ui.showMessage(levelUpMsg);
            gp.ui.messageCounter = 0;
            gp.playSE(2);
            gp.ui.updateExpBar();
        }
    }

    @Override
    public int getDefense() {
        return super.defense;
    }

    // getImage()...
     @Override
    public void getImage() {
        // Vinci's overworld knight: frames 1-4 down, 5-8 left, 9-12 right, 13-16 up
        down1 = setupImage("/player/knight_01"); down2 = setupImage("/player/knight_02");
        down3 = setupImage("/player/knight_03"); down4 = setupImage("/player/knight_04");
        left1 = setupImage("/player/knight_05"); left2 = setupImage("/player/knight_06");
        left3 = setupImage("/player/knight_07"); left4 = setupImage("/player/knight_08");
        right1 = setupImage("/player/knight_09"); right2 = setupImage("/player/knight_10");
        right3 = setupImage("/player/knight_11"); right4 = setupImage("/player/knight_12");
        up1 = setupImage("/player/knight_13"); up2 = setupImage("/player/knight_14");
        up3 = setupImage("/player/knight_15"); up4 = setupImage("/player/knight_16");
        idleFrames = loadBattleFrames("knight_idle", 8);
        attackFrames = loadBattleFrames("knight_attack", 9);
        defendFrames = loadBattleFrames("knight_defend", 10);
        battleSprite = idleFrames.length > 0 ? idleFrames[0] : null;
    }

    private BufferedImage[] loadBattleFrames(String prefix, int count) {
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            frames[i] = setupImage(String.format("/battle/%s_%02d", prefix, i + 1), gp.tileSize * 4, gp.tileSize * 4);
        }
        return frames;
    }

    /** Starts the attack or defend animation; it falls back to idle when it finishes. */
    public void playBattleAnimation(String anim) {
        battleAnim = anim;
        battleAnimStart = gp.frameCount;
    }

    /** Frame to draw on the battle screen this tick. */
    public BufferedImage currentBattleFrame() {
        int t = gp.frameCount - battleAnimStart;
        BufferedImage[] frames = "attack".equals(battleAnim) ? attackFrames
                : "defend".equals(battleAnim) ? defendFrames : idleFrames;
        if (frames != idleFrames) {
            int f = t / 5;
            if (f < frames.length) return frames[f];
            battleAnim = "idle";
            battleAnimStart = gp.frameCount;
            t = 0;
        }
        return idleFrames.length == 0 ? null : idleFrames[(t / 8) % idleFrames.length];
    }

    @Override
    public BufferedImage[] getBattleFrames() {
        return idleFrames;
    }

    private void initializeSkills() {
        playerSkills = new Skill[4];
        playerSkills[0] = new Skill("Sword Strike", 15, 5);
        playerSkills[1] = new Skill("Iron Guard", 0, 10); // Could be replaced later
        playerSkills[2] = new Skill("Heavy Blow", 25, 15);
        playerSkills[3] = new Skill("Shield Bash", 18, 12);
    }
    
     @Override
    public void update() {
        if (encounterCooldown > 0) encounterCooldown--;
        if (hurtCooldown > 0) hurtCooldown--;
        if (keyH.enterPressed) {
            gp.eHandler.checkInteraction(); // fountains in front of the player
        }

        if (keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed) {
            if (keyH.upPressed) direction = "up";
            else if (keyH.downPressed) direction = "down";
            else if (keyH.leftPressed) direction = "left";
            else if (keyH.rightPressed) direction = "right";

            // Check collision BEFORE moving
            collisionOn = false;
            gp.cChecker.checkTile(this);
            int objIndex = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndex); // Handle object pickup
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interactNPC(npcIndex); // Handle NPC interaction
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            contactMonster(monsterIndex); // Handle monster contact
            gp.eHandler.checkEvent(); // Check for map events

            // Move if collision is off
            if (!collisionOn) {
                switch (direction) {
                    case "up": worldY -= speed; break;
                    case "down": worldY += speed; break;
                    case "left": worldX -= speed; break;
                    case "right": worldX += speed; break;
                }
            }

            spriteCounter++;
            if (spriteCounter > 12) {
                 if (spriteNumber == 1) spriteNumber = 2;
                 else if (spriteNumber == 2) spriteNumber = 3;
                 else if (spriteNumber == 3) spriteNumber = 4;
                 else if (spriteNumber == 4) spriteNumber = 1;
                 spriteCounter = 0;
            }
        }
        
        if(gp.keyH.enterPressed) {
        	
        }
    }

    public void resetPlayer() {
        setDefaultValues();
        gp.ui.updateExpBar();
    }

    public void pickUpObject(int i) {
        if (i == 999 || gp.obj[i] == null) return;
        Entity o = gp.obj[i];
        if (o instanceof object.OBJ_Key) {
            addItem("Key", 1);
            gp.playSE(main.GamePanel.SE_COIN);
            gp.ui.showMessage("You found a key!");
            gp.obj[i] = null;
        } else if (o instanceof object.OBJ_Chest) {
            object.OBJ_Chest chest = (object.OBJ_Chest) o;
            if (!chest.opened) {
                chest.opened = true;
                addItem("Key", 1);
                gp.playSE(main.GamePanel.SE_UNLOCK);
                gp.ui.showMessage("You opened the chest and found a key!");
            }
        } else if (o instanceof object.OBJ_Door) {
            if (removeItem("Key", 1)) {
                gp.obj[i] = null;
                gp.playSE(main.GamePanel.SE_UNLOCK);
                gp.ui.showMessage("You unlocked the door.");
            } else if (!gp.ui.messageOn) {
                gp.ui.showMessage("It's locked. You need a key.");
            }
        }
    }

    public void interactNPC(int i) {
        if (i != 999) { // If an NPC index is returned
            if (gp.keyH.enterPressed) {
                gp.keyH.enterPressed = false; // Consume enter press immediately

                // Check if the NPC is the ShopKeeper
                if (gp.npc[i] instanceof NPC_ShopKeeper) {
                	gp.gameState = GamePanel.dialogueState;
                	gp.npc[i].speak(); 
                } else {
                    gp.gameState = GamePanel.dialogueState;
                    gp.npc[i].speak();
                }
            }
        }
    }

    public void contactMonster(int i) {
        if (i != 999) {
             if (gp.monster[i] != null && encounterCooldown == 0) {
                gp.gameState = GamePanel.battleState1;
                gp.battleHandler.startBattle(this, gp.monster[i]);
                gp.battleHandler.advanceBattle(); 
             }
        }
    }

     public BufferedImage getTitleScreenImage() {
        if (down1 == null) {
             getImage();
        }
        return down1; 
    }


    @Override
    public Skill[] getSkills() {
        return playerSkills;
    }
    @Override
    public String[] getSkillNames() {
        String[] names = new String[playerSkills.length];
        for (int i = 0; i < playerSkills.length; i++) {
            names[i] = (playerSkills[i] != null) ? playerSkills[i].getName() : "---";
        }
        return names;
    }
    
    @Override
    public void setDefense(int defense) {
        super.setBaseDefense(defense);
    }

    public HashMap<String, Integer> getInventory() {
        return inventory;
    }

    public void addItem(String itemName, int quantity) {
        if (itemName == null || quantity <= 0) return;
        inventory.put(itemName, inventory.getOrDefault(itemName, 0) + quantity);
    }

    public boolean removeItem(String itemName, int quantity) {
        if (!inventory.containsKey(itemName) || quantity <= 0) {
            return false; 
        }

        int currentQuantity = inventory.get(itemName);
        if (currentQuantity < quantity) {
            return false; 
        }

        int newQuantity = currentQuantity - quantity;
        if (newQuantity <= 0) {
            inventory.remove(itemName); 
        } else {
            inventory.put(itemName, newQuantity); 
        }
        return true;
    }


    // --- Getters for private/protected fields ---
    public int getExp() { return exp; }
    public int getLevel() { return level; }
    public int getGold() { return gold; }
    public int getScore() { return score; }
    @Override public int getSpeed() { return super.speed; }
    @Override public int getDamage() { return super.damage; }

    public int getExpToNextLevel() {
        if (level >= 0 && level < expToNextLevel.length) {
            return expToNextLevel[level];
        }
        return Integer.MAX_VALUE;
    }
     public int getScreenX() { return screenX; }
     public int getScreenY() { return screenY; }

     public void setLevel(int level) { this.level = Math.max(1, level); }
     public void setExp(int exp) { this.exp = Math.max(0, exp); }
     public void setGold(int gold) { this.gold = Math.max(0, gold); }
     public void setScore(int score) { this.score = Math.max(0, score); }
     public void setMaxLife(int maxLife) { this.maxLife = Math.max(1, maxLife); }
     public void setMaxMana(int maxMana) { this.maxMana = Math.max(0, maxMana); }
     public void setBaseDamage(int damage) { super.damage = Math.max(1, damage); } 
     public void setSpeed(int speed) { super.speed = Math.max(1, speed); } 
     
     public List<String> getUsableItemNames() {
         List<String> usableItems = new ArrayList<>();
         for (String itemName : inventory.keySet()) {
             Item item = gp.itemManager.getItem(itemName);
             if (item instanceof object.PotionHP || item instanceof object.PotionMP) {
                 usableItems.add(itemName);
             }
         }
         return usableItems;
     }
}