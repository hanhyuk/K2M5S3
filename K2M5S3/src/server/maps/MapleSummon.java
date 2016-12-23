/*
 Maple Team ProJect
 Á¦ÀÛ ¿ëµµ : ¼­¹ö¿î¿µ¿ë
 ÆÀ¿ø¸ñ·Ï
 * °­µ¿¿ø dongwon1852@nate.com 
 * ±è¼±ÀÏ fileupload@nate.com
 * ±è¼®Çö azxcs3@nate.com
 * ±èÁø¼º m0nday_s@nate.com
 * °øÁØÇù kkong1001@nate.com
 * ±è¹ÎÈ£ rubystory0603@nate.com
 * ÀÌÀç¿Õ ejwj5592@nate.com
 * ÃÖ¿ëÀç virgo_s_t@nate.com
 * ¼­¼º´ö abq1239@nate.com
 */


package server.maps;

import constants.GameConstants;
import client.MapleClient;
import client.MapleCharacter;
import client.skills.SkillFactory;
import client.skills.SkillStatEffect;
import packet.creators.MainPacketCreator;
import tools.Timer.ShowTimer;
import java.awt.Point;
import java.lang.ref.WeakReference;

public class MapleSummon extends AnimatedHinaMapObjectExtend {
    private final MapleCharacter owner;
    private final WeakReference<MapleCharacter> ownerchr;
    private final int skillLevel;
    private final int skill;
    private int hp;
    private int maelstromid;
    private SummonMovementType movementType;
    
    public MapleSummon(final MapleCharacter owner, final SkillStatEffect skill, final Point pos, final SummonMovementType movementType) {
        this(owner, skill.getSourceId(), skill.getLevel(), pos, movementType);
    }

    public MapleSummon(final MapleCharacter owner, final int skill, final Point pos, final SummonMovementType movementType) {
	super();
	this.owner = owner;
        this.ownerchr = new WeakReference<MapleCharacter>(owner);
	this.skill = skill;
        this.movementType = movementType;
	this.skillLevel = owner.getSummonLinkSkillLevel(SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(skill)));
	if (skillLevel == 0) {
	    return;
	}
	setPosition(pos);
    }
    
    public MapleSummon(MapleCharacter owner, int skill, int duration, Point pos, SummonMovementType movementType) {
        this.owner = owner;
        this.ownerchr = new WeakReference<MapleCharacter>(owner);
        this.skill = skill;
        int lkk = 0;
        if (owner.getSkillLevel(GameConstants.getLinkedAttackSkill(skill)) > 0) {
            lkk = owner.getSkillLevel(GameConstants.getLinkedAttackSkill(skill));
        }
        this.skillLevel = (byte) lkk;
        this.movementType = movementType;
        setPosition(pos);
    }

    @Override
    public final void sendSpawnData(final MapleClient client) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                 if (!isRemovableSummon()) {
                client.getSession().write(MainPacketCreator.spawnSummon(MapleSummon.this, skillLevel, 0, false));          
                 }
            }
        };
	ShowTimer.getInstance().schedule(r, 2000);
    }

    @Override
    public final void sendDestroyData(final MapleClient client) {
	client.getSession().write(MainPacketCreator.removeSummon(this, true));
    }

    public final int getSkill() {
	return skill;
    }

    public final int getHP() {
	return hp;
    }

    public final void addHP(final int delta) {
	this.hp += delta;
    }
    
    public final MapleCharacter getOwnerChr() {
        return ownerchr.get();
    }

    public final SummonMovementType getMovementType() {
	return movementType;
    }
    
    public final boolean isStaticSummon() {
        return SkillFactory.getSkill(getSkill()).getEffect(1).isStaticSummon();
    }

    public final boolean isSummon() {
	switch (skill) {
	    case 12111004:
            case 1301013:
            case 1311014:
	    case 2321003:
	    case 2121005:
	    case 2221005:
            case 2211011:
	    case 5211001: 
	    case 5211002:
	    case 5220002:
            case 4341006: 
            case 6111100:
            case 3221014:
	    case 13111004:
	    case 11001004:
	    case 12001004:
	    case 13001004:
	    case 14001005:
            case 35111005:
            case 35111011:
            case 15001004:
            case 22171081:
            case 35120002:
            case 35121011:
            case 35121009: 
            case 35121010:
            case 14000027: 
		return true;
	}
	return false;
    }
       
    public final boolean isRemovableSummon() {
        switch (skill) {
            case 35111002:
                return true;
        }
        return false;
    }
    
    public final int getSummonType() {
        if (GameConstants.isAngel(skill)) {
            return 2; //buffs and stuff
        } else {
            switch (skill) {
                case 35120002:
                case 35121010:
                case 14000027: 
                case 14111024:
                    return 0;
                case 1301013:
                case 36121014:
                    return 2; //buffs and stuff
                case 35111001:
                case 35111009:
                case 35111010:
                case 23111008:
                case 23111009:
                case 23111010:
                case 36121002:
                case 36121013: 
                    return 3; //attacks what you attack
                case 35121009: 
                    return 5; //sub summons
                case 35121003:
                    return 6; //charge
                case 33101010:
                case 33001011:
                    return 10;
            }
        }
        return 1;
    }
    
    public final boolean isGaviota() {
        return skill == 5211002;
    }

     public final int getSkillLevel() {
	return skillLevel;
    }

    @Override
    public final MapleMapObjectType getType() {
	return MapleMapObjectType.SUMMON;
    }
    
    public final MapleCharacter getOwner() {
	return owner;
    }
    
    public final int getMaelstromId() {
        return maelstromid;
    }
    
    public final void setMaelstromId(int maelstromid) {
        this.maelstromid = maelstromid;
    }
}
