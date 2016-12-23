package client.skills;

import server.life.Element;

public interface ISkill {

    public int getId();
    public void setName(String name);
    public String getName();
    public SkillStatEffect getEffect(int level);
    public byte getMaxLevel();
    public int getAnimationTime();
    public boolean canBeLearnedBy(int job);
    public boolean isFourthJob();
    public boolean getAction();
    public Element getElement();
    public boolean isBeginnerSkill();
    public boolean hasRequiredSkill();
    public boolean isInvisible();
    public boolean isChargeSkill();
    public int getRequiredSkillLevel();
    public int getRequiredSkillId();
    public int getMasterLevel();
    public boolean canCombatOrdered();
    public boolean haveMasterLevel();
    public boolean CheckMasterLevel();
    public boolean ishyper();
}
