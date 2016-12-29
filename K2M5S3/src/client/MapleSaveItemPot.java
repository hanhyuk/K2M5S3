package client;

import java.util.HashMap;
import java.util.Map;

public class MapleSaveItemPot { // 아래의 조건을 실행하면 모두 쿼리로 저장됨
	protected Map<Integer, MapleItempot> imphold = new HashMap<Integer, MapleItempot>();

	public void putImp(MapleItempot imp) { // 아이템팟을 해당슬롯에 추가
		imphold.put(imp.getSlot(), imp);
	}

	public MapleItempot getImpInSlot(int slot) { // 특정슬롯에 아이템팟을 불러옴
		return imphold.get(slot);
	}

	public void removeImpInSlot(int slot) { // 아이템팟을 해당슬롯에서 삭제
		imphold.remove(slot);
	}
}
