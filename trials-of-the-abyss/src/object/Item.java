package object;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GamePanel; // Assuming setupImage might need gp later, though not strictly required now
import main.UtilityTool;

// Base class for all inventory items
public class Item {

    // Common Item Properties
    public BufferedImage image;
    public String name;
    public String description = "";
    public int value; // Could be sell value, heal amount, etc. depending on type
    public int price;

    // Utility for loading images (similar to Entity)
    // Made non-static to potentially access gp if needed later
    protected BufferedImage setupImage(String imagePath, GamePanel gp) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage scaledImage = null;
        try {
            // Use ClassLoader to get resource stream
            BufferedImage original = ImageIO.read(getClass().getResourceAsStream("/objects/" + imagePath + ".png"));
            if (original != null) {
                // Assuming items are drawn at standard tile size in inventory/world
                scaledImage = uTool.scaledImage(original, gp.tileSize, gp.tileSize);
            } else {
                System.err.println("Error: Could not load item image /objects/" + imagePath + ".png");
            }
        } catch (IOException e) {
            System.err.println("IOException loading item image: " + imagePath);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException: Image resource not found: /objects/" + imagePath + ".png");
            // e.printStackTrace(); // Can be noisy
        }
        return scaledImage;
    }

    // Placeholder use method - specific items will override this
    // Takes Player reference to apply effects
    public boolean use(entity.Player player) {
        System.out.println("Using item: " + name);
        // Default implementation does nothing useful, returns false (not consumed/no
        // effect)
        return false;
    }
}