package launch.world;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.items.ItemFactory;
import database.MYSQL;
import handler.channel.AuctionHandler.AuctionInfo;
import handler.channel.AuctionHandler.AuctionItemPackage;

public class WorldAuction {
	private static final WorldAuction instance = new WorldAuction();
	
	public List<AuctionItemPackage> items = new ArrayList<>();
	public Map<Integer, List<AuctionInfo>> auctions = new HashMap<>();

	private WorldAuction() {}
	public static WorldAuction getInstance() {
		return instance;
	}
	
	public void cachingAuctionInfo() {
		try {
			ItemFactory.loadItems(null, ItemFactory.InventoryType.AUCTION, null, null, null);
			try (PreparedStatement ps = MYSQL.getConnection().prepareStatement("SELECT * FROM `auctions`")) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						addAuction(rs.getInt("characterid"), rs.getLong("bid"), rs.getInt("inventoryid"), rs.getByte("status"));
					}
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void addAuction(final int cid, final long bid, final int iid, final byte status) {
		if (auctions.get(iid) == null) {
			auctions.put(iid, new ArrayList<AuctionInfo>());
		}
		boolean isBest = true, isExist = false;
		for (AuctionInfo ai : auctions.get(iid)) {
			if (ai.getCharacterId() == cid) {
				isExist = true;
				auctions.get(iid).set(auctions.get(iid).indexOf(ai), new AuctionInfo(bid, cid, status));
			}
			if (bid < ai.getBid()) {
				isBest = false;
			}
		}
		if (!isExist) {
			auctions.get(iid).add(new AuctionInfo(bid, cid, status));
		}
		if (isBest) {
			findByIid(iid).setBid(bid);
		}
	}

	public long getBidById(final int cid, final int iid) {
		long bid = 0;
		for (AuctionInfo ai : auctions.get(iid)) {
			if (ai.getCharacterId() == cid && ai.getBid() >= bid) {
				bid = ai.getBid();
			}
		}
		return bid;
	}

	public List<AuctionItemPackage> getItems() {
		List<AuctionItemPackage> items_ = new ArrayList<>();
		for (AuctionItemPackage aitem : items) {
			if (aitem.getBuyer() == 999999 || aitem.getBuyer() == 0) {
				items_.add(aitem);
			}
		}
		return items_;
	}

	public List<AuctionItemPackage> getCompleteItems(final int charid) {
		List<AuctionItemPackage> items_ = new ArrayList<>();
		for (AuctionItemPackage aitem : items) {
			if (aitem.getOwnerId() == charid || aitem.getBuyer() == charid || getBidById(charid, (int) aitem.getItem().getInventoryId()) > 0) {
				items_.add(aitem);
			}
		}
		return items_;
	}

	public void addItem(final AuctionItemPackage aitem) {
		aitem.getItem().setInventoryId(items.size() + 1);
		items.add(aitem);
	}

	public AuctionItemPackage findByIid(final int id) {
		for (AuctionItemPackage item : items) {
			if (item.getItem().getInventoryId() == id) {
				return item;
			}
		}
		return null;
	}

	public void save() {
		try {
			final Connection con = MYSQL.getConnection();
			ItemFactory.saveItemsFromAuction(items);
			PreparedStatement ps = con.prepareStatement("INSERT INTO auctions (`inventoryid`, `characterid`, `bid`, `status`) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			for (Map.Entry<Integer, List<AuctionInfo>> au : auctions.entrySet()) {
				ps.setInt(1, au.getKey());
				for (AuctionInfo ai : au.getValue()) {
					ps.setInt(2, ai.getCharacterId());
					ps.setLong(3, ai.getBid());
					ps.setByte(4, ai.getStatus());
					ps.executeUpdate();
				}
			}
			ps.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
