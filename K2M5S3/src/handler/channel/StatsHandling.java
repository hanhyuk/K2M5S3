package handler.channel;

import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.MapleCharacterStat;
import client.MapleClient;
import client.skills.ISkill;
import client.skills.SkillFactory;
import client.stats.PlayerStat;
import constants.GameConstants;
import packet.creators.MainPacketCreator;
import packet.transfer.read.ReadingMaple;
import tools.Pair;
import tools.Randomizer;

public class StatsHandling {

    public static final void DistributeAP(final ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {
	final List<Pair<PlayerStat, Long>> statupdate = new ArrayList<Pair<PlayerStat, Long>>(2);
	c.getSession().write(MainPacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
	final MapleCharacterStat stat = chr.getStat();
	if (chr.getRemainingAp() > 0) {
	    switch (rh.readInt()) {
		case 64: // Str
		    if (stat.getStr() >= c.getPlayer().getMaxStats()) {
			return;
		    }
		    stat.setStr(stat.getStr() + 1);
		    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.STR,(long) stat.getStr()));
		    break;
		case 128: // Dex
		    if (stat.getDex() >= c.getPlayer().getMaxStats()) {
			return;
		    }
		    stat.setDex(stat.getDex() + 1);
		    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.DEX,(long)  stat.getDex()));
		    break;
		case 256: // Int
		    if (stat.getInt() >= c.getPlayer().getMaxStats()) {
			return;
		    }
		    stat.setInt(stat.getInt() + 1);
		    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.INT,(long)  stat.getInt()));
		    break;
		case 512: // Luk
		    if (stat.getLuk() >= c.getPlayer().getMaxStats()) {
			return;
		    }
		    stat.setLuk(stat.getLuk() + 1);
		    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.LUK,(long)  stat.getLuk()));
		    break;
		case 2048: // HP
		    int MaxHP = stat.getMaxHp();
		    if (chr.getHpApUsed() >= 10000 || MaxHP >= 500000) {
			return;
		    }
		    ISkill improvingMaxHP = null;
		    int improvingMaxHPLevel = 0;
		    if (chr.getJob() == 0) { // Beginner
			MaxHP += Randomizer.rand(8, 12);
		    } else if (chr.getJob() >= 100 && chr.getJob() <= 132) { // Warrior
			improvingMaxHP = SkillFactory.getSkill(1000001);
			improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
			MaxHP += Randomizer.rand(20, 24);
			if (improvingMaxHPLevel >= 1) {
			    MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
			}
		    } else if (chr.getJob() >= 200 && chr.getJob() <= 232) { // Magician
			MaxHP += Randomizer.rand(6, 10);
		    } else if (chr.getJob() >= 300 && chr.getJob() <= 322) { // Bowman
			MaxHP += Randomizer.rand(16, 20);
		    } else if (chr.getJob() >= 400 && chr.getJob() <= 422) { // Thief
			MaxHP += Randomizer.rand(20, 24);
		    } else if (chr.getJob() >= 500 && chr.getJob() <= 522) { // Pirate
			improvingMaxHP = SkillFactory.getSkill(5100000);
			improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
			MaxHP += Randomizer.rand(16, 20);
			if (improvingMaxHPLevel >= 1) {
			    MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
			}
		    } else if (chr.getJob() >= 1100 && chr.getJob() <= 1111) { // Soul Master
			improvingMaxHP = SkillFactory.getSkill(11000000);
			improvingMaxHPLevel = chr.getSkillLevel(improvingMaxHP);
			MaxHP += Randomizer.rand(36, 42);
			if (improvingMaxHPLevel >= 1) {
			    MaxHP += improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
			}
		    } else if (chr.getJob() >= 1200 && chr.getJob() <= 1211) { // Flame Wizard
			MaxHP += Randomizer.rand(15, 21);
		    } else if ((chr.getJob() >= 1300 && chr.getJob() <= 1311) || (chr.getJob() >= 1400 && chr.getJob() <= 1411)) { // Wind Breaker and Night Walker
                        MaxHP += Randomizer.rand(30, 36);
                    } else if (chr.getJob() == 3101 || (chr.getJob() >= 3120 && chr.getJob() <= 3122)) {
			MaxHP += Randomizer.rand(30, 36);
		    } else { // GameMaster
			MaxHP += Randomizer.rand(50, 100);
		    }
                    MaxHP = Math.min(500000, MaxHP);
                    chr.setHpApUsed(chr.getHpApUsed() + 1);
                    stat.setMaxHp(MaxHP);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.MAXHP, (long) MaxHP));
                    break;
		case 8192: // MP
		    int MaxMP = stat.getMaxMp();
		    if (chr.getMpApUsed() >= 10000 && stat.getMaxMp() >= 500000) {
			return;
		    }
		    if (chr.getJob() == 0) { // Beginner
			MaxMP += Randomizer.rand(6, 8);
		    } else if (chr.getJob() >= 100 && chr.getJob() <= 132) { // Warrior
			MaxMP += Randomizer.rand(2, 4);
		    } else if (chr.getJob() >= 200 && chr.getJob() <= 232) { // Magician
			ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
			int improvingMaxMPLevel = chr.getSkillLevel(improvingMaxMP);
			if (improvingMaxMPLevel >= 1) {
			    MaxMP += Randomizer.rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
			} else {
			    MaxMP += Randomizer.rand(18, 20);
			}
		    } else if (chr.getJob() >= 300 && chr.getJob() <= 322) { // Bowman
			MaxMP += Randomizer.rand(10, 12);
		    } else if (chr.getJob() >= 400 && chr.getJob() <= 422) { // Thief
			MaxMP += Randomizer.rand(10, 12);
		    } else if (chr.getJob() >= 500 && chr.getJob() <= 522) { // Pirate
			MaxMP += Randomizer.rand(10, 12);
		    } else if (chr.getJob() >= 1100 && chr.getJob() <= 1111) { // Soul Master
			MaxMP += Randomizer.rand(6, 9);
		    } else if (chr.getJob() >= 1200 && chr.getJob() <= 1211) { // Flame Wizard
			ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
			int improvingMaxMPLevel = chr.getSkillLevel(improvingMaxMP);
			MaxMP += Randomizer.rand(33, 36);
			if (improvingMaxMPLevel >= 1) {
			    MaxMP += improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
			}
		    } else if ((chr.getJob() >= 1300 && chr.getJob() <= 1311) || (chr.getJob() >= 1400 && chr.getJob() <= 1411)) { // Wind Breaker and Night Walker
			MaxMP += Randomizer.rand(21, 24);
		    } else { // GameMaster
			MaxMP += Randomizer.rand(50, 100);
		    }
		    MaxMP = Math.min(500000, MaxMP);
		    chr.setMpApUsed(chr.getMpApUsed() + 1);
		    stat.setMaxMp(MaxMP);
		    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.MAXMP,(long)  MaxMP));
		    break;
		default:
		    c.getSession().write(MainPacketCreator.resetActions());
		    return;
	    }
	    chr.setRemainingAp(chr.getRemainingAp() - 1);
	    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.AVAILABLEAP,  (long) chr.getRemainingAp()));
	    c.getSession().write(MainPacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
            chr.getStat().recalcLocalStats();
            if (GameConstants.isDemonAvenger(chr.getJob())) {
                chr.getStat().giveDemonWatk(chr);
            }
	}
    }
    
    public static final void DistributeHyperSp(ReadingMaple rh, final int skillid, final MapleCharacter chr) {
        ISkill skill = SkillFactory.getSkill(skillid); 
        final int curLevel = chr.getOriginSkillLevel(skill);
        /*if (chr.getHyperSP() < curLevel) {
            chr.Message(2, "다음 레벨업 까지 필요한\r\n하이퍼 스탯 포인트가 부족합니다.");
            return;
        }
        chr.gainHyperSp(-1);*/
        chr.updateSingleStat(PlayerStat.AVAILABLE_HYPERSP, 149642);
        chr.changeSkillLevel(skill, (byte) (curLevel + 1), (byte) chr.getMasterLevel(skill));
        if (chr.getJob() == 132) {
            chr.checkBerserk();
        }
        chr.saveToDB(false, false);
        chr.checkInduerense();
        chr.checkMercedesRecovery();
        chr.setNullSelfRecovery();
        chr.checkSelfRecovery();
    }
    
    
    public static final void DistributeSP(ReadingMaple rh, final int skillid, final MapleClient c, final MapleCharacter chr) {
	final ISkill skill = SkillFactory.getSkill(skillid);
	if (skill.hasRequiredSkill()) {
	    if (chr.getOriginSkillLevel(SkillFactory.getSkill(skill.getRequiredSkillId())) < skill.getRequiredSkillLevel()) {
                return;
	    }
	}
	final int curLevel = chr.getOriginSkillLevel(skill);
        final int addskilllevel = rh.readInt();
        chr.gainSP(-addskilllevel);
	chr.updateSingleStat(PlayerStat.AVAILABLESP, chr.getRemainingSp());
        chr.send(MainPacketCreator.updateSp(chr, false));
        chr.changeSkillLevel(skill, (byte) (curLevel + addskilllevel), chr.getMasterLevel(skill));
        if (chr.getJob() == 132) { // 다크나이트
            chr.checkBerserk();
        }
        chr.saveToDB(false, false);
        chr.checkInduerense();
        chr.checkMercedesRecovery();
        chr.setNullSelfRecovery();
        chr.checkSelfRecovery();
    }

    public static final void AutoAssignAP(final ReadingMaple rh, final MapleClient c, final MapleCharacter chr) {
        final int count = rh.readInt();
        final int PrimaryStat = rh.readInt();
        final int amount = rh.readInt();
        final int SecondaryStat = count == 2 ? rh.readInt() : 0;
        final int amount2 = count == 2 ? rh.readInt() : 0;
        
        final MapleCharacterStat playerst = chr.getStat();

        List<Pair<PlayerStat, Long>> statupdate = new ArrayList<Pair<PlayerStat, Long>>(2);
        c.getSession().write(MainPacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
        if (chr.getRemainingAp() == amount + amount2 || GameConstants.isXenon(chr.getJob())) {
            switch (PrimaryStat) {
                case 64: // Str
                    if (playerst.getStr() + amount > 999) {
                        return;
                    }
                    playerst.setStr(playerst.getStr() + amount);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.STR,(long)  playerst.getStr()));
                    break;
                case 128: // Dex
                    if (playerst.getDex() + amount > 999) {
                        return;
                    }
                    playerst.setDex(playerst.getDex() + amount);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.DEX,(long)  playerst.getDex()));
                    break;
                case 256: // Int
                    if (playerst.getInt() + amount > 999) {
                        return;
                    }
                    playerst.setInt(playerst.getInt() + amount);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.INT,(long)  playerst.getInt()));
                    break;
                case 512: // Luk
                    if (playerst.getLuk() + amount > 999) {
                        return;
                    }
                    playerst.setLuk(playerst.getLuk() + amount);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.LUK,(long)  playerst.getLuk()));
                    break;
                case 2048: //Max Hp
                    if (playerst.getMaxHp() + (amount * 30) > 500000) {
                        return;
                    }
                    playerst.setMaxHp(playerst.getMaxHp() + (amount * 30));
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.MAXHP,(long)  playerst.getMaxHp()));
                    break;
                default:
                    c.getSession().write(MainPacketCreator.resetActions());
                    return;
            }
            switch (SecondaryStat) {
                case 64: // Str
                    if (playerst.getStr() + amount2 > 999) {
                        return;
                    }
                    playerst.setStr(playerst.getStr() + amount2);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.STR,(long)  playerst.getStr()));
                    break;
                case 128: // Dex
                    if (playerst.getDex() + amount2 > 999) {
                        return;
                    }
                    playerst.setDex(playerst.getDex() + amount2);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.DEX,(long)  playerst.getDex()));
                    break;
                case 256: // Int
                    if (playerst.getInt() + amount2 > 999) {
                        return;
                    }
                    playerst.setInt(playerst.getInt() + amount2);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.INT,(long)  playerst.getInt()));
                    break;
                case 512: // Luk
                    if (playerst.getLuk() + amount2 > 999) {
                        return;
                    }
                    playerst.setLuk(playerst.getLuk() + amount2);
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.LUK,(long)  playerst.getLuk()));
                    break;
                case 2048: //Max Hp
                    if (playerst.getMaxHp() + (amount * 30) > 500000) {
                        return;
                    }
                    playerst.setMaxHp(playerst.getMaxHp() + (amount * 30));
                    statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.MAXHP,(long)  playerst.getMaxHp()));
                    break;  
            }
            chr.setRemainingAp(chr.getRemainingAp() - (amount + amount2));
            statupdate.add(new Pair<PlayerStat, Long>(PlayerStat.AVAILABLEAP, (long) chr.getRemainingAp()));
            c.getSession().write(MainPacketCreator.updatePlayerStats(statupdate, true, chr.getJob()));
            chr.getStat().recalcLocalStats();
            if (GameConstants.isDemonAvenger(chr.getJob())) chr.getStat().giveDemonWatk(chr);
        }
    }
}
