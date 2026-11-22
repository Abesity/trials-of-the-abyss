package tile;

import java.awt.Graphics2D;
// BufferedImage import removed as it's not directly used here besides in Tile class
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.UtilityTool;
import main.WorldData;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class TileManager {

    GamePanel gp;
    public Tile[] tile; // Array holding different tile types
    public int mapTileNum[][]; // 2D array storing the tile map layout

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tile = new Tile[200]; // Increased size to accommodate new tiles
        // Initialize map array based on world size
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadSpikeFrames();
        loadMap("/maps/world1.txt"); // Vinci's first world
    }

    // Loads tile images and properties
    public void getTileImage() {
    	setUp(0,"00",true);
    	setUp(1,"01",true);
    	setUp(2,"02",true);
    	setUp(3,"03",false);
    	setUp(4,"04",false);
    	setUp(5,"05",false);
    	setUp(6,"06",false);
    	setUp(7,"07",false);
    	setUp(8,"08",false);
    	setUp(9,"09",true);
    	setUp(10,"10",true);
    	setUp(11,"11",true);
    	setUp(12,"12",true);
    	setUp(13,"13",true);
    	setUp(14,"14",true);
    	setUp(15,"15",true);
    	setUp(16,"16",true);
    	setUp(17,"17",true);
    	setUp(18,"18",true);
    	setUp(19,"19",true);
    	setUp(20,"20",true);
    	setUp(21,"21",true);
    	setUp(22,"22",true);
    	setUp(23,"23",false);
    	setUp(24,"24",true);
    	setUp(25,"25",false);
    	setUp(26,"26",true);
    	setUp(27,"27",true);
    	setUp(28,"28",true);
    	setUp(29,"29",true);
    	setUp(30,"30",true);
    	setUp(31,"31",true);
    	setUp(32,"32",true);
    	setUp(33,"33",true);
    	setUp(34,"34",true);
    	setUp(35,"35",true);
    	setUp(36,"36",true);
    	setUp(37,"37",true);
    	setUp(38,"38",true);
    	setUp(39,"39",true);
    	setUp(40,"40",true);
    	setUp(41,"41",true);
    	setUp(42,"42",true);
    	setUp(43,"43",false);
    	setUp(44,"44",false);
    	setUp(45,"45",false);
    	setUp(46,"46",true);
    	setUp(47,"47",true);
    	setUp(48,"48",true);
    	setUp(49,"49",true);
    	setUp(50,"50",true);
    	setUp(51,"51",true);
    	setUp(52,"52",true);
    	setUp(53,"53",true);
    	setUp(54,"54",false);
    	setUp(55,"55",false);
    	setUp(56,"56",false);
    	setUp(57,"57",false);
    	setUp(58,"58",false);
    	setUp(59,"59",false);
    	setUp(60,"60",true);
    	setUp(61,"61",false);
    	setUp(62,"62",false);
    	setUp(63,"63",false);
    	setUp(64,"64",false);
    	setUp(65,"65",true);
    	setUp(66,"66",false);
    	setUp(67,"67",false);
    	setUp(68,"68",false);
    	setUp(69,"69",false);
    	setUp(70,"70",true);
    	setUp(71,"71",true);
    	setUp(72,"72",true);
    	setUp(73,"73",false);
    	setUp(74,"74",true);
    	setUp(75,"75",true);
    	setUp(76,"76",false);
    	setUp(77,"77",false);
    	setUp(78,"78",false);
    	setUp(79,"79",false);
    	setUp(80,"80",true);
    	setUp(81,"81",false);
    	setUp(82,"82",false);
    	setUp(83,"83",false);
    	setUp(84,"84",false);
    	setUp(85,"85",true);
    	setUp(86,"86",true);
    	setUp(87,"87",true);
    	setUp(88,"88",true);
    	setUp(89,"89",true);
    	setUp(90,"90",false);
    	setUp(91,"91",false);
    	setUp(92,"92",false);
    	setUp(93,"93",false);
    	setUp(94,"94",true);
    	setUp(95,"95",true);
    	setUp(96,"96",false);
    	setUp(97,"97",false);
    	setUp(98,"98",false);
    	setUp(99,"99",false);
    	setUp(100,"100",true);
    	setUp(101,"101",false);
    	setUp(102,"102",false);
    	setUp(103,"103",false);
    	setUp(104,"104",false);
    	setUp(105,"105",false);
    	setUp(106,"106",false);
    	setUp(107,"107",false);
    	setUp(108,"108",false);
    	setUp(109,"109",false);
    	setUp(110,"110",false);
    	setUp(111,"111",true);
    	setUp(112,"112",true);
    	setUp(113,"113",true);
    	setUp(114,"114",true);
    	setUp(115,"115",true);
    	setUp(116,"116",true);
    	setUp(117,"117",true);
    	setUp(118,"118",false);
    	setUp(119,"119",false);
    	setUp(120,"120",false);
    	setUp(121,"121",false);
    	setUp(122,"122",true);
    	setUp(123,"123",true);
    	setUp(124,"124",true);
    	setUp(125,"125",true);
    	setUp(126,"126",false);
    	setUp(127,"127",false);
    	setUp(128,"128",false);
    	setUp(129,"129",false);
    	setUp(130,"130",false);
    	setUp(131,"131",false);
    	setUp(132,"132",true);
    	setUp(133,"133",true);
    	setUp(134,"134",true);
    	setUp(135,"135",true);
    	setUp(136,"136",true);
    	setUp(137,"137",true);
    	setUp(138,"138",false);
    	setUp(139,"139",false);
    	setUp(140,"140",false);
    	setUp(141,"141",false);
    	setUp(142,"142",true);
    	setUp(143,"143",true);
    	setUp(144,"144",true);
    	setUp(145,"145",true);
    	setUp(146,"146",true);
    	setUp(147,"147",true);
    	setUp(148,"148",false);
    	setUp(149,"149",false);
    	setUp(150,"150",false);
    	setUp(151,"151",false);
    	setUp(152,"152",true);
    	setUp(153,"153",true);
    	setUp(154,"154",true);
    	setUp(155,"155",true);
    	setUp(156,"156",true);
    	setUp(157,"157",true);
    	setUp(158,"158",false);
    	setUp(159,"159",true);
    	setUp(160,"160",false);
    	setUp(161,"161",false);
    	setUp(162,"162",false);
    	setUp(163,"163",false);
    	setUp(164,"164",true);
    	setUp(165,"165",true);
    	setUp(166,"166",true);
    	setUp(167,"167",false);
    	setUp(168,"168",true);
    	setUp(169,"169",true);
    	setUp(170,"170",true);
    	setUp(171,"171",true);
    	setUp(172,"172",true);
    	setUp(173,"173",true);
    	setUp(174,"174",true);
    	setUp(175,"175",true);
    	setUp(176,"176",true);
    	setUp(177,"177",true);
    	setUp(178,"178",false);
    	setUp(179,"179",false);
    	setUp(180,"180",false);
    	setUp(181,"181",true);
    	setUp(182,"182",true);
    	setUp(183,"183",true);
    	setUp(184,"184",true);
    	setUp(185,"185",true);
    	setUp(186,"186",true);
    	setUp(187,"187",true);
    	setUp(188,"188",true);
    	setUp(189,"189",true);
    	setUp(190,"190",true);
    	setUp(191,"191",true);
    	setUp(192,"192",true);
    	setUp(193,"193",true);
    	setUp(194,"194",true);
    	setUp(195,"195",true);
    	setUp(196,"196",true);
    	 
    }

    // Helper method to load and scale tile images
    public void setUp(int index, String imageName, boolean collision) {
        UtilityTool uTool = new UtilityTool();
        try {
            // Ensure index is within bounds
            if (index >= tile.length) {
                System.err.println("Error: Tile index " + index + " is out of bounds for array size " + tile.length);
                return; // Or resize the array
            }

            tile[index] = new Tile();
            // Load image relative to the class location
            InputStream is = getClass().getResourceAsStream("/tiles/" + imageName + ".png");
            if (is == null) {
                System.err.println("Error loading tile image: /tiles/" + imageName + ".png not found.");
                // Optionally load a default "missing" texture
                is = getClass().getResourceAsStream("/tiles/missing.png"); // Assuming you have a missing.png
                if (is == null)
                    return; // Exit if even missing texture fails
                tile[index].image = ImageIO.read(is);
            } else {
                tile[index].image = ImageIO.read(is);
            }

            // Scale the image
            tile[index].image = uTool.scaledImage(tile[index].image, gp.tileSize, gp.tileSize);
            tile[index].collision = collision;

        } catch (IOException e) {
            System.err.println("IOException during tile setup for index " + index + ", image " + imageName);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException during tile setup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Loads map data from a file
    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("Error loading map: File not found - " + filePath);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (row < gp.maxWorldRow) { // Iterate through rows
                String line = br.readLine();
                if (line == null)
                    break; // End of file

                String numbers[] = line.trim().split("\\s+"); // Split by whitespace

                // Ensure the line has the expected number of columns
                if (numbers.length < gp.maxWorldCol) {
                    System.err.println("Warning: Map line " + (row + 1) + " has fewer columns (" + numbers.length
                            + ") than expected (" + gp.maxWorldCol + ") in " + filePath);
                    // Optionally fill remaining columns with a default tile (e.g., 0)
                }

                col = 0;
                while (col < gp.maxWorldCol) {
                    if (col < numbers.length) {
                        try {
                            int num = Integer.parseInt(numbers[col]);
                            // Optional: Check if tile number is valid for the loaded tiles
                            if (num < 0 || num >= tile.length || tile[num] == null) {
                                System.err.println("Warning: Invalid tile number " + num + " at [" + col + "," + row
                                        + "] in map " + filePath + ". Using tile 0.");
                                mapTileNum[col][row] = 0; // Use default tile
                            } else {
                                mapTileNum[col][row] = num;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing number '" + numbers[col] + "' at [" + col + "," + row
                                    + "] in map " + filePath + ". Using tile 0.");
                            mapTileNum[col][row] = 0; // Use default tile on parsing error
                        }
                    } else {
                        // If the line was too short, fill remaining with default
                        mapTileNum[col][row] = 0;
                    }
                    col++;
                }
                row++;
            }
            br.close();

        } catch (Exception e) {
            System.err.println("Exception during map loading: " + filePath);
            e.printStackTrace();
        }
    }

    // Draws the visible portion of the map
    public void draw(Graphics2D g2) {

        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            // Check if tileNum is valid before drawing
            if (tileNum < 0 || tileNum >= tile.length || tile[tileNum] == null || tile[tileNum].image == null) {
                // Skip drawing this tile or draw a placeholder if desired
                // System.err.println("Attempting to draw invalid tile number: " + tileNum + "
                // at [" + worldCol + "," + worldRow + "]");
                worldCol++;
                if (worldCol == gp.maxWorldCol) {
                    worldCol = 0;
                    worldRow++;
                }
                continue; // Skip to next tile
            }

            // Calculate tile position relative to player
            // ****** CORRECTION IS HERE ******
            int playerWorldX = gp.player.getWorldX(); // Use getter
            int playerWorldY = gp.player.getWorldY(); // Use getter
            int playerScreenX = gp.player.getScreenX();
            int playerScreenY = gp.player.getScreenY();

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - playerWorldX + playerScreenX;
            int screenY = worldY - playerWorldY + playerScreenY;
            // ****** END OF CORRECTION AREA ******

            // --- Simple Culling ---
            // Check if the tile is within the screen bounds (+/- one tile for smooth edges)
            if (worldX + gp.tileSize > playerWorldX - playerScreenX &&
                    worldX - gp.tileSize < playerWorldX + playerScreenX &&
                    worldY + gp.tileSize > playerWorldY - playerScreenY &&
                    worldY - gp.tileSize < playerWorldY + playerScreenY) {
                g2.drawImage(tile[tileNum].image, screenX, screenY, null); // Draw the tile image
            }
            // More efficient culling: calculate start/end col/row based on screen bounds
            // int startCol = Math.max(0, (playerWorldX - playerScreenX) / gp.tileSize);
            // int endCol = Math.min(gp.maxWorldCol, (playerWorldX + playerScreenX +
            // gp.tileSize) / gp.tileSize);
            // int startRow = Math.max(0, (playerWorldY - playerScreenY) / gp.tileSize);
            // int endRow = Math.min(gp.maxWorldRow, (playerWorldY + playerScreenY +
            // gp.tileSize) / gp.tileSize);
            // Then iterate only from startCol/Row to endCol/Row

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    // ---- Spike traps (Vinci's Spikes_Animated frames) ----
    private BufferedImage[] sandA = new BufferedImage[5], sandB = new BufferedImage[5];
    private int[][] spikes = new int[0][];
    private int[][] glow = new int[0][];

    private void loadSpikeFrames() {
        UtilityTool u = new UtilityTool();
        for (int i = 0; i < 5; i++) {
            sandA[i] = loadScaled(u, "/spikes/sand_a_" + (i + 1) + ".png");
            sandB[i] = loadScaled(u, "/spikes/sand_b_" + (i + 1) + ".png");
        }
    }

    private BufferedImage loadScaled(UtilityTool u, String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            return is == null ? null : u.scaledImage(ImageIO.read(is), gp.tileSize, gp.tileSize);
        } catch (IOException e) {
            return null;
        }
    }

    public void loadWorld(int world) {
        loadMap("/maps/world" + world + ".txt");
        WorldData d = WorldData.get(world);
        spikes = d.spikes;
        int n = d.teleporters.length * 2;
        glow = new int[n + d.powerups.length][];
        for (int i = 0; i < d.teleporters.length; i++) {
            glow[i * 2] = new int[] { d.teleporters[i][0], d.teleporters[i][1] };
            glow[i * 2 + 1] = new int[] { d.teleporters[i][2], d.teleporters[i][3] };
        }
        for (int i = 0; i < d.powerups.length; i++) glow[n + i] = d.powerups[i];
    }

    /** Spike cycle: 2 seconds, raised for the last 0.7 s. */
    private int spikePhase() {
        return gp.frameCount % 120;
    }

    public boolean spikesUp() {
        int t = spikePhase();
        return t >= 66 && t < 108;
    }

    private int spikeFrame() {
        int t = spikePhase();
        if (t < 60) return 0;
        if (t < 66) return t < 63 ? 1 : 2;
        if (t < 102) return 3;
        if (t < 108) return 4;
        return 0;
    }

    /** Draws animated spikes and the glow on teleporters / power-ups over the map. */
    public void drawOverlays(Graphics2D g2) {
        int f = spikeFrame();
        for (int[] p : spikes) {
            int sx = p[0] * gp.tileSize - gp.player.getWorldX() + gp.player.getScreenX();
            int sy = p[1] * gp.tileSize - gp.player.getWorldY() + gp.player.getScreenY();
            if (sx < -gp.tileSize || sy < -gp.tileSize || sx > gp.screenWidth || sy > gp.screenHeight) continue;
            int under = mapTileNum[p[0]][p[1]];
            BufferedImage img;
            if (under == 110 || under == 130) {          // dungeon spikes: holes (110) / raised (130)
                img = tile[f >= 2 && f <= 3 ? 130 : 110].image;
            } else {
                BufferedImage[] set = (under == 178 || under == 179) ? sandB : sandA;
                img = set[f];
            }
            if (img != null) g2.drawImage(img, sx, sy, null);
        }
        float pulse = (float) (0.25 + 0.2 * Math.sin(gp.frameCount / 12.0));
        java.awt.Composite old = g2.getComposite();
        for (int i = 0; i < glow.length; i++) {
            int[] p = glow[i];
            if (i >= glow.length - WorldData.get(gp.currentWorld).powerups.length && gp.eHandler.powerUpTaken) continue;
            int sx = p[0] * gp.tileSize - gp.player.getWorldX() + gp.player.getScreenX();
            int sy = p[1] * gp.tileSize - gp.player.getWorldY() + gp.player.getScreenY();
            if (sx < -gp.tileSize || sy < -gp.tileSize || sx > gp.screenWidth || sy > gp.screenHeight) continue;
            boolean power = i >= glow.length - WorldData.get(gp.currentWorld).powerups.length;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            g2.setColor(power ? new Color(255, 214, 90) : new Color(120, 200, 255));
            g2.fillOval(sx + 6, sy + 10, gp.tileSize - 12, gp.tileSize - 20);
            g2.setComposite(old);
        }
    }
}
