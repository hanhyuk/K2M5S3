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
	 * ��� �Ұ����� ĳ���͸� ����Ʈ�� wz ���Ͽ��� �ε��Ѵ�.
	 */
	public void loadForbiddenNames() {
		final MapleDataProvider provider = MapleDataProviderFactory.getDataProvider("Etc.wz");
		final MapleData nameData = provider.getData("ForbiddenName.img");
		
		for( MapleData data : nameData.getChildren() ) {
			forbiddenNameList.add(MapleDataTool.getString(data));
		}
	}

	/**
	 * true : ���Ұ� ĳ���� false : ��밡�� ĳ����
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