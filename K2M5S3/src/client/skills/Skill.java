package client.skills;

import constants.GameConstants;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.Element;
import tools.StringUtil;
import java.util.ArrayList;
import java.util.List;
import provider.MapleDataProviderFactory;

public class Skill implements ISkill {

    private String name;
    private final List<SkillStatEffect> effects = new ArrayList<>();
    private Element element;
    private byte level;
    private int id = 0, skillType = 0, animationTime = 0, requiredSkill = 0, masterLevel = 0;
    private boolean action = false, invisible = false, chargeskill = false, hyper = false, combatOrdered = false;

    Skill(final int id) {
        super();
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
       
    public static final Skill loadFromData(final int id, final MapleData data) {
        Skill ret = new Skill(id);
        try {
            boolean isBuff = false;
            ret.skillType = MapleDataTool.getInt("skillType", data, -1);
            final String elem = MapleDataTool.getString("elemAttr", data, null);
            if (elem != null) {
                ret.element = Element.getFromChar(elem.charAt(0));
            } else {
                ret.element = Element.NEUTRAL;
            }
            ret.invisible = MapleDataTool.getInt("invisible", data, 0) > 0;
            ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
            ret.combatOrdered = MapleDataTool.getInt("combatOrders", data, 0) > 0;
            ret.hyper = (data.getChildByPath("hyper") != null);
            
            final MapleData effect = data.getChildByPath("effect");
            if (ret.skillType == 2) {
                isBuff = true;
            } else {
                final MapleData action_ = data.getChildByPath("action");
                final MapleData hit = data.getChildByPath("hit");
                final MapleData ball = data.getChildByPath("ball");
                if ((action_ == null && data.getChildByPath("prepare/action") != null) || (id == 5201001)) {
                    ret.action = true;
                } else {
                    ret.action = true;
                }
                isBuff = effect != null && hit == null && ball == null;
                isBuff |= action_ != null && MapleDataTool.getString("0", action_, "").equals("alert2");
                if (StringUtil.getLeftPaddedStr(String.valueOf(id / 10000), '0', 3).equals("8000")) { //�ҿ�, �� ��ų ��.
                    isBuff = true;
                } 
                if (MapleDataTool.getInt("attackCount", data, 0) > 0 || MapleDataTool.getInt("mobCount", data, 0) > 0 || MapleDataTool.getInt("damage", data, 0) > 0) { //���� ��ų.
                    isBuff = false;
                } else if (id != 15001021 && id != 20041222 && id != 20051284 && id != 32121006 && id != 37000010 && id != 37001001 && id != 37101001 && id != 37111000 && id != 37110001 && id != 37111003 && id != 61121052 && id != 65121052) { 
                    isBuff = true;
                }
            }
            
            ret.chargeskill = data.getChildByPath("keydown") != null;

            if (data.getChildByPath("level") != null) {
                int i = 1;
                for (MapleData level : data.getChildByPath("level")) {
                    ret.effects.add(SkillStatEffect.loadSkillEffectFromData(level, id, isBuff, i));
                    i++;
                }
            } else if (data.getChildByPath("common") != null) {
                int MaxLevel = MapleDataTool.getIntConvert("maxLevel", data.getChildByPath("common"));
                for (int i = 1; i <= (MaxLevel + (ret.combatOrdered ? 2 : 0)); i++) {
                    ret.effects.add(SkillStatEffect.loadSkillEffectFromData(data.getChildByPath("common"), id, isBuff, i));
                }
            }            
            if (effect != null) {
                for (final MapleData effectEntry : effect) {
                    ret.animationTime = MapleDataTool.getIntConvert("delay", effectEntry, 0);
                }
            }
        } catch (Exception ex) {
            if (ServerConstants.realese) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    @Override
    public SkillStatEffect getEffect(final int level) {
        if (effects.size() < level) {
            if (effects.size() > 0) { //incAllskill
                return effects.get(effects.size() - 1);
            }
            return null;
        } else if (level <= 0) {
            return effects.get(0);
        }
        return effects.get(level - 1);
    }

    @Override
    public boolean canCombatOrdered() {
        return combatOrdered;
    }

    @Override
    public boolean getAction() {
        return action;
    }

    @Override
    public boolean isChargeSkill() {
        return chargeskill;
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public boolean hasRequiredSkill() {
        return level > 0;
    }

    @Override
    public int getRequiredSkillLevel() {
        return level;
    }

    @Override
    public int getRequiredSkillId() {
        return requiredSkill;
    }

    @Override
    public byte getMaxLevel() {
        return (byte) effects.size();
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public int getAnimationTime() {
        return animationTime;
    }

    @Override
    public int getMasterLevel() {
        return masterLevel;
    }

    @Override
    public boolean isBeginnerSkill() {
        if (id >= 10000000 || id <= 999999) {
            if (id / 10000 == 1000 || id / 10000 == 2000 || id / 10000 == 2001 || id / 10000 == 2002 || id / 10000 == 2003 || id / 10000 == 3000 || id / 10000 == 3001 || id / 10000 == 3002 || id / 10000 == 5000 || id / 10000 == 2004 || id / 10000 == 6000 || id / 10000 == 6001) {
                return true;
            }
        }
        return false;
    }
   
    @Override
    public boolean haveMasterLevel() {
        MapleData data = MapleDataProviderFactory.getDataProvider(MapleDataProviderFactory.fileInWZPath("Skill.wz")).getData(StringUtil.getLeftPaddedStr(String.valueOf((id / 10000 == 1) ? id : 
                (id / 10000 == 8000) ? String.valueOf(id).substring(0, 6) : id / 10000), '0', 3) + ".img").getChildByPath("skill/" + StringUtil.getLeftPaddedStr(String.valueOf(id), '0', 7));
        return MapleDataTool.getInt("maxLevel", data, 0) > 20;
    }

    @Override
    public boolean canBeLearnedBy(int job) {
        int jid = job;
        int skillForJob = id / 10000;
        if (skillForJob == 2001 && GameConstants.isEvan(job)) {
            return true; //special exception for evan -.-
        }
        if (job < 1000) {
            if (jid / 100 != skillForJob / 100 && skillForJob / 100 != 0) { // wrong job
                return false;
            }
        } else {
            if (jid / 1000 != skillForJob / 1000 && skillForJob / 1000 != 0) { // wrong job
                return false;
            }
        }
        if (GameConstants.isAdventurer(skillForJob) && !GameConstants.isAdventurer(job)) {
            return false;
        } else if (GameConstants.isKOC(skillForJob) && !GameConstants.isKOC(job)) {
            return false;
        } else if (GameConstants.isAran(skillForJob) && !GameConstants.isAran(job)) {
            return false;
        } else if (GameConstants.isEvan(skillForJob) && !GameConstants.isEvan(job)) {
            return false;
        }
        if ((skillForJob / 10) % 10 > (jid / 10) % 10) { // wrong 2nd job
            return false;
        }
        if (skillForJob % 10 > jid % 10) { // wrong 3rd/4th job
            return false;
        }
        return true;
    }

    @Override
    public boolean isFourthJob() {
        if ((id / 10000) == 2312) { //all 10 skills.
            return true;
        }
        if ((getMaxLevel() <= 15 && !invisible && getMasterLevel() <= 0)) {
            return false;
        }
        if (id / 10000 >= 2212 && id / 10000 < 3000) { //evan skill
            return ((id / 10000) % 10) >= 7;
        }
        if (id / 10000 >= 430 && id / 10000 <= 434) { //db skill
            return ((id / 10000) % 10) == 4 || getMasterLevel() > 0;
        }
        return ((id / 10000) % 10) == 2 && id < 90000000 && !isBeginnerSkill();
    }
    
    @Override
    public boolean CheckMasterLevel() {
        switch (id) {
            case 1120012:
            case 1320011:
            case 4340010:
            case 4340012:
            case 5120011:
            case 5120012:
            case 5220012: 
            case 5220014:
            case 5320007:
            case 5321004:
            case 5321006:
            case 21120011:
            case 21120014:
            case 22171004:
            case 23121008:
            case 142121007:
            case 142121008:
            case 142120012:
            case 142120013:
                return false;
            case 11121000:
            case 12121000:
            case 13121000:
            case 14121000:
            case 15121000:
            case 142120000:
            case 142120003:
            case 142120009:
            case 142120010:
            case 142120011:
            case 142121004:
            case 142121005:
            case 142121006:
            case 142121016:
            case 142121030:
            case 142120033:
            case 142120036:
                return true;
        }
        return (ishyper() || haveMasterLevel());
    }
    
    @Override
    public boolean ishyper() {
        return hyper;
    }
    
    public int getSkillType() {
        return skillType;
    }
}
