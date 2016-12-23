/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package handler.channel;

import constants.GameConstants;
import client.MapleCharacter;
import client.skills.ISkill;
import client.skills.SkillFactory;
import client.skills.SkillStatEffect;
import tools.AttackPair;
import java.awt.Point;
import java.util.List;

public class AttackInfo {

    public byte skillLevel, animation, tbyte, speed, AOE, csstar, hits, targets, slot, unk;
    public short display, value;
    public int skill, charge, lastAttackTickCount;
    public Point position;
    public List<AttackPair> allDamage;
    public final SkillStatEffect getAttackEffect(final MapleCharacter chr, int skillLevel, final ISkill skill_) {
	if (skillLevel == 0) {
	    return null;
	}
	if (GameConstants.isLinkedAttackSkill(skill)) {
	    final ISkill skillLink = SkillFactory.getSkill(skill);
	    return skillLink.getEffect(skillLevel);
	}
	return skill_.getEffect(skillLevel);
    }
}
