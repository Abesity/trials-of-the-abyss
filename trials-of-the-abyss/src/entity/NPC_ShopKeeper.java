package entity;

import main.GamePanel;
import java.awt.Rectangle;
import java.util.ArrayList; // Import ArrayList

// Basic Shopkeeper NPC
public class NPC_ShopKeeper extends Entity {

    // NEW: List of item names the shopkeeper sells
    private ArrayList<String> itemsForSale;

    public NPC_ShopKeeper(GamePanel gp) {
        super(gp);

        direction = "down";
        speed = 0;
        name = "Shop";

        solidArea = new Rectangle(0, 0, 96, 48); // covers both potions on the counter
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
        setDialogue();
        setItemsForSale(); // NEW: Populate shop inventory
    }

    /**
     * The shop is the potion shelf on Vinci's tavern counters, so it has no
     * sprite of its own: the counter tiles already show the potions.
     */
    @Override
    public void getImage() {
    }

    @Override
    public void draw(java.awt.Graphics2D g2) {
    }

    public void setDialogue() {
        dialogues[0] = "Potions for sale. Press TAB to switch\nbetween buying and selling.";
    }

    // NEW: Define what the shop sells
    private void setItemsForSale() {
        itemsForSale = new ArrayList<>();
        itemsForSale.add("HP Potion");
        itemsForSale.add("MP Potion");
        // Add names of other items the shop should sell here later
        // itemsForSale.add("Strength Buff");
        // itemsForSale.add("Iron Shield");
    }

    // NEW: Getter for the shop's inventory
    public ArrayList<String> getItemsForSale() {
        return itemsForSale;
    }

    // Modified speak method from previous step (no changes needed here)
    @Override
    public void speak() {
         switch (gp.player.getDirection()) { 
            case "up":
                direction = "up";
                break;
            case "down":
                direction = "down";
                break;
            case "left":
                direction = "left";
                break;
            case "right":
                direction = "right";
                break;
          }

        gp.ui.currentDialogue = dialogues[0]; // Use first dialogue line
        gp.gameState = GamePanel.shopState; // Transition to shop

        // Reset shop UI state
        gp.ui.shopMode = 0; // 0 for Buy, 1 for Sell
        gp.ui.shopPlayerInvCol = 0;
        gp.ui.shopPlayerInvRow = 0;
        gp.ui.shopKeeperInvCol = 0;
        gp.ui.shopKeeperInvRow = 0;
        dialogueIndex = 0; // Reset for next interaction
    }

    @Override
    public void setAction() {  
        
    }
}