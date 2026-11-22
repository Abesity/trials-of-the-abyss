package main;

import entity.Entity;

public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        // Use entity's solidArea directly for dimensions, but getters for
        // position/speed
        int entityLeftWorldX = entity.getWorldX() + entity.solidArea.x;
        int entityRightWorldX = entity.getWorldX() + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.getWorldY() + entity.solidArea.y;
        int entityBottomWorldY = entity.getWorldY() + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;

        // Predict next position based on direction and speed
        switch (entity.getDirection()) { // Use getter
            case "up":
                entityTopRow = (entityTopWorldY - entity.getSpeed()) / gp.tileSize; // Use getter
                // Check bounds before accessing map array
                if (entityTopRow >= 0 && entityLeftCol >= 0 && entityRightCol < gp.maxWorldCol) {
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                    if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                } else
                    entity.collisionOn = true; // Collision if moving out of bounds
                break;
            case "down":
                entityBottomRow = (entityBottomWorldY + entity.getSpeed()) / gp.tileSize; // Use getter
                if (entityBottomRow < gp.maxWorldRow && entityLeftCol >= 0 && entityRightCol < gp.maxWorldCol) {
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                } else
                    entity.collisionOn = true;
                break;
            case "left":
                entityLeftCol = (entityLeftWorldX - entity.getSpeed()) / gp.tileSize; // Use getter
                if (entityLeftCol >= 0 && entityTopRow >= 0 && entityBottomRow < gp.maxWorldRow) {
                    tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                    if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                } else
                    entity.collisionOn = true;
                break;
            case "right":
                entityRightCol = (entityRightWorldX + entity.getSpeed()) / gp.tileSize; // Use getter
                if (entityRightCol < gp.maxWorldCol && entityTopRow >= 0 && entityBottomRow < gp.maxWorldRow) {
                    tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                    if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                        entity.collisionOn = true;
                    }
                } else
                    entity.collisionOn = true;
                break;
        }
    }

    public int checkObject(Entity entity, boolean isPlayer) {
        int index = 999; // Default index indicating no collision

        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null) {
                // Get entity's current solid area world position
                entity.solidArea.x = entity.getWorldX() + entity.solidArea.x; // Use getter
                entity.solidArea.y = entity.getWorldY() + entity.solidArea.y; // Use getter

                // Get object's solid area world position
                gp.obj[i].solidArea.x = gp.obj[i].getWorldX() + gp.obj[i].solidArea.x; // Use getter
                gp.obj[i].solidArea.y = gp.obj[i].getWorldY() + gp.obj[i].solidArea.y; // Use getter

                // Predict entity's next position based on direction
                switch (entity.getDirection()) { // Use getter
                    case "up":
                        entity.solidArea.y -= entity.getSpeed();
                        break; // Use getter
                    case "down":
                        entity.solidArea.y += entity.getSpeed();
                        break; // Use getter
                    case "left":
                        entity.solidArea.x -= entity.getSpeed();
                        break; // Use getter
                    case "right":
                        entity.solidArea.x += entity.getSpeed();
                        break; // Use getter
                }

                // Check for intersection
                if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                    if (gp.obj[i].collision) { // Check object's collision property
                        entity.collisionOn = true;
                    }
                    if (isPlayer) { // If the checking entity is the player, return object index
                        index = i;
                    }
                }

                // IMPORTANT: Reset solidArea positions to defaults relative to entity's origin
                // (0,0)
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                gp.obj[i].solidArea.x = gp.obj[i].solidAreaDefaultX;
                gp.obj[i].solidArea.y = gp.obj[i].solidAreaDefaultY;
            }
        }
        return index;
    }

    // Checks collision between entity and an array of target entities (NPCs or
    // Monsters)
    public int checkEntity(Entity entity, Entity[] targets) {
        int index = 999;

        for (int i = 0; i < targets.length; i++) {
            if (targets[i] != null && targets[i] != entity) { // Ensure target exists and is not the entity itself

                // Get entity's current solid area world position
                entity.solidArea.x = entity.getWorldX() + entity.solidArea.x;
                entity.solidArea.y = entity.getWorldY() + entity.solidArea.y;

                // Get target's solid area world position
                targets[i].solidArea.x = targets[i].getWorldX() + targets[i].solidArea.x;
                targets[i].solidArea.y = targets[i].getWorldY() + targets[i].solidArea.y;

                // Predict entity's next position
                switch (entity.getDirection()) {
                    case "up":
                        entity.solidArea.y -= entity.getSpeed();
                        break;
                    case "down":
                        entity.solidArea.y += entity.getSpeed();
                        break;
                    case "left":
                        entity.solidArea.x -= entity.getSpeed();
                        break;
                    case "right":
                        entity.solidArea.x += entity.getSpeed();
                        break;
                }

                // Check for intersection
                if (entity.solidArea.intersects(targets[i].solidArea)) {
                    entity.collisionOn = true; // Set collision flag for the moving entity
                    index = i; // Return the index of the target entity collided with
                    // Don't break here, might need to check all targets if multiple overlap
                }

                // Reset solidArea positions
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                targets[i].solidArea.x = targets[i].solidAreaDefaultX;
                targets[i].solidArea.y = targets[i].solidAreaDefaultY;

                // If a collision was found and we only need the first one (e.g., for player
                // hitting NPC)
                // we could potentially break here if index != 999.
                // However, for NPC/Monster movement, letting it check all might prevent
                // phasing.
            }
        }
        return index;
    }

    // Checks collision between a specific entity (NPC/Monster) and the player
    // Returns true if collision occurs, false otherwise
    public boolean checkPlayer(Entity entity) {
        boolean collisionOccurred = false;

        // Get entity's solid area world position
        entity.solidArea.x = entity.getWorldX() + entity.solidArea.x;
        entity.solidArea.y = entity.getWorldY() + entity.solidArea.y;

        // Get player's solid area world position
        gp.player.solidArea.x = gp.player.getWorldX() + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.getWorldY() + gp.player.solidArea.y;

        // Predict entity's next position
        switch (entity.getDirection()) {
            case "up":
                entity.solidArea.y -= entity.getSpeed();
                break;
            case "down":
                entity.solidArea.y += entity.getSpeed();
                break;
            case "left":
                entity.solidArea.x -= entity.getSpeed();
                break;
            case "right":
                entity.solidArea.x += entity.getSpeed();
                break;
        }

        // Check for intersection
        if (entity.solidArea.intersects(gp.player.solidArea)) {
            entity.collisionOn = true; // Mark the NPC/Monster as collided
            collisionOccurred = true; // Indicate collision with player happened
            // Player's collision flag isn't set here; player checks against NPCs/Monsters
            // separately
        }

        // Reset solidArea positions
        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        gp.player.solidArea.x = gp.player.solidAreaDefaultX;
        gp.player.solidArea.y = gp.player.solidAreaDefaultY;

        return collisionOccurred;
    }
}