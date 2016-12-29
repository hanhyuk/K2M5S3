package client.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import provider.MapleData;
import provider.MapleDataTool;
import tools.StringUtil;

public class SkillFactory {

	public static final Map<Integer, ISkill> skills = new HashMap<>();
	public static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<>();
	public static ReentrantLock lock = new ReentrantLock();

	public static ISkill getSkill(final int id) {
		if (!skills.isEmpty()) {
			return skills.get(id);
		}
		return null;
	}

	public static String getSkillName(final int id, final MapleData stringData) {
		if (id == 0) {
			return "∆Ú≈∏";
		}
		String strId = Integer.toString(id);
		strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
		MapleData skillroot = stringData.getChildByPath(strId);
		if (skillroot != null) {
			return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
		}
		return "";
	}

	public static String getSkillName(final int id) {
		ISkill skill = getSkill(id);
		if (skill != null) {
			return skill.getName();
		}
		return null;
	}

	public static final SummonSkillEntry getSummonData(final int skillid) {
		return SummonSkillInformation.get(skillid);
	}
}
