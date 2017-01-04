package server.items;

import client.MapleCharacter;
import client.MapleClient;
import client.items.Equip;
import client.items.ItemFlag;
import client.items.MapleInventoryType;
import client.items.MapleWeaponType;
import client.stats.EnchantEquipStats;
import client.stats.EquipStats;
import constants.GameConstants;
import constants.ServerConstants;
import handler.channel.InventoryHandler;
import packet.creators.MainPacketCreator;
import packet.creators.PacketProvider;
import packet.opcode.SendPacketOpcode;
import packet.transfer.read.ReadingMaple;
import packet.transfer.write.Packet;
import packet.transfer.write.WritingPacket;
import provider.MapleData;
import provider.MapleDataTool;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;

public class EnforceSystem {
    
    public static void AddItemRecv(ReadingMaple rh, MapleClient c) {
        short position;
        Equip item;
        byte a = rh.readByte();
        if (a == 0) {
            rh.skip(4);
            position = rh.readShort();
            short sposition = rh.readShort();
            boolean success = false;
            if (position > 0) {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(position);
            } else {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(position);
            }
            int persent[] = {30, 100, 70};
            int persent1[] = {15, 100, 70, 30};
            int persentFever[] = {45, 100, 95};
            int persent1Fever[] = {25, 100, 95, 45};
            if (c.getPlayer().getGMLevel() > 5) {
                success = true;
            }
            if (c.getPlayer().scrollcount() % 3 == 0 && c.getPlayer().scrollcount() != 12) {
                if (ServerConstants.IS_FEVER_TIME) {
                    if (Randomizer.isSuccess(persentFever[((sposition + 1) % 3)])) {
                        success = true;
                    }
                } else {
                    if (Randomizer.isSuccess(persent[((sposition + 1) % 3)])) {
                        success = true;
                    }
                }
            }
            if (c.getPlayer().scrollcount() % 4 == 0 || c.getPlayer().scrollcount() == 12) {
                if (ServerConstants.IS_FEVER_TIME) {
                    if (Randomizer.isSuccess(persent1Fever[((sposition + 1) % 4)])) {
                        success = true;
                    }
                } else {
                    if (Randomizer.isSuccess(persent1[((sposition + 1) % 4)])) {
                        success = true;
                    }
                }
            }
            c.send(UpdateItemResult(item, success, sposition, c.getPlayer()));
            Equip zeros = null;
            if (GameConstants.isZero(c.getPlayer().getJob())) {
                if (item.getPosition() == -11) {
                    zeros = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
                } else if (item.getPosition() == -10) {
                    zeros = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                }
            }
            c.send(MainPacketCreator.updateEquipSlot(item));
            if (zeros != null) {
                c.send(MainPacketCreator.updateEquipSlot(zeros));
            }
        } else if (a == 0x32) {
            position = rh.readShort();
            rh.skip(2);
            if (position > 0) {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(position);
            } else {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(position);
            }
            c.send(AddItemOnSystem(c, c.getPlayer().scrollcount(), item, a, ServerConstants.IS_FEVER_TIME ? 1 : 0));
        } else if (a == 0x35) {//StarForceChance
            if (ServerConstants.IS_FEVER_TIME || c.getPlayer().getGMLevel() > 0) {
                if (c.getPlayer().ForcingItem() > 0) {
                    item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)(c.getPlayer().ForcingItem()));
                } else {
                    item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)(c.getPlayer().ForcingItem()));
                }
                StarSystem(c.getPlayer(), item, false);
            } else {
                c.getPlayer().send(StarForceChance());
            }
        } else if (a == 0x1) {//StarForceUpgrade!
            boolean chance = false;
            rh.skip(4);
            if (c.getPlayer().ForcingItem() > 0) {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)c.getPlayer().ForcingItem());
            } else {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)c.getPlayer().ForcingItem());
            }
            if (rh.readByte() == 0) {
                chance = true;
            }
            StarSystem(c.getPlayer(), item, chance);
        } else if (a == 0x34) {
            short slot = rh.readShort();
            c.getPlayer().setForcingItem(slot);
            if (slot > 0) {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
            } else {
                item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
            }
            StarSystemSet(item, c.getPlayer());
            c.getPlayer().send(StarForceItemSet(c, item, StarForceMeso(item), c.getPlayer().StarPer()[0], c.getPlayer().StarPer()[1], c.getPlayer().StarPer()[2]));
        } else if (a == 2) {
            rh.skip(4);
            short targetslot = rh.readShort();
            short traceslot = rh.readShort();
            Equip targetitem = null;
            if (targetslot > 0) {
                targetitem = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(targetslot);
            } else {
                targetitem = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(targetslot);
            }
            Equip originalitem = targetitem;
            Equip traceitem = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(traceslot);
            targetitem.set(traceitem);
            for (int i = 0; i < targetitem.getEnhance(); i++) {
                StarSystemUpgrade(targetitem, 0, c.getPlayer());
            }
            InventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, traceslot, (short)1, false);
            c.getPlayer().send(EquipTrace(originalitem, targetitem));
            c.getPlayer().send(MainPacketCreator.updateEquipSlot(targetitem));
        }
    }
    
    public static int AddAllSmalls(int a) {
        int b = 0;
        if (a <= 0) {
            return 0;
        } else {
            for (int i = 1; i < 2147483647; i++) {
                if ((a - i) > 0) {
                    b += (a - i);
                } else {
                    break;
                }
            }
        }
        return b + a;
    }
    
    public static Pair<String, Boolean> isSuperial(int itemid) {
        if ((itemid >= 1102471 && itemid <= 1102475) || (itemid >= 1072732 && itemid <= 1072736) || (itemid >= 1132164 && itemid <= 1132168)) {
            return new Pair<String, Boolean>("Helisium", true);
        } else if ((itemid >= 1102476 && itemid <= 1102480) || (itemid >= 1072737 && itemid <= 1072741) || (itemid >= 1132169 && itemid <= 1132173)) {
            return new Pair<String, Boolean>("Nova", true);
        } else if ((itemid >= 1102481 && itemid <= 1102485) || (itemid >= 1072743 && itemid <= 1072747) || (itemid >= 1132174 && itemid <= 1132178 || (itemid >= 1082543 && itemid <= 1082547))) {
            return new Pair<String, Boolean>("Tilent", true);
        } else if ((itemid >= 1122241 && itemid <= 1122245)) {
            return new Pair<String, Boolean>("MindPendent", true);
        }
        return new Pair<String, Boolean>(null, false);
    }
    
    public static Packet StarForceItemSet(MapleClient c, Equip item, long meso, int sucper, int downper, int desper) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(0x34);
        packet.write(downper > 0 ? desper > 0 ? 2 : 1 : 0);
        packet.writeLong(meso);
        packet.writeLong(0); //MVP할인율, 1.2.251+
        packet.writeInt(sucper * 10);
        packet.writeInt(desper * 10);
        packet.write(0);
        StarForceStat(c, item, packet, item.getEnhance() == 0 ? 0 : (item.getEnhance() - 25));
        
        return packet.getPacket();
    }
    
    public static EnchantEquipStats Stat1(Equip item, int ReqJob) {
        if (GameConstants.isWeapon(item.getItemId())) {
            if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.AXE1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BLUNT1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.AXE2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BLUNT2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SPEAR || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.POLE_ARM || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.KNUCKLE || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.HANDCANNON || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.TEDO || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DAGGER || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.ENERGYSWORD) {
                return EnchantEquipStats.STR;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SOULSHOOTER || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BOW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CROSSBOW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.GUN || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DUALBOW) {
                return EnchantEquipStats.DEX;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.WAND || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.STAFF || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.PLANE) {
                return EnchantEquipStats.INT;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CLAW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CAIN || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.KATARA) {
                return EnchantEquipStats.LUK;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DESPERADO) {
                return EnchantEquipStats.HP;
            }
        } else {
                switch (ReqJob) {
                    case 0:
                        return EnchantEquipStats.STR;
                    case 1:
                    case 16:
                        return EnchantEquipStats.STR;
                    case 2:
                        return EnchantEquipStats.INT;
                    case 4:
                        return EnchantEquipStats.DEX;
                    case 8:
                        return EnchantEquipStats.LUK;
                }
        }
        return null;
    }
    
    public static EnchantEquipStats Stat2(Equip item, int ReqJob) {
        if (GameConstants.isWeapon(item.getItemId())) {
            if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.AXE1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BLUNT1H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.AXE2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BLUNT2H || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SPEAR || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.POLE_ARM || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.KNUCKLE || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.HANDCANNON || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SWORD || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.TEDO || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DAGGER || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.ENERGYSWORD) {
                return EnchantEquipStats.DEX;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.SOULSHOOTER || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.BOW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CROSSBOW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.GUN || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DUALBOW) {
                return EnchantEquipStats.STR;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.WAND || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.STAFF || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.PLANE) {
                return EnchantEquipStats.LUK;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CLAW || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.CAIN || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.KATARA || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DAGGER) {
                return EnchantEquipStats.DEX;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DESPERADO) {
                return EnchantEquipStats.STR;
            }
        } else {
                switch (ReqJob) {
                    case 0:
                        return EnchantEquipStats.DEX;
                    case 1:
                    case 16:
                        return EnchantEquipStats.DEX;
                    case 2:
                        return EnchantEquipStats.LUK;
                    case 4:
                        return EnchantEquipStats.STR;
                    case 8:
                        return EnchantEquipStats.DEX;
                }
        }
        return null;
    }
    
    public static EnchantEquipStats Stat3(Equip item, int ReqJob) {
        if (GameConstants.isWeapon(item.getItemId())) {
            if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.ENERGYSWORD) {
                return EnchantEquipStats.LUK;
            } else if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.DESPERADO) {
                return EnchantEquipStats.DEX;
            }
        } else {
                switch (ReqJob) {
                    case 0:
                        return EnchantEquipStats.INT;
                    case 8:
                        return EnchantEquipStats.STR;
                    case 16:
                        return EnchantEquipStats.LUK;
                }
        }
        return null;
    }
    
    public static EnchantEquipStats Stat4(Equip item, int ReqJob) {
        if (!GameConstants.isWeapon(item.getItemId())) {
            switch (ReqJob) {
                case 0:
                    return EnchantEquipStats.LUK;
                }
        }
        return null;
    }
    
    public static EnchantEquipStats Atk(Equip item) {
        if (GameConstants.isWeapon(item.getItemId())) {
            if (GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.WAND || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.STAFF || GameConstants.getWeaponType(item.getItemId()) == MapleWeaponType.PLANE) {
                return EnchantEquipStats.MATK;
            } else {
                return EnchantEquipStats.WATK;
            }
        }
        return null;
    }
    
    public static void StarForceStat(MapleClient c, Equip item, WritingPacket packet, int enhance) {
        c.getPlayer().stata().removeAll(c.getPlayer().stata());
        int stats = 0;
        MapleWeaponType a;
        final ItemInformation ii = ItemInformation.getInstance();
        final MapleData IData = ii.getItemData(item.getItemId());
        final MapleData info = IData.getChildByPath("info");
        int Job = MapleDataTool.getInt("reqJob", info, 0);
        Pair<String, Boolean> superial = isSuperial(item.getItemId());
        if (superial.getRight()) {
            if (enhance >= 5 && enhance < 15) {
                if (superial.getLeft().equals("Helisium")) {
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, 3 + AddAllSmalls(enhance - 5)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, 3 + AddAllSmalls(enhance - 5)));
                } else if (superial.getLeft().equals("Nova")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, 6 + AddAllSmalls(enhance - 5)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, 6 + AddAllSmalls(enhance - 5)));
                } else if (superial.getLeft().equals("Tilent")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, 9 + AddAllSmalls(enhance - 5)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, 9 + AddAllSmalls(enhance - 5)));
                } else if (superial.getLeft().equals("MindPendent")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, 9 + AddAllSmalls(enhance - 5)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, 9 + AddAllSmalls(enhance - 5)));
                }
            } else {
                if (superial.getLeft().equals("Helisium")) {
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.STR, 5 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.DEX, 5 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.INT, 5 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.LUK, 5 + AddAllSmalls(enhance)));
                } else if (superial.getLeft().equals("Nova")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.STR, 10 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.DEX, 10 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.INT, 10 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.LUK, 10 + AddAllSmalls(enhance)));
                } else if (superial.getLeft().equals("Tilent")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.STR, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.DEX, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.INT, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.LUK, 20 + AddAllSmalls(enhance)));
                } else if (superial.getLeft().equals("MindPendent")){
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.STR, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.DEX, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.INT, 20 + AddAllSmalls(enhance)));
                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.LUK, 20 + AddAllSmalls(enhance)));
                }
            }
        } else {
        if (GameConstants.isWeapon(item.getItemId())) {
                    if (ItemInformation.getInstance().getEquipStats(item.getItemId()).get("reqLevel") > 100) {
                        if (enhance < 15) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), Atk(item) == EnchantEquipStats.WATK ? ((item.getWatk() / 50) + 1) : ((item.getMatk() / 50) + 1)));
                        } else if (enhance < 22) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), enhance - 7));
                        } else {
                            switch (enhance) {
                                case 22:
                                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), 31));
                                    break;
                                case 23:
                                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), 62));
                                    break;
                                case 24:
                                    c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), 124));
                                    break;
                            }
                        }
                        if (enhance >= 4 && enhance < 10) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 5));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 5));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 5)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 5)); }
                        } else if (enhance >= 10 && enhance < 15) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 7));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 7));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 7)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 7)); }
                        } else if (enhance >= 15 && enhance < 20) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 15));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 15));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 15)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 15)); }
                        } else if (enhance >= 20 && enhance < 25) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 30));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 30));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 30)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 30)); }
                        } else {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 3));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 3));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 3)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 3)); }
                        }
                        if (Stat1(item, Job) != EnchantEquipStats.HP && Stat2(item, Job) != EnchantEquipStats.HP && Stat3(item, Job) != EnchantEquipStats.HP && Stat4(item, Job) != EnchantEquipStats.HP) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.HP, 10));
                        }
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MP, 10));
                    } else {
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Atk(item), Atk(item) == EnchantEquipStats.WATK ? ((item.getWatk() / 50) + 1) : ((item.getMatk() / 50) + 1)));
                        if (Stat1(item, Job) != EnchantEquipStats.HP && Stat2(item, Job) != EnchantEquipStats.HP && Stat3(item, Job) != EnchantEquipStats.HP && Stat4(item, Job) != EnchantEquipStats.HP) {
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.HP, 5));
                        }
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MP, 5));
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 2));
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 2));
                        if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 2)); }
                        if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 2)); }
                    }
        } else {
                    if (ItemInformation.getInstance().getEquipStats(item.getItemId()).get("reqLevel") > 100) {
                        if (enhance >= 4 && enhance < 10) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 5));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 5));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 5)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 5)); }
                        } else if (enhance >= 10 && enhance < 15) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 7));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 7));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 7)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 7)); }
                        } else if (enhance >= 15 && enhance < 20) {
                            if (enhance >= 17) {
                                c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, enhance-4));
                                c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, enhance-4));
                            }
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 15));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 15));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 15)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 15)); }
                        } else if (enhance >= 20 && enhance < 25) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WATK, enhance+2));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MATK, enhance+2));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 30));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 30));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 30)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 30)); }
                        } else {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 3));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 3));
                            if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 3)); }
                            if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 3)); }
                        }
                        if (Stat1(item, Job) != EnchantEquipStats.HP && Stat2(item, Job) != EnchantEquipStats.HP && Stat3(item, Job) != EnchantEquipStats.HP && Stat4(item, Job) != EnchantEquipStats.HP) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.HP, 50));
                        }
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MP, 50));
                        if (enhance >= 4 && enhance < 10) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 15));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 15));
                        } else if (enhance >= 10 && enhance < 15) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 25));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 25));                    
                        } else if (enhance >= 15 && enhance < 20) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 55));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 55));
                        } else if (enhance >= 20 && enhance < 25) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 95));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 95));
                        } else {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 9));
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 9));
                        }
                    } else {
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat1(item, Job), 2));
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat2(item, Job), 2));
                        if (Stat3(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat3(item, Job), 2)); }
                        if (Stat4(item, Job) != null) { c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(Stat4(item, Job), 2)); }
                        if (Stat1(item, Job) != EnchantEquipStats.HP && Stat2(item, Job) != EnchantEquipStats.HP && Stat3(item, Job) != EnchantEquipStats.HP && Stat4(item, Job) != EnchantEquipStats.HP) {
                            c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.HP, 5));
                        }
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MP, 5));
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.WDEF, 5));
                        c.getPlayer().stata().add(new Pair<EnchantEquipStats, Integer>(EnchantEquipStats.MDEF, 5));
                    }
        }
        }
        for (int i = 0; i < c.getPlayer().stata().size(); i++) {
            stats |= c.getPlayer().stata().get(i).left.getValue();
        }
        if (packet != null) {
            packet.writeInt(stats);
            for (int i = 0; i < c.getPlayer().stata().size(); i++) {
                packet.writeInt(c.getPlayer().stata().get(i).getRight());
            }
        }
        
    }
    
    public static Packet StarForceChance() {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(HexTool.getByteArrayFromHexString("35 00 00 5C 13 A5"));
        
        return packet.getPacket();
    }
    
    public static void StarSystemUpgrade(Equip equip, int success, MapleCharacter chr) {
        if (success == 1) {
            for (int i = 0; i < chr.stata().size(); i++) {
                EquipStats use = null;
                switch (chr.stata().get(i).getLeft().getValue()) {
                    case 0x1:
                        equip.setWatk((short)(equip.getWatk() + chr.stata().get(i).getRight()));
                        break;
                    case 0x2:
                        equip.setMatk((short)(equip.getMatk() + chr.stata().get(i).getRight()));
                        break;
                    case 0x4:
                        equip.setStr((short)(equip.getStr() + chr.stata().get(i).getRight()));
                        break;
                    case 0x8:
                        equip.setDex((short)(equip.getDex() + chr.stata().get(i).getRight()));
                        break;
                    case 0x10:
                        equip.setInt((short)(equip.getInt() + chr.stata().get(i).getRight()));
                        break;
                    case 0x20:
                        equip.setLuk((short)(equip.getLuk() + chr.stata().get(i).getRight()));
                        break;
                    case 0x40:
                        equip.setWdef((short)(equip.getWdef() + chr.stata().get(i).getRight()));
                        break;
                    case 0x80:
                        equip.setMdef((short)(equip.getMdef() + chr.stata().get(i).getRight()));
                        break;
                    case 0x100:
                        equip.setHp((short)(equip.getHp() + chr.stata().get(i).getRight()));
                        break;
                    case 0x200:
                        equip.setMp((short)(equip.getMp() + chr.stata().get(i).getRight()));
                        break;
                    case 0x400:
                        equip.setAcc((short)(equip.getAcc() + chr.stata().get(i).getRight()));
                        break;
                    case 0x800:
                        equip.setAvoid((short)(equip.getAvoid() + chr.stata().get(i).getRight()));
                        break;
                }
            }
            if (equip.getEnhance() > 25) {
                equip.setEnhance((byte)(equip.getEnhance() + 1));
            } else {
                equip.setEnhance((byte)26);
            }
        } else if (success == 0) {
            Equip equipc = (Equip)equip.copy();
            StarForceStat(chr.getClient(), equipc, null, equip.getEnhance() >= 26 ? equip.getEnhance() - 26 : equip.getEnhance() - 1);
            
            for (int i = 0; i < chr.stata().size(); i++) {
                EquipStats use = null;
                switch (chr.stata().get(i).getLeft().getValue()) {
                    case 0x1:
                        equip.setWatk((short)(equip.getWatk() - chr.stata().get(i).getRight()));
                        break;
                    case 0x2:
                        equip.setMatk((short)(equip.getMatk() - chr.stata().get(i).getRight()));
                        break;
                    case 0x4:
                        equip.setStr((short)(equip.getStr() - chr.stata().get(i).getRight()));
                        break;
                    case 0x8:
                        equip.setDex((short)(equip.getDex() - chr.stata().get(i).getRight()));
                        break;
                    case 0x10:
                        equip.setInt((short)(equip.getInt() - chr.stata().get(i).getRight()));
                        break;
                    case 0x20:
                        equip.setLuk((short)(equip.getLuk() - chr.stata().get(i).getRight()));
                        break;
                    case 0x40:
                        equip.setWdef((short)(equip.getWdef() - chr.stata().get(i).getRight()));
                        break;
                    case 0x80:
                        equip.setMdef((short)(equip.getMdef() - chr.stata().get(i).getRight()));
                        break;
                    case 0x100:
                        equip.setHp((short)(equip.getHp() - chr.stata().get(i).getRight()));
                        break;
                    case 0x200:
                        equip.setMp((short)(equip.getMp() - chr.stata().get(i).getRight()));
                        break;
                    case 0x400:
                        equip.setAcc((short)(equip.getAcc() - chr.stata().get(i).getRight()));
                        break;
                    case 0x800:
                        equip.setAvoid((short)(equip.getAvoid() - chr.stata().get(i).getRight()));
                        break;
                }
            }
            equip.setEnhance((byte)(equip.getEnhance() > 26 ? equip.getEnhance() - 1 : equip.getEnhance() - 26));
        } else if (success == 2) {//실패&펑
            if (GameConstants.isZero(chr.getJob()) && (equip.getPosition() == -11 || equip.getPosition() == -10)) {
                Equip wa = (Equip)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11));
                Equip wb = (Equip)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10));
                Equip waa = (Equip)wa.copy();
                Equip wab = (Equip)wb.copy();
                waa.setItemTrace((short)1);
                wab.setItemTrace((short)1);
                InventoryManipulator.addFromDrop(chr.getClient(), waa, false);
                InventoryManipulator.addFromDrop(chr.getClient(), wab, false);
                InventoryHandler.resetZeroWeapon(chr);
            } else {
                if (equip.getPosition() > 0) {
                    ((Equip)(chr.getInventory(MapleInventoryType.EQUIP).getItem(equip.getPosition()))).setItemTrace((short)1);
                } else {
                    ((Equip)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equip.getPosition()))).setItemTrace((short)1);
                }
            }
        }
    }
    
    public static void StarSystemUpdate(Equip equip, int success, MapleCharacter chr, boolean isZeroWeapon) {
        if (!isZeroWeapon) {
            chr.gainMeso(-StarForceMeso(equip), false);
        }
        chr.send(UpdateItemStar(equip, success, chr));
        chr.send(MainPacketCreator.updateEquipSlot(equip));
    }
    
    public static void StarSystemSet(Equip equip, MapleCharacter chr) {
        int enhance = (equip.getEnhance() > 25 ? equip.getEnhance() - 25 : 0);
        switch (enhance) {
            case 0:
                chr.StarPers(95, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(50, 0, 0);
                }
                break;
            case 1:
                chr.StarPers(90, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(45, 55, 0);
                }
                break;
            case 2:
                chr.StarPers(85, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(40, 60, 0);
                }
                break;
            case 3:
                chr.StarPers(85, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(35, 65, 0);
                }
                break;
            case 4:
                chr.StarPers(80, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(35, 65, 0);
                }
                break;
            case 5:
                chr.StarPers(75, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(30, 70, 0);
                }
                break;
            case 6:
                chr.StarPers(70, 30, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(30, 70, 0);
                }
                break;
            case 7:
                chr.StarPers(65, 35, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(30, 70, 0);
                }
                break;
            case 8:
                chr.StarPers(60, 40, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(25, 74, 1);
                }
                break;
            case 9:
                chr.StarPers(55, 45, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(25, 72, 3);
                }
                break;
            case 10:
                chr.StarPers(45, 0, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(20, 75, 5);
                }
                break;
            case 11:
                chr.StarPers(35, 65, 0);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(15, 75, 10);
                }
                break;
            case 12:
                chr.StarPers(30, 69, 1);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(10, 75, 15);
                }
                break;
            case 13:
                chr.StarPers(30, 69, 1);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(3, 30, 67);
                }
                break;
            case 14:
                chr.StarPers(30, 69, 1);
                if (isSuperial(equip.getItemId()).right) {
                    chr.StarPers(1, 20, 79);
                }
                break;
            case 15:
                chr.StarPers(30, 0, 1);
                break;
            case 16:
                chr.StarPers(30, 68, 2);
                break;
            case 17:
                chr.StarPers(30, 68, 2);
                break;
            case 18:
                chr.StarPers(30, 67, 3);
                break;
            case 19:
                chr.StarPers(30, 67, 3);
                break;
            case 20:
                chr.StarPers(30, 0, 7);
                break;
            case 21:
                chr.StarPers(30, 63, 7);
                break;
            case 22:
                chr.StarPers(3, 78, 19);
                break;
            case 23:
                chr.StarPers(2, 69, 29);
                break;
            case 24:
                chr.StarPers(1, 59, 40);
                break;
        }
        if (ServerConstants.IS_FEVER_TIME || chr.getGMLevel() > 0) {
            chr.StarPers(100, 0, 0);
        }
    }
    
    public static void StarSystem(MapleCharacter chr, Equip equip, boolean isChance) {
        int success = 0; int FailAndDown = 0; int FailAndDestroy = 0; int meso = 0;
        int enhance = (equip.getEnhance() > 25 ? equip.getEnhance() - 25 : 0);
        switch (enhance) {
            case 0:
                success = 95;
                if (isSuperial(equip.getItemId()).right) {
                    success = 50;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 1:
                success = 90;
                if (isSuperial(equip.getItemId()).right) {
                    success = 45;
                    FailAndDown = 55;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 2:
                success = 85;
                if (isSuperial(equip.getItemId()).right) {
                    success = 40;
                    FailAndDown = 60;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 3:
                success = 85;
                if (isSuperial(equip.getItemId()).right) {
                    success = 35;
                    FailAndDown = 65;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 4:
                success = 80;
                if (isSuperial(equip.getItemId()).right) {
                    success = 35;
                    FailAndDown = 65;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 5:
                success = 75;
                if (isSuperial(equip.getItemId()).right) {
                    success = 30;
                    FailAndDown = 70;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 6:
                success = 70;
                FailAndDown = 30;
                if (isSuperial(equip.getItemId()).right) {
                    success = 30;
                    FailAndDown = 70;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 7:
                success = 65;
                FailAndDown = 35;
                if (isSuperial(equip.getItemId()).right) {
                    success = 30;
                    FailAndDown = 70;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 8:
                success = 60;
                FailAndDown = 40;
                if (isSuperial(equip.getItemId()).right) {
                    success = 25;
                    FailAndDown = 74;
                    FailAndDestroy = 1;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 9:
                success = 55;
                FailAndDown = 45;
                if (isSuperial(equip.getItemId()).right) {
                    success = 25;
                    FailAndDown = 72;
                    FailAndDestroy = 3;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 10:
                success = 45;
                if (isSuperial(equip.getItemId()).right) {
                    success = 20;
                    FailAndDown = 75;
                    FailAndDestroy = 5;
                }
                if (isChance) {
                    success += 5;
                }
                break;
            case 11:
                success = 35;
                FailAndDown = 65;
                if (isSuperial(equip.getItemId()).right) {
                    success = 15;
                    FailAndDown = 75;
                    FailAndDestroy = 10;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 12:
                success = 30;
                FailAndDown = 69;
                FailAndDestroy = 1;
                if (isSuperial(equip.getItemId()).right) {
                    success = 10;
                    FailAndDown = 75;
                    FailAndDestroy = 15;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 13:
                success = 30;
                FailAndDown = 69;
                FailAndDestroy = 1;
                if (isSuperial(equip.getItemId()).right) {
                    success = 3;
                    FailAndDown = 30;
                    FailAndDestroy = 67;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 14:
                success = 30;
                FailAndDown = 69;
                FailAndDestroy = 1;
                if (isSuperial(equip.getItemId()).right) {
                    success = 1;
                    FailAndDown = 20;
                    FailAndDestroy = 69;
                }
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 15:
                success = 30;
                FailAndDestroy = 1;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 16:
                success = 30;
                FailAndDown = 68;
                FailAndDestroy = 2;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 17:
                success = 30;
                FailAndDown = 68;
                FailAndDestroy = 2;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 18:
                success = 30;
                FailAndDown = 67;
                FailAndDestroy = 3;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 19:
                success = 30;
                FailAndDown = 67;
                FailAndDestroy = 3;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 20:
                success = 30;
                FailAndDestroy = 7;
                if (isChance) {
                    success += 3;
                    FailAndDown -= 3;
                }
                break;
            case 21:
                success = 30;
                FailAndDown = 63;
                FailAndDestroy = 7;
                if (isChance) {
                    success += 5;
                    FailAndDown -= 5;
                }
                break;
            case 22:
                success = 3;
                FailAndDown = 78;
                FailAndDestroy = 19;
                if (isChance) {
                    success += 1;
                    FailAndDown -= 1;
                }
                break;
            case 23:
                success = 2;
                FailAndDown = 69;
                FailAndDestroy = 29;
                if (isChance) {
                    success += 1;
                    FailAndDown -= 1;
                }
                break;
            case 24:
                success = 1;
                FailAndDown = 59;
                FailAndDestroy = 40;
                if (isChance) {
                    success += 1;
                    FailAndDown -= 1;
                }
                break;
        }
        if (ServerConstants.IS_FEVER_TIME || chr.getGMLevel() > 0) {
            success = 100;
            FailAndDown = 0;
            FailAndDestroy = 0;
        }
        Equip zeros = null;
        if (GameConstants.isZero(chr.getJob())) {
            if (equip.getPosition() == -11) {
                zeros = (Equip)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10));
            } else if (equip.getPosition() == -10) {
                zeros = (Equip)(chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11));
            }
        }
        if (Randomizer.isSuccess(success)) {
            if (zeros != null) {
                StarSystemUpdate(zeros, 1, chr, true);
            }
            StarSystemUpdate(equip, 1, chr, false);
        } else {//1 : 성공 3 : 유지 2 : 펑 0 : 떨어짐으로 추정
            int Total = 100 - success;//일단 100에서 확률을빼서 ex 3퍼 : 97퍼로만든다
            if (FailAndDown > 0 && FailAndDestroy > 0) { //내려갈 확률, 터질 확률 전부 있을때 //97
                if (Randomizer.isSuccess(FailAndDown, Total)) {//내려갈 확률에 걸린다면
                    if (zeros != null) {
                        StarSystemUpdate(zeros, 0, chr, true);
                    }
                    StarSystemUpdate(equip, 0, chr, false);
                } else {//터질 확률에 걸린다면
                    if (zeros != null) {
                        StarSystemUpdate(equip, 2, chr, true);
                    }
                    StarSystemUpdate(equip, 2, chr, false);
                }
            } else if (FailAndDown > 0 && FailAndDestroy == 0) {//내려갈 확률만 있다면
                if (zeros != null) {
                    StarSystemUpdate(equip, 0, chr, true);
                }
                StarSystemUpdate(equip, 0, chr, false);
            } else if (FailAndDown == 0 && FailAndDestroy == 0) {//등급 유지된다면
                if (zeros != null) {
                    StarSystemUpdate(equip, 3, chr, true);
                }
                StarSystemUpdate(equip, 3, chr, false);
            } else if (FailAndDown == 0 && FailAndDestroy > 0) {//터지는 확률만 있다면
                if (Randomizer.isSuccess(FailAndDestroy, Total)) {//펑
                    if (zeros != null) {
                        StarSystemUpdate(equip, 2, chr, true);
                    }
                    StarSystemUpdate(equip, 2, chr, false);
                } else {//유지
                    if (zeros != null) {
                        StarSystemUpdate(equip, 3, chr, true);
                    }
                    StarSystemUpdate(equip, 3, chr, false);
                }
            }
        }
    }
    public static int ItemJuhun(MapleClient c, int scrollnumber) {
        int juhun = 0;
        String itemstring[] = {c.getPlayer().scrollstring1(), c.getPlayer().scrollstring2(), c.getPlayer().scrollstring3(), c.getPlayer().scrollstring4(), c.getPlayer().scrollstring5()};
        int a = 0;
        if (c.getPlayer().scrollcount() % 3 == 0 && c.getPlayer().scrollcount() != 12) {
            a = 3;
        } else if (c.getPlayer().scrollcount() % 4 == 0 || c.getPlayer().scrollcount() == 12) {
            a = 4;
        }
        if (((scrollnumber +1) % a) == 1) {//100%일때
            if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운" || itemstring[scrollnumber / a] == "체력") {
                juhun = 26;
            } else if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "마력(지력)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                juhun = 160;
            } else if (itemstring[scrollnumber / a] == "공격력" || itemstring[scrollnumber / a] == "마력") {
                juhun = 9;
            }
        } else if (((scrollnumber + 1) % a) == 2) {//70%일때
            if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운" || itemstring[scrollnumber / a] == "체력") {
                juhun = 34;
            } else if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "마력(지력)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                juhun = 200;
            } else if (itemstring[scrollnumber / a] == "공격력" || itemstring[scrollnumber / a] == "마력") {
                juhun = 11;
            }
        } else if ((((scrollnumber + 1)) % a) == 0) {//30%일때
            if (a == 4) {
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운" || itemstring[scrollnumber / a] == "체력") {
                    juhun = 50;
                } else if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "마력(지력)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    juhun = 290;
                } else if (itemstring[scrollnumber / a] == "공격력" || itemstring[scrollnumber / a] == "마력") {
                    juhun = 20;
                }
            } else {
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운" || itemstring[scrollnumber / a] == "체력") {
                    juhun = 40;
                } else if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "마력(지력)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    juhun = 240;
                } else if (itemstring[scrollnumber / a] == "공격력" || itemstring[scrollnumber / a] == "마력") {
                    juhun = 14;
                }
            }
        } else if ((((scrollnumber + 1)) % a) == 3) {
            if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운" || itemstring[scrollnumber / a] == "체력") {
                juhun = 40;
            } else if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "마력(지력)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                juhun = 240;
            } else if (itemstring[scrollnumber / a] == "공격력" || itemstring[scrollnumber / a] == "마력") {
                juhun = 14;
            }
        }
        return juhun;
    }
    public static void Itemupgrade(MapleClient c, Equip item, int scrollnumber, boolean success, MapleCharacter chr, boolean zeroWeapon) {//숫자별로
        int juhun = ItemJuhun(c, scrollnumber);
        int 주흔코드 = 4001832;
        if (success) {
            String itemstring[] = {c.getPlayer().scrollstring1(), c.getPlayer().scrollstring2(), c.getPlayer().scrollstring3(), c.getPlayer().scrollstring4(), c.getPlayer().scrollstring5()};
            int a = 0;
            if (c.getPlayer().scrollcount() % 3 == 0 && c.getPlayer().scrollcount() != 12) {
                a = 3;
            } else if (c.getPlayer().scrollcount() % 4 == 0 || c.getPlayer().scrollcount() == 12) {
                a = 4;
            }
            if (((scrollnumber +1) % a) == 1) {//100%일때
                if (itemstring[scrollnumber / a] == "힘"){
                        item.setStr((short) (item.getStr() + 2));
                } else if (itemstring[scrollnumber / a] == "민첩"){
                item.setDex((short) (item.getDex() + 2));
                } else if (itemstring[scrollnumber / a] == "지력") {
                item.setInt((short) (item.getInt() + 2));
                } else if (itemstring[scrollnumber / a] == "운") {
                item.setLuk((short) (item.getLuk() + 2));
                } else if (itemstring[scrollnumber / a] == "공격력(힘)") {
                    item.setStr((short) (item.getStr() + 1));
                } else if (itemstring[scrollnumber / a] == "공격력(민첩)") {
                    item.setDex((short) (item.getDex() + 1));
                } else if (itemstring[scrollnumber / a] == "공격력(운)") {
                    item.setLuk((short) (item.getLuk() + 1));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setInt((short) (item.getInt() + 1));
                } else if (itemstring[scrollnumber / a] == "공격력(체력)" || itemstring[scrollnumber / a] == "체력") {
                    item.setHp((short) (item.getHp() + 55));
                }
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운") {
                    item.setHp((short) (item.getHp() + 20));
                    item.setWdef((short) (item.getWdef() + 2));
                    item.setMdef((short) (item.getMdef() + 2));
                }
                if (itemstring[scrollnumber / a] == "공격력") {
                    item.setWatk((short) (item.getWatk() + 1));
                } else if (itemstring[scrollnumber / a] == "마력") {
                    item.setMatk((short) (item.getMatk() + 1));
                }
                if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    item.setWatk((short) (item.getWatk() + 3));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setMatk((short) (item.getMatk() + 3));
                }

            } else if (((scrollnumber + 1) % a) == 2) {//70%일때
                if (itemstring[scrollnumber / a] == "힘"){
                item.setStr((short) (item.getStr() + 3));
                } else if (itemstring[scrollnumber / a] == "민첩"){
                item.setDex((short) (item.getDex() + 3));
                } else if (itemstring[scrollnumber / a] == "지력") {
                item.setInt((short) (item.getInt() + 3));
                } else if (itemstring[scrollnumber / a] == "운") {
                item.setLuk((short) (item.getLuk() + 3));
                } else if (itemstring[scrollnumber / a] == "공격력(힘)") {
                    item.setStr((short) (item.getStr() + 2));
                } else if (itemstring[scrollnumber / a] == "공격력(민첩)") {
                    item.setDex((short) (item.getDex() + 2));
                } else if (itemstring[scrollnumber / a] == "공격력(운)") {
                    item.setLuk((short) (item.getLuk() + 2));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setInt((short) (item.getInt() + 2));
                } else if (itemstring[scrollnumber / a] == "공격력(체력)" || itemstring[scrollnumber / a] == "체력") {
                    item.setHp((short) (item.getHp() + 110));
                }
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운") {
                    item.setHp((short) (item.getHp() + 40));
                    item.setWdef((short) (item.getWdef() + 4));
                    item.setMdef((short) (item.getMdef() + 4));
                }
                if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    item.setWatk((short) (item.getWatk() + 5));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setMatk((short) (item.getMatk() + 5));
                }
                if (itemstring[scrollnumber / a] == "공격력") {
                    item.setWatk((short) (item.getWatk() + 2));
                } else if (itemstring[scrollnumber / a] == "마력") {
                    item.setMatk((short) (item.getMatk() + 2));
                }
            } else if ((((scrollnumber + 1)) % a) == 0) {//30%일때
                if (a == 4) {
                    if (itemstring[scrollnumber / a] == "힘"){
                    item.setStr((short) (item.getStr() + 7));
                    } else if (itemstring[scrollnumber / a] == "민첩"){
                    item.setDex((short) (item.getDex() + 7));
                    } else if (itemstring[scrollnumber / a] == "지력") {
                    item.setInt((short) (item.getInt() + 7));
                    } else if (itemstring[scrollnumber / a] == "운") {
                    item.setLuk((short) (item.getLuk() + 7));
                    } else if (itemstring[scrollnumber / a] == "공격력(힘)") {
                    item.setStr((short) (item.getStr() + 4));
                } else if (itemstring[scrollnumber / a] == "공격력(민첩)") {
                    item.setDex((short) (item.getDex() + 4));
                } else if (itemstring[scrollnumber / a] == "공격력(운)") {
                    item.setLuk((short) (item.getLuk() + 4));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setInt((short) (item.getInt() + 4));
                } else if (itemstring[scrollnumber / a] == "공격력(체력)" || itemstring[scrollnumber / a] == "체력") {
                    item.setHp((short) (item.getHp() + 255));
                }
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운") {
                    item.setHp((short) (item.getHp() + 90));
                    item.setWdef((short) (item.getWdef() + 9));
                    item.setMdef((short) (item.getMdef() + 9));
                }
                if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    item.setWatk((short) (item.getWatk() + 9));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setMatk((short) (item.getMatk() + 9));
                }
                if (itemstring[scrollnumber / a] == "공격력") {
                    item.setWatk((short) (item.getWatk() + 4));
                } else if (itemstring[scrollnumber / a] == "마력") {
                    item.setMatk((short) (item.getMatk() + 4));
                }
                } else {
                if (itemstring[scrollnumber / a] == "힘"){
                item.setStr((short) (item.getStr() + 5));
                } else if (itemstring[scrollnumber / a] == "민첩"){
                item.setDex((short) (item.getDex() + 5));
                } else if (itemstring[scrollnumber / a] == "지력") {
                item.setInt((short) (item.getInt() + 5));
                } else if (itemstring[scrollnumber / a] == "운") {
                item.setLuk((short) (item.getLuk() + 5));
                } else if (itemstring[scrollnumber / a] == "공격력(민첩)") {
                    item.setDex((short) (item.getDex() + 3));
                } else if (itemstring[scrollnumber / a] == "공격력(운)") {
                    item.setLuk((short) (item.getLuk() + 3));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setInt((short) (item.getInt() + 3));
                } else if (itemstring[scrollnumber / a] == "공격력(체력)" || itemstring[scrollnumber / a] == "체력") {
                    item.setHp((short) (item.getHp() + 180));
                } else if (itemstring[scrollnumber / a] == "공격력(힘)") {
                    item.setStr((short) (item.getStr() + 3));
                }
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운") {
                    item.setHp((short) (item.getHp() + 70));
                    item.setWdef((short) (item.getWdef() + 7));
                    item.setMdef((short) (item.getMdef() + 7));
                }
                if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    item.setWatk((short) (item.getWatk() + 7));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setMatk((short) (item.getMatk() + 7));
                }
                if (itemstring[scrollnumber / a] == "공격력") {
                    item.setWatk((short) (item.getWatk() + 3));
                } else if (itemstring[scrollnumber / a] == "마력") {
                    item.setMatk((short) (item.getMatk() + 3));
                }
                }
            } else if ((((scrollnumber + 1)) % a) == 3) {
                if (itemstring[scrollnumber / a] == "힘"){
                item.setStr((short) (item.getStr() + 5));
                } else if (itemstring[scrollnumber / a] == "민첩"){
                item.setDex((short) (item.getDex() + 5));
                } else if (itemstring[scrollnumber / a] == "지력") {
                item.setInt((short) (item.getInt() + 5));
                } else if (itemstring[scrollnumber / a] == "운") {
                item.setLuk((short) (item.getLuk() + 5));
                } else if (itemstring[scrollnumber / a] == "공격력(민첩)") {
                    item.setDex((short) (item.getDex() + 3));
                } else if (itemstring[scrollnumber / a] == "공격력(운)") {
                    item.setLuk((short) (item.getLuk() + 3));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setInt((short) (item.getInt() + 3));
                } else if (itemstring[scrollnumber / a] == "공격력(체력)" || itemstring[scrollnumber / a] == "체력") {
                    item.setHp((short) (item.getHp() + 180));
                } else if (itemstring[scrollnumber / a] == "공격력(힘)") {
                    item.setStr((short) (item.getStr() + 3));
                }
                if (itemstring[scrollnumber / a] == "힘" || itemstring[scrollnumber / a] == "민첩" || itemstring[scrollnumber / a] == "지력" || itemstring[scrollnumber / a] == "운") {
                    item.setHp((short) (item.getHp() + 70));
                    item.setWdef((short) (item.getWdef() + 7));
                    item.setMdef((short) (item.getMdef() + 7));
                }
                if (itemstring[scrollnumber / a] == "공격력(힘)" || itemstring[scrollnumber / a] == "공격력(민첩)" || itemstring[scrollnumber / a] == "공격력(운)" || itemstring[scrollnumber / a] == "공격력(체력)") {
                    item.setWatk((short) (item.getWatk() + 7));
                } else if (itemstring[scrollnumber / a] == "마력(지력)") {
                    item.setMatk((short) (item.getMatk() + 7));
                }
                if (itemstring[scrollnumber / a] == "공격력") {
                    item.setWatk((short) (item.getWatk() + 3));
                } else if (itemstring[scrollnumber / a] == "마력") {
                    item.setMatk((short) (item.getMatk() + 3));
                }
            }
            item.setLevel((byte) (item.getLevel() + 1));
            item.setUpgradeSlots((byte)((byte)item.getUpgradeSlots() - 1));
        } else {
            if (!ItemFlag.SAFETY.check(item.getFlag())) {
                item.setUpgradeSlots((byte)((byte)item.getUpgradeSlots() - 1));
            }
        }
        if (ItemFlag.SAFETY.check(item.getFlag())) {
            item.setFlag((short) (item.getFlag() - ItemFlag.SAFETY.getValue()));
        }
        if (!zeroWeapon) {
            chr.gainItem(주흔코드, (short)-juhun, false, 0, null);
        }
    }
    
    public static Packet EquipTrace(Equip equip, Equip trace) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(0x65);
        packet.writeInt(1);
        packet.write(0);
        PacketProvider.addStarForceItemInfo(packet, equip);
        PacketProvider.addStarForceItemInfo(packet, trace);
        
        return packet.getPacket();
    }
    
    public static Packet UpdateItemStar(Equip equip, int success, MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(0x65);
        packet.write(success);
        packet.writeInt(0);
        PacketProvider.addStarForceItemInfo(packet, equip);
        StarSystemUpgrade(equip, success, chr);
        if (success != 4) {
            PacketProvider.addStarForceItemInfo(packet, equip);
        }
        return packet.getPacket();
    }
    
    public static String ScrollDesc(MapleCharacter chr, int scrollnumber) {
        String itemstring[] = {chr.scrollstring1(), chr.scrollstring2(), chr.scrollstring3(), chr.scrollstring4(), chr.scrollstring5()};
        String persent[] = {"15%", "100%", "70%", "30%"};
        int a = 0;
        if (chr.scrollcount() % 3 == 0 && chr.scrollcount() != 12) {
            a = 3;
        } else if (chr.scrollcount() % 4 == 0 || chr.scrollcount() == 12) {
            a = 4;
        }
        return persent[(scrollnumber + 1) % 4] + " " + itemstring[scrollnumber / a] + " 주문서";
    }

    public static Packet UpdateItemResult(Equip item, boolean success, int scrollnumber, MapleCharacter chr) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(0x64);
        packet.write(0);
        packet.writeInt(success ? 1 : 0);
        packet.writeMapleAsciiString(ScrollDesc(chr, scrollnumber)); //1.2.251+
        PacketProvider.addStarForceItemInfo(packet, item);
        Equip zeros = null;
        if (GameConstants.isZero(chr.getJob())) {
            if (item.getPosition() == -11) {
                zeros = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
            } else if (item.getPosition() == -10) {
                zeros = (Equip)chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
            }
        }
        if (zeros != null) {
            Itemupgrade(chr.getClient(), zeros, scrollnumber, success, chr, true);
        }
        Itemupgrade(chr.getClient(), item, scrollnumber, success, chr, false);
        PacketProvider.addStarForceItemInfo(packet, item);
        
        return packet.getPacket();
    }
    
    public static void scrollstaticvoid(int job, int itemtype, MapleClient cc) {
        boolean a = false;
        boolean b = false;
        boolean c = false;
        if (itemtype > 120 && itemtype < 172) {//a = 무기
            a = true;
        } else if (itemtype == 108) {//b = 장갑
            b = true;
        } else {//c = 다른것들
            c = true;
        }
        if (a) {
            switch(job) {
                case 0:
                    cc.getPlayer().itemstaticcounts(5);
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().itemstatic1s(5); //힘
                    cc.getPlayer().itemstatic2s(18); //지력
                    cc.getPlayer().itemstatic3s(9); //민첩
                    cc.getPlayer().itemstatic4s(33); //운
                    cc.getPlayer().itemstatic5s(257); //체력
                    cc.getPlayer().scrollstring1s("공격력(힘)");
                    cc.getPlayer().scrollstring2s("마력(지력)");
                    cc.getPlayer().scrollstring3s("공격력(민첩)");
                    cc.getPlayer().scrollstring4s("공격력(운)");
                    cc.getPlayer().scrollstring5s("공격력(체력)");
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 1:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(5); //공격력(힘)
                    cc.getPlayer().itemstatic2s(257); //체력
                    cc.getPlayer().scrollstring1s("공격력(힘)");
                    cc.getPlayer().scrollstring2s("공격력(체력)");
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 2:
                    cc.getPlayer().itemstaticcounts(1);
                    cc.getPlayer().itemstatic1s(18); //마력(지력)
                    cc.getPlayer().scrollstring1s("마력(지력)");
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 4:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(9); //공격력(민첩)
                    cc.getPlayer().itemstatic2s(257); //체력
                    cc.getPlayer().scrollstring1s("공격력(민첩)");
                    cc.getPlayer().scrollstring2s("공격력(체력)");
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 8:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(33); //공격력(운)
                    cc.getPlayer().itemstatic2s(257); //체력
                    cc.getPlayer().scrollstring1s("공격력(운)");
                    cc.getPlayer().scrollstring2s("공격력(체력)");
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 16:
                case 24:
                    cc.getPlayer().itemstaticcounts(3);
                    cc.getPlayer().itemstatic1s(5); //공격력(힘)
                    cc.getPlayer().itemstatic2s(9); //공격력(민첩)
                    cc.getPlayer().itemstatic3s(257); //체력
                    cc.getPlayer().scrollstring1s("공격력(힘)");
                    cc.getPlayer().scrollstring2s("공격력(민첩)");
                    cc.getPlayer().scrollstring3s("공격력(체력)");
                    cc.getPlayer().scrollacounts(4);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
            }
        } else if (b) {
            switch(job) {
                case 0:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(1); //공격력
                    cc.getPlayer().itemstatic2s(2); //마력
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollstring2s("마력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 1:
                    cc.getPlayer().itemstaticcounts(1);
                    cc.getPlayer().itemstatic1s(1);
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 2:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(1); //공격력
                    cc.getPlayer().itemstatic2s(2); //마력
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollstring2s("마력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 4:
                    cc.getPlayer().itemstaticcounts(1);
                    cc.getPlayer().itemstatic1s(1);
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 8:
                    cc.getPlayer().itemstaticcounts(1);
                    cc.getPlayer().itemstatic1s(1);
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 16:
                    cc.getPlayer().itemstaticcounts(1);
                    cc.getPlayer().itemstatic1s(1);
                    cc.getPlayer().scrollstring1s("공격력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
            }
        } else if (c) {
            switch(job) {
                case 0:
                    cc.getPlayer().itemstaticcounts(5);
                    cc.getPlayer().itemstatic1s(452);
                    cc.getPlayer().itemstatic2s(464);
                    cc.getPlayer().itemstatic3s(456);
                    cc.getPlayer().itemstatic4s(480);
                    cc.getPlayer().itemstatic5s(448);
                    cc.getPlayer().scrollstring1s("힘");
                    cc.getPlayer().scrollstring2s("지력");
                    cc.getPlayer().scrollstring3s("민첩");
                    cc.getPlayer().scrollstring4s("운");
                    cc.getPlayer().scrollstring5s("체력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 1:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(452);
                    cc.getPlayer().itemstatic2s(448);
                    cc.getPlayer().scrollstring1s("힘");
                    cc.getPlayer().scrollstring2s("체력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 2:
                    cc.getPlayer().itemstaticcounts(2);//주문서 종류의 갯수
                    cc.getPlayer().itemstatic1s(464);//주문서종류별 고유번호1
                    cc.getPlayer().itemstatic2s(448);//2
                    cc.getPlayer().scrollstring1s("지력");//주문서 종류별 이름
                    cc.getPlayer().scrollstring2s("체력");
                    cc.getPlayer().scrollacounts(3);//주문서 종류별 주문서의 갯수
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());//총 주문서의 갯수(주문서종류 * 종류별주문서의갯수)
                    break;
                case 4:
                    cc.getPlayer().itemstaticcounts(2);
                    cc.getPlayer().itemstatic1s(456);
                    cc.getPlayer().itemstatic2s(448);
                    cc.getPlayer().scrollstring1s("민첩");
                    cc.getPlayer().scrollstring2s("체력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 8:
                    cc.getPlayer().itemstaticcounts(3);
                    cc.getPlayer().itemstatic1s(452);
                    cc.getPlayer().itemstatic2s(456);
                    cc.getPlayer().itemstatic3s(480);
                    cc.getPlayer().scrollstring1s("힘");
                    cc.getPlayer().scrollstring2s("민첩");
                    cc.getPlayer().scrollstring3s("운");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
                case 16:
                    cc.getPlayer().itemstaticcounts(3);
                    cc.getPlayer().itemstatic1s(452);
                    cc.getPlayer().itemstatic2s(456);
                    cc.getPlayer().itemstatic3s(448);
                    cc.getPlayer().scrollstring1s("힘");
                    cc.getPlayer().scrollstring2s("민첩");
                    cc.getPlayer().scrollstring3s("체력");
                    cc.getPlayer().scrollacounts(3);
                    cc.getPlayer().scrollcounts(cc.getPlayer().itemstaticcount() * cc.getPlayer().scrollacount());
                    break;
            }
        }
    }
    public static Packet AddItemOnSystem(MapleClient c, int ScrollCount, Equip item, int a, int FeverTime) {
        WritingPacket packet = new WritingPacket();
        packet.writeShort(SendPacketOpcode.EQUIP_UPGRADE_SYSTEM_SEND.getValue());
        packet.write(0x32);
        packet.write(FeverTime);
        int itemtype = item.getItemId() / 10000;
        final ItemInformation ii = ItemInformation.getInstance();
        final MapleData IData = ii.getItemData(item.getItemId());
        final MapleData info = IData.getChildByPath("info");
        int Job = MapleDataTool.getInt("reqJob", info, 0);
        scrollstaticvoid(Job, itemtype, c);
        int itemstatic[] = {c.getPlayer().itemstatic1(), c.getPlayer().itemstatic2(), c.getPlayer().itemstatic3(), c.getPlayer().itemstatic4(), c.getPlayer().itemstatic5()};
        String itemstring[] = {c.getPlayer().scrollstring1(), c.getPlayer().scrollstring2(), c.getPlayer().scrollstring3(), c.getPlayer().scrollstring4(), c.getPlayer().scrollstring5()};
        String Persent[] = {"100% ", "70% ", "30% ", "15% "};
        int juhunatk[] = {160, 200, 240, 290};
        int juhundefence[] = {26, 34, 40};
        int juhunglove[] = {9, 11, 14};
        int statdefence1[] = {2, 3, 5};
        int statdefence2[] = {2, 4, 7};
        int statdefenceHP[] = {20, 40, 70};
        int statHP[] = {55, 115, 180};
        int statAtkHp[] = {55, 115, 180, 255};
        int stathpa[] = {1, 2, 4};
        int statWeapon[] = {1, 2, 3, 4};
        int statWeapona[] = {3, 5, 7, 9};
        packet.write(c.getPlayer().scrollcount());
        for (int j = 0; j < c.getPlayer().itemstaticcount(); j++) {
            for (int i = 0; i < c.getPlayer().scrollacount(); i++) {
                packet.writeInt(i);
                packet.writeMapleAsciiString(Persent[i] + itemstring[j] + " 주문서");
                packet.writeLong(0); //1.2.251+, MVP 추가확률.
                packet.writeInt(itemstatic[j]);
                if (itemstatic[j] > 448 && itemstatic[j] <= 480) {
                    packet.writeInt(statdefence1[i]);
                    packet.writeInt(statdefence2[i]);
                    packet.writeInt(statdefence2[i]);
                    packet.writeInt(statdefenceHP[i]);
                    packet.writeInt(juhundefence[i]);
                } else if (itemstatic[j] == 448) {
                    packet.writeInt(statHP[i]);
                    packet.writeInt(stathpa[i]);
                    packet.writeInt(stathpa[i]);
                    packet.writeInt(juhundefence[i]);
                } else if (itemstatic[j] == 1 || itemstatic[j] == 2) {
                    packet.writeInt(statWeapon[i]);
                    packet.writeInt(juhunglove[i]);
                } else if (itemstatic[j] == 5 || itemstatic[j] == 9 || itemstatic[j] == 18 || itemstatic[j] == 33) {
                    packet.writeInt(statWeapona[i]);
                    packet.writeInt(statWeapon[i]);
                    packet.writeInt(juhunatk[i]);
                } else if (itemstatic[j] == 257) {
                    packet.writeInt(statWeapona[i]);
                    packet.writeInt(statAtkHp[i]);
                    packet.writeInt(juhunatk[i]);
                }
                packet.write(0);
                c.getPlayer().scrollorders(c.getPlayer().scrollorder() + 1);
            }
        }
        return packet.getPacket();
    }
    
    public static long StarForceMeso(Equip item) {
        long base = 0;
        int ReqLevel = ItemInformation.getInstance().getEquipStats(item.getItemId()).get("reqLevel");
        int enhance = item.getEnhance() == 0 ? 0 : item.getEnhance() - 25;
        if (isSuperial(item.getItemId()).right) {
            switch(isSuperial(item.getItemId()).left) {
                case "Helisium":
                    return 5956600;
                case "Nova" :
                    return 18507900;
                case "Tilent" :
                    return 55832200;
            }
        } else {
        if (ReqLevel < 110) {
            base = 41000;
            return (base + (enhance * 40000));
        } else if (ReqLevel >= 110 && ReqLevel < 120) {
            base = 54200;
            return (base + ((enhance-10) * 53300));
        } else if (ReqLevel >= 120 && ReqLevel < 130) {
            if (enhance <= 9) {
                base = 70100;
                return (base + (enhance * 70200));
            } else {
                base = 5602100;
                return (base + ((enhance-10) * 1509500));
            }
        } else if (ReqLevel >= 130 && ReqLevel < 140) {
            if (enhance <= 9) {
                base = 88900;
                return (base + (enhance * 98800));
            } else {
                base = 7122300;
                return (base + ((enhance-10) * 2156500));
            }
        } else if (ReqLevel >= 140 && ReqLevel < 150) {
            if (enhance <= 9) {
                base = 110800;
                return (base + (enhance * 110900));
            } else {
                base = 8895400;
                return (base + ((enhance-10) * 5706700));
            }
        } else if (ReqLevel >= 150) {
            if (enhance <= 9) {
                base = 136000;
                return (base + (enhance * 153600));
            } else {
                base = 10940700;
                return (base + ((enhance-10) * 8930500));
            }
        }
        }
        return 0;
    }
}