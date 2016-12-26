package launch.helpers;

import java.util.ArrayList;
import java.util.List;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class MapleLoginHelper {

	private static MapleLoginHelper instance = new MapleLoginHelper();
	private List<String> forbiddenNameList = new ArrayList<String>();

	public static MapleLoginHelper getInstance() {
		return instance;
	}
	
	/**
	 * 사용 불가능한 캐릭터명 리스트를 wz 파일에서 로딩한다.
	 */
	public void loadForbiddenNames() {
		final MapleDataProvider provider = MapleDataProviderFactory.getDataProvider("Etc.wz");
		final MapleData nameData = provider.getData("ForbiddenName.img");
		
		for( MapleData data : nameData.getChildren() ) {
			forbiddenNameList.add(MapleDataTool.getString(data));
		}
	}

	/**
	 * true : 사용불가 캐릭명 false : 사용가능 캐릭명
	 */
	public boolean isForbiddenName(final String in) {
		for (final String name : forbiddenNameList) {
			if (in.contains(name)) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getForbiddenNameList() {
		return forbiddenNameList;
	}
}