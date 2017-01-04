package client.commands;

import java.util.Map;

import client.MapleCharacter;
import client.MapleCharacterStat;
import client.MapleClient;
import client.items.IEquip;
import client.items.IItem;
import client.items.MapleInventoryType;
import client.stats.PlayerStat;
import constants.ServerConstants;
import launch.ChannelServer;
import packet.creators.MainPacketCreator;
import scripting.NPCScriptManager;
import scripting.ReactorScriptManager;
import server.items.InventoryManipulator;
import server.life.MapleMonsterProvider;
import server.maps.MapleMap;
import server.maps.MaplePortal;
import tools.ArrayMap;
import tools.Pair;

public class PlayerCommand implements Command {
	@Override
	public void execute(final MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();

		if (splitted[0].equals("@��")) {
			int str = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getStr() + str > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < str || c.getPlayer().getRemainingAp() < 0 || str < 0) {
				c.getPlayer().dropMessage(5, "������ �߻��߽��ϴ�.");
			} else {
				stat.setStr(stat.getStr() + str);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - str);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.STR, stat.getStr());
			}
		} else if (splitted[0].equals("@��Ʈ")) {
			int int_ = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getInt() + int_ > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < int_ || c.getPlayer().getRemainingAp() < 0 || int_ < 0) {
				c.getPlayer().dropMessage(5, "������ �߻��߽��ϴ�.");
			} else {
				stat.setInt(stat.getInt() + int_);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - int_);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.INT, stat.getInt());
			}
		} else if (splitted[0].equals("@����")) {
			int dex = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getDex() + dex > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < dex || c.getPlayer().getRemainingAp() < 0 || dex < 0) {
				c.getPlayer().dropMessage(5, "������ �߻��߽��ϴ�.");
			} else {
				stat.setDex(stat.getDex() + dex);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - dex);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.DEX, stat.getDex());
			}
		} else if (splitted[0].equals("@��")) {
			int luk = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getLuk() + luk > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < luk || c.getPlayer().getRemainingAp() < 0 || luk < 0) {
				c.getPlayer().dropMessage(5, "������ �߻��߽��ϴ�.");
			} else {
				stat.setLuk(stat.getLuk() + luk);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - luk);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.LUK, stat.getLuk());
			}
		} else if (splitted[0].equals("@�κ��ʱ�ȭ")) {
			Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<Pair<Short, Short>, MapleInventoryType>();
			if (splitted[1].equals("���")) {
				for (MapleInventoryType type : MapleInventoryType.values()) {
					for (IItem item : c.getPlayer().getInventory(type)) {
						eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), type);
					}
				}
			} else if (splitted[1].equals("����")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIPPED);
				}
			} else if (splitted[1].equals("���")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
				}
			} else if (splitted[1].equals("�Һ�")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
				}
			} else if (splitted[1].equals("��ġ")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
				}
			} else if (splitted[1].equals("��Ÿ")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
				}
			} else if (splitted[1].equals("ĳ��")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
				}
			} else {
				c.getPlayer().dropMessage(6, "[���/����/���/�Һ�/��ġ/��Ÿ/ĳ��]");
			}
			for (Map.Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
				InventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
			}
		} else if (splitted[0].equals("@����")) {
			//TODO @���� ��ɾ ����� ������ �����ϴ� �뵵�� �ƴѵ�... Ȯ���غ���.
//			NPCScriptManager.getInstance().dispose(c);
//			MapleMonsterProvider.getInstance().clearDrops();
//			ReactorScriptManager.getInstance().clearDrops();
//			c.getPlayer().dropMessage(5, "[�˸�] ������ �Ϸ�Ǿ����ϴ�.");
		} else if (splitted[0].equals("@��") || splitted[0].equals("@��")) {
			NPCScriptManager.getInstance().dispose(c);
			c.getSession().write(MainPacketCreator.resetActions());
		} else if (splitted[0].equals("@������������")) {
			if (c.getPlayer().getParty() == null) {
				c.getPlayer().dropMessage(5, "���Ե� ��Ƽ�� �����ϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition() == null) {
				c.getPlayer().dropMessage(5, "���Ե� �����밡 �����ϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossMap() == -1) {
				c.getPlayer().dropMessage(5, "���� �����밡 ������ ������� �ƴմϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossChannel() == -1) {
				c.getPlayer().dropMessage(5, "���� �����밡 ������ ������� �ƴմϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossChannel() != c.getChannel()) {
				c.getPlayer().dropMessage(5, "������ ��� �ִ� ������� ä���� �ٸ��ϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().containDeadChar(c.getPlayer().getId())) {
				c.getPlayer().dropMessage(5, "������ ��� �� ����Ͽ� ������ �� �� �����ϴ�.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().isBossKilled()) {
				c.getPlayer().dropMessage(5, "�̹� ������ ���� ���̹Ƿ� �������� �� �����ϴ�.");
				return;
			}
			MapleMap map = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getParty().getExpedition().getLastBossMap());
			c.getPlayer().changeMap(map, map.getPortal(0));
			switch (map.getId()) {
			case 280030000:
			case 280030001:
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm06/FinalFight"));
				break;
			case 240060200: // ȥ����
			case 240060201: // ī����ȥ����
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm14/HonTale"));
				break;
			case 220080001: // ��Ǯ
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm09/TimeAttack"));
				break;
			}
		} else if (splitted[0].equals("@����") || splitted[0].equals("@��׽ý�") || splitted[0].equals("@����")) {
			int jobid = c.getPlayer().getJob();
			if (jobid == 0 || jobid == 1000 || jobid == 2000 || jobid == 2001 || jobid == 2002 || jobid == 2003 || jobid == 2004 || jobid == 3000 || jobid == 3001 || jobid == 5000 || jobid == 6000 || jobid == 6001 || (jobid == 10112 && c.getPlayer().getMapId() == ServerConstants.startMap)) {
				c.getPlayer().dropMessage(5, "[�ý���] �ʺ��ڴ� �������� �̵� �� �� �����ϴ�.");
				return;
			}
			if (c.getPlayer().getMapId() == 910340500 || c.getPlayer().getMapId() == 240050200 || c.getPlayer().getMapId() == 272000600 || c.getPlayer().getMapId() == 921160400) {
				c.getPlayer().dropMessage(5, "[�ý���] �������̵� ���߿��� �̵� �� �� �����ϴ�.");
				return;
			}
			MapleMap target = c.getChannelServer().getMapFactory().getMap(100000000);
			MaplePortal targetPortal = null;
			if (splitted.length > 1) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splitted[1]));
				} catch (IndexOutOfBoundsException e) {
					c.getPlayer().dropMessage(5, "���� ��Ż�� ���� �ֽ��ϴ�.");
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		} else if (splitted[0].equals("@��������")) {
			int jobid = c.getPlayer().getJob();
			if (jobid == 0 || jobid == 1000 || jobid == 2000 || jobid == 2001 || jobid == 2002 || jobid == 2003 || jobid == 2004 || jobid == 3000 || jobid == 3001 || jobid == 5000 || jobid == 6000 || jobid == 6001) {
				c.getPlayer().dropMessage(5, "�ʺ��ڴ� ������������ �̵� �� �� �����ϴ�.");
				return;
			}
			MapleMap target = c.getChannelServer().getMapFactory().getMap(910000000);
			MaplePortal targetPortal = null;
			if (splitted.length > 1) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splitted[1]));
				} catch (IndexOutOfBoundsException e) {
					// noop, assume the gm didn't know how many portals there
					// are
					c.getPlayer().dropMessage(5, "���� ��Ż�� ���� �ֽ��ϴ�.");
				} catch (NumberFormatException a) {
					// noop, assume that the gm is drunk
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		} else if (splitted[0].equals("@����") || splitted[0].equals("@��ɾ�")) {
			c.getPlayer().dropMessage(5, "��밡���� ��ɾ�� ������ �����ϴ� :");
			c.getPlayer().dropMessage(5, "@��, @����, @��Ʈ, @�� <���� ��ġ> : �ش� ������ ���콺 Ŭ�� ��� ���� �� �ֽ��ϴ�.");
			c.getPlayer().dropMessage(5, "@������������ : ���� ���̵� ���̴� �������� ������ ������ �̵��մϴ�.");
			c.getPlayer().dropMessage(5, "@�� : ���� �� ä�ÿܿ� �ƹ��͵� �ȵɶ� ����ϼ���.");
			c.getPlayer().dropMessage(5, "@���� : ���� ĳ���͸� ������ �����մϴ�.");
			c.getPlayer().dropMessage(5, "@���� : " + ServerConstants.serverName + " ������ ��׽ý��� �̵��˴ϴ�.");
			c.getPlayer().dropMessage(5, "@�κ��ʱ�ȭ : �κ��ʱ�ȭ �� ���/����/���/�Һ�/��ġ/��Ÿ/ĳ��");
			c.getPlayer().dropMessage(5, "@��ų������ : ���� �ڽ��� ���� ��ų�� �������մϴ�.");
			c.getPlayer().dropMessage(5, "@������������ : �������� �������⸦ ���� �մϴ�.");
			c.getPlayer().dropMessage(5, "@����� : ����� �������� ���׸�ġ�� ȣ�� �մϴ�.");
			c.getPlayer().dropMessage(5, "@���ݸ��ε� : HP,MP�� �� ��ä������ ����ϸ� �� ä�����ϴ�.");
			c.getPlayer().dropMessage(5, "@�Ŀ�����Ʈ���� : ���� �����ϰ� �ִ� �Ŀ�����Ʈ�� �ٸ� �������� ���� �� �� �ֽ��ϴ�.");
			c.getPlayer().dropMessage(5, "~�Ҹ� : ��üä��");
		} else if (splitted[0].equals("@��õ��")) {
			NPCScriptManager.getInstance().dispose(c);
			NPCScriptManager.getInstance().start(c, 9010031);
		} else if (splitted[0].equals("@������������")) {
			IEquip equip = null;
			equip = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
			if (equip == null) {
				c.getPlayer().Message(1, "�������� �������Ⱑ �������� �ʽ��ϴ�.");
				c.getSession().write(MainPacketCreator.resetActions());
				return;
			}
			c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot((byte) -10);
			c.getPlayer().equipChanged();
			InventoryManipulator.addFromDrop(c, equip, false);
			c.getPlayer().getStat().recalcLocalStats();
			c.getPlayer().send(MainPacketCreator.getPlayerInfo(c.getPlayer()));
			MapleMap currentMap = c.getPlayer().getMap();
			currentMap.removePlayer(c.getPlayer());
			currentMap.addPlayer(c.getPlayer());
		} else if (splitted[0].equals("@�����")) {
			NPCScriptManager.getInstance().start(c, 9030300);
			c.getPlayer().ea();
		} else if (splitted[0].equals("@�Ŀ�����Ʈ����")) {
			MapleCharacter who = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
			final int rc = Integer.parseInt(splitted[2]);
			if (who != null) {
				if (rc > 0) {
					if (c.getPlayer().getRC() > rc) {
						who.gainRC(rc);
						who.dropMessage(6, "[�˸�] " + c.getPlayer().getName() + "�����κ��� " + rc + "�Ŀ�����Ʈ�� ȹ���߽��ϴ�.");
						c.getPlayer().dropMessage(5, "[�˸�] " + splitted[1] + "�Կ��� " + rc + "�Ŀ�����Ʈ�� �����߽��ϴ�.");
					} else {
						c.getSession().write(MainPacketCreator.getGMText(20, "[�˸�] �����Ͻ� ����Ʈ�� �����մϴ�."));
					}
				} else {
					c.getSession().write(MainPacketCreator.getGMText(20, "[�˸�] �����Ͻ� ����Ʈ�� 0����Ʈ���� �۽��ϴ�."));
				}
			} else {
				c.getPlayer().dropMessage(5, "��� �÷��̾ �߰����� ���߽��ϴ�.");
			}
		} else if (splitted[0].equals("@���ݸ��ε�")) {
			MapleCharacter player = c.getPlayer();
			player.getStat().recalcLocalStats();
			c.getPlayer().dropMessage(5, "[�˸�] ���ݸ��ε带 �Ϸ� �Ͽ����ϴ�.");
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] { new CommandDefinition("��", "<�ø���>", "�ش� ������ ���콺 Ŭ�� ��� ���� �� �ֽ��ϴ�.", 0), new CommandDefinition("��Ʈ", "<�ø���>", "�ش� ������ ���콺 Ŭ�� ��� ���� �� �ֽ��ϴ�.", 0), new CommandDefinition("����", "<�ø���>", "�ش� ������ ���콺 Ŭ�� ��� ���� �� �ֽ��ϴ�.", 0), new CommandDefinition("��", "<�ø���>", "�ش� ������ ���콺 Ŭ�� ��� ���� �� �ֽ��ϴ�.", 0), new CommandDefinition("��", "", "���� �� ä�ÿܿ� �ƹ��͵� �ȵɶ� ����ϼ���.", 0),
				new CommandDefinition("��", "", "���� �� ä�ÿܿ� �ƹ��͵� �ȵɶ� ����ϼ���.", 0), new CommandDefinition("��ɾ�", "", "���� ��ɾ ����մϴ�.", 0), new CommandDefinition("����", "", "ĳ���͸� ������ �����մϴ�.", 0), new CommandDefinition("������������", "", "���� ���̵� ���̴� �������� ������ ������ �̵��մϴ�.", 0), new CommandDefinition("����", "", "�ش� ������ ������ ��׽ý��� �̵��մϴ�.", 0), new CommandDefinition("��׽ý�", "", "�ش� ������ ������ ��׽ý��� �̵��մϴ�.", 0),
				new CommandDefinition("��������", "", "������������ �̵��մϴ�.", 0), new CommandDefinition("�κ��ʱ�ȭ", "���/����/���/�Һ�/��ġ/��Ÿ/ĳ��", "�ش� ���� �κ��丮�� ��� ��������ϴ�.", 0), new CommandDefinition("����", "", "���� ��ɾ ����մϴ�.", 0), new CommandDefinition("����", "", "�����̵�", 0), new CommandDefinition("��õ��", "", "��õ��", 0), new CommandDefinition("�����", "", "�����", 0), new CommandDefinition("���ݸ��ε�", "", "���ݸ��ε�", 0),
				new CommandDefinition("������������", "", "������������", 0), new CommandDefinition("�Ŀ�����Ʈ����", "", "�Ŀ�����Ʈ�� �����մϴ�.", 0), };
	}
}
