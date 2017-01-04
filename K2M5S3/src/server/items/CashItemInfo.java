package server.items;

/**
 * 캐시템 정보들을 캐싱할때 이 클래스를 사용한다.
 * 그리고 멤버 변수들을 보면 Etc.wz/Commodity.img.xml 파일에 있는 것들 중 일부만이 정의되어 있다.
 * 만약 추가적인 항목이 필요하다면 참고하여 작성하면 된다.
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