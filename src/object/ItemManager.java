package object;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import main.GamePanel;

// Manages item definitions and provides access to item data like images
public class ItemManager {

    GamePanel gp;
    // Map to store item definitions, keyed by item name/ID
    private HashMap<String, Item> itemRegistry;

    public ItemManager(GamePanel gp) {
        this.gp = gp;
        itemRegistry = new HashMap<>();
        loadItemDefinitions();
    }

    // Load all known item types here
    private void loadItemDefinitions() {
        // Instantiate items (this loads their images via setupImage)
        Item hpPotion = new PotionHP(gp);
        Item mpPotion = new PotionMP(gp);
        Item key = new ItemKey(gp);
        // Add more items here...
        // Item sword = new Sword(gp);

        // Register items using their unique names
        itemRegistry.put(hpPotion.name, hpPotion);
        itemRegistry.put(mpPotion.name, mpPotion);
        itemRegistry.put(key.name, key);
        // itemRegistry.put(sword.name, sword);
    }

    // Get the Item object (contains image, description, use method etc.)
    public Item getItem(String itemName) {
        return itemRegistry.get(itemName);
    }

    // Convenience method to directly get an item's image
    public BufferedImage getItemImage(String itemName) {
        Item item = itemRegistry.get(itemName);
        if (item != null) {
            return item.image;
        }
        // Optional: Return a default 'unknown' image if item not found
        // return getUnknownItemImage();
        return null; // Or return null if no default
    }

    // Placeholder for a default image if an item name isn't found
    // private BufferedImage getUnknownItemImage() {
    // // Load and return a 'question mark' or similar image
    // }
}