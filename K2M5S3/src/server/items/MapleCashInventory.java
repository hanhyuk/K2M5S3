package server.items;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import client.items.IItem;
import client.items.ItemFactory;
import constants.GameConstants;
import packet.creators.CashPacket;

public class MapleCashInventory {
	private static final Logger logger = LoggerFactory.getLogger(MapleCashInventory.class);

	private List<IItem> inventory = new ArrayList<IItem>();
	private int accid;

	public MapleCashInventory(int accid) {
		this.accid = accid;
	}

	public List<IItem> getInventory() {
		return inventory;
	}

	public int getAccId() {
		return accid;
	}

	public void addItem(IItem item) {
		inventory.add(item);
	}

	public IItem findByCashId(int id) {
		for (IItem item : inventory) {
			if (item.getUniqueId() == id) {
				return item;
			}
		}
		return null;
	}

	public void removeItemByCashId(int id) {
		int index = -1;
		for (IItem item : inventory) {
			index++;
			if (item.getUniqueId() == id) {
				break;
			}
		}
		if (index != -1) {
			inventory.remove(index);
		} else {
			logger.debug("[오류] 캐시아이템을 발견하지 못해 삭제하지 못했습니다.");
		}
	}

	public void loadFromDB() {
		ItemFactory.loadItemsFromCashShopInventory(this);
	}

	public void saveToDB() {
		ItemFactory.saveItemsFromCashShop(this);
	}

	public void removeFromInventory(IItem item) {
		inventory.remove(item);
	}

	public void checkExpire(MapleClient c) {
		List<IItem> toberemove = new ArrayList<IItem>();
		for (IItem item : inventory) {
			if (item != null && !GameConstants.isPet(item.getItemId()) && item.getExpiration() > 0 && item.getExpiration() < System.currentTimeMillis()) {
				toberemove.add(item);
			}
		}
		if (toberemove.size() > 0) {
			for (IItem item : toberemove) {
				removeFromInventory(item);
				c.getSession().write(CashPacket.itemExpired(item.getUniqueId()));
			}
			toberemove.clear();
		}
	}
}