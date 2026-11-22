package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List; 
import entity.Entity;
import entity.NPC_ShopKeeper;
import object.Item;

public class KeyHandler implements KeyListener {

    GamePanel gp;
    // Added 'i' for inventory key - choose another if 'i' is used
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, backSpacePressed, spacePressed, inventoryPressed;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Route key handling
        switch (gp.gameState) {
            case GamePanel.titleState:
                handleTitleStateKeys(code);
                break;
            case GamePanel.playState:
                handlePlayStateKeys(code);
                break;
            case GamePanel.pauseState:
                handlePauseStateKeys(code);
                break;
            case GamePanel.dialogueState:
                handleDialogueStateKeys(code);
                break;
            case GamePanel.battleState1:
            case GamePanel.battleState2: 
                handleBattleStateKeys(code);
                break;
            case GamePanel.victoryState:
            case GamePanel.defeatState:
                handleEndStateKeys(code);
                break;
            case GamePanel.inventoryState:
                handleInventoryStateKeys(code);
                break;
            case GamePanel.shopState:
                handleShopStateKeys(code);
                break;
            case GamePanel.endingState:
                if (code == KeyEvent.VK_ENTER) {
                    gp.gameState = GamePanel.titleState;
                    gp.ui.titleScreenState = 0;
                    gp.ui.commandNum = 0;
                }
                break;
        }
    }

    // --- Helper methods for key handling based on state ---

    private void handleTitleStateKeys(int code) {
        if (gp.ui.titleScreenState == 0) { // Main Menu
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum = (gp.ui.commandNum + 2) % 3; // Cycle up (0->2, 1->0, 2->1)
            } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum = (gp.ui.commandNum + 1) % 3; // Cycle down (0->1, 1->2, 2->0)
            } else if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
                switch (gp.ui.commandNum) {
                    case 0: // New Game
                        gp.ui.titleScreenState = 0;
                        gp.newGame();
                        break;
                    case 1: // Load Game
                        gp.ui.titleScreenState = 2;
                        gp.ui.loadSaveFileInformation();
                        gp.ui.commandNum = 0;
                        gp.ui.loadFileSlotSelected = 0;
                        break;
                    case 2: // Quit
                        System.exit(0);
                        break;
                }
            }
        } else if (gp.ui.titleScreenState == 2) { 
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (gp.ui.commandNum == 2) {
                    gp.ui.loadFileSlotSelected = 2;
                    gp.ui.commandNum = 1;
                } else {
                    gp.ui.loadFileSlotSelected = (gp.ui.loadFileSlotSelected + 2) % 3;
                }
            } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                if (gp.ui.loadFileSlotSelected == 2 && gp.ui.commandNum != 2) {
                    gp.ui.commandNum = 2;
                } else if (gp.ui.commandNum != 2) {
                    gp.ui.loadFileSlotSelected = (gp.ui.loadFileSlotSelected + 1) % 3;
                } else {
                    gp.ui.commandNum = 0;
                }
            } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_D
                    || code == KeyEvent.VK_RIGHT) {
                if (gp.ui.commandNum < 2) {
                    gp.ui.commandNum = 1 - gp.ui.commandNum;
                }
            } else if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
                int selectedSlotIndex = gp.ui.loadFileSlotSelected;
                int slotNumber = selectedSlotIndex + 1;

                if (gp.ui.commandNum == 0) { // Load
                    gp.loadGame(slotNumber);
                } else if (gp.ui.commandNum == 1) { // Delete
                    String fileNameToDelete = "savegame" + slotNumber + ".txt";
                    File fileToDelete = new File(fileNameToDelete);
                    if (fileToDelete.exists()) {
                        if (fileToDelete.delete()) {
                            gp.ui.showMessage("Save file " + slotNumber + " deleted!");
                            gp.ui.loadSaveFileInformation();
                        } else {
                            gp.ui.showMessage("Failed to delete save file " + slotNumber + ".");
                        }
                    } else {
                        gp.ui.showMessage("Slot " + slotNumber + " is already empty.");
                    }
                } else if (gp.ui.commandNum == 2) { // Back
                    gp.ui.titleScreenState = 0;
                    gp.ui.commandNum = 1;
                }
            } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE) {
                gp.ui.titleScreenState = 0;
                gp.ui.commandNum = 1;
            }
        }
    }

    private void handlePlayStateKeys(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            upPressed = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)
            downPressed = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)
            leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT)
            rightPressed = true;
        if (code == KeyEvent.VK_P) {
            gp.gameState = GamePanel.pauseState;
            gp.ui.commandNum = 0;
        }
        if (code == KeyEvent.VK_ENTER)
            enterPressed = true; // <<< ENTER Key Flag Set
        if (code == KeyEvent.VK_BACK_SPACE)
            backSpacePressed = true;
        if (code == KeyEvent.VK_SPACE)
            spacePressed = true;
        if (code == KeyEvent.VK_I) { // Open inventory key
            gp.gameState = GamePanel.inventoryState;
        }
    }

    private void handlePauseStateKeys(int code) {
        if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
            gp.gameState = GamePanel.playState;
            gp.ui.pauseScreenState = 0;
            gp.ui.commandNum = 0;
        }

        if (gp.ui.pauseScreenState == 0) { // Main Pause Menu
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum = (gp.ui.commandNum + 2) % 3;
            } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum = (gp.ui.commandNum + 1) % 3;
            } else if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
                switch (gp.ui.commandNum) {
                    case 0: // SAVE GAME
                        gp.ui.pauseScreenState = 1;
                        gp.ui.loadSaveFileInformation();
                        gp.ui.saveFileSlotSelected = 0;
                        gp.ui.commandNum = 0;
                        break;
                    case 1: // SETTINGS (Not implemented)
                        gp.ui.showMessage("Settings not implemented yet!");
                        break;
                    case 2: // QUIT TO TITLE
                        gp.ui.titleScreenState = 0;
                        gp.ui.commandNum = 0;
                        gp.gameState = GamePanel.titleState;
                        gp.ui.pauseScreenState = 0;
                        gp.stopMusic();
                        break;
                }
            }
        } else if (gp.ui.pauseScreenState == 1) { // Save Slot Selection Screen
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (gp.ui.commandNum == 1) {
                    gp.ui.saveFileSlotSelected = 2;
                    gp.ui.commandNum = 0;
                } else {
                    gp.ui.saveFileSlotSelected = (gp.ui.saveFileSlotSelected + 2) % 3;
                }
            } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                if (gp.ui.saveFileSlotSelected == 2 && gp.ui.commandNum == 0) {
                    gp.ui.commandNum = 1;
                } else if (gp.ui.commandNum == 0) {
                    gp.ui.saveFileSlotSelected = (gp.ui.saveFileSlotSelected + 1) % 3;
                } else {
                    gp.ui.commandNum = 0;
                    gp.ui.saveFileSlotSelected = 0;
                }
            } else if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
                if (gp.ui.commandNum == 1) { // 'Back' selected
                    gp.ui.pauseScreenState = 0;
                    gp.ui.commandNum = 0;
                } else { // A slot is selected
                    int slotToSave = gp.ui.saveFileSlotSelected + 1;
                    gp.saveGame(slotToSave);
                    // Option: Return to game or stay in pause menu? Let's return to game.
                    gp.ui.pauseScreenState = 0;
                    gp.gameState = GamePanel.playState; // Return to game after saving
                    gp.ui.commandNum = 0;
                }
            } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE) {
                gp.ui.pauseScreenState = 0;
                gp.ui.commandNum = 0;
            }
        }
    }

    private void handleDialogueStateKeys(int code) {
        if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
            // Simple exit dialogue, could be enhanced later for multi-page dialogues
            gp.gameState = GamePanel.playState;
        }
    }

    private void handleBattleStateKeys(int code) {
        // --- State 1: Main Action Choice (Attack, Items, Skill, Run) ---
        if (gp.gameState == GamePanel.battleState1) {
            if (gp.battleHandler.currentState == BattleHandler.BattleState.PLAYER_CHOICE) {
                // Navigation (2x2 grid)
                if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) { if (gp.ui.commandNum >= 2) gp.ui.commandNum -= 2; }
                else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) { if (gp.ui.commandNum < 2) gp.ui.commandNum += 2; }
                else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) { if (gp.ui.commandNum % 2 != 0) gp.ui.commandNum--; }
                else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) { if (gp.ui.commandNum % 2 == 0) gp.ui.commandNum++; }
                // Action Selection
                else if (code == KeyEvent.VK_ENTER) { gp.battleHandler.executePlayerChoice(gp.ui.commandNum); }
            }
            // Allow skipping non-input phases? (Optional)
            // else if (code == KeyEvent.VK_ENTER) { gp.battleHandler.advanceBattle(); }
        }
        // --- State 2: Sub-Menu Choice (Skills or Items) ---
        else if (gp.gameState == GamePanel.battleState2) {
            // --- Skill Selection ---
             if (gp.battleHandler.currentState == BattleHandler.BattleState.SKILL_SELECT) {
                 int numSkills = gp.player.getSkills().length; int cols = 2; int maxCommandNum = numSkills - 1;
                  if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) { if (gp.ui.commandNum >= cols) gp.ui.commandNum -= cols; }
                  else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) { if (gp.ui.commandNum + cols <= maxCommandNum) gp.ui.commandNum += cols; }
                  else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) { if (gp.ui.commandNum % cols != 0) gp.ui.commandNum--; }
                  else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) { if ((gp.ui.commandNum + 1) % cols != 0 && gp.ui.commandNum + 1 <= maxCommandNum) gp.ui.commandNum++; }
                  else if (code == KeyEvent.VK_ENTER) { gp.battleHandler.executePlayerSkill(gp.ui.commandNum); }
                  else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE) { // Go Back
                      gp.gameState = GamePanel.battleState1; gp.battleHandler.currentState = BattleHandler.BattleState.PLAYER_CHOICE;
                      gp.ui.commandNum = 2; // Highlight 'Skill' option
                      gp.battleHandler.battleText = "What will you do?"; // Reset prompt
                  }
             }
             // --- NEW: Item Selection ---
             else if (gp.battleHandler.currentState == BattleHandler.BattleState.ITEM_SELECT) {
                 List<String> usableItems = gp.player.getUsableItemNames();
                 int numItems = usableItems.size();
                 int cols = 2; // Assuming 2 columns for items too
                 int maxCommandNum = numItems - 1;

                 if (numItems > 0) { // Only allow navigation if there are items
                     if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                         if (gp.ui.commandNum >= cols) gp.ui.commandNum -= cols;
                         // Add wrapping or boundary checks if needed for multi-row items
                     } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                         if (gp.ui.commandNum + cols <= maxCommandNum) gp.ui.commandNum += cols;
                         // Add wrapping or boundary checks if needed
                     } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                         if (gp.ui.commandNum % cols != 0) gp.ui.commandNum--;
                     } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                          if ((gp.ui.commandNum + 1) % cols != 0 && gp.ui.commandNum + 1 <= maxCommandNum) gp.ui.commandNum++;
                     } else if (code == KeyEvent.VK_ENTER) {
                          gp.battleHandler.executePlayerItemUse(gp.ui.commandNum); // Execute item use
                     }
                 }
                 // Always allow going back, even if no items
                 if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_BACK_SPACE) { // Go Back
                     gp.gameState = GamePanel.battleState1; gp.battleHandler.currentState = BattleHandler.BattleState.PLAYER_CHOICE;
                     gp.ui.commandNum = 1; // Highlight 'Items' option
                     gp.battleHandler.battleText = "What will you do?"; // Reset prompt
                 }
             }
        }
    }

    private void handleEndStateKeys(int code) {
        if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
            boolean wasDefeated = (gp.gameState == GamePanel.defeatState);

            gp.battleHandler.exitBattle(); // Clean up battle info

            if (wasDefeated) {
                gp.ui.titleScreenState = 0;
                gp.ui.commandNum = 0;
                gp.gameState = GamePanel.titleState;
                gp.ui.pauseScreenState = 0;
                gp.stopMusic();
            } else {
                gp.gameState = GamePanel.playState;
            }
            gp.repaint();
        }
    }

    // Handle keys in Inventory state
    private void handleInventoryStateKeys(int code) {
        int maxCols = 5; // Must match UI drawing
        int inventorySize = gp.player.getInventory().size();
        // Prevent division by zero if inventory is empty
        int maxRows = (inventorySize == 0) ? 1 : (inventorySize + maxCols - 1) / maxCols;

        if (code == KeyEvent.VK_I || code == KeyEvent.VK_ESCAPE) {
            gp.gameState = GamePanel.playState; // Return to game
        }
        // --- Navigation (Ensure inventorySize > 0 for navigation) ---
        else if (inventorySize > 0) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.inventorySlotRow--;
                if (gp.ui.inventorySlotRow < 0) {
                    gp.ui.inventorySlotRow = maxRows - 1;
                }
                // Adjust column if the new row is shorter and cursor is out of bounds
                int index = gp.ui.inventorySlotRow * maxCols + gp.ui.inventorySlotCol;
                if (index >= inventorySize) {
                    gp.ui.inventorySlotCol = (inventorySize - 1) % maxCols;
                }
            } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                int oldRow = gp.ui.inventorySlotRow;
                gp.ui.inventorySlotRow++;
                if (gp.ui.inventorySlotRow >= maxRows) {
                    gp.ui.inventorySlotRow = 0;
                } // Wrap
                // Adjust column if new position is invalid
                int index = gp.ui.inventorySlotRow * maxCols + gp.ui.inventorySlotCol;
                if (index >= inventorySize) {
                    // If moving down past last row or into shorter row, snap to last item
                    gp.ui.inventorySlotRow = (inventorySize - 1) / maxCols;
                    gp.ui.inventorySlotCol = (inventorySize - 1) % maxCols;
                }
            } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                gp.ui.inventorySlotCol--;
                if (gp.ui.inventorySlotCol < 0) {
                    gp.ui.inventorySlotCol = maxCols - 1;
                }
                // Adjust if new position is beyond the last item in the row/inventory
                int index = gp.ui.inventorySlotRow * maxCols + gp.ui.inventorySlotCol;
                if (index >= inventorySize && gp.ui.inventorySlotRow == maxRows - 1) {
                    gp.ui.inventorySlotCol = (inventorySize - 1) % maxCols; // Snap to last item's col in last row
                } else if (index >= inventorySize) {
                    gp.ui.inventorySlotCol = maxCols - 1; // Snap to last col in current row if not last row
                }
            } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                gp.ui.inventorySlotCol++;
                // Adjust if new position is beyond the last item
                int index = gp.ui.inventorySlotRow * maxCols + gp.ui.inventorySlotCol;
                if (index >= inventorySize || gp.ui.inventorySlotCol >= maxCols) {
                    gp.ui.inventorySlotCol = 0; // Wrap
                }
            } else if (code == KeyEvent.VK_ENTER) { // <<< ENTER Key Logic
                // Get the selected item name
                ArrayList<String> itemNames = new ArrayList<>(gp.player.getInventory().keySet());
                int selectedIndex = gp.ui.inventorySlotRow * maxCols + gp.ui.inventorySlotCol;

                if (selectedIndex >= 0 && selectedIndex < itemNames.size()) { // Check bounds
                    String selectedItemName = itemNames.get(selectedIndex);
                    // Attempt to use the item
                    boolean itemWasUsed = gp.player.useItem(selectedItemName);
                    // Optional: Close inventory after using an item? Or keep it open? Currently
                    // stays open.
                    // if (itemWasUsed) { gp.gameState = GamePanel.playState; }
                }
            }
        } // End if inventorySize > 0
    }

    private void handleShopStateKeys(int code) {
        int playerInventorySize = gp.player.getInventory().size();
        int playerCols = 5; // Match UI drawing
        int playerMaxRows = (playerInventorySize == 0) ? 1 : (playerInventorySize + playerCols - 1) / playerCols;

        // Find shopkeeper and get their items
        NPC_ShopKeeper shopKeeper = null; // <<< Variable Declared Here
        for (Entity npc : gp.npc) {
            if (npc instanceof NPC_ShopKeeper) {
                shopKeeper = (NPC_ShopKeeper) npc;
                break; // Found it, stop looking
            }
        }
        ArrayList<String> shopItems = (shopKeeper != null) ? shopKeeper.getItemsForSale() : new ArrayList<>();
        int shopInventorySize = shopItems.size();
        int shopCols = 5; // Match UI drawing
        int shopMaxRows = (shopInventorySize == 0) ? 1 : (shopInventorySize + shopCols - 1) / shopCols;

        if (code == KeyEvent.VK_ESCAPE) {
            gp.gameState = GamePanel.playState; // Exit shop
            // Reset shop UI state if needed
            gp.ui.shopMode = 0;
            // Consider resetting cursors too if desired on exit
            // gp.ui.shopPlayerInvCol = 0; ... etc.
        } else if (code == KeyEvent.VK_TAB) {
            gp.ui.shopMode = 1 - gp.ui.shopMode; // Toggle 0 (Buy) and 1 (Sell)
            System.out.println("Shop Mode toggled to: " + gp.ui.shopMode);

        }
        // --- Navigation ---
        else if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            if (gp.ui.shopMode == 0) { // Buy Mode (Shop List)
                if (shopInventorySize > 0) {
                    gp.ui.shopKeeperInvRow--;
                    if (gp.ui.shopKeeperInvRow < 0) {
                        gp.ui.shopKeeperInvRow = shopMaxRows - 1;
                    } // Wrap
                    // Adjust column if wrapped to a potentially shorter last row
                    int index = gp.ui.shopKeeperInvRow * shopCols + gp.ui.shopKeeperInvCol;
                    if (index >= shopInventorySize) {
                        gp.ui.shopKeeperInvCol = (shopInventorySize - 1) % shopCols;
                    }
                }
            } else { // Sell Mode (Player List)
                if (playerInventorySize > 0) {
                    gp.ui.shopPlayerInvRow--;
                    if (gp.ui.shopPlayerInvRow < 0) {
                        gp.ui.shopPlayerInvRow = playerMaxRows - 1;
                    } // Wrap
                    // Adjust column
                    int index = gp.ui.shopPlayerInvRow * playerCols + gp.ui.shopPlayerInvCol;
                    if (index >= playerInventorySize) {
                        gp.ui.shopPlayerInvCol = (playerInventorySize - 1) % playerCols;
                    }
                }
            }
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            if (gp.ui.shopMode == 0) { // Buy Mode (Shop List)
                if (shopInventorySize > 0) {
                    gp.ui.shopKeeperInvRow++;
                    if (gp.ui.shopKeeperInvRow >= shopMaxRows) {
                        gp.ui.shopKeeperInvRow = 0;
                    } // Wrap
                    // Adjust column if new position is invalid
                    int index = gp.ui.shopKeeperInvRow * shopCols + gp.ui.shopKeeperInvCol;
                    if (index >= shopInventorySize) {
                        gp.ui.shopKeeperInvRow = (shopInventorySize - 1) / shopCols;
                        gp.ui.shopKeeperInvCol = (shopInventorySize - 1) % shopCols;
                    }
                }
            } else { // Sell Mode (Player List)
                if (playerInventorySize > 0) {
                    gp.ui.shopPlayerInvRow++;
                    if (gp.ui.shopPlayerInvRow >= playerMaxRows) {
                        gp.ui.shopPlayerInvRow = 0;
                    } // Wrap
                    // Adjust column
                    int index = gp.ui.shopPlayerInvRow * playerCols + gp.ui.shopPlayerInvCol;
                    if (index >= playerInventorySize) {
                        gp.ui.shopPlayerInvRow = (playerInventorySize - 1) / playerCols;
                        gp.ui.shopPlayerInvCol = (playerInventorySize - 1) % playerCols;
                    }
                }
            }
        } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            if (gp.ui.shopMode == 0) { // Buy Mode (Shop List)
                if (shopInventorySize > 0) {
                    gp.ui.shopKeeperInvCol--;
                    if (gp.ui.shopKeeperInvCol < 0) {
                        gp.ui.shopKeeperInvCol = shopCols - 1;
                    } // Wrap
                    // Adjust column if new position invalid
                    int index = gp.ui.shopKeeperInvRow * shopCols + gp.ui.shopKeeperInvCol;
                    if (index >= shopInventorySize) {
                        gp.ui.shopKeeperInvCol = (shopInventorySize - 1) % shopCols;
                    }
                }
            } else { // Sell Mode (Player List)
                if (playerInventorySize > 0) {
                    gp.ui.shopPlayerInvCol--;
                    if (gp.ui.shopPlayerInvCol < 0) {
                        gp.ui.shopPlayerInvCol = playerCols - 1;
                    } // Wrap
                    // Adjust column if new position invalid
                    int index = gp.ui.shopPlayerInvRow * playerCols + gp.ui.shopPlayerInvCol;
                    if (index >= playerInventorySize && gp.ui.shopPlayerInvRow == playerMaxRows - 1) {
                        gp.ui.shopPlayerInvCol = (playerInventorySize - 1) % playerCols;
                    } else if (index >= playerInventorySize) {
                        gp.ui.shopPlayerInvCol = playerCols - 1;
                    }
                }
            }
        } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            if (gp.ui.shopMode == 0) { // Buy Mode (Shop List)
                if (shopInventorySize > 0) {
                    gp.ui.shopKeeperInvCol++;
                    // Adjust column / Wrap
                    int index = gp.ui.shopKeeperInvRow * shopCols + gp.ui.shopKeeperInvCol;
                    if (index >= shopInventorySize || gp.ui.shopKeeperInvCol >= shopCols) {
                        gp.ui.shopKeeperInvCol = 0;
                    }
                }
            } else { // Sell Mode (Player List)
                if (playerInventorySize > 0) {
                    gp.ui.shopPlayerInvCol++;
                    // Adjust column / Wrap
                    int index = gp.ui.shopPlayerInvRow * playerCols + gp.ui.shopPlayerInvCol;
                    if (index >= playerInventorySize || gp.ui.shopPlayerInvCol >= playerCols) {
                        gp.ui.shopPlayerInvCol = 0;
                    }
                }
            }
        }
        // --- Actions ---
        else if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.shopMode == 0) { // Buy Mode
                int selectedIndex = gp.ui.shopKeeperInvRow * shopCols + gp.ui.shopKeeperInvCol;
                if (shopKeeper != null && selectedIndex >= 0 && selectedIndex < shopItems.size()) { // Check bounds
                    String itemName = shopItems.get(selectedIndex);
                    Item item = gp.itemManager.getItem(itemName);
                    if (item != null) {
                        if (gp.player.getGold() >= item.price) {
                            gp.player.setGold(gp.player.getGold() - item.price);
                            gp.player.addItem(itemName, 1);
                            gp.playSE(1); // Assuming SE 1 is coin sound
                            gp.ui.showMessage("Bought " + itemName + "!");
                        } else {
                            gp.ui.showMessage("Not enough gold!");
                        }
                    }
                }
            } else { // Sell Mode
                ArrayList<String> playerItemNames = new ArrayList<>(gp.player.getInventory().keySet());
                int selectedIndex = gp.ui.shopPlayerInvRow * playerCols + gp.ui.shopPlayerInvCol;
                if (selectedIndex >= 0 && selectedIndex < playerItemNames.size()) { // Check bounds
                    String itemName = playerItemNames.get(selectedIndex);
                    Item item = gp.itemManager.getItem(itemName);
                    if ("Key".equals(itemName)) {
                        gp.ui.showMessage("You'll need that key. It can't be sold.");
                    } else if (item != null) {
                        int sellValue = item.price / 2; // Sell for half price
                        sellValue = Math.max(1, sellValue); // Ensure sell value is at least 1

                        if (gp.player.removeItem(itemName, 1)) { // Use removeItem to handle quantity/removal
                            gp.player.setGold(gp.player.getGold() + sellValue);
                            gp.playSE(1); // Coin sound
                            gp.ui.showMessage("Sold " + itemName + " for " + sellValue + "g.");

                            // Recalculate size/rows AFTER selling
                            playerInventorySize = gp.player.getInventory().size();
                            playerMaxRows = (playerInventorySize == 0) ? 1
                                    : (playerInventorySize + playerCols - 1) / playerCols;
                            // Adjust cursor if needed
                            if (gp.ui.shopPlayerInvRow >= playerMaxRows || (gp.ui.shopPlayerInvRow * playerCols
                                    + gp.ui.shopPlayerInvCol) >= playerInventorySize) {
                                gp.ui.shopPlayerInvRow = Math.max(0, playerMaxRows - 1);
                                gp.ui.shopPlayerInvCol = (playerInventorySize > 0)
                                        ? (playerInventorySize - 1) % playerCols
                                        : 0;
                            }

                        } // else: removeItem already handled the case where item couldn't be removed
                    }
                }
            }
        } // End Enter Key
    } // End handleShopStateKeys

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)
            downPressed = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)
            leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT)
            rightPressed = false;
        if (code == KeyEvent.VK_ENTER)
            enterPressed = false; // <<< Reset ENTER Flag
        if (code == KeyEvent.VK_BACK_SPACE)
            backSpacePressed = false;
        if (code == KeyEvent.VK_SPACE)
            spacePressed = false;
        if (code == KeyEvent.VK_I)
            inventoryPressed = false;
    }
}