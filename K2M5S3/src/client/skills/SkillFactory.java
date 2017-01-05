package client.skills;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import launch.helpers.MapleCacheData;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.items.MapleProfessionRecipe;
import server.items.MapleProfessionRecipeEntry;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;

public class SkillFactory {

	public static final Map<Integer, ISkill> skills = new HashMap<>();
	public static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap<>();
	public static ReentrantLock lock = new ReentrantLock();
	
	public static void cacheSkillData() {
		final MapleData stringWzInSkillImg = MapleDataProviderFactory.getDataProvider("String.wz").getData("Skill.img");
		final MapleDataProvider skillWz = MapleDataProviderFactory.getDataProvider("Skill.wz");
		final MapleDataDirectoryEntry skillWzRoot = skillWz.getRoot();

		int skillid = 0;
		MapleData summonData;
		SummonSkillEntry sse;
		
		for (MapleDataFileEntry topDir : skillWzRoot.getFiles()) {
			if (topDir.getName().length() <= 10) {
				for (MapleData data : skillWz.getData(topDir.getName())) {
					if (data.getName().equals("skill")) {
						for (MapleData S_data : data) {
							if (S_data != null) {
								skillid = Integer.parseInt(S_data.getName());
								ISkill skill = Skill.loadFromData(skillid, S_data);
								skill.setName(SkillFactory.getSkillName(skillid, stringWzInSkillImg));
								SkillFactory.skills.put(skillid, skill);
							}
							summonData = S_data.getChildByPath("summon/attack1/info");
							if (summonData == null) {
								MapleData summonData2 = S_data.getChildByPath("summon/die/info"); // 자폭스킬
																									// (마인
																									// 등)
								if (summonData2 != null) {
									summonData = summonData2;
								}
							}
							if (summonData != null) {
								sse = new SummonSkillEntry();
								sse.type = (byte) MapleDataTool.getInt("type", summonData, 0);
								sse.mobCount = (byte) (skillid == 33101008 ? 3 : MapleDataTool.getInt("mobCount", summonData, 1));
								sse.attackCount = (byte) MapleDataTool.getInt("attackCount", summonData, 1);
								if (summonData.getChildByPath("range/lt") != null) {
									final MapleData ltd = summonData.getChildByPath("range/lt");
									sse.lt = (Point) ltd.getData();
									sse.rb = (Point) summonData.getChildByPath("range/rb").getData();
								} else {
									sse.lt = new Point(-100, -100);
									sse.rb = new Point(100, 100);
								}
								sse.delay = MapleDataTool.getInt("effectAfter", summonData, 0) + MapleDataTool.getInt("attackAfter", summonData, 0);
								for (MapleData effect : summonData) {
									if (effect.getChildren().size() > 0) {
										for (final MapleData effectEntry : effect) {
											sse.delay += MapleDataTool.getIntConvert("delay", effectEntry, 0);
										}
									}
								}
								MapleData aa = S_data.getChildByPath("summon/attack1");
								if (S_data.getChildByPath("summon/die/info") != null) { // 자폭스킬
																						// (마인
																						// 등)
									aa = S_data.getChildByPath("summon/die");
								}
								for (MapleData effect : aa) {
									sse.delay += MapleDataTool.getIntConvert("delay", effect, 0);
								}
								SummonSkillInformation.put(skillid, sse);
							}
						}
					}
				}
			} else if (topDir.getName().startsWith("Recipe_")) {
				for (MapleData data : skillWz.getData(topDir.getName())) {
					skillid = Integer.parseInt(data.getName());
					MapleProfessionRecipeEntry entry = new MapleProfessionRecipeEntry(MapleDataTool.getInt("reqSkillLevel", data, 0), MapleDataTool.getInt("reqSkillProficiency", data, 0), MapleDataTool.getInt("incSkillProficiency", data, 0), MapleDataTool.getInt("incFatigability", data, 0), MapleDataTool.getInt("needOpenItem", data, 0), MapleDataTool.getInt("period", data, -1));
					for (MapleData targetData : data.getChildByPath("target")) {
						entry.target.add(new Triple<Integer, Integer, Integer>(MapleDataTool.getInt("item", targetData, 0), MapleDataTool.getInt("count", targetData, 0), MapleDataTool.getInt("probWeight", targetData, 100)));
					}
					for (MapleData recipeData : data.getChildByPath("recipe")) {
						entry.recipe.add(new Pair<Integer, Integer>(MapleDataTool.getInt("item", recipeData, 0), MapleDataTool.getInt("count", recipeData, 0)));
					}
					MapleProfessionRecipe.getInstance().recipes.put(skillid, entry);
				}
			}
		}
	}
	
	
	public static ISkill getSkill(final int id) {
		if (!skills.isEmpty()) {
			return skills.get(id);
		}
		return null;
	}

	public static String getSkillName(final int id, final MapleData stringData) {
		if (id == 0) {
			return "평타";
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
