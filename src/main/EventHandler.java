package main;

/**
 * Map events from Vinci's placement file: teleporters, the exit to the next
 * world, spike traps, healing fountains and the power-up.
 */
public class EventHandler {
    GamePanel gp;
    WorldData data;
    boolean canTouchEvent = true;
    int previousEventX, previousEventY;
    public boolean powerUpTaken = false;
    public boolean bossDefeated = false;

    public EventHandler(GamePanel gp) {
        this.gp = gp;
    }

    public void loadWorld(int world) {
        data = WorldData.get(world);
        canTouchEvent = false;
        previousEventX = gp.player.getWorldX();
        previousEventY = gp.player.getWorldY();
    }

    private int playerCol() {
        return (gp.player.getWorldX() + gp.player.solidArea.x + gp.player.solidArea.width / 2) / gp.tileSize;
    }

    private int playerRow() {
        return (gp.player.getWorldY() + gp.player.solidArea.y + gp.player.solidArea.height / 2) / gp.tileSize;
    }

    private static boolean contains(int[][] list, int col, int row) {
        for (int[] p : list) if (p[0] == col && p[1] == row) return true;
        return false;
    }

    /** Called every frame while playing. */
    public void checkEvent() {
        if (data == null) return;
        int col = playerCol(), row = playerRow();

        // re-arm events once the player has walked a tile away from the last one
        int distance = Math.max(Math.abs(gp.player.getWorldX() - previousEventX),
                Math.abs(gp.player.getWorldY() - previousEventY));
        if (distance > gp.tileSize) canTouchEvent = true;

        if (contains(data.spikes, col, row) && gp.tileM.spikesUp() && gp.player.hurtCooldown == 0) {
            gp.player.setLife(gp.player.getLife() - 8);
            gp.player.hurtCooldown = 60;
            gp.playSE(GamePanel.SE_HIT);
            gp.ui.showMessage("Ouch! Spikes! -8 HP");
            if (gp.player.getLife() <= 0) {
                gp.battleHandler.battleText = "The spikes got you...";
                gp.gameState = GamePanel.defeatState;
            }
        }

        if (!powerUpTaken && contains(data.powerups, col, row)) {
            powerUpTaken = true;
            gp.player.setBaseDamage(gp.player.getDamage() + 3);
            gp.player.setSpeed(gp.player.getSpeed() + 1);
            gp.playSE(GamePanel.SE_HEAL);
            gp.ui.showMessage("Power up! Attack +3, Speed +1");
        }

        if (!canTouchEvent) return;

        for (int[] t : data.teleporters) {
            if ((t[0] == col && t[1] == row) || (t[2] == col && t[3] == row)) {
                boolean fromA = t[0] == col && t[1] == row;
                teleport(fromA ? t[2] : t[0], fromA ? t[3] : t[1]);
                return;
            }
        }

        if (contains(data.end, col, row)) {
            if (gp.currentWorld < 3) {
                gp.playSE(GamePanel.SE_TELEPORT);
                gp.changeWorld(gp.currentWorld + 1, true);
            } else if (bossDefeated) {
                gp.finishGame();
            } else {
                gp.ui.showMessage("The King Slime still guards the Abyss.");
                markTouched();
            }
        }
    }

    private void teleport(int col, int row) {
        gp.player.setWorldX(col * gp.tileSize);
        gp.player.setWorldY(row * gp.tileSize);
        gp.playSE(GamePanel.SE_TELEPORT);
        markTouched();
    }

    private void markTouched() {
        canTouchEvent = false;
        previousEventX = gp.player.getWorldX();
        previousEventY = gp.player.getWorldY();
    }

    /** ENTER while facing a fountain refills HP (red) or MP (blue). */
    public void checkInteraction() {
        if (data == null) return;
        int col = playerCol(), row = playerRow();
        switch (gp.player.getDirection()) {
            case "up": row--; break;
            case "down": row++; break;
            case "left": col--; break;
            case "right": col++; break;
        }
        if (contains(data.fountainsRed, col, row)) {
            gp.keyH.enterPressed = false;
            gp.player.setLife(gp.player.getMaxLife());
            gp.playSE(GamePanel.SE_HEAL);
            gp.ui.currentDialogue = "You drink from the red fountain.\nYour HP is fully restored.";
            gp.gameState = GamePanel.dialogueState;
        } else if (contains(data.fountainsBlue, col, row)) {
            gp.keyH.enterPressed = false;
            gp.player.setMana(gp.player.getMaxMana());
            gp.playSE(GamePanel.SE_HEAL);
            gp.ui.currentDialogue = "You drink from the blue fountain.\nYour MP is fully restored.";
            gp.gameState = GamePanel.dialogueState;
        }
    }
}
