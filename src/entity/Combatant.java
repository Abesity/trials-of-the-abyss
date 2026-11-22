package entity;

import java.awt.image.BufferedImage;

public interface Combatant {

    int getMaxLife();
    int getLife();
    void setLife(int life);

    int getMaxMana();
    int getMana();
    void setMana(int mana);
    int getManaRegen(); 

    int getDamage();
    int getDefense();
    void setDefense(int defense); 
    
    String getName();
    Entity.Skill[] getSkills(); 
    String[] getSkillNames();   
    
    BufferedImage getBattleSprite();

    /** Animation frames used on the battle screen (Vinci's 64x64 battle art). */
    BufferedImage[] getBattleFrames();
}