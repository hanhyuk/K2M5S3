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
	 * ĳ�� ������ ������ ��� �ִ�.(Commodity.img.xml)
	 */
	public Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
	public MapleDataProvider itemWzInPet = MapleDataProviderFactory.getDataProvider("Item.wz/Pet");
	public MapleDataProvider etcWz = MapleDataProviderFactory.getDataProvider("Etc.wz");

	public static CashItemFactory getInstance() {
		return instance;
	}

	/**
	 * ItemId ���� 0 �̻��̸� �ش� ���� itemStats�� ĳ���Ѵ�.
	 */
	public CashItemFactory() {
		for (MapleData field : etcWz.getData("Commodity.img").getChildren()) {
			int itemId = MapleDataTool.getIntConvert("ItemId", field, 0);

			boolean onSale = itemId > 0;

			int period = 0; // �Ⱓ
			if (itemId >= 5000000 && itemId <= 5002000) { // �������� �� �� ��� life ����
															// �Ⱓ���� ����.
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
	 * ������ ���̵� �ش��ϴ� ĳ���� ������ ��ȯ�Ѵ�.
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
	 * ������ ������ȣ�� �ش��ϴ� ĳ���� ������ ��ȯ�Ѵ�.
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
	 * ��Ű���� ������ ������ ���̵� �޾Ƽ�, �� ���빰�� �ش��ϴ� ������ ������ ������ ��ȯ�Ѵ�.
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