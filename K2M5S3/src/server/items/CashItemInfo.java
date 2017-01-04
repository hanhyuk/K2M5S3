package server.items;

/**
 * ĳ���� �������� ĳ���Ҷ� �� Ŭ������ ����Ѵ�.
 * �׸��� ��� �������� ���� Etc.wz/Commodity.img.xml ���Ͽ� �ִ� �͵� �� �Ϻθ��� ���ǵǾ� �ִ�.
 * ���� �߰����� �׸��� �ʿ��ϴٸ� �����Ͽ� �ۼ��ϸ� �ȴ�.
 */
public class CashItemInfo {
	private int itemId, count, price, period;

	public CashItemInfo(int itemId, int count, int price, int period) {
		this.itemId = itemId;
		this.count = count;
		this.price = price;
		this.period = period;
	}

	public int getId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	public int getPeriod() {
		return period;
	}

	public int getPrice() {
		return price;
	}
}