package server.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class CashItemFactory {
	private static CashItemFactory instance = new CashItemFactory();
	/**
	 * 캐시 아이템 정보가 들어 있다.(Commodity.img.xml)
	 */
	public Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
	public MapleDataProvider itemWzInPet = MapleDataProviderFactory.getDataProvider("Item.wz/Pet");
	public MapleDataProvider etcWz = MapleDataProviderFactory.getDataProvider("Etc.wz");

	public static CashItemFactory getInstance() {
		return instance;
	}

	/**
	 * ItemId 값이 0 이상이면 해당 정보 itemStats에 캐싱한다.
	 */
	public CashItemFactory() {
		for (MapleData field : etcWz.getData("Commodity.img").getChildren()) {
			int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);

			boolean onSale = itemId > 0;

			int period = 0; // 기간
			if (itemId >= 5000000 && itemId <= 5002000) { // 아이템이 펫 인 경우 life 값을
															// 기간으로 설정.
				period = MapleDataTool.getIntConvert("life", itemWzInPet.getData(itemId + ".img").getChildByPath("info"));
			} else {
				period = MapleDataTool.getIntConvert("Period", field, 0);
			}
			if (onSale) {
				final CashItemInfo stats = new CashItemInfo(MapleDataTool.getIntConvert("ItemId", field), MapleDataTool.getIntConvert("Count", field, 1), MapleDataTool.getIntConvert("Price", field, 0), period);
				itemStats.put(MapleDataTool.getIntConvert("SN", field, 0), stats);
			}
		}
	}

	/**
	 * 아이템 아이디에 해당하는 캐시템 정보를 반환한다.
	 * 
	 * @param id
	 * @return
	 */
	public CashItemInfo getItemInfoFromItemId(int id) {
		for (CashItemInfo cii : itemStats.values()) {
			if (cii.getId() == id) {
				return cii;
			}
		}
		return null;
	}

	/**
	 * 아이템 고유번호에 해당하는 캐시템 정보를 반환한다.
	 * 
	 * @param sn
	 * @return
	 */
	public CashItemInfo getItemInfoFromSN(int sn) {
		CashItemInfo stats = itemStats.get(sn);
		if (stats == null) {
			return null;
		}
		return stats;
	}

	/**
	 * 패키지로 구성된 아이템 아이디를 받아서, 그 내용물에 해당하는 아이템 정보를 구성후 반환한다.
	 * 
	 * @param itemId
	 * @return
	 */
	public List<Pair<Integer, CashItemInfo>> getPackageItemInfoList(int itemId) {
		List<Pair<Integer, CashItemInfo>> ret = new ArrayList<Pair<Integer, CashItemInfo>>();
		MapleData b = etcWz.getData("CashPackage.img").getChildByPath(Integer.toString(itemId)).getChildByPath("SN");
		for (MapleData c : b.getChildren()) {
			int sn = MapleDataTool.getIntConvert(c.getName(), b);
			ret.add(new Pair(sn, getItemInfoFromSN(sn)));
		}

		return ret;
	}
}