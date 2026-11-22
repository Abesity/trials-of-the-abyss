package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import entity.Entity;
import entity.Player;
import entity.Combatant;
import monster.MON_Slime; 
import object.Item;

public class BattleHandler {
    GamePanel gp;
    Player player;
    Combatant enemy; 
    public int battleCounter = 0; 
    private Random random = new Random(); 

    public static enum BattleState {
        START,
        PLAYER_CHOICE,
        SKILL_SELECT,
        ITEM_SELECT,
        PLAYER_ACTION,
        ENEMY_ACTION,
        VICTORY,
        DEFEAT
    }

    public BattleState currentState = BattleState.START; 

    private ArrayList<String> actionLog = new ArrayList<>();
    private static final int MAX_LOG_ENTRIES = 5;

    public String battleText = ""; 
    public String playerActionText = ""; 
    public String enemyActionText = ""; 
    public int enemyHitAt = -100, playerHitAt = -100; // frame of the last hit, for the shake effect

    public BattleHandler(GamePanel gp) {
        this.gp = gp;
    }
    
    public Combatant getEnemy() { 
    	return enemy;
    }
    
    public void startBattle(Player player, Entity monsterEntity) {
        this.player = player;
        if (monsterEntity instanceof Combatant) {
            this.enemy = (Combatant) monsterEntity; 
            currentState = BattleState.START; 
            battleText = "A wild " + enemy.getName() + " appears!"; 
            battleCounter = 0; 
            actionLog.clear();
            playerActionText = "";
            enemyActionText = "";
            gp.ui.commandNum = 0; 
            player.guarding = false;
            player.playBattleAnimation("idle");
            gp.stopMusic();
            gp.playMusic(GamePanel.MUSIC_BATTLE);
        } else {
            System.err.println("Error: Cannot start battle with non-Combatant entity: " + monsterEntity.getName());
            gp.gameState = GamePanel.playState; 
        }
    }

    public void advanceBattle() {
        if (currentState == null
                || (currentState != BattleState.VICTORY && currentState != BattleState.DEFEAT && enemy == null)) {
            if (currentState != BattleState.VICTORY && currentState != BattleState.DEFEAT) {
                System.err.println("Warning: advanceBattle called with invalid state or null enemy.");
                if (gp.gameState != GamePanel.playState) {
                    exitBattle();
                }
                return;
            }
        }

        switch (currentState) {
            case START:
                currentState = BattleState.PLAYER_CHOICE;
                battleText = "What will you do?";
                playerActionText = "";
                enemyActionText = "";
                gp.ui.commandNum = 0;
                break;
            case PLAYER_ACTION:
                if (enemy != null && enemy.getLife() <= 0) {
                    endBattle(true);
                } else if (enemy != null) {
                    enemyTurn();
                } else {
                    System.err.println("Error: Enemy is null after player action resolved.");
                    endBattle(true);
                }
                break;
            case ENEMY_ACTION:
                if (player.getLife() <= 0) {
                    endBattle(false);
                } else {
                    currentState = BattleState.PLAYER_CHOICE;
                    battleText = "What will you do?";
                    playerActionText = "";
                    enemyActionText = "";
                    gp.ui.commandNum = 0;
                    battleCounter++;
                }
                break;
            case VICTORY:
            case DEFEAT:
            case SKILL_SELECT:
            case ITEM_SELECT:
                break;
            default:
                System.err.println("Warning: Reached unexpected battle state: " + currentState + ". Resetting.");
                currentState = BattleState.START;
                break;
        }
        if (gp.gameState == GamePanel.battleState1 || gp.gameState == GamePanel.battleState2) {
            gp.repaint();
        }
    }

    
    public ArrayList<String> getActionLog() {
        return actionLog;
    }

    public void logAction(String action) {
        if (action == null || action.trim().isEmpty())
            return;
        actionLog.add(0, action);
        if (actionLog.size() > MAX_LOG_ENTRIES) {
            actionLog.remove(actionLog.size() - 1);
        }
    }
    
    public void executePlayerChoice(int selection) {
        if (currentState != BattleState.PLAYER_CHOICE)
            return;
        switch (selection) {
            case 0:
                playerAttack();
                currentState = BattleState.PLAYER_ACTION;
                advanceBattle();
                break;
            case 1:
            	if (!player.getUsableItemNames().isEmpty()) {
                    currentState = BattleState.ITEM_SELECT;
                    gp.gameState = GamePanel.battleState2; 
                    gp.ui.commandNum = 0; 
                    battleText = "Select an item:";
                    gp.repaint();
                } else {
                    battleText = "No usable items!";
                }
                break;
            case 2:
                currentState = BattleState.SKILL_SELECT;
                gp.gameState = GamePanel.battleState2;
                gp.ui.commandNum = 0;
                battleText = "Select a skill:";
                gp.repaint();
                break;
            case 3:
                attemptRun();
                break;
        }
    }

