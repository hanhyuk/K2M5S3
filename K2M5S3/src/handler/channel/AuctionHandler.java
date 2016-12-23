/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package handler.channel;

import client.MapleClient;
import client.items.Item;
import client.items.MapleInventoryType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import packet.creators.PacketProvider;
import packet.opcode.SendPacketOpcode;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import server.items.InventoryManipulator;
import tools.HexTool;

public class AuctionHandler {

    public static final void Handle(final ReadingMaple rh, final MapleClient c, byte op) {
        switch (op) {
            case 0: { //����
                c.getSession().write(AuctionPacket.AuctionOn());
                c.getSession().write(AuctionPacket.showItemList(WorldAuction.getItems(), false));
                break;
            }
            case 1: {
                final boolean isbargain = rh.readInt() > 0;
                final int itemid = rh.readInt();
                final int quantity = rh.readInt();
                final long bid = rh.readLong();
                final long meso = rh.readLong();
                final int time = rh.readInt();
                final byte inv = rh.readByte();
                final short slot = rh.readShort();
                Item item = (Item) c.getPlayer().getInventory(MapleInventoryType.getByType(inv)).getItem(slot), copyItem;
                if (item == null || item.getItemId() != itemid || item.getQuantity() < quantity || c.getPlayer().getMeso() < 2000) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                copyItem = (Item) item.copy();
                copyItem.setQuantity((short) quantity);
                AuctionItemPackage aitem = new AuctionItemPackage(c.getPlayer().getId(), c.getPlayer().getName(), copyItem, bid, meso, System.currentTimeMillis() + (time * 60 * 60 * 1000), isbargain, 0, 0, System.currentTimeMillis(), 0);
                WorldAuction.addItem(aitem);
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 1, (byte) 0));
                c.getSession().write(AuctionPacket.AuctionSell(aitem));
                InventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(inv), slot, (short) quantity, false);
                c.getSession().write(AuctionPacket.showItemList(WorldAuction.getItems(), false));
                break;
            }
            case 2: { //������ ��� ���
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 2, (byte) 0));
                break;
            }
            case 3: {
                final int id = rh.readInt();
                final long meso = rh.readLong();
                AuctionItemPackage item = WorldAuction.findByIid(id);
                if (item == null || c.getPlayer().getMeso() < meso) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                item.setBuyer(c.getPlayer().getId());
                item.setBuyTime(System.currentTimeMillis());
                item.setType(2);
                c.getSession().write(AuctionPacket.showCompleteItemList(WorldAuction.getCompleteItems(c.getPlayer().getId()), c.getPlayer().getName(), c.getPlayer().getId()));
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 3, (byte) 0));
                c.getSession().write(AuctionPacket.AuctionBuy(item, meso, 2));
                break;
            }
            case 4: {
                final int id = rh.readInt();
                final int type = rh.readInt();
                //1 = ������ ��ȯ, 2 = ��ǰ ����, 3 = ��� ����, 4 = ��ǰ ��ȯ (���Ǹ�) , 5 = ������ ��ȯ (���� �߻�) , 6 = ���� �Ϸ�(��ȸ����), 7 = ���� �Ϸ�(����), 8 = ���� �Ϸ� (�Ǹ� �Ϸ�), 9 = ���� �Ϸ� (���Ǹ�), 10 = ���� �Ϸ� (���� �߻�)
                final long meso = rh.readLong();
                AuctionItemPackage item = WorldAuction.findByIid(id);
                if (item == null || c.getPlayer().getMeso() < meso) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                item.setBuyer(c.getPlayer().getId());
                item.setBuyTime(System.currentTimeMillis());
                item.setType(type == 2 ? 8 : 0);
                c.getSession().write(AuctionPacket.showCompleteItemList(WorldAuction.getCompleteItems(c.getPlayer().getId()), c.getPlayer().getName(), c.getPlayer().getId()));
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 5, (byte) 0));
                c.getSession().write(AuctionPacket.AuctionBuy(item, meso, 2));

                break;
            }
            case 5: {
                final int id = rh.readInt();
                final long meso = rh.readLong();
                AuctionItemPackage item = WorldAuction.findByIid(id);
                if (item == null || c.getPlayer().getMeso() < meso) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                item.setBuyer(999999);
                item.setBuyTime(System.currentTimeMillis());
                item.setType(0);
                WorldAuction.addAuction(c.getPlayer().getId(), meso, id, (byte) 0);
                c.getSession().write(AuctionPacket.showCompleteItemList(WorldAuction.getCompleteItems(c.getPlayer().getId()), c.getPlayer().getName(), c.getPlayer().getId()));
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 4, (byte) 0));
                c.getSession().write(AuctionPacket.AuctionBuy(item, meso, 1));
                break;
            }
            case 6: {
                final int id = rh.readInt();
                final int status = rh.readInt();
                //0 = ����,  1 = ������ ��ȯ, 2 = ��ǰ ����, 3 = ��� ����, 4 = ��ǰ ��ȯ (���Ǹ�) , 5 = ������ ��ȯ (���� �߻�) , 6 = ���� �Ϸ�(��ȸ����), 7 = ���� �Ϸ�(����), 8 = ���� �Ϸ� (�Ǹ� �Ϸ�), 9 = ���� �Ϸ� (���Ǹ�), 10 = ���� �Ϸ� (���� �߻�)

                final long meso = rh.readLong();
                AuctionItemPackage item = WorldAuction.findByIid(id);
                if (item == null) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                if (status == 1) {
                    c.getPlayer().gainMeso(WorldAuction.getBidById(c.getPlayer().getId(), id), true);
                } else if (status == 3) {
                    c.getPlayer().gainMeso(item.getBid(), true);
                } else if (status == 2 || status == 4) {
                    InventoryManipulator.addbyItem(c, item.getItem());
                }

                boolean isOnwer = c.getPlayer().getId() == item.getOwnerId();
                if (status != 1) {
                    item.setType(status == 2 ? (item.getType(isOnwer, true) == 18 ? 27 : 17) : status == 3 ? (item.getType(isOnwer, true) == 17 ? 28 : 18) : status == 4 ? 9 : 0);
                }
                c.getSession().write(AuctionPacket.showCompleteItemList(WorldAuction.getCompleteItems(c.getPlayer().getId()), c.getPlayer().getName(), c.getPlayer().getId()));
                c.getSession().write(AuctionPacket.AuctionMessage((byte) 6, (byte) 0));
                c.getSession().write(AuctionPacket.AuctionBuy(item, meso, status == 1 ? 7 : 8));
                break;
            }
            case 7: { //�˻�
                c.getSession().write(AuctionPacket.showItemList(WorldAuction.getItems(), true));
                break;
            }
            case 8: { //����� ������Ʈ
                c.getSession().write(AuctionPacket.AuctionOn());
                break;
            }
            case 9: {
                c.getSession().write(AuctionPacket.showCompleteItemList(WorldAuction.getCompleteItems(c.getPlayer().getId()), c.getPlayer().getName(), c.getPlayer().getId()));
                break;
            }
            case 10: { //�뷮����
                break;
            }
            case 11: { //����
                final int id = rh.readInt();
                final long meso = rh.readLong();
                final String bargaining = rh.readMapleAsciiString();
                AuctionItemPackage item = WorldAuction.findByIid(id);
                if (item == null || c.getPlayer().getMeso() < meso) {
                    c.getPlayer().dropMessage(1, "������ �߻��߽��ϴ�.");
                    return;
                }
                c.getSession().write(AuctionPacket.AuctionBargaining(item, meso, bargaining));
                break;
            }
        }
    }

    public static class WorldAuction {
        static List<AuctionItemPackage> items = new ArrayList<>();
        static Map<Integer, List<AuctionInfo>> auctions = new HashMap<>();

        public static void addAuction(final int cid, final long bid, final int iid, final byte status) {
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

        public static long getBidById(final int cid, final int iid) {
            long bid = 0;
            for (AuctionInfo ai : auctions.get(iid)) {
                if (ai.getCharacterId() == cid && ai.getBid() >= bid) {
                    bid = ai.getBid();
                }
            }
            return bid;
        }

        public static List<AuctionItemPackage> getItems() {
            List<AuctionItemPackage> items_ = new ArrayList<>();
            for (AuctionItemPackage aitem : items) {
                if (aitem.getBuyer() == 999999 || aitem.getBuyer() == 0) {
                    items_.add(aitem);
                }
            }
            return items_;
        }

        public static List<AuctionItemPackage> getCompleteItems(final int charid) {
            List<AuctionItemPackage> items_ = new ArrayList<>();
            for (AuctionItemPackage aitem : items) {
                if (aitem.getOwnerId() == charid || aitem.getBuyer() == charid || getBidById(charid, (int) aitem.getItem().getInventoryId()) > 0) {
                    items_.add(aitem);
                }
            }
            return items_;
        }

        public static final void addItem(final AuctionItemPackage aitem) {
            aitem.getItem().setInventoryId(items.size() + 1);
            items.add(aitem);
        }

        public static final AuctionItemPackage findByIid(final int id) {
            for (AuctionItemPackage item : items) {
                if (item.getItem().getInventoryId() == id) {
                    return item;
                }
            }
            return null;
        }
    }

    public static class AuctionInfo {
        private int  cid;
        private long bid;
        private byte status;

        public AuctionInfo(final long bid, final int cid, final byte status) {
            this.bid = bid;
            this.cid = cid;
            this.status = status;
        }

        public void setBid(final long bid) {
            this.bid = bid;
        }

        public long getBid() {
            return bid;
        }

        public int getCharacterId() {
            return cid;
        }

        public void setStatus(final byte status) {
            this.status = status;
        }

        public byte getStatus() {
            return status;
        }
    }

    public static class AuctionItemPackage {
        private long expiredTime, buyTime, startTime;
        private long bid = 0, mesos = 0;
        private Item item;
        private boolean bargain;
        private int ownerid, buyer, type;
        private String ownername;

        public AuctionItemPackage(final int ownerid, final String ownername, final Item item, final long bid, final long mesos, final long expiredTime, final boolean bargain, final int buyer, final long buyTime, final long startTime, final int type) {
            this.ownerid = ownerid;
            this.ownername = ownername;
            this.item = item;
            this.bid = bid;
            this.mesos = mesos;
            this.expiredTime = expiredTime;
            this.bargain = bargain;
            this.buyer = buyer;
            this.buyTime = buyTime;
            this.startTime = startTime;
            this.type = type;
        }

        public int getOwnerId() {
            return ownerid;
        }

        public String getOwnerName() {
            return ownername;
        }

        public void setExpiredTime(long expiredTime) {
            this.expiredTime = expiredTime;
        }

        public long getExpiredTime() {
            return expiredTime;
        }

        public void setBuyTime(long buyTime) {
            this.buyTime = buyTime;
        }

        public long getBuyTime() {
            return buyTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getBid() {
            return bid;
        }

        public void setBid(long set) {
            bid = set;
        }

        public long getMesos() {
            return mesos;
        }

        public void setMesos(long set) {
            mesos = set;
        }

        public Item getItem() {
            return item;
        }

        public boolean isBargain() {
            return bargain;
        }

        public int getBuyer() {
            return buyer;
        }

        public void setBuyer(int buyer) {
            this.buyer = buyer;
        }

        public int getType(boolean isOwner, boolean isReal) {
            if (isReal)
                return type;

            if (type == 17) 
                return isOwner ? 3 : 7;

            if (type == 27) 
                return isOwner ? 8 : 7;

            if (type == 18) 
                return isOwner ? 8 : 2;

            if (type == 28) 
                return isOwner ? 8 : 7;  

            if (type == 2)
                return isOwner ? 3 : 2;

            if (type == 0)
                return isOwner ? 4 : 0;

            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public static class AuctionPacket {

        public static Packet AuctionMessage(byte message, byte sub) {
            WritingPacket mplew = new WritingPacket();
            /*
             1 : ��� 
             0 : ���� 1 : ���� ���� ���� 2 : ������ ���� 3 : ��� ������ ���� 4 : �Ǹ� ���� ���� ���� 5 : ���� ������ > ��� ���Ű�
             2 : ���
             3 : ��� ����
             0 : ���� 3 : �ڽ��� ����Ѱ� X 4: �޼� ���� 5 : ���� ����
             4 : ���� 
             0 : ����  1 : �ְ� ���� 2 : ������ �̹� ��ȸ ���� 3 : ��� ���� �������� ���� 4 : �ڽ����� ���� �Ұ� 5 : �̹� �ְ� 6 : �޼� ���� 7 : �̼��� ������ 8 ; ���� �ݾ��� �ʹ� ����(���� �������� 5%�̻�) 9 : ���� ����
             5 : ���� 
             0 : ����  5 : �� ����
             6 : ��ȯ
             0 : ���� 3 : �κ� ����
             */
            mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
            mplew.write(message);
            mplew.write(sub);
            return mplew.getPacket();
        }

        public static Packet AuctionOn() {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
            mplew.write(8); //1.2.251+
            mplew.writeInt(0);
            
            return mplew.getPacket();
        }

        public static Packet showItemList(List<AuctionItemPackage> items, final boolean isSearch) {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
            mplew.write(7);
            mplew.write(isSearch ? 0 : 1); //0 : ã��, 1 : ��������
            mplew.writeInt(items.size()); //����
            for (AuctionItemPackage aitem : items) {
                Item item = aitem.getItem();
                mplew.writeInt(item.getItemId());
                mplew.writeInt(item.getQuantity()); //����
                addAuctionItemInfo(mplew, aitem);
            }
            if (isSearch) {
                for (AuctionItemPackage aitem : items) {
                    mplew.writeLong(aitem.getItem().getItemId());//������ �ڵ�
                }
            }
            return mplew.getPacket();
        }

        public static Packet showCompleteItemList(List<AuctionItemPackage> items, final String buyername, final int ownerId) {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
            mplew.write(9);
            mplew.writeInt(items.size()); //����
            for (AuctionItemPackage aitem : items) {
                boolean Refund = false;
                Item item = aitem.getItem();
                int status = aitem.getType(ownerId == aitem.getOwnerId(), false);
                mplew.writeInt((int) item.getInventoryId());
                mplew.write(HexTool.getByteArrayFromHexString("A9 D6 2C 05"));
                mplew.writeInt(aitem.getBuyer());
                mplew.writeInt(item.getItemId());
                mplew.writeInt(status); // 1 = ��ȸ���� 2 = ���� 3 = �Ǹ� �Ϸ�
                mplew.writeLong(status == 0 ? WorldAuction.getBidById(ownerId, (int) item.getInventoryId()) : aitem.getMesos()); //�ڽ� ������
                mplew.writeLong(PacketProvider.getTime(aitem.getBuyTime() + (12 * 60 * 60 * 1000)));
                mplew.writeLong(aitem.getBid());
                mplew.writeInt(Refund ? 0 : aitem.getItem().getItemId() / 1000000 == 1 ? 1 : 2); // 1: ��� , 2 : �Һ�
                mplew.writeInt(0x10);
                mplew.write(Refund ? 0 : 1);
                if (!Refund) {
                    addCompleteAuctionItemInfo(mplew, aitem, ownerId, buyername);
                    mplew.writeInt((int) item.getInventoryId());
                    mplew.write(HexTool.getByteArrayFromHexString("05 0A 2C 05"));
                    mplew.writeInt(aitem.getBuyer());
                    mplew.writeInt(item.getItemId());
                    mplew.writeInt(5); 
                    mplew.writeLong(aitem.getMesos());
                    mplew.writeLong(PacketProvider.getTime(aitem.getBuyTime()));
                    mplew.writeLong(0);
                    mplew.writeInt(0); // 1: ��� , 2 : �Һ�
                    mplew.writeInt(4);
                    mplew.write(0);
                }
            }
            return mplew.getPacket();
        }

        public static Packet AuctionSell(AuctionItemPackage aitem) {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION.getValue());
            mplew.write(8);
            mplew.writeInt(1); //����
            addAuctionItemInfo(mplew, aitem);

            return mplew.getPacket();
        }

        public static Packet AuctionBuy(AuctionItemPackage aitem, final long price, final int status) {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION_BUY.getValue());
            /*
             Type
             0 = ����
             1 = ��ȸ ����
             2 = ����
             3 = �Ǹ� �Ϸ�
             4 = �Ǹŵ��� �ʾҽ��ϴ�.
             7 = ��ȯ
             8 = ����
             */
            mplew.write(1); //�Ƹ� �� �������ϵ�
            mplew.writeInt((int) aitem.getItem().getInventoryId());
            mplew.write(HexTool.getByteArrayFromHexString("A9 D6 2C 05"));
            mplew.writeInt(aitem.getBuyer());
            mplew.writeInt(aitem.getItem().getItemId());
            mplew.writeInt(status);
            mplew.writeLong(price);
            mplew.writeLong(PacketProvider.getTime(aitem.getBuyTime() + (12 * 60 * 60 * 1000)));
            mplew.writeLong(0);
            mplew.writeInt(status == 1 ? 2 : status == 8 ? 11 : status == 7 ? 2 : 0);
            mplew.writeInt(0x10);

            return mplew.getPacket();
        }

        public static Packet AuctionBargaining(AuctionItemPackage aitem, long bargainmeso, final String bargainstring) {
            WritingPacket mplew = new WritingPacket();
            mplew.writeShort(SendPacketOpcode.AUCTION_BUY.getValue());
            mplew.write(0);
            mplew.writeInt(aitem.isBargain() ? 1 : 0);
            mplew.write(HexTool.getByteArrayFromHexString("76 20 C1 00"));
            mplew.write(HexTool.getByteArrayFromHexString("9E 95 7B 05"));
            mplew.write(HexTool.getByteArrayFromHexString("11 88 6D 01"));
            mplew.write(HexTool.getByteArrayFromHexString("FC 11 06 7D"));
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(aitem.getOwnerName());
            mplew.writeLong(bargainmeso);
            mplew.writeMapleAsciiString(bargainstring);
            mplew.writeLong(0);
            mplew.writeInt(-1);
            mplew.write0(5);

            return mplew.getPacket();
        }

        public static Packet addAuctionItemInfo(WritingPacket mplew, AuctionItemPackage aitem) {
            Item item = aitem.getItem();
            mplew.writeInt((int) item.getInventoryId());
            mplew.writeInt((int) item.getInventoryId());
            mplew.writeInt(aitem.isBargain() ? 1 : 0);
            mplew.write(HexTool.getByteArrayFromHexString("05 95 B5 00"));
            mplew.writeInt(aitem.getOwnerId());
            mplew.writeInt(0);
            mplew.writeInt(aitem.getItem().getItemId() / 1000000 == 1 ? 1 : 2); // 1: ��� , 2 : �Һ�
            mplew.writeInt(7);
            mplew.writeAsciiString(aitem.getOwnerName(), 13);
            mplew.writeLong(aitem.getBid());
            mplew.writeLong(-1);
            mplew.writeLong(aitem.getMesos());
            mplew.writeLong(PacketProvider.getTime(aitem.getExpiredTime()));
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 FF FF"));
            mplew.writeLong(item.getInventoryId());
            mplew.write(HexTool.getByteArrayFromHexString("60 D5 FF FF FF FF"));
            mplew.writeLong(item.getInventoryId());
            mplew.writeLong(PacketProvider.getTime(aitem.getStartTime()));
            mplew.write(HexTool.getByteArrayFromHexString("D0 07 00 00 00 00 00 00 00 00 00 00"));
            PacketProvider.addItemInfo(mplew, item, true, true, null);

            return mplew.getPacket();
        }

        public static Packet addCompleteAuctionItemInfo(WritingPacket mplew, AuctionItemPackage aitem, final int ownerId, final String buyername) {
            Item item = aitem.getItem();
            int status = aitem.getType(ownerId == aitem.getOwnerId(), false);
            mplew.writeInt((int) item.getInventoryId());
            mplew.writeInt((int) item.getInventoryId());
            mplew.writeInt(aitem.isBargain() ? 1 : 0);
            mplew.write(HexTool.getByteArrayFromHexString("80 F3 33 06"));
            mplew.writeInt(aitem.getOwnerId());
            mplew.writeInt(status == 0 ? 1 : 3); //������ 1? ������ 3
            mplew.writeInt(1);
            mplew.writeInt(0x10);
            mplew.writeAsciiString(aitem.getOwnerName(), 13);
            mplew.writeLong(aitem.getBid()); //���� ������
            mplew.writeLong(aitem.getBid());
            mplew.writeLong(aitem.getMesos());
            mplew.writeLong(PacketProvider.getTime(aitem.getExpiredTime()));
            mplew.write(HexTool.getByteArrayFromHexString("A9 D6 2C 05"));
            mplew.writeAsciiString(buyername, 13);
            mplew.writeInt(-1);
            mplew.writeLong(item.getInventoryId());
            mplew.writeLong(PacketProvider.getTime(aitem.getStartTime()));
            mplew.writeInt(2000);
            mplew.writeLong(0);
            PacketProvider.addItemInfo(mplew, item, true, true, null);

            return mplew.getPacket();
        }
    }     
}