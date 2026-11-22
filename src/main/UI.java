package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import battle.BattleScreenDrawer;
import entity.Entity;
import entity.NPC_ShopKeeper;
import entity.Combatant;
import object.Item; // Import Item
import object.ItemManager;
import main.BattleHandler;

public class UI {

    // --- Fields ---

    GamePanel gp;
    Graphics2D g2;
    Font maruMonica;

    // Message & Dialogue
    public boolean messageOn = false;
    public String message = "";
    public int messageCounter = 0;
    public String currentDialogue = "";

    // Command & State Management
    public int commandNum = 0;
    public int titleScreenState = 0; // 0: Main Menu, 2: Load Screen
    public int pauseScreenState = 0; // 0: Main Pause, 1: Save Select
    public int saveFileSlotSelected = 0;
    public int loadFileSlotSelected = 0;
    // Inventory Cursor Position
    public int inventorySlotCol = 0;
    public int inventorySlotRow = 0;

    // Shop State
    public int shopMode = 0;
    public int shopCommandNum = 0; // 0: Buy, 1: Sell, 2: Exit (example)
    public int shopPlayerInvCol = 0;
    public int shopPlayerInvRow = 0;
    public int shopKeeperInvCol = 0;
    public int shopKeeperInvRow = 0;

    // Experience Bar Animation
    private int animatedExpWidth = 0;
    private int targetExpWidth = 0;

    // Save/Load File Info Cache
    private String[] loadFileNames = new String[3];
    private int[] loadFileLevels = new int[3];
    private int[] loadFileHP = new int[3];
    private int[] loadFileMaxHP = new int[3];
    private int[] loadFileMP = new int[3];
    private int[] loadFileMaxMP = new int[3];
    private double[] loadFilePlayTime = new double[3];

    // --- Constructor ---

    public UI(GamePanel gp) {
        this.gp = gp;
        // Load Font
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/x12y16pxMaruMonica.ttf");
            if (is != null) {
                maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                System.err.println("Error: Font file not found!");
                maruMonica = new Font("Arial", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            maruMonica = new Font("Arial", Font.PLAIN, 12);
        }

        // Load initial save file info
        loadSaveFileInformation();
    }

    // --- Core Public Methods ---

    // Displays a temporary message on the screen.
    public void showMessage(String text) {
        message = text;
        messageOn = true;
        messageCounter = 0; // Reset display timer
    }

    // Main drawing method, called by GamePanel. Routes drawing based on gameState.
    public void draw(Graphics2D g2) {
        this.g2 = g2;
        g2.setFont(maruMonica);
        g2.setColor(Color.white);

        // Route drawing based on Game State
        switch (gp.gameState) {
            case GamePanel.titleState:
                if (titleScreenState == 0)
                    drawMainMenuTitleScreen();
                else if (titleScreenState == 2)
                    drawLoadFileSelectionScreen();
                break;
            case GamePanel.playState:
                drawPlayerStats(); // Draw HUD
                drawPlayTime(); // Draw playtime
                break;
            case GamePanel.pauseState:
                drawPauseScreen(); // Draw pause overlay
                // HUD drawn underneath pause screen by GamePanel paintComponent ordering
                break;
            case GamePanel.dialogueState:
                drawDialogueScreen(); // Draw dialogue box
                // HUD drawn underneath dialogue by GamePanel paintComponent ordering
                break;
            case GamePanel.battleState1: // Battle main menu
            case GamePanel.battleState2: // Battle skill menu
                // Delegate battle drawing to BattleScreenDrawer
                BattleScreenDrawer battleUI = new BattleScreenDrawer(gp, g2, commandNum);
                battleUI.drawBattleScreen();
                break;
            case GamePanel.victoryState:
                drawVictoryScreen();
                break;
            case GamePanel.defeatState:
                drawDefeatScreen();
                break;
            case GamePanel.inventoryState: // NEW
                drawInventoryScreen();
                break;
            case GamePanel.shopState: // Corrected: Added case
                drawShopScreen();
                break;
            case GamePanel.endingState:
                drawEndingScreen();
                break;
        }

        // Draw temporary message overlay if active (drawn on top of everything else)
        if (messageOn) {
            drawMessageOverlay();
        }
    }

    // Updates the target width for the animated EXP bar.
    public void updateExpBar() {
        int requiredExp = gp.player.getExpToNextLevel();
        if (requiredExp > 0 && requiredExp != Integer.MAX_VALUE) {
            targetExpWidth = (int) (((double) gp.player.getExp() / requiredExp) * (gp.screenWidth - (gp.tileSize * 2)));
        } else {
            targetExpWidth = (gp.player.getExp() > 0) ? (gp.screenWidth - (gp.tileSize * 2)) : 0;
        }
        targetExpWidth = Math.max(0, Math.min(targetExpWidth, gp.screenWidth - (gp.tileSize * 2)));
        if (gp.player.getExp() == 0 && animatedExpWidth != 0) {
            animatedExpWidth = 0;
        }
    }

    // --- UI State Drawing Methods ---

    public void drawMainMenuTitleScreen() {
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Title Text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 70F));
        String text = "Trials of the Abyss";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;
        // Shadow
        g2.setColor(Color.gray);
        g2.drawString(text, x + 4, y + 4);
        // Main Text
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Player Image
        x = gp.screenWidth / 2 - gp.tileSize; // Center image
        y += gp.tileSize * 2;
        BufferedImage playerImage = gp.player.getTitleScreenImage();
        if (playerImage != null) {
            g2.drawImage(playerImage, x, y, gp.tileSize * 2, gp.tileSize * 2, null);
        } else { // Fallback if image is null
            g2.setColor(Color.BLUE);
            g2.fillRect(x, y, gp.tileSize * 2, gp.tileSize * 2);
        }