    public void executePlayerSkill(int selection) {
        if (currentState != BattleState.SKILL_SELECT || enemy == null)
            return;
        Entity.Skill[] skills = player.getSkills();
        if (skills == null || selection < 0 || selection >= skills.length || skills[selection] == null) {
            playerActionText = "Invalid skill selection!";
            logAction(playerActionText);
            gp.repaint();
            return;
        }
        Entity.Skill skill = skills[selection];
        if (player.getMana() < skill.getManaCost()) {
            playerActionText = "Not enough MP for " + skill.getName() + "!";
            logAction(playerActionText);
            gp.repaint();
            return;
        }
        player.setMana(player.getMana() - skill.getManaCost());
        int damage = 0;
        String effectDescription = "";
        if ("Iron Guard".equals(skill.getName())) {
            player.guarding = true; // halves the next enemy hit (was a no-op in the 2025 build)
            player.playBattleAnimation("defend");
            effectDescription = player.getName() + " uses Iron Guard! The next hit is halved.";
            damage = 0;
        } else {
            player.playBattleAnimation("attack");
            enemyHitAt = gp.frameCount;
            gp.playSE(GamePanel.SE_HIT);
            damage = calculateSkillDamage(skill, enemy);
            enemy.setLife(enemy.getLife() - damage);
            effectDescription = player.getName() + " uses " + skill.getName() + "! Deals " + damage + " damage.";
        }
        playerActionText = effectDescription;
        logAction(playerActionText);
        currentState = BattleState.PLAYER_ACTION;
        gp.gameState = GamePanel.battleState1;
        advanceBattle();
    }
    
    public void executePlayerItemUse(int selectionIndex) {
        if (currentState != BattleState.ITEM_SELECT) return;

        List<String> usableItems = player.getUsableItemNames();

        if (selectionIndex < 0 || selectionIndex >= usableItems.size()) {
            battleText = "Invalid item selection!"; // Should not happen with proper nav
            return;
        }

        String itemName = usableItems.get(selectionIndex);
        Item item = gp.itemManager.getItem(itemName);

        if (item != null) {
             boolean usedSuccessfully = player.useItem(itemName); 
             
             if (usedSuccessfully) {
                playerActionText = gp.ui.message; 
                logAction(playerActionText);
                currentState = BattleState.PLAYER_ACTION;
                gp.gameState = GamePanel.battleState1;
                advanceBattle(); 
            } else {
                battleText = gp.ui.message; 
                gp.repaint();
            }
        } else {
            battleText = "Error: Item not found!"; 
        }
    }

    private void playerAttack() {
        if (enemy == null)
            return;
        int damage = calculateDamage(player, enemy);
        player.playBattleAnimation("attack");
        enemyHitAt = gp.frameCount;
        gp.playSE(GamePanel.SE_HIT);
        enemy.setLife(enemy.getLife() - damage);
        playerActionText = player.getName() + " attacks! Deals " + damage + " damage.";
        logAction(playerActionText);
    }

    private void attemptRun() {
        if (random.nextDouble() < 0.7) {
            battleText = "You escaped successfully!";
            logAction(battleText);
            exitBattle();
        } else {
            playerActionText = "Couldn't escape!";
            logAction(playerActionText);
            currentState = BattleState.PLAYER_ACTION;
            player.getDefense();
            advanceBattle();
        }
    }

    // enemyTurn...
    private void enemyTurn() {
        if (enemy == null || enemy.getLife() <= 0)
            return;
        Entity.Skill skillToUse = null;
        boolean canAffordAnySkill = false;
        if (enemy.getSkills() != null) {
            for (Entity.Skill skill : enemy.getSkills()) {
                if (skill != null && enemy.getMana() >= skill.getManaCost()) {
                    canAffordAnySkill = true;
                    break;
                }
            }
        }
        boolean attemptSkill = canAffordAnySkill && random.nextDouble() < 0.4;
        if (attemptSkill) {
            List<Entity.Skill> affordableSkills = new ArrayList<>();
            for (Entity.Skill skill : enemy.getSkills()) {
                if (skill != null && enemy.getMana() >= skill.getManaCost()) {
                    affordableSkills.add(skill);
                }
            }
            if (!affordableSkills.isEmpty()) {
                skillToUse = affordableSkills.get(random.nextInt(affordableSkills.size()));
            }
        }
        int damage;
        if (skillToUse != null) {
            enemy.setMana(enemy.getMana() - skillToUse.getManaCost());
            damage = guardedDamage(calculateSkillDamage(skillToUse, player));
            player.setLife(player.getLife() - damage);
            enemyActionText = enemy.getName() + " uses " + skillToUse.getName() + "! Deals " + damage + " damage.";
        } else {
            damage = guardedDamage(calculateDamage(enemy, player));
            player.setLife(player.getLife() - damage);
            enemyActionText = enemy.getName() + " attacks! Deals " + damage + " damage.";
        }
        playerHitAt = gp.frameCount;
        logAction(enemyActionText);
        currentState = BattleState.ENEMY_ACTION;
        advanceBattle();
    }

