package battle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List; 
import main.GamePanel;
import entity.Combatant;
import main.BattleHandler;

public class BattleScreenDrawer {

    private static final int MAX_LOG_ENTRIES = 3;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 210);
    private static final Color BORDER_COLOR = Color.WHITE;
    private static final Color PLAYER_TEXT_COLOR = Color.CYAN;
    private static final Color ENEMY_TEXT_COLOR = Color.ORANGE;

    private final GamePanel gp;
    private final Graphics2D g2;
    private final int commandNum;
    
    public BattleScreenDrawer(GamePanel gp, Graphics2D g2, int commandNum) {
        this.gp = gp;
        this.g2 = g2;
        this.commandNum = commandNum;
    }

    public void drawBattleScreen() {
        if (gp.gameState != GamePanel.battleState1 && gp.gameState != GamePanel.battleState2) {
            return;
        }

        Combatant currentEnemy = gp.battleHandler.getEnemy(); 

        drawMainBattleContainer();
        drawTurnCounter();

        if (currentEnemy != null) {
            drawEnemyInfo(currentEnemy);
        }
        drawPlayerInfo();
        drawActionLog();
        drawBattleMessage(currentEnemy);
    }


    private void drawMainBattleContainer() {
        int boxX = gp.tileSize / 2;
        int boxY = gp.tileSize / 4;
        int boxWidth = gp.screenWidth - gp.tileSize;
        int boxHeight = gp.screenHeight - gp.tileSize * 4 - gp.tileSize / 2;
        int arc = 35;
        // Background
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, arc, arc);
        // Border
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX + 3, boxY + 3, boxWidth - 6, boxHeight - 6, arc - 5, arc - 5);
    }

    private void drawTurnCounter() {
        int centerX = gp.screenWidth / 2;
        int turnY = gp.tileSize; 
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        String turnText = "Turn " + (gp.battleHandler.battleCounter + 1);

        int textWidth = g2.getFontMetrics().stringWidth(turnText);
        int textHeight = g2.getFontMetrics().getHeight(); // Use font metrics for height
        int bgWidth = textWidth + 20; // Padding
        int bgHeight = textHeight + 5; // Padding
        int bgX = centerX - bgWidth / 2;
        int bgY = turnY - textHeight + 5 ; 

        // Draw background box
        g2.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
        g2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 15, 15);

        // Draw turn text
        g2.setColor(Color.WHITE);
        g2.drawString(turnText, centerX - textWidth / 2, turnY);
    }

    private void drawEnemyInfo(Combatant enemy) {
        // enemy on the right, its status box top-left (kept clear of the 64x64 battle art)
        int enemySpriteX = gp.screenWidth - gp.tileSize * 5 - gp.tileSize / 2;
        int enemySpriteY = gp.tileSize / 2 + 8;
        int enemyBoxX = gp.tileSize + gp.tileSize / 2;
        int enemyBoxY = gp.tileSize + gp.tileSize / 2;
        // Draw sprite and status box
        java.awt.image.BufferedImage[] frames = enemy.getBattleFrames();
        java.awt.image.BufferedImage sprite = (frames != null && frames.length > 0)
                ? frames[(gp.frameCount / 8) % frames.length] : enemy.getBattleSprite();
        drawEntitySprite(sprite, enemySpriteX + shake(gp.battleHandler.enemyHitAt), enemySpriteY, Color.RED);
        drawStatusBox(enemyBoxX, enemyBoxY, enemy.getName(), enemy.getLife(), enemy.getMaxLife(), enemy.getMana(), enemy.getMaxMana());
    }

    private void drawPlayerInfo() {
        // knight bottom-left, its status box bottom-right
        int playerSpriteX = gp.tileSize;
        int playerSpriteY = gp.tileSize * 3 + 8;
        int playerBoxX = gp.screenWidth - gp.tileSize * 6 - gp.tileSize / 2;
        int playerBoxY = gp.tileSize * 5;
        // Draw sprite and status box
        drawEntitySprite(gp.player.currentBattleFrame(), playerSpriteX + shake(gp.battleHandler.playerHitAt), playerSpriteY, Color.BLUE);
        drawStatusBox(playerBoxX, playerBoxY, gp.player.getName(), gp.player.getLife(), gp.player.getMaxLife(), gp.player.getMana(), gp.player.getMaxMana());
    }

    /** Small horizontal shake for a few frames after being hit. */
    private int shake(int hitAt) {
        int t = gp.frameCount - hitAt;
        return (t >= 0 && t < 14) ? ((t / 2) % 2 == 0 ? 6 : -6) : 0;
    }

    private void drawEntitySprite(BufferedImage sprite, int x, int y, Color fallbackColor) {
        int spriteSize = gp.tileSize * 4; 
        if (sprite != null) {
            g2.drawImage(sprite, x, y, spriteSize, spriteSize, null);
        } else {
            g2.setColor(fallbackColor);
            g2.fillRect(x, y, spriteSize, spriteSize);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, spriteSize, spriteSize);
            drawCenteredText("?", x + spriteSize / 2, y + spriteSize / 2 + 15, 40, Color.WHITE); // Adjust Y for centering
        }
    }


    private void drawStatusBox(int x, int y, String name, int currentHP, int maxHP, int currentMP, int maxMP) {
        int width = gp.tileSize * 5;
        int height = gp.tileSize * 2 + 10;
        int arc = 15;

        // Box background and border
        g2.setColor(new Color(50, 50, 50, 200)); // Dark semi-transparent gray
        g2.fillRoundRect(x, y, width, height, arc, arc);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, height, arc, arc);

        // Name
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        g2.drawString(name, x + 10, y + 25); // Y position for name baseline

        // Health and Mana Bars
        int barX = x + 10;
        int hpBarY = y + 35; // Position below name
        drawHealthBar(barX, hpBarY, currentHP, maxHP);

        int mpBarY = hpBarY + 25; // Position below HP bar
        drawManaBar(barX, mpBarY, currentMP, maxMP);
    }

    public void drawHealthBar(int x, int y, int currentHP, int maxHP) {
        int barWidth = gp.tileSize * 4 + 20;
        int barHeight = 18;
        int hpWidth = (maxHP > 0) ? (int) (((double) currentHP / maxHP) * barWidth) : 0;
        hpWidth = Math.max(0, hpWidth); 
        
        // Draw background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(x, y, barWidth, barHeight, 5, 5);
        // Draw foreground (filled portion) with dynamic color
        g2.setColor(getHealthBarColor(currentHP, maxHP));
        g2.fillRoundRect(x, y, hpWidth, barHeight, 5, 5);
        // Draw border
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, barWidth, barHeight, 5, 5);

        // Draw HP text (e.g., "100 / 100")
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
        String hpText = currentHP + " / " + maxHP;
        int textWidth = g2.getFontMetrics().stringWidth(hpText);
        int textX = x + (barWidth - textWidth) / 2; // Center text
        int textY = y + barHeight - 4; // Position text baseline near bottom of bar
        // Shadow
        g2.setColor(Color.BLACK);
        g2.drawString(hpText, textX + 1, textY + 1);
        // Main text
        g2.setColor(Color.WHITE);
        g2.drawString(hpText, textX, textY);
    }

    private void drawManaBar(int x, int y, int currentMP, int maxMP) {
        int barWidth = gp.tileSize * 4 + 20;
        int barHeight = 18;
        // Calculate filled portion width
        int mpWidth = (maxMP > 0) ? (int) (((double) currentMP / maxMP) * barWidth) : 0;
        mpWidth = Math.max(0, mpWidth); // Ensure width is not negative

        // Draw background (dark blue)
        g2.setColor(new Color(0, 0, 70));
        g2.fillRoundRect(x, y, barWidth, barHeight, 5, 5);
        // Draw foreground (lighter blue)
        g2.setColor(new Color(50, 50, 220));
        g2.fillRoundRect(x, y, mpWidth, barHeight, 5, 5);
        // Draw border
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, barWidth, barHeight, 5, 5);

        // Draw MP text (e.g., "50 / 50")
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
        String mpText = currentMP + " / " + maxMP;
        int textWidth = g2.getFontMetrics().stringWidth(mpText);
        int textX = x + (barWidth - textWidth) / 2; // Center text
        int textY = y + barHeight - 4; // Position text baseline
        // Shadow
        g2.setColor(Color.BLACK);
        g2.drawString(mpText, textX + 1, textY + 1);
        // Main text
        g2.setColor(Color.WHITE);
        g2.drawString(mpText, textX, textY);
    }

    private Color getHealthBarColor(int currentHP, int maxHP) {
        if (maxHP <= 0) return Color.GRAY; // Handle division by zero or invalid max HP
        float percentage = (float) currentHP / maxHP;
        if (percentage > 0.6) return new Color(0, 200, 0); // Green for > 60%
        if (percentage > 0.3) return new Color(230, 200, 0); // Yellow for > 30%
        return new Color(200, 0, 0); // Red for <= 30%
    }

    // --- Private Helper Methods: Action Log & Messages ---

    private void drawActionLog() {
        int logX = gp.tileSize / 2;
        int logY = gp.screenHeight - gp.tileSize * 4 - gp.tileSize / 2 + 10; // Position above message box
        int logWidth = gp.screenWidth - gp.tileSize;
        int logHeight = gp.tileSize * 2 - 5; // Height for ~3 lines
        int arc = 20;

        // Draw background and border
        g2.setColor(new Color(0, 0, 0, 180)); // Dark semi-transparent
        g2.fillRoundRect(logX, logY, logWidth, logHeight, arc, arc);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(logX, logY, logWidth, logHeight, arc, arc);

        // Draw log entries
        int textX = logX + 15; // Indent text
        int textY = logY + 20; // Starting Y position
        int lineHeight = 18; // Spacing between lines

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));

        ArrayList<String> actions = gp.battleHandler.getActionLog();
        int count = Math.min(MAX_LOG_ENTRIES, actions.size()); // Show max 3 entries

        for (int i = 0; i < count; i++) {
            String action = actions.get(i); // Get recent actions first
            if (action != null) {
                // Color code based on who performed the action
                if (action.startsWith(gp.player.getName())) {
                    g2.setColor(PLAYER_TEXT_COLOR); // Cyan for player
                } else {
                    // Check if it starts with the current enemy's name
                     Combatant enemy = gp.battleHandler.getEnemy();
                     if (enemy != null && action.startsWith(enemy.getName())) {
                         g2.setColor(ENEMY_TEXT_COLOR); // Orange for enemy
                     } else {
                         g2.setColor(Color.LIGHT_GRAY); // Default color
                     }
                }
                // Draw the action text
                g2.drawString(action, textX, textY + i * lineHeight);
            }
        }
    }

    public void drawBattleMessage(Combatant enemy) { // Enemy passed for context if needed later
        int msgBoxX = gp.tileSize / 2;
        int msgBoxY = gp.screenHeight - gp.tileSize * 2 - 15; // Bottom of the screen
        int msgBoxWidth = gp.screenWidth - gp.tileSize;
        int msgBoxHeight = gp.tileSize * 2 + 5;
        int arc = 15;

        g2.setColor(new Color(240, 240, 240)); g2.fillRoundRect(msgBoxX, msgBoxY, msgBoxWidth, msgBoxHeight, arc, arc);
        g2.setColor(new Color(50, 50, 50)); g2.setStroke(new BasicStroke(3)); g2.drawRoundRect(msgBoxX, msgBoxY, msgBoxWidth, msgBoxHeight, arc, arc);

        // Set font for message text
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F));
        int textX = msgBoxX + 20;
        int textY = msgBoxY + 25;

        // Draw relevant message based on state
        BattleHandler.BattleState currentState = gp.battleHandler.getCurrentState();
        if (currentState == BattleHandler.BattleState.START || gp.battleHandler.battleCounter == 0) {
            drawMessage(gp.battleHandler.battleText, textX, textY);
        } else if (currentState == BattleHandler.BattleState.PLAYER_CHOICE ||
                   currentState == BattleHandler.BattleState.SKILL_SELECT ||
                   currentState == BattleHandler.BattleState.ITEM_SELECT) { // Added ITEM_SELECT
            drawMessage(gp.battleHandler.battleText, textX, textY); // "What will you do?" or "Select..."
        } else { // Display resolved actions
             boolean drawnPlayer = false;
             if (gp.battleHandler.playerActionText != null && !gp.battleHandler.playerActionText.isEmpty()) { drawMessage(gp.battleHandler.playerActionText, textX, textY); textY += 25; drawnPlayer = true; }
             if (gp.battleHandler.enemyActionText != null && !gp.battleHandler.enemyActionText.isEmpty()) { if (!drawnPlayer) { /* Draw at original textY */ } drawMessage(gp.battleHandler.enemyActionText, textX, textY); }
        }

        // Draw Options based on state
        if (currentState == BattleHandler.BattleState.PLAYER_CHOICE) {
            // Modified: Changed "Defend" to "Items"
            drawBattleOptions(msgBoxX, msgBoxY, new String[]{"Attack", "Items", "Skill", "Run"});
        } else if (currentState == BattleHandler.BattleState.SKILL_SELECT) {
            drawBattleOptions(msgBoxX, msgBoxY, gp.player.getSkillNames()); // Show skills
        } else if (currentState == BattleHandler.BattleState.ITEM_SELECT) { // NEW
            drawBattleItemOptions(msgBoxX, msgBoxY, gp.player.getUsableItemNames()); // Show usable items
        }
    }

    private void drawMessage(String text, int x, int y) {
        if (text == null) return;
        // The 2025 build left this method as an empty stub, so battle text never showed.
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        int maxWidth = gp.screenWidth / 3 - 30;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = line.length() == 0 ? word : line + " " + word;
            if (g2.getFontMetrics().stringWidth(next) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, y);
                y += 22;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(next);
            }
        }
        g2.drawString(line.toString(), x, y);
    }

    // Draw Main Battle Options (Attack, Items, Skill, Run)
    private void drawBattleOptions(int boxX, int boxY, String[] options) {
       g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
       int optionsX = boxX + gp.screenWidth / 3; // Position options right of message
       int optionsY = boxY + 25;
       int spacingX = gp.tileSize * 3 + 10;
       int spacingY = 25;

       for (int row = 0; row < 2; row++) {
           for (int col = 0; col < 2; col++) {
               int index = row * 2 + col;
               if (index < options.length && options[index] != null) {
                   int drawX = optionsX + col * spacingX;
                   int drawY = optionsY + row * spacingY;
                   if (index == commandNum) { // commandNum from constructor highlights selection
                       g2.setColor(Color.BLUE); g2.drawString("> " + options[index], drawX - 15, drawY);
                   } else {
                       g2.setColor(Color.DARK_GRAY); g2.drawString(options[index], drawX, drawY);
                   }
               }
           }
       }
       g2.setColor(Color.BLACK);
    }

    private void drawBattleItemOptions(int boxX, int boxY, List<String> itemNames) {
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        int optionsX = boxX + gp.screenWidth / 3;
        int optionsY = boxY + 25;
        int spacingX = gp.tileSize * 3 + 10; // Adjust spacing if needed
        int spacingY = 25;
        int cols = 2; // Items also in 2 columns for consistency

        for (int i = 0; i < itemNames.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            
            if (row >= 2) break; 

            String itemName = itemNames.get(i);
            String displayText = itemName; 

            int drawX = optionsX + col * spacingX;
            int drawY = optionsY + row * spacingY;

            if (i == commandNum) { 
                g2.setColor(Color.BLUE); g2.drawString("> " + displayText, drawX - 15, drawY);
            } else {
                g2.setColor(Color.DARK_GRAY); g2.drawString(displayText, drawX, drawY);
            }
        }
        g2.setColor(Color.BLACK);
    }

     

     // Draws text centered horizontally at a given point
     private void drawCenteredText(String text, int centerX, int y, int fontSize, Color color) {
         g2.setFont(g2.getFont().deriveFont(Font.BOLD, fontSize));
         g2.setColor(color);
         // Calculate width and adjust drawing X coordinate
         int textWidth = g2.getFontMetrics().stringWidth(text);
         g2.drawString(text, centerX - textWidth / 2, y);
     }
}