        // Menu Options
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));
        y += gp.tileSize * 4; // Position first option

        text = "NEW GAME";
        x = getXforCenteredText(text);
        if (commandNum == 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize, y);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, y);

        y += gp.tileSize; // Move down for next option
        text = "LOAD GAME";
        x = getXforCenteredText(text);
        if (commandNum == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize, y);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, y);

        y += gp.tileSize; // Move down for next option
        text = "QUIT";
        x = getXforCenteredText(text);
        if (commandNum == 2) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize, y);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, y);

        g2.setColor(Color.WHITE); // Reset color
    }

    public void drawLoadFileSelectionScreen() {
        drawSubWindow(gp.tileSize, gp.tileSize, gp.screenWidth - (gp.tileSize * 2),
                gp.screenHeight - (gp.tileSize * 2));
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 42F));
        String text = "Select Save File";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F));
        int startY = gp.tileSize * 3;
        int lineHeight = 22; // Adjusted line height slightly
        int slotBoxHeight = gp.tileSize + lineHeight * 2 + 10; // Adjusted height for horizontal layout

        for (int i = 0; i < 3; i++) {
            int currentY = startY + i * slotBoxHeight;
            int slotX = gp.tileSize * 2;

            // Highlight selected slot
            if (loadFileSlotSelected == i && gp.ui.commandNum != 2) {
                g2.setColor(Color.YELLOW);
                g2.drawString(">", slotX - gp.tileSize / 2, currentY + lineHeight);
            } else {
                g2.setColor(Color.WHITE);
            }
            // Draw Slot Name (Date/Empty/Corrupted)
            g2.drawString((i + 1) + ". " + loadFileNames[i], slotX, currentY + lineHeight);

            // Draw Details Horizontally if slot has data
            if (loadFileLevels[i] > 0) {
                int detailY = currentY + lineHeight * 2; // Y position for details line
                int detailX = slotX + 20; // Starting X for details

                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F)); // Smaller font for details
                g2.setColor(Color.LIGHT_GRAY);

                String levelText = "Lvl: " + loadFileLevels[i];
                g2.drawString(levelText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(levelText) + gp.tileSize; // Add space

                String hpText = "HP: " + loadFileHP[i] + "/" + loadFileMaxHP[i];
                g2.drawString(hpText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(hpText) + gp.tileSize; // Add space

                String mpText = "MP: " + loadFileMP[i] + "/" + loadFileMaxMP[i];
                g2.drawString(mpText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(mpText) + gp.tileSize; // Add space

                String timeText = "Time: " + formatTime(loadFilePlayTime[i]);
                g2.drawString(timeText, detailX, detailY);

                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F)); // Restore font size
            }
            g2.setColor(Color.WHITE); // Reset color
        }

        // Draw Load/Delete/Back options (positions might need minor adjustment)
        int optionsY = startY + 3 * slotBoxHeight; // Adjusted Y position
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
        text = "Load";
        x = gp.screenWidth / 2 - gp.tileSize * 4; // Adjusted X
        if (commandNum == 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize / 2, optionsY);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, optionsY);
        text = "Delete";
        x = gp.screenWidth / 2 + gp.tileSize * 0; // Adjusted X
        if (commandNum == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize / 2, optionsY);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, optionsY);
        text = "Back";
        x = gp.screenWidth / 2 + gp.tileSize * 4; // Adjusted X
        if (commandNum == 2) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize / 2, optionsY);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, optionsY);
        g2.setColor(Color.WHITE);
    }

    public void drawPauseScreen() {
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (pauseScreenState == 0) { // Main Pause Menu
            // "PAUSED" Title
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
            g2.setColor(Color.WHITE);
            String text = "PAUSED";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;
            g2.drawString(text, x, y);

            // Menu Options
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));
            y += gp.tileSize * 3; // Position first option

            text = "SAVE GAME";
            x = getXforCenteredText(text);
            if (commandNum == 0) {
                g2.setColor(Color.YELLOW);
                g2.drawString(">", x - gp.tileSize, y);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.drawString(text, x, y);

            y += gp.tileSize; // Move down for next option
            text = "SETTINGS";
            x = getXforCenteredText(text);
            if (commandNum == 1) {
                g2.setColor(Color.YELLOW);
                g2.drawString(">", x - gp.tileSize, y);
            } else {
                g2.setColor(Color.GRAY);
            } // Gray out if not implemented
            g2.drawString(text, x, y);

            y += gp.tileSize; // Move down for next option
            text = "QUIT TO TITLE";
            x = getXforCenteredText(text);
            if (commandNum == 2) {
                g2.setColor(Color.YELLOW);
                g2.drawString(">", x - gp.tileSize, y);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.drawString(text, x, y);

            g2.setColor(Color.WHITE); // Reset color

        } else if (pauseScreenState == 1) { // Save Slot Selection Screen
            // Delegate drawing to the specific method
            drawSaveSlotSelectionScreen();
        }
    }

    public void drawSaveSlotSelectionScreen() {
        drawSubWindow(gp.tileSize, gp.tileSize, gp.screenWidth - (gp.tileSize * 2),
                gp.screenHeight - (gp.tileSize * 2));
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 42F));
        String text = "Select Slot to Save";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F));
        int startY = gp.tileSize * 3;
        int lineHeight = 22; // Adjusted
        int slotBoxHeight = gp.tileSize + lineHeight * 2 + 10; // Adjusted

        for (int i = 0; i < 3; i++) {
            int currentY = startY + i * slotBoxHeight;
            int slotX = gp.tileSize * 2;
            // Highlight selected slot
            if (saveFileSlotSelected == i && commandNum != 1) { // commandNum 1 is 'Back'
                g2.setColor(Color.YELLOW);
                g2.drawString(">", slotX - gp.tileSize / 2, currentY + lineHeight);
            } else {
                g2.setColor(Color.WHITE);
            }
            // Draw Slot Name
            g2.drawString((i + 1) + ". " + loadFileNames[i], slotX, currentY + lineHeight);

            // Draw Details Horizontally
            if (loadFileLevels[i] > 0) {
                int detailY = currentY + lineHeight * 2;
                int detailX = slotX + 20;
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
                g2.setColor(Color.LIGHT_GRAY);
                String levelText = "Lvl: " + loadFileLevels[i];
                g2.drawString(levelText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(levelText) + gp.tileSize;
                String hpText = "HP: " + loadFileHP[i] + "/" + loadFileMaxHP[i];
                g2.drawString(hpText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(hpText) + gp.tileSize;
                String mpText = "MP: " + loadFileMP[i] + "/" + loadFileMaxMP[i];
                g2.drawString(mpText, detailX, detailY);
                detailX += g2.getFontMetrics().stringWidth(mpText) + gp.tileSize;
                String timeText = "Time: " + formatTime(loadFilePlayTime[i]);
                g2.drawString(timeText, detailX, detailY);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F)); // Restore font
            }
            g2.setColor(Color.WHITE); // Reset color
        }
        // Draw Instructions and Back option
        int optionsY = startY + 3 * slotBoxHeight; // Adjusted Y
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("Press ENTER on a slot to save.", getXforCenteredText("Press ENTER on a slot to save."),
                optionsY);
        text = "Back";
        x = getXforCenteredText(text);
        optionsY += gp.tileSize;
        if (commandNum == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x - gp.tileSize / 2, optionsY);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.drawString(text, x, optionsY);
        g2.setColor(Color.WHITE);

    }

    public void drawDialogueScreen() {
        // Window dimensions
        int x = gp.tileSize * 2;
        int y = gp.screenHeight - gp.tileSize * 4 - gp.tileSize / 2; // Position near bottom
        int width = gp.screenWidth - (gp.tileSize * 4);
        int height = gp.tileSize * 4;

        drawSubWindow(x, y, width, height); // Draw the background box [cite: 22]

        // Text settings
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F)); // [cite: 22]
        g2.setColor(Color.WHITE); // [cite: 22]
        x += gp.tileSize / 2; // Padding inside the box [cite: 22]
        y += gp.tileSize; // Starting Y position for text [cite: 22]

        // Draw dialogue, handling line breaks (\n)
        for (String line : currentDialogue.split("\n")) { // [cite: 22]
            g2.drawString(line, x, y); // [cite: 22]
            y += 40; // Move to the next line [cite: 22]
        }
    }

    public void drawShopScreen() {
        // --- Main Frame ---
        final int frameX = gp.tileSize * 1;
        final int frameY = gp.tileSize * 1;
        final int frameWidth = gp.screenWidth - (gp.tileSize * 2);
        final int frameHeight = gp.screenHeight - (gp.tileSize * 2);
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // --- Title & Mode ---
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        g2.setColor(Color.WHITE);
        String title = (shopMode == 0) ? "Shop - Buy" : "Shop - Sell";
        int titleX = frameX + gp.tileSize / 2;
        int titleY = frameY + gp.tileSize / 2 + 15;
        g2.drawString(title, titleX, titleY);

        // --- Player Gold Display ---
        int goldX = frameX + frameWidth - gp.tileSize * 4;
        int goldY = titleY + 5;
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        g2.drawString("Gold: " + gp.player.getGold(), goldX, goldY);

        // --- Column Titles ---
        final int playerInvX = frameX + gp.tileSize / 2;
        final int playerInvY = frameY + gp.tileSize * 2;
        final int shopInvX = frameX + frameWidth / 2 + gp.tileSize / 2;
        final int shopInvY = frameY + gp.tileSize * 2;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        g2.drawString("Your Items", playerInvX, playerInvY - 10);
        g2.drawString("Shop Items", shopInvX, shopInvY - 10);

        // --- Slot Drawing Setup ---
        final int slotStartXPlayer = playerInvX;
        final int slotStartYPlayer = playerInvY;
        final int slotStartXKeeper = shopInvX;
        final int slotStartYKeeper = shopInvY;
        final int slotSizeWithPadding = gp.tileSize + 10;
        final int slotPadding = 5;
        final int itemIconSize = gp.tileSize;
        final int cols = 5;

        // --- Draw Player Inventory (Left Side) ---
        ArrayList<String> playerItemNames = new ArrayList<>(gp.player.getInventory().keySet());
        for (int i = 0; i < playerItemNames.size(); i++) {
            String itemName = playerItemNames.get(i);
            int quantity = gp.player.getInventory().get(itemName);
            Item item = gp.itemManager.getItem(itemName);

            int currentSlotCol = i % cols;
            int currentSlotRow = i / cols;
            int slotX = slotStartXPlayer + currentSlotCol * slotSizeWithPadding;
            int slotY = slotStartYPlayer + currentSlotRow * slotSizeWithPadding;

            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(slotX, slotY, itemIconSize + slotPadding, itemIconSize + slotPadding, 5, 5);

            if (item != null && item.image != null) {
                g2.drawImage(item.image, slotX + slotPadding, slotY + slotPadding, itemIconSize, itemIconSize, null);
            } else {
                /* Placeholder */ }

            drawItemQuantity(g2, quantity, slotX, slotY, slotSizeWithPadding, slotPadding);

            if (shopMode == 1 && item != null) { // Draw Sell Price
                String priceText = String.valueOf(item.price / 2);
                int priceX = slotX + slotPadding;
                int priceY = slotY + slotPadding + itemIconSize + 14;
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14F));
                g2.setColor(Color.YELLOW);
                g2.drawString(priceText + "g", priceX, priceY);
            }
        }

        // --- Draw Shopkeeper Inventory (Right Side) ---
        // Find the shopkeeper instance
        NPC_ShopKeeper shopKeeper = null;
        for (Entity npc : gp.npc) {
            if (npc instanceof NPC_ShopKeeper) {
                shopKeeper = (NPC_ShopKeeper) npc;
                break;
            }
        }

        // Declare shopItems here to be accessible later
        ArrayList<String> shopItems = new ArrayList<>();
        if (shopKeeper != null) {
            shopItems = shopKeeper.getItemsForSale(); // Assign if keeper exists
            for (int i = 0; i < shopItems.size(); i++) {
                String itemName = shopItems.get(i);
                Item item = gp.itemManager.getItem(itemName);
                if (item == null)
                    continue;

                int currentSlotCol = i % cols;
                int currentSlotRow = i / cols;
                int slotX = slotStartXKeeper + currentSlotCol * slotSizeWithPadding;
                int slotY = slotStartYKeeper + currentSlotRow * slotSizeWithPadding;

                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(slotX, slotY, itemIconSize + slotPadding, itemIconSize + slotPadding, 5, 5);

                if (item.image != null) {
                    g2.drawImage(item.image, slotX + slotPadding, slotY + slotPadding, itemIconSize, itemIconSize,
                            null);
                } else {
                    /* Placeholder */ }

                if (shopMode == 0) { // Draw Buy Price
                    String priceText = String.valueOf(item.price);
                    int priceX = slotX + slotPadding;
                    int priceY = slotY + slotPadding + itemIconSize + 14;
                    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14F));
                    g2.setColor(Color.YELLOW);
                    g2.drawString(priceText + "g", priceX, priceY);
                }
            }
        } // End drawing shop items

        // --- Draw Cursors ---
        Item selectedItem = null; // Initialize here for description panel access
        if (shopMode == 0) { // Buy Mode - Cursor on Shop side
            if (!shopItems.isEmpty()) { // Use the shopItems list declared above
                int cursorX = slotStartXKeeper + shopKeeperInvCol * slotSizeWithPadding;
                int cursorY = slotStartYKeeper + shopKeeperInvRow * slotSizeWithPadding;
                int cursorWidth = itemIconSize + slotPadding;
                int cursorHeight = itemIconSize + slotPadding;
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
                int selectedIndex = shopKeeperInvRow * cols + shopKeeperInvCol;
                if (selectedIndex < shopItems.size()) {
                    selectedItem = gp.itemManager.getItem(shopItems.get(selectedIndex));
                }
            }
        } else { // Sell Mode - Cursor on Player side
            if (!playerItemNames.isEmpty()) {
                int cursorX = slotStartXPlayer + shopPlayerInvCol * slotSizeWithPadding;
                int cursorY = slotStartYPlayer + shopPlayerInvRow * slotSizeWithPadding;
                int cursorWidth = itemIconSize + slotPadding;
                int cursorHeight = itemIconSize + slotPadding;
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
                int selectedIndex = shopPlayerInvRow * cols + shopPlayerInvCol;
                if (selectedIndex < playerItemNames.size()) {
                    selectedItem = gp.itemManager.getItem(playerItemNames.get(selectedIndex));
                }
            }
        }

        // --- Draw Description Panel ---
        int descFrameX = frameX + gp.tileSize / 2;
        int descFrameY = frameY + frameHeight - gp.tileSize * 3 + gp.tileSize / 2;
        int descFrameWidth = frameWidth - gp.tileSize;
        int descFrameHeight = gp.tileSize * 2;
        drawSubWindow(descFrameX, descFrameY, descFrameWidth, descFrameHeight);

        if (selectedItem != null) { // Draw description if an item is selected
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
            g2.setColor(Color.WHITE);
            int textX = descFrameX + gp.tileSize / 2;
            int textY = descFrameY + gp.tileSize / 2;
            for (String line : selectedItem.description.split("\n")) {
                g2.drawString(line, textX, textY);
                textY += 20;
            }
        }

        // --- Draw Controls Info ---
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        g2.setColor(Color.WHITE);
        int controlsY = frameY + frameHeight - 10; // Position controls slightly higher
        g2.drawString("[Enter] " + (shopMode == 0 ? "Buy" : "Sell"), frameX + gp.tileSize / 2, controlsY);
        g2.drawString("[TAB] Switch Mode", getXforCenteredText("[TAB] Switch Mode"), controlsY);
        g2.drawString("[ESC] Exit", frameX + frameWidth - gp.tileSize * 3, controlsY);
    }

    private void drawItemQuantity(Graphics2D g2d, int quantity, int slotX, int slotY, int slotSize, int padding) {
        if (quantity > 0) {
            String qtyText = String.valueOf(quantity);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 16F));
            int qtyTextWidth = g2d.getFontMetrics().stringWidth(qtyText);
            // Position bottom-right inside the visual slot area
            int qtyX = slotX + gp.tileSize + padding - qtyTextWidth - 2;
            int qtyY = slotY + gp.tileSize + padding - 2;
            // Shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString(qtyText, qtyX + 1, qtyY + 1);
            // Text
            g2d.setColor(Color.WHITE);
            g2d.drawString(qtyText, qtyX, qtyY);
        }
    }

    /** Shown after the King Slime is beaten and the player leaves world 3. */
    public void drawEndingScreen() {
        g2.setColor(new Color(10, 12, 30));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 56F));
        g2.setColor(new Color(120, 160, 255));
        String text = "You escaped the Abyss!";
        int y = gp.tileSize * 2;
        g2.drawString(text, getXforCenteredText(text), y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 26F));
        g2.setColor(Color.WHITE);
        int minutes = (int) (gp.playTime / 60), seconds = (int) (gp.playTime % 60);
        String[] stats = { "Level " + gp.player.getLevel() + "   Score " + gp.player.getScore(),
                String.format("Time %d:%02d", minutes, seconds) };
        y += gp.tileSize;
        for (String s : stats) { g2.drawString(s, getXforCenteredText(s), y); y += 32; }

        String[] credits = { "Art, worlds and level design: Vinci F. Villanueva",
                "Pixel-to-code integration: Miguel Matheo C. Rivero",
                "Mechanics and gameplay: Christian Kyle B. Tapales",
                "Programming: Marc Louis P. Ortizano, Matt Cedric N. Ybanez,",
                "Wyben C. Daal",
                "Font: x12y16pxMaruMonica by hicc" };
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 21F));
        g2.setColor(new Color(200, 210, 240));
        y += 16;
        for (String s : credits) { g2.drawString(s, getXforCenteredText(s), y); y += 28; }

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        g2.setColor(Color.YELLOW);
        text = "Press ENTER to return to the title screen";
        g2.drawString(text, getXforCenteredText(text), gp.screenHeight - gp.tileSize / 2);
    }

    public void drawVictoryScreen() {
        // Overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // "VICTORY!" Text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
        g2.setColor(Color.YELLOW);
        String text = "VICTORY!";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;
        g2.drawString(text, x, y);

        // Battle Result Message (e.g., "You defeated the Slime!")
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
        g2.setColor(Color.WHITE);
        y += gp.tileSize * 2;
        text = gp.battleHandler.battleText; // Get result message from BattleHandler
        x = getXforCenteredText(text);
        g2.drawString(text, x, y);

        // Player Stats after battle
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F));
        y += gp.tileSize;
        text = "HP: " + gp.player.getLife() + "/" + gp.player.getMaxLife();
        x = getXforCenteredText(text);
        g2.drawString(text, x, y);

        y += 30; // Spacing
        text = "MP: " + gp.player.getMana() + "/" + gp.player.getMaxMana();
        x = getXforCenteredText(text);
        g2.drawString(text, x, y);

        // Display Level Up message if applicable (comes from the general message
        // system)
        if (messageOn && message.contains("Level Up")) {
            g2.setColor(Color.CYAN); // Highlight level up message
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 26F));
            y += 40; // More spacing
            x = getXforCenteredText(message);
            g2.drawString(message, x, y);
            g2.setColor(Color.WHITE); // Reset color
        }

        // Continue prompt
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        text = "Press ENTER to continue";
        x = getXforCenteredText(text);
        y = gp.screenHeight - gp.tileSize; // Position near bottom
        g2.drawString(text, x, y);
    }

    public void drawDefeatScreen() {
        // Overlay
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // "DEFEAT" Text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
        g2.setColor(Color.RED);
        String text = "DEFEAT";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 4;
        g2.drawString(text, x, y);

        // Defeat Message (e.g., "You were defeated...")
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        g2.setColor(Color.WHITE);
        y += gp.tileSize * 2;
        text = gp.battleHandler.battleText; // Get message from BattleHandler
        x = getXforCenteredText(text);
        g2.drawString(text, x, y);

        // Continue prompt
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        text = "Press ENTER to return to Title Screen";
        x = getXforCenteredText(text);
        y = gp.screenHeight - gp.tileSize * 2; // Position near bottom
        g2.drawString(text, x, y);
    }

    // Draw Inventory Screen (Updated with Cursor and Description)
    public void drawInventoryScreen() {
        // Frame for the whole inventory screen
        final int frameX = gp.tileSize;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.screenWidth - (gp.tileSize * 2);
        final int frameHeight = gp.screenHeight - (gp.tileSize * 2);
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32F));
        g2.setColor(Color.WHITE);
        String title = "Inventory";
        int titleX = getXforCenteredText(title);
        int titleY = frameY + gp.tileSize / 2 + 15; // Adjust Y for title position
        g2.drawString(title, titleX, titleY);

        // --- Left Side: Player Stats ---
        final int statsX = frameX + gp.tileSize / 2;
        int statsY = frameY + gp.tileSize * 2; // Start Y position for stats
        final int lineHeight = 24; // Line height for stats
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));

        g2.drawString("Level:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getLevel()), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("HP:", statsX, statsY);
        g2.drawString(gp.player.getLife() + "/" + gp.player.getMaxLife(), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("MP:", statsX, statsY);
        g2.drawString(gp.player.getMana() + "/" + gp.player.getMaxMana(), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("Attack:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getDamage()), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("Defense:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getDefense()), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("Speed:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getSpeed()), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight * 2; // Add extra space before Gold/Score
        g2.drawString("Gold:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getGold()), statsX + gp.tileSize * 2, statsY);
        statsY += lineHeight;
        g2.drawString("Score:", statsX, statsY);
        g2.drawString(String.valueOf(gp.player.getScore()), statsX + gp.tileSize * 2, statsY);

        // --- Right Side: Inventory Slots ---
        final int slotStartX = frameX + frameWidth / 2; // Start slots in the middle
        final int slotStartY = frameY + gp.tileSize;
        final int slotSizeWithPadding = gp.tileSize + 10; // Slot box total size
        final int slotPadding = 5; // Padding inside the slot border
        final int cols = 5; // Number of inventory columns

        // Convert HashMap keys to a list for indexed access
        ArrayList<String> itemNames = new ArrayList<>(gp.player.getInventory().keySet());

        // Draw item slots and items
        for (int i = 0; i < gp.player.maxInventorySize; i++) { // Draw all slots up to max size
            // Calculate slot grid position
            int currentSlotCol = i % cols;
            int currentSlotRow = i / cols;
            int slotX = slotStartX + currentSlotCol * slotSizeWithPadding;
            int slotY = slotStartY + currentSlotRow * slotSizeWithPadding;

            // Draw slot background (always draw the slot)
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRoundRect(slotX, slotY, gp.tileSize + slotPadding, gp.tileSize + slotPadding, 5, 5);

            // If an item exists at this index in the list, draw it
            if (i < itemNames.size()) {
                String itemName = itemNames.get(i);
                int quantity = gp.player.getInventory().get(itemName);
                BufferedImage itemImage = gp.itemManager.getItemImage(itemName); // Get image from ItemManager

                // Draw Item Image
                if (itemImage != null) {
                    g2.drawImage(itemImage, slotX + slotPadding, slotY + slotPadding, gp.tileSize, gp.tileSize, null);
                } else {
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(slotX + slotPadding, slotY + slotPadding, gp.tileSize, gp.tileSize);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(slotX + slotPadding, slotY + slotPadding, gp.tileSize, gp.tileSize);
                }

                // Draw Quantity
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16F));
                String qtyText = String.valueOf(quantity);
                int qtyTextWidth = g2.getFontMetrics().stringWidth(qtyText);
                int qtyX = slotX + gp.tileSize + slotPadding - qtyTextWidth - 2; // Position bottom-right inside slot
                int qtyY = slotY + gp.tileSize + slotPadding - 2;
                // Shadow
                g2.setColor(Color.BLACK);
                g2.drawString(qtyText, qtyX + 1, qtyY + 1);
                // Text
                g2.setColor(Color.WHITE);
                g2.drawString(qtyText, qtyX, qtyY);
            }

        } // End loop through slots/items

        // --- Draw Inventory Cursor ---
        // Cursor position remains based on inventorySlotCol/Row
        int cursorX = slotStartX + inventorySlotCol * slotSizeWithPadding;
        int cursorY = slotStartY + inventorySlotRow * slotSizeWithPadding;
        int cursorWidth = gp.tileSize + slotPadding; // Match slot size
        int cursorHeight = gp.tileSize + slotPadding;

        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10); // Rounded rectangle cursor

        // --- Draw Item Description Panel ---
        int descFrameX = frameX + gp.tileSize / 2;
        int descFrameY = frameY + frameHeight - gp.tileSize * 3; // Position description box lower
        int descFrameWidth = frameWidth / 2 - gp.tileSize; // Half width approx
        int descFrameHeight = gp.tileSize * 2 + gp.tileSize / 2; // Height for a few lines
        drawSubWindow(descFrameX, descFrameY, descFrameWidth, descFrameHeight);

        // Get selected item details only if the cursor points to a valid item index
        int selectedIndex = inventorySlotRow * cols + inventorySlotCol;
        if (selectedIndex < itemNames.size()) { // Check if cursor index is valid
            String selectedItemName = itemNames.get(selectedIndex);
            Item selectedItem = gp.itemManager.getItem(selectedItemName);

            if (selectedItem != null) {
                // Draw description text
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
                g2.setColor(Color.WHITE);
                int textX = descFrameX + gp.tileSize / 2;
                int textY = descFrameY + gp.tileSize / 2 + 5; // Start text Y
                for (String line : selectedItem.description.split("\n")) {
                    g2.drawString(line, textX, textY);
                    textY += 20; // Line spacing for description
                }
            }
        } else {
            // Optionally draw "[Empty]" or nothing in description if cursor is on empty
            // slot
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
            g2.setColor(Color.GRAY);
            int textX = descFrameX + gp.tileSize / 2;
            int textY = descFrameY + gp.tileSize / 2 + 5;
            g2.drawString("[Empty Slot]", textX, textY);
        }
    }

    // --- HUD Drawing Methods ---

    public void drawPlayerStats() {
        int barX = gp.tileSize / 2;
        int barY = gp.tileSize / 2;
        int barWidth = gp.tileSize * 5;
        int barHeight = 20;

        // Draw HP Bar
        drawStatusBar(barX, barY, barWidth, barHeight, gp.player.getLife(), gp.player.getMaxLife(),
                new Color(180, 0, 30), new Color(255, 60, 90), "HP");
        barY += barHeight + 5; // Move down for MP bar

        // Draw MP Bar
        drawStatusBar(barX, barY, barWidth, barHeight, gp.player.getMana(), gp.player.getMaxMana(),
                new Color(0, 30, 180), new Color(60, 90, 255), "MP");

        // Draw Text Stats (Level, Gold, Score)
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        g2.setColor(Color.WHITE);
        int textX = gp.tileSize / 2;
        int textY = barY + barHeight + 25; // Position below MP bar

        String text = "Lv: " + gp.player.getLevel();
        g2.drawString(text, textX, textY);

        textY += 20; // Line spacing
        text = "Gold: " + gp.player.getGold();
        g2.drawString(text, textX, textY);

        textY += 20; // Line spacing
        text = "Score: " + gp.player.getScore();
        g2.drawString(text, textX, textY);

        // Draw EXP Bar at the bottom
        drawExpBar();
    }

    private void drawPlayTime() {
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24F));
        String timeText = formatTime(gp.playTime); // Format the playtime
        int textWidth = g2.getFontMetrics().stringWidth(timeText);
        int x = gp.screenWidth - textWidth - gp.tileSize / 2; // Position top-right
        int y = gp.tileSize; // Y position near the top

        // Draw shadow first
        g2.setColor(Color.black);
        g2.drawString(timeText, x + 1, y + 1);
        // Draw main text
        g2.setColor(Color.white);
        g2.drawString(timeText, x, y);
    }

    public void drawExpBar() {
        int barWidth = gp.screenWidth - (gp.tileSize * 2); // Bar spans most of the width
        int barHeight = 15;
        int x = gp.tileSize; // X position with padding
        int y = gp.screenHeight - gp.tileSize / 2 - barHeight; // Position near bottom

        // Calculate target fill width based on current/required EXP
        updateExpBar(); // Ensure targetExpWidth is up-to-date before drawing

        // Animate the bar fill
        if (animatedExpWidth < targetExpWidth) {
            animatedExpWidth += 4; // Speed of animation
            if (animatedExpWidth > targetExpWidth)
                animatedExpWidth = targetExpWidth; // Cap at target
        } else if (animatedExpWidth > targetExpWidth) {
            // If target decreases (e.g., level up resets EXP visually), snap or animate
            // down
            if (gp.player.getExp() == 0)
                animatedExpWidth = 0; // Snap to 0 if player EXP is 0
            else
                animatedExpWidth = targetExpWidth; // Otherwise snap to new target
        }

        // Draw Background
        g2.setColor(new Color(50, 50, 50));
        g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

        // Draw Foreground (Filled portion)
        g2.setColor(new Color(0, 255, 15)); // Green color for EXP
        g2.fillRoundRect(x, y, animatedExpWidth, barHeight, 8, 8);

        // Draw Border
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, barWidth, barHeight, 8, 8);

        // Draw Text (EXP: current / required)
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14F));
        int requiredExp = gp.player.getExpToNextLevel();
        String expText = "EXP: " + gp.player.getExp() + " / "
                + (requiredExp == Integer.MAX_VALUE ? "MAX" : requiredExp);
        int textX = x + (barWidth / 2) - (g2.getFontMetrics().stringWidth(expText) / 2); // Center text
        int textY = y + barHeight - 3; // Position text vertically within the bar

        // Shadow
        g2.setColor(Color.BLACK);
        g2.drawString(expText, textX + 1, textY + 1);
        // Main Text
        g2.setColor(Color.WHITE);
        g2.drawString(expText, textX, textY);
    }

    // --- Private Helper Methods ---

    // Draws the temporary message overlay near the top of the screen.
    private void drawMessageOverlay() {
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20F));
        int msgX = gp.tileSize / 2;
        int msgY = gp.tileSize * 4; // Position below HUD
        int msgWidth = g2.getFontMetrics().stringWidth(message) + 20; // Width based on text
        int msgHeight = 30;

        // Draw background box
        g2.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black
        g2.fillRoundRect(msgX - 5, msgY - msgHeight + 5, msgWidth, msgHeight, 10, 10);

        // Draw text
        g2.setColor(Color.white);
        g2.drawString(message, msgX, msgY);

        // Increment counter to time out the message
        messageCounter++;
        if (messageCounter > 180) { // Display for ~3 seconds (180 frames / 60 FPS)
            messageOn = false;
            messageCounter = 0;
        }
    }

    // Draws a standard status bar (HP, MP).
    private void drawStatusBar(int x, int y, int width, int height, int currentValue, int maxValue, Color bgColor,
            Color fgColor, String label) {
        // Draw Background
        g2.setColor(bgColor);
        g2.fillRoundRect(x, y, width, height, 5, 5);

        // Calculate Fill Width
        int fillWidth = 0;
        if (maxValue > 0) { // Avoid division by zero
            fillWidth = (int) (((double) currentValue / maxValue) * width);
        }
        // Ensure fillWidth is not negative
        fillWidth = Math.max(0, fillWidth);

        // Draw Foreground (Fill)
        g2.setColor(fgColor);
        g2.fillRoundRect(x, y, fillWidth, height, 5, 5);

        // Draw Border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, height, 5, 5);

        // Draw Text (Label and Value)
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16F));
        g2.setColor(Color.WHITE);
        String valueText = currentValue + "/" + maxValue;

        // Draw Label on the left
        int labelWidth = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, x + 5, y + height - 5);

        // Draw Value centered in the remaining space
        int valueWidth = g2.getFontMetrics().stringWidth(valueText);
        int textX = x + labelWidth + (width - labelWidth - valueWidth) / 2; // Center align
        // Shadow
        g2.setColor(Color.BLACK);
        g2.drawString(valueText, textX + 1, y + height - 4 + 1); // Adjust Y for font baseline
        // Main Text
        g2.setColor(Color.WHITE);
        g2.drawString(valueText, textX, y + height - 4); // Adjust Y for font baseline
    }

    // Draws a rounded rectangle sub-window.
    public void drawSubWindow(int x, int y, int width, int height) {
        // Background
        g2.setColor(new Color(0, 0, 0, 210)); // Dark semi-transparent
        g2.fillRoundRect(x, y, width, height, 35, 35);

        // Border
        g2.setColor(new Color(255, 255, 255)); // White border
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25); // Inset border
    }

    // Calculates the X coordinate needed to center text horizontally.
    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2;
    }

    // Formats total seconds into HH:MM:SS string.
    private String formatTime(double totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // --- Save/Load File Info Handling ---

    // Loads information from save files for preview display.
    public void loadSaveFileInformation() {
        // Use try-with-resources for robustness
        for (int i = 0; i < 3; i++) {
            String fileName = "savegame" + (i + 1) + ".txt";
            File file = new File(fileName);

            if (file.exists() && file.length() > 0) { // Check if file exists AND is not empty
                try (FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader)) {

                    // Get last modified time
                    BasicFileAttributes attributes = Files.readAttributes(Paths.get(fileName),
                            BasicFileAttributes.class);
                    Instant creationTimeInstant = attributes.lastModifiedTime().toInstant();
                    LocalDateTime time = LocalDateTime.ofInstant(creationTimeInstant, ZoneOffset.systemDefault());
                    String formattedTime = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    // Read preview data with null checks
                    String line;
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: Level");
                    loadFileLevels[i] = Integer.parseInt(line);
                    for (int j = 0; j < 3; j++) {
                        line = bufferedReader.readLine();
                        if (line == null)
                            throw new IOException("Incomplete save: Stats");
                    } // Skip EXP, Gold, Score
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: HP");
                    loadFileHP[i] = Integer.parseInt(line);
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: MaxHP");
                    loadFileMaxHP[i] = Integer.parseInt(line);
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: MP");
                    loadFileMP[i] = Integer.parseInt(line);
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: MaxMP");
                    loadFileMaxMP[i] = Integer.parseInt(line);
                    line = bufferedReader.readLine();
                    if (line == null)
                        throw new IOException("Incomplete save: Playtime");
                    loadFilePlayTime[i] = Double.parseDouble(line);

                    // If all reads successful:
                    loadFileNames[i] = "Slot " + (i + 1) + " - " + formattedTime;

                } catch (IOException | NumberFormatException | NullPointerException e) {
                    // Handle errors reading the file content
                    System.err.println("Error reading save file " + (i + 1) + " content: " + e.getMessage());
                    loadFileNames[i] = "Slot " + (i + 1) + " - CORRUPTED";
                    resetSlotInfo(i);
                } catch (Exception e) {
                    // Catch any other unexpected errors during file attribute reading etc.
                    System.err.println("Unexpected error processing save file " + (i + 1) + ": " + e.getMessage());
                    loadFileNames[i] = "Slot " + (i + 1) + " - ERROR";
                    resetSlotInfo(i);
                }
            } else {
                // File doesn't exist or is empty
                loadFileNames[i] = "Slot " + (i + 1) + " - Empty";
                resetSlotInfo(i);
            }
        }
    }

    // Resets cached info for a specific save slot.
    private void resetSlotInfo(int i) {
        loadFileLevels[i] = 0;
        loadFileHP[i] = 0;
        loadFileMaxHP[i] = 0;
        loadFileMP[i] = 0;
        loadFileMaxMP[i] = 0;
        loadFilePlayTime[i] = 0.0;
        // loadFileNames[i] is set by the caller (e.g., to "Empty" or "Corrupted")
    }
} // End of UI class