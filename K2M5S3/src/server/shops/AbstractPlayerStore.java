package server.shops;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleCharacter;
import client.items.IItem;
import client.items.ItemFactory;
import constants.GameConstants;
import constants.ServerConstants;
import database.MYSQL;
import launch.ChannelServer;
import packet.creators.PlayerShopPacket;
import packet.transfer.write.Packet;
import server.maps.AbstractHinaMapObject;
import server.maps.MapleMap;
import tools.Pair;

public abstract class AbstractPlayerStore extends AbstractHinaMapObject implements IMapleCharacterShop {
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlayerStore.class);

	private boolean open;
	private String ownerName, des;
	private MapleCharacter chr = null;
	private int ownerId, owneraccount, itemId, channel, map;
	private AtomicInteger meso = new AtomicInteger(0);
	protected WeakReference<MapleCharacter> chrs[];
	protected List<MapleCharacterShopItem> items = new LinkedList<MapleCharacterShopItem>();
	private List<String> visitors = new LinkedList<String>();
	protected List<BoughtItem> bought = new LinkedList<BoughtItem>();
	protected List<Pair<String, Byte>> messages = new LinkedList<Pair<String, Byte>>();

	protected String password = "";

	public AbstractPlayerStore(MapleCharacter owner, int ownerId, int ownerAccId, Point pos, int itemId, String desc, int slots) {

		this.setPosition(pos);
		this.ownerName = owner.getName();
		this.chr = owner;
		this.ownerId = ownerId;
		this.owneraccount = ownerAccId;
		this.itemId = itemId;
		this.des = desc;
		this.open = false;
		this.map = owner.getMapId();
		this.channel = owner.getClient().getChannel();
		chrs = new WeakReference[slots];
		for (int i = 0; i < chrs.length; i++) {
			chrs[i] = new WeakReference<MapleCharacter>(null);
		}
	}

	@Override
	public void broadcastToVisitors(Packet packet) {
		broadcastToVisitors(packet, true);
	}

	public void broadcastToVisitors(Packet packet, boolean owner) {
		for (WeakReference<MapleCharacter> chr : chrs) {
			if (chr != null && chr.get() != null) {
				chr.get().getClient().getSession().write(packet);
			}
		}
		if (getShopType() != IMapleCharacterShop.HIRED_MERCHANT && owner && ((MapleCharacterShop) this).getMCOwner() != null) {
			((MapleCharacterShop) this).getMCOwner().getClient().getSession().write(packet);
		}
	}

	public void broadcastToVisitors(Packet packet, int exception) {
		for (WeakReference<MapleCharacter> chr : chrs) {
			if (chr != null && chr.get() != null && getVisitorSlot(chr.get()) != exception) {
				chr.get().getClient().getSession().write(packet);
			}
		}
		if (getShopType() != IMapleCharacterShop.HIRED_MERCHANT && ((MapleCharacterShop) this).getMCOwner() != null && exception != ownerId) {
			((MapleCharacterShop) this).getMCOwner().getClient().getSession().write(packet);
		}
	}

	@Override
	public int getMeso() {
		return meso.get();
	}

	@Override
	public void setMeso(int meso) {
		this.meso.set(meso);
	}

	@Override
	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	public boolean saveItems() {
		if (getShopType() != 1) {
			return false;
		}
		Connection con = MYSQL.getConnection();
		try {
			PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
			ps.setInt(1, this.owneraccount);
			ps.setInt(2, this.ownerId);
			ps.executeUpdate();
			ps.close();
			ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", 1);
			ps.setInt(1, this.ownerId);
			ps.setInt(2, this.owneraccount);
			ps.setLong(3, this.meso.get());
			ps.setLong(4, System.currentTimeMillis());

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			if (!rs.next()) {
				rs.close();
				ps.close();
				throw new RuntimeException("Error, adding merchant to DB");
			}
			int packageid = rs.getInt(1);
			rs.close();
			ps.close();
			List iters = new ArrayList();

			for (MapleCharacterShopItem pItems : this.items) {
				if ((pItems.item != null) && (pItems.bundles > 0) && ((pItems.item.getQuantity() > 0) || (GameConstants.isRechargable(pItems.item.getItemId())))) {
					IItem item = pItems.item.copy();
					item.setQuantity((short) (item.getQuantity() * pItems.bundles));
					iters.add(new Pair(item, GameConstants.getInventoryType(item.getItemId())));
				}
			}
			ItemFactory.saveItemsFromMerchant(new Pair(packageid, iters));
			return true;
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return false;
	}

	public boolean saveItems(HiredMerchant hired) {
		Connection con = MYSQL.getConnection();
		try {

			PreparedStatement del = con.prepareStatement("SELECT PackageId FROM hiredmerch WHERE characterid = ?");
			del.setInt(1, ownerId);
			ResultSet rss = del.executeQuery();
			del = con.prepareStatement("DELETE FROM hiredmerch WHERE PackageId = ?");
			PreparedStatement del2 = con.prepareStatement("DELETE FROM hiredmerchantsaves WHERE id = ?");

			PreparedStatement del3 = con.prepareStatement("DELETE FROM hiredmerchantsaveitems WHERE merchid = ?");
			while (rss.next()) {
				del.setInt(1, rss.getInt("PackageId"));
				del.executeUpdate();
				del2.setInt(1, rss.getInt("PackageId"));
				del2.executeUpdate();

				PreparedStatement gc = con.prepareStatement("SELECT uniqueid FROM hiredmerchantsaveitems WHERE merchid = ?");
				gc.setInt(1, rss.getInt("PackageId"));
				ResultSet gcRs = gc.executeQuery();
				PreparedStatement delitem = con.prepareStatement("DELETE FROM inventoryitems WHERE uniqueid = ?");
				while (gcRs.next()) {
					delitem.setInt(1, gcRs.getInt("uniqueid"));
					delitem.executeUpdate();
				}
				delitem.close();

				del3.setInt(1, rss.getInt("PackageId"));
				del3.executeUpdate();
			}
			del.close();

			PreparedStatement ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", MYSQL.RETURN_GENERATED_KEYS);
			ps.setInt(1, ownerId);
			ps.setInt(2, owneraccount);
			ps.setInt(3, meso.get());
			ps.setLong(4, System.currentTimeMillis());

			ps.executeUpdate();

			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			final int packageid = rs.getInt(1);
			rs.close();
			ps.close();

			PreparedStatement hiredSave = con.prepareStatement("INSERT INTO hiredmerchantsaves VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			hiredSave.setInt(1, packageid);
			hiredSave.setString(2, getDescription());
			hiredSave.setInt(3, getItemId());
			hiredSave.setString(4, getOwnerName());
			hiredSave.setInt(5, getOwnerId());
			hiredSave.setInt(6, getOwnerAccId());
			hiredSave.setInt(7, getPosition().x);
			hiredSave.setInt(8, getPosition().y);
			hiredSave.setInt(9, hired.getMap().getId());
			hiredSave.setInt(10, hired.getChannel());
			hiredSave.setLong(11, hired.getStartTime());
			hiredSave.setInt(12, getMeso());
			hiredSave.executeUpdate();
			hiredSave.close();

			List<IItem> itemlist = new ArrayList<IItem>();
			int i = 0;
			for (MapleCharacterShopItem pItems : items) {
				if (pItems.bundles <= 0) {
					continue;
				}
				IItem t = pItems.item.copy();
				t.setQuantity((short) (/* pItems.bundles * */pItems.item.getQuantity()));
				try {
					PreparedStatement save1 = con.prepareStatement("INSERT INTO hiredmerchantsaveitems VALUES (?, ?, ?, ?, ?)");
					save1.setInt(1, i);
					save1.setInt(2, packageid);
					save1.setInt(3, t.getUniqueId());
					save1.setShort(4, pItems.bundles);
					save1.setInt(5, pItems.price);
					save1.executeUpdate();
					save1.close();
				} catch (Exception e) {
					logger.debug("[오류] 고용상점 아이템을 재소환 dB에 저장하는 도중 오류 발생. {}", e);
				}
				itemlist.add(t);
				i++;
			}
			ItemFactory.saveItemsFromMerchant(new Pair(packageid, itemlist));
			return true;
		} catch (SQLException e) {
			logger.debug("{}", e);
		}
		return false;
	}

	public MapleCharacter getVisitor(int num) {
		return chrs[num].get();
	}

	@Override
	public void addVisitor(MapleCharacter visitor) {
		int i = getFreeSlot();
		if (i > 0) {
			if (getShopType() == IMapleCharacterShop.HIRED_MERCHANT || getShopType() == IMapleCharacterShop.PLAYER_SHOP) {
				broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));
			}
			chrs[i - 1] = new WeakReference<MapleCharacter>(visitor);
			if (!isOwner(visitor)) {
				visitors.add(visitor.getName());
			}
			if (i == 3) {
				update();
			}
		}
	}

	public void update() {
		if (getShopType() == IMapleCharacterShop.HIRED_MERCHANT) {
			((HiredMerchant) this).getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
		} else {
			((MapleCharacterShop) this).getMCOwner().getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(((MapleCharacterShop) this).getMCOwner()));
		}
	}

	@Override
	public void removeVisitor(MapleCharacter visitor) {
		final byte slot = getVisitorSlot(visitor);
		boolean shouldUpdate = getFreeSlot() == -1;
		if (slot > 0) {
			broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), slot);
			chrs[slot - 1] = new WeakReference<MapleCharacter>(null);
			if (shouldUpdate) {
				update();
			}
		}
	}

	@Override
	public byte getVisitorSlot(MapleCharacter visitor) {
		for (byte i = 0; i < chrs.length; i++) {
			if (chrs[i] != null && chrs[i].get() != null && chrs[i].get().getId() == visitor.getId()) {
				return (byte) (i + 1);
			}
		}
		if (visitor.getId() == ownerId) { // can visit own store in merch,
											// otherwise not.
			return 0;
		}
		return -1;
	}

	public MapleCharacter getMCOwnerChannel() {
		return ChannelServer.getInstance(channel).getPlayerStorage().getCharacterById(ownerId);
	}

	public MapleCharacter getMCOwner() {
		return getMap().getCharacterById(chr, ownerId);
	}

	public MapleMap getMap() {
		return ChannelServer.getInstance(channel).getMapFactory().getMap(map);
	}

	@Override
	public void removeAllVisitors(int error, int type) {
		for (int i = 0; i < chrs.length; i++) {
			MapleCharacter visitor = getVisitor(i);
			if (visitor != null) {
				if (type != -1) {
					visitor.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(error, type, 0x11));
				}
				broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(getVisitorSlot(visitor)), getVisitorSlot(visitor));
				visitor.setPlayerShop(null);
				chrs[i] = new WeakReference<MapleCharacter>(null);
			}
		}
		update();
	}

	@Override
	public String getOwnerName() {
		return ownerName;
	}

	@Override
	public int getOwnerId() {
		return ownerId;
	}

	@Override
	public int getOwnerAccId() {
		return owneraccount;
	}

	@Override
	public String getDescription() {
		if (des == null) {
			return "";
		}
		return des;
	}

	@Override
	public List<Pair<Byte, MapleCharacter>> getVisitors() {
		List<Pair<Byte, MapleCharacter>> chrz = new LinkedList<Pair<Byte, MapleCharacter>>();
		for (byte i = 0; i < chrs.length; i++) { // include owner or no
			if (chrs[i] != null && chrs[i].get() != null) {
				chrz.add(new Pair<Byte, MapleCharacter>((byte) (i + 1), chrs[i].get()));
			}
		}
		return chrz;
	}

	@Override
	public List<MapleCharacterShopItem> getItems() {
		return items;
	}

	@Override
	public void addItem(MapleCharacterShopItem item) {
		items.add(item);
	}

	@Override
	public boolean removeItem(int item) {
		return false;
	}

	@Override
	public void removeFromSlot(int slot) {
		items.remove(slot);
	}

	@Override
	public byte getFreeSlot() {
		for (byte i = 0; i < chrs.length; i++) {
			if (chrs[i] == null || chrs[i].get() == null) {
				return (byte) (i + 1);
			}
		}
		return -1;
	}

	@Override
	public int getGameType() {
		if (getShopType() == IMapleCharacterShop.HIRED_MERCHANT) { // hiredmerch
			return 5;
		} else if (getShopType() == IMapleCharacterShop.PLAYER_SHOP) { // shop
																		// lol
			return 4;
		}
		return 0;
	}

	@Override
	public int getItemId() {
		return itemId;
	}

	@Override
	public boolean isOwner(MapleCharacter chr) {
		return chr.getId() == ownerId && chr.getName().equals(ownerName);
	}

	@Override
	public List<BoughtItem> getBoughtItems() {
		return bought;
	}

	@Override
	public int getMaxSize() {
		return chrs.length + 1;
	}

	@Override
	public int getSize() {
		return getFreeSlot() == -1 ? getMaxSize() : getFreeSlot();
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public List<Pair<String, Byte>> getMessages() {
		return messages;
	}

	public static final class BoughtItem {
		public int id;
		public int quantity;
		public int totalPrice;
		public String buyer;

		public BoughtItem(final int id, final int quantity, final int totalPrice, final String buyer) {
			this.id = id;
			this.quantity = quantity;
			this.totalPrice = totalPrice;
			this.buyer = buyer;
		}
	}
}