    private int guardedDamage(int damage) {
        if (player.guarding) {
            player.guarding = false;
            player.playBattleAnimation("defend");
            return Math.max(1, damage / 2);
        }
        return damage;
    }

    private int calculateDamage(Combatant attacker, Combatant defender) {
        int baseDamage = attacker.getDamage();
        int targetDefense = defender.getDefense();
        int rawDamage = baseDamage - targetDefense;
        int calculatedDamage = Math.max(1, rawDamage);
        calculatedDamage = (int) (calculatedDamage * (0.9 + random.nextDouble() * 0.2));
        return Math.max(1, calculatedDamage);
    }

    private int calculateSkillDamage(Entity.Skill skill, Combatant defender) {
        int baseDamage = skill.getDamage();
        if (baseDamage <= 0)
            return 0;
        int targetDefense = defender.getDefense();
        int effectiveDefense = targetDefense / 2;
        int rawDamage = baseDamage - effectiveDefense;
        int calculatedDamage = Math.max(1, rawDamage);
        calculatedDamage = (int) (calculatedDamage * (0.9 + random.nextDouble() * 0.2));
        return Math.max(1, calculatedDamage);
    }

    // endBattle...
    private void endBattle(boolean victory) {
        boolean keyDropped = false;
        Combatant defeatedEnemy = enemy;
        int enemyArrayIndex = -1;
        if (defeatedEnemy != null) {
            for (int i = 0; i < gp.monster.length; i++) {
                if (gp.monster[i] == defeatedEnemy) {
                    enemyArrayIndex = i;
                    break;
                }
            }
        }
        if (victory) {
            if (enemyArrayIndex != -1 && defeatedEnemy instanceof Entity) {
                Entity enemyEntity = (Entity) defeatedEnemy;
                player.gainRewards(enemyEntity.getExpDrop(), enemyEntity.getGoldDrop(), enemyEntity.getScoreDrop());
                if (enemyEntity instanceof monster.Monster) {
                    monster.Monster m = (monster.Monster) enemyEntity;
                    if (m.dropsKey()) {
                        player.addItem("Key", 1);
                        keyDropped = true;
                    }
                    if (m.isBoss()) gp.eHandler.bossDefeated = true;
                }
            } else if (defeatedEnemy != null) {
                System.err.println("Warning: Could not find defeated enemy " + defeatedEnemy.getName()
                        + " in monster array or it's not an Entity.");
            }
            currentState = BattleState.VICTORY;
            battleText = "You defeated the " + (defeatedEnemy != null ? defeatedEnemy.getName() : "enemy") + "!"
                    + (keyDropped ? " It dropped a key!" : "");
            gp.stopMusic();
            gp.playSE(GamePanel.SE_FANFARE);
            gp.gameState = GamePanel.victoryState;
            if (enemyArrayIndex != -1) {
                gp.monster[enemyArrayIndex] = null;
            }
            gp.ui.updateExpBar();
        } else {
            currentState = BattleState.DEFEAT;
            battleText = "You were defeated...";
            gp.gameState = GamePanel.defeatState;
        }
        enemy = null;
        actionLog.clear();
        gp.repaint();
    }

    // exitBattle...
    public void exitBattle() {
        boolean wasRunning = (currentState != BattleState.VICTORY && currentState != BattleState.DEFEAT);
        if (player != null) {
            player.guarding = false;
            player.encounterCooldown = wasRunning ? 90 : 30; // step away before the next fight
        }
        gp.stopMusic();
        gp.playMusic(GamePanel.MUSIC_OVERWORLD);
        enemy = null;
        currentState = BattleState.START;
        battleCounter = 0;
        actionLog.clear();
        battleText = "";
        playerActionText = "";
        enemyActionText = "";
        gp.gameState = GamePanel.playState;
        gp.repaint();
    }

    public boolean isWaitingForInput() {
        return currentState == BattleState.PLAYER_CHOICE || currentState == BattleState.SKILL_SELECT;
    }

    public boolean isBattleEndedState() {
        return currentState == BattleState.VICTORY || currentState == BattleState.DEFEAT;
    }

    public BattleState getCurrentState() {
        return currentState;
    }
}