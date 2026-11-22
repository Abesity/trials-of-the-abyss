package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap; // Import HashMap for inventory
import javax.swing.JPanel;
import entity.Entity;
import entity.Player;
import object.ItemManager;
import tile.TileManager;

public class GamePanel extends JPanel implements Runnable {

    // Screen Settings (final is good practice for constants)
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol; // 768
    public final int screenHeight = tileSize * maxScreenRow; // 576

    // World Settings
    public final int maxWorldCol = 100;
    public final int maxWorldRow = 100;

    // FPS
    final int FPS = 60;

    // Systems
    public TileManager tileM = new TileManager(this); // public ok for now
    public KeyHandler keyH = new KeyHandler(this); // public ok
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this); // public ok
    public AssetSetter aSetter = new AssetSetter(this); // public ok
    public UI ui = new UI(this); // public ok
    public EventHandler eHandler = new EventHandler(this); // public ok
    Thread gameThread;

    // Entity and Object Storage
    public Player player = new Player(this, keyH); // Player instance
    public BattleHandler battleHandler = new BattleHandler(this); // Battle system
    public Entity obj[] = new Entity[50]; // doors, keys and chests
    public Entity npc[] = new Entity[10]; // Array for NPCs
    public Entity monster[] = new Entity[20]; // Array for monsters
    public ItemManager itemManager = new ItemManager(this); // NEW: Instantiate ItemManager
    ArrayList<Entity> entityList = new ArrayList<>(); // For drawing order

    // Game State Management
    public int gameState;
    public static final int titleState = 0; // Use static final for constants
    public static final int playState = 1;
    public static final int pauseState = 2;
    public static final int dialogueState = 3;
    public static final int battleState1 = 4; // Battle main menu
    public static final int battleState2 = 5; // Battle skill menu
    public static final int victoryState = 6;
    public static final int defeatState = 7;
    public static final int inventoryState = 8; // NEW: Inventory screen state
    public static final int shopState = 9; // NEW: Shop screen state
    public static final int endingState = 10; // cleared the third world

    // Sound indices (see Sound.java)
    public static final int MUSIC_OVERWORLD = 0, SE_COIN = 1, SE_HEAL = 2, SE_UNLOCK = 3, SE_FANFARE = 4,
            SE_HIT = 5, SE_TELEPORT = 6, MUSIC_BATTLE = 7;

    public int currentWorld = 1;
    public int frameCount = 0; // drives animations (spikes, battle frames)

    // Game Time Tracking
    public double playTime = 0; // Track accumulated play time

    // Constructor
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true); // Improves rendering performance
        this.addKeyListener(keyH);
        this.setFocusable(true); // Allows panel to receive key input
    }

    // Initial game setup
    public void setUpGame() {
        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        // playMusic(0); // Start music later, maybe on entering playState from title
        gameState = titleState; // Start at the title screen
    }

    // Start the game loop thread
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Game loop logic (Delta time loop)
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS; // Time per frame in nanoseconds
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0; // For FPS counter
        int drawCount = 0; // For FPS counter

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            // If enough time has passed for a frame update
            if (delta >= 1) {
                update(); // Update game logic
                repaint(); // Redraw the screen (calls paintComponent)
                delta--;
                drawCount++;

                // Increment play time only during the play state
                if (gameState == playState) {
                    playTime += (1.0 / FPS);
                }
            }

            // Print FPS approximately every second
            if (timer >= 1_000_000_000) {
                // System.out.println("FPS: " + drawCount); // Optional FPS display
                drawCount = 0;
                timer = 0;
            }
        }
    }

    // Update game logic based on state
    public void update() {
        frameCount++;
        // Only update entities in playState
        if (gameState == playState) {
            player.update();
            eHandler.checkEvent();
            // Update NPCs
            for (Entity n : npc) {
                if (n != null)
                    n.update();
            }
            // Update Monsters
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    monster[i].update();
                    // Check if monster is now dead after update (e.g., poison)
                    // This check might be better placed elsewhere if monsters don't die outside
                    // battle
                }
            }
        }
        // No updates needed for pause, dialogue, inventory, title, or end states
        // (handled by KeyHandler/UI)
    }

    // Overrides JPanel's paintComponent method for custom drawing
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Calls the parent class's drawing method
        Graphics2D g2 = (Graphics2D) g; // Cast to Graphics2D for more features

        // Draw based on game state
        if (gameState == titleState) {
            ui.draw(g2); // UI handles title screen drawing
        }
        // For all other states that show the game world...
        else {
            // --- Draw Game World (Play, Pause, Dialogue, Inventory background etc.) ---

            // Draw Tiles (background)
            tileM.draw(g2);
            tileM.drawOverlays(g2);

            // --- Add Entities to Draw List ---
            entityList.clear(); // Clear list from previous frame

            entityList.add(player);

            for (Entity n : npc) {
                if (n != null)
                    entityList.add(n);
            }
            for (Entity o : obj) {
                if (o != null)
                    entityList.add(o);
            }
            for (Entity m : monster) {
                if (m != null)
                    entityList.add(m);
            }

            // --- Sort Entities by Y-coordinate for correct layering ---
            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    // Use getters for position
                    return Integer.compare(e1.getWorldY(), e2.getWorldY());
                }
            });

            // --- Draw Sorted Entities ---
            for (Entity entity : entityList) {
                entity.draw(g2);
            }
            // Entity list is cleared next frame, no need to clear here

            // --- Draw UI on top ---
            // UI draw method handles drawing specific screens based on gameState
            ui.draw(g2);
        }

        g2.dispose(); // Release graphics context resources
    }

    // --- Save/Load Game --- (Using Getters/Setters)
    /** Starts a fresh run in world 1. */
    public void newGame() {
        player.resetPlayer();
        playTime = 0;
        eHandler.powerUpTaken = false;
        eHandler.bossDefeated = false;
        changeWorld(1, true);
        gameState = playState;
        stopMusic();
        playMusic(MUSIC_OVERWORLD);
    }

    /**
     * Loads one of Vinci's three worlds. Keys carry over between worlds; if the
     * player arrives with fewer than the world needs, they are topped up so the
     * run can never get stuck behind a locked door.
     */
    public void changeWorld(int world, boolean moveToStart) {
        currentWorld = world;
        tileM.loadWorld(world);
        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        WorldData d = WorldData.get(world);
        if (moveToStart) {
            player.setWorldX(d.start[0] * tileSize);
            player.setWorldY(d.start[1] * tileSize);
            player.setDirection("down");
            int keySources = d.keys.length + d.chests.length;
            for (int[] e : d.enemies) if (e[3] == 1) keySources++;
            int needed = d.doors.length - keySources;
            int have = player.getInventory().getOrDefault("Key", 0);
            if (needed > have) player.addItem("Key", needed - have);
            ui.showMessage("World " + world);
        }
        eHandler.loadWorld(world);
    }

    /** Reached the exit of world 3 after beating the King Slime. */
    public void finishGame() {
        gameState = endingState;
        stopMusic();
        playSE(SE_FANFARE);
    }

    public void saveGame(int slot) {
        if (slot < 1 || slot > 3) {
            ui.showMessage("Invalid save slot: " + slot);
            return;
        }
        String fileName = "savegame" + slot + ".txt";
        try (FileWriter fw = new FileWriter(fileName);
                BufferedWriter bw = new BufferedWriter(fw)) {

            // Player Stats
            bw.write(String.valueOf(player.getLevel()));
            bw.newLine();
            bw.write(String.valueOf(player.getExp()));
            bw.newLine();
            bw.write(String.valueOf(player.getGold()));
            bw.newLine();
            bw.write(String.valueOf(player.getScore()));
            bw.newLine();
            bw.write(String.valueOf(player.getLife()));
            bw.newLine();
            bw.write(String.valueOf(player.getMaxLife()));
            bw.newLine();
            bw.write(String.valueOf(player.getMana()));
            bw.newLine();
            bw.write(String.valueOf(player.getMaxMana()));
            bw.newLine();
            bw.write(String.valueOf(playTime));
            bw.newLine(); // Save playtime
            // Player Position & Direction
            bw.write(String.valueOf(player.getWorldX()));
            bw.newLine();
            bw.write(String.valueOf(player.getWorldY()));
            bw.newLine();
            bw.write(player.getDirection());
            bw.newLine();
            // Player Base Stats (might change with level ups)
            bw.write(String.valueOf(player.getDamage()));
            bw.newLine(); // Save base damage
            bw.write(String.valueOf(player.getDefense()));
            bw.newLine(); // Save base defense
            // Save Speed
            bw.write(String.valueOf(player.getSpeed()));
            bw.newLine(); // NEW: Save Speed
            bw.write(currentWorld + "," + (eHandler.powerUpTaken ? 1 : 0) + "," + (eHandler.bossDefeated ? 1 : 0));
            bw.newLine();
            for (int i = 0; i < obj.length; i++) {
                int flag = obj[i] == null ? 0 : (obj[i] instanceof object.OBJ_Chest && ((object.OBJ_Chest) obj[i]).opened) ? 2 : 1;
                bw.write(String.valueOf(flag));
                bw.newLine();
            }

            // Save Monster State (0 = null/dead, 1 = alive)
            for (int i = 0; i < monster.length; i++) {
                bw.write(monster[i] == null ? "0" : "1");
                bw.newLine();
            }

            // --- Save Inventory --- // NEW
            // Format: itemName,quantity (one line per item)
            // End marker: "---END_INVENTORY---"
            for (HashMap.Entry<String, Integer> entry : player.getInventory().entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
            bw.write("---END_INVENTORY---");
            bw.newLine();

            ui.showMessage("Game saved to slot " + slot + "!");
            ui.loadSaveFileInformation(); // Refresh UI display

        } catch (IOException e) {
            e.printStackTrace();
            ui.showMessage("Save failed!");
        }
    }

    public void loadGame(int slot) {
        if (slot < 1 || slot > 3) {
            ui.showMessage("Invalid load slot: " + slot);
            ui.titleScreenState = 2; // Stay on load screen
            return;
        }
        String fileName = "savegame" + slot + ".txt";
        try (FileReader fr = new FileReader(fileName);
                BufferedReader br = new BufferedReader(fr)) {

            // --- Load Player Stats ---
            int loadedLevel = Integer.parseInt(br.readLine());
            int loadedExp = Integer.parseInt(br.readLine());
            int loadedGold = Integer.parseInt(br.readLine());
            int loadedScore = Integer.parseInt(br.readLine());
            int loadedLife = Integer.parseInt(br.readLine());
            int loadedMaxLife = Integer.parseInt(br.readLine());
            int loadedMana = Integer.parseInt(br.readLine());
            int loadedMaxMana = Integer.parseInt(br.readLine());
            double loadedPlayTime = Double.parseDouble(br.readLine());
            // --- Load Position & Direction ---
            int loadedWorldX = Integer.parseInt(br.readLine());
            int loadedWorldY = Integer.parseInt(br.readLine());
            String loadedDirection = br.readLine();
            // --- Load Base Stats ---
            int loadedDamage = Integer.parseInt(br.readLine());
            int loadedDefense = Integer.parseInt(br.readLine());
            int loadedSpeed = Integer.parseInt(br.readLine()); // NEW: Load Speed

            // --- Apply Loaded Data to Player ---
            player.setLevel(loadedLevel);
            player.setExp(loadedExp);
            player.setGold(loadedGold);
            player.setScore(loadedScore);
            player.setMaxLife(loadedMaxLife);
            player.setLife(loadedLife);
            player.setMaxMana(loadedMaxMana);
            player.setMana(loadedMana);
            player.setWorldX(loadedWorldX);
            player.setWorldY(loadedWorldY);
            player.setDirection(loadedDirection);
            player.setBaseDamage(loadedDamage);
            player.setBaseDefense(loadedDefense);
            player.setSpeed(loadedSpeed); // NEW: Set Speed

            playTime = loadedPlayTime; // Restore playtime

            // --- Load Monster State ---
            // First, reset all monsters based on AssetSetter defaults
            String[] worldInfo = br.readLine().split(",");
            changeWorld(Integer.parseInt(worldInfo[0]), false);
            player.setWorldX(loadedWorldX);
            player.setWorldY(loadedWorldY);
            eHandler.loadWorld(currentWorld);
            eHandler.powerUpTaken = worldInfo[1].equals("1");
            eHandler.bossDefeated = worldInfo[2].equals("1");
            for (int i = 0; i < obj.length; i++) {
                String flag = br.readLine();
                if ("0".equals(flag)) obj[i] = null;
                else if ("2".equals(flag) && obj[i] instanceof object.OBJ_Chest) ((object.OBJ_Chest) obj[i]).opened = true;
            }
            // Then, nullify monsters marked as dead in the save file
            for (int i = 0; i < monster.length; i++) {
                String status = br.readLine();
                if (status != null && status.equals("0")) {
                    monster[i] = null; // Remove monster if save says it's dead
                }
                // If status is "1" or file ends unexpectedly, monster remains as set by
                // AssetSetter
            }

            // --- Load Inventory --- // NEW
            player.getInventory().clear(); // Clear existing inventory before loading
            String line;
            while ((line = br.readLine()) != null && !line.equals("---END_INVENTORY---")) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        String itemName = parts[0];
                        int quantity = Integer.parseInt(parts[1]);
                        player.getInventory().put(itemName, quantity);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing inventory line: " + line);
                    }
                }
            }

            // --- Finalize Load ---
            gameState = playState; // Change game state
            ui.titleScreenState = 0; // Reset title screen state
            ui.updateExpBar(); // Update UI elements
            stopMusic(); // Stop title music
            playMusic(MUSIC_OVERWORLD);
            ui.showMessage("Game loaded from slot " + slot + "!");
            ui.loadSaveFileInformation(); // Refresh save/load screen info (though we're leaving it now)

        } catch (FileNotFoundException e) {
            ui.showMessage("No save file found in slot " + slot + "!");
            ui.titleScreenState = 2; // Stay on load screen
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            ui.showMessage("Failed to load game: File corrupted?");
            ui.titleScreenState = 2; // Stay on load screen
            playTime = 0; // Reset playtime if load fails
            // Optionally reset player to defaults?
            // player.setDefaultValues();
        }
    }

    // Sound methods
    public void playMusic(int i) {
        music.stop();
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void playSE(int i) { // Sound Effect
        se.setFile(i);
        se.play();
    }
}