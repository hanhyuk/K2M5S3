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

		if (splitted[0].equals("@힘")) {
			int str = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getStr() + str > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < str || c.getPlayer().getRemainingAp() < 0 || str < 0) {
				c.getPlayer().dropMessage(5, "오류가 발생했습니다.");
			} else {
				stat.setStr(stat.getStr() + str);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - str);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.STR, stat.getStr());
			}
		} else if (splitted[0].equals("@인트")) {
			int int_ = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getInt() + int_ > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < int_ || c.getPlayer().getRemainingAp() < 0 || int_ < 0) {
				c.getPlayer().dropMessage(5, "오류가 발생했습니다.");
			} else {
				stat.setInt(stat.getInt() + int_);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - int_);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.INT, stat.getInt());
			}
		} else if (splitted[0].equals("@덱스")) {
			int dex = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getDex() + dex > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < dex || c.getPlayer().getRemainingAp() < 0 || dex < 0) {
				c.getPlayer().dropMessage(5, "오류가 발생했습니다.");
			} else {
				stat.setDex(stat.getDex() + dex);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - dex);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.DEX, stat.getDex());
			}
		} else if (splitted[0].equals("@럭")) {
			int luk = Integer.parseInt(splitted[1]);
			final MapleCharacterStat stat = c.getPlayer().getStat();

			if (stat.getLuk() + luk > c.getPlayer().getMaxStats() || c.getPlayer().getRemainingAp() < luk || c.getPlayer().getRemainingAp() < 0 || luk < 0) {
				c.getPlayer().dropMessage(5, "오류가 발생했습니다.");
			} else {
				stat.setLuk(stat.getLuk() + luk);
				c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - luk);
				c.getPlayer().updateSingleStat(PlayerStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
				c.getPlayer().updateSingleStat(PlayerStat.LUK, stat.getLuk());
			}
		} else if (splitted[0].equals("@인벤초기화")) {
			Map<Pair<Short, Short>, MapleInventoryType> eqs = new ArrayMap<Pair<Short, Short>, MapleInventoryType>();
			if (splitted[1].equals("모두")) {
				for (MapleInventoryType type : MapleInventoryType.values()) {
					for (IItem item : c.getPlayer().getInventory(type)) {
						eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), type);
					}
				}
			} else if (splitted[1].equals("장착")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIPPED);
				}
			} else if (splitted[1].equals("장비")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
				}
			} else if (splitted[1].equals("소비")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
				}
			} else if (splitted[1].equals("설치")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
				}
			} else if (splitted[1].equals("기타")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
				}
			} else if (splitted[1].equals("캐시")) {
				for (IItem item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
					eqs.put(new Pair<Short, Short>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
				}
			} else {
				c.getPlayer().dropMessage(6, "[모두/장착/장비/소비/설치/기타/캐시]");
			}
			for (Map.Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
				InventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
			}
		} else if (splitted[0].equals("@저장")) {
			//TODO @저장 명령어가 사용자 정보를 저장하는 용도가 아닌듯... 확인해보자.
//			NPCScriptManager.getInstance().dispose(c);
//			MapleMonsterProvider.getInstance().clearDrops();
//			ReactorScriptManager.getInstance().clearDrops();
//			c.getPlayer().dropMessage(5, "[알림] 저장이 완료되었습니다.");
		} else if (splitted[0].equals("@랙") || splitted[0].equals("@랙")) {
			NPCScriptManager.getInstance().dispose(c);
			c.getSession().write(MainPacketCreator.resetActions());
		} else if (splitted[0].equals("@원정대재입장")) {
			if (c.getPlayer().getParty() == null) {
				c.getPlayer().dropMessage(5, "가입된 파티가 없습니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition() == null) {
				c.getPlayer().dropMessage(5, "가입된 원정대가 없습니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossMap() == -1) {
				c.getPlayer().dropMessage(5, "현재 원정대가 보스를 잡는중이 아닙니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossChannel() == -1) {
				c.getPlayer().dropMessage(5, "현재 원정대가 보스를 잡는중이 아닙니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().getLastBossChannel() != c.getChannel()) {
				c.getPlayer().dropMessage(5, "보스를 잡고 있는 원정대와 채널이 다릅니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().containDeadChar(c.getPlayer().getId())) {
				c.getPlayer().dropMessage(5, "보스를 잡던 중 사망하여 재입장 할 수 없습니다.");
				return;
			}
			if (c.getPlayer().getParty().getExpedition().isBossKilled()) {
				c.getPlayer().dropMessage(5, "이미 보스를 죽인 후이므로 재입장할 수 없습니다.");
				return;
			}
			MapleMap map = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getParty().getExpedition().getLastBossMap());
			c.getPlayer().changeMap(map, map.getPortal(0));
			switch (map.getId()) {
			case 280030000:
			case 280030001:
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm06/FinalFight"));
				break;
			case 240060200: // 혼테일
			case 240060201: // 카오스혼테일
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm14/HonTale"));
				break;
			case 220080001: // 파풀
				c.getPlayer().send(MainPacketCreator.musicChange("Bgm09/TimeAttack"));
				break;
			}
		} else if (splitted[0].equals("@광장") || splitted[0].equals("@헤네시스") || splitted[0].equals("@마을")) {
			int jobid = c.getPlayer().getJob();
			if (jobid == 0 || jobid == 1000 || jobid == 2000 || jobid == 2001 || jobid == 2002 || jobid == 2003 || jobid == 2004 || jobid == 3000 || jobid == 3001 || jobid == 5000 || jobid == 6000 || jobid == 6001 || (jobid == 10112 && c.getPlayer().getMapId() == ServerConstants.startMap)) {
				c.getPlayer().dropMessage(5, "[시스템] 초보자는 광장으로 이동 할 수 없습니다.");
				return;
			}
			if (c.getPlayer().getMapId() == 910340500 || c.getPlayer().getMapId() == 240050200 || c.getPlayer().getMapId() == 272000600 || c.getPlayer().getMapId() == 921160400) {
				c.getPlayer().dropMessage(5, "[시스템] 보스레이드 도중에는 이동 할 수 없습니다.");
				return;
			}
			MapleMap target = c.getChannelServer().getMapFactory().getMap(100000000);
			MaplePortal targetPortal = null;
			if (splitted.length > 1) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splitted[1]));
				} catch (IndexOutOfBoundsException e) {
					c.getPlayer().dropMessage(5, "없는 포탈의 값이 있습니다.");
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		} else if (splitted[0].equals("@자유시장")) {
			int jobid = c.getPlayer().getJob();
			if (jobid == 0 || jobid == 1000 || jobid == 2000 || jobid == 2001 || jobid == 2002 || jobid == 2003 || jobid == 2004 || jobid == 3000 || jobid == 3001 || jobid == 5000 || jobid == 6000 || jobid == 6001) {
				c.getPlayer().dropMessage(5, "초보자는 자유시장으로 이동 할 수 없습니다.");
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
					c.getPlayer().dropMessage(5, "없는 포탈의 값이 있습니다.");
				} catch (NumberFormatException a) {
					// noop, assume that the gm is drunk
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		} else if (splitted[0].equals("@도움말") || splitted[0].equals("@명령어")) {
			c.getPlayer().dropMessage(5, "사용가능한 명령어는 다음과 같습니다 :");
			c.getPlayer().dropMessage(5, "@힘, @덱스, @인트, @럭 <찍을 수치> : 해당 스탯을 마우스 클릭 대신 찍을 수 있습니다.");
			c.getPlayer().dropMessage(5, "@원정대재입장 : 보스 레이드 중이던 원정대의 마지막 맵으로 이동합니다.");
			c.getPlayer().dropMessage(5, "@렉 : 공격 등 채팅외에 아무것도 안될때 사용하세요.");
			c.getPlayer().dropMessage(5, "@저장 : 현재 캐릭터를 강제로 저장합니다.");
			c.getPlayer().dropMessage(5, "@광장 : " + ServerConstants.serverName + " 광장인 헤네시스로 이동됩니다.");
			c.getPlayer().dropMessage(5, "@인벤초기화 : 인벤초기화 탭 모두/장착/장비/소비/설치/기타/캐시");
			c.getPlayer().dropMessage(5, "@스킬마스터 : 현재 자신의 직업 스킬을 마스터합니다.");
			c.getPlayer().dropMessage(5, "@보조무기해제 : 장착중인 보조무기를 해제 합니다.");
			c.getPlayer().dropMessage(5, "@경매장 : 경매장 관리인인 에그리치를 호출 합니다.");
			c.getPlayer().dropMessage(5, "@스텟리로드 : HP,MP가 다 안채워질때 사용하면 다 채워집니다.");
			c.getPlayer().dropMessage(5, "@후원포인트선물 : 현재 소유하고 있는 후원포인트를 다른 유저에게 선물 할 수 있습니다.");
			c.getPlayer().dropMessage(5, "~할말 : 전체채팅");
		} else if (splitted[0].equals("@추천인")) {
			NPCScriptManager.getInstance().dispose(c);
			NPCScriptManager.getInstance().start(c, 9010031);
		} else if (splitted[0].equals("@보조무기해제")) {
			IEquip equip = null;
			equip = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -10);
			if (equip == null) {
				c.getPlayer().Message(1, "장착중인 보조무기가 존재하지 않습니다.");
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
		} else if (splitted[0].equals("@경매장")) {
			NPCScriptManager.getInstance().start(c, 9030300);
			c.getPlayer().ea();
		} else if (splitted[0].equals("@후원포인트선물")) {
			MapleCharacter who = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
			final int rc = Integer.parseInt(splitted[2]);
			if (who != null) {
				if (rc > 0) {
					if (c.getPlayer().getRC() > rc) {
						who.gainRC(rc);
						who.dropMessage(6, "[알림] " + c.getPlayer().getName() + "님으로부터 " + rc + "후원포인트를 획득했습니다.");
						c.getPlayer().dropMessage(5, "[알림] " + splitted[1] + "님에게 " + rc + "후원포인트를 지급했습니다.");
					} else {
						c.getSession().write(MainPacketCreator.getGMText(20, "[알림] 선물하실 포인트가 부족합니다."));
					}
				} else {
					c.getSession().write(MainPacketCreator.getGMText(20, "[알림] 선물하실 포인트가 0포인트보다 작습니다."));
				}
			} else {
				c.getPlayer().dropMessage(5, "대상 플레이어를 발견하지 못했습니다.");
			}
		} else if (splitted[0].equals("@스텟리로드")) {
			MapleCharacter player = c.getPlayer();
			player.getStat().recalcLocalStats();
			c.getPlayer().dropMessage(5, "[알림] 스텟리로드를 완료 하였습니다.");
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] { new CommandDefinition("힘", "<올릴양>", "해당 스탯을 마우스 클릭 대신 찍을 수 있습니다.", 0), new CommandDefinition("인트", "<올릴양>", "해당 스탯을 마우스 클릭 대신 찍을 수 있습니다.", 0), new CommandDefinition("덱스", "<올릴양>", "해당 스탯을 마우스 클릭 대신 찍을 수 있습니다.", 0), new CommandDefinition("럭", "<올릴양>", "해당 스탯을 마우스 클릭 대신 찍을 수 있습니다.", 0), new CommandDefinition("랙", "", "공격 등 채팅외에 아무것도 안될때 사용하세요.", 0),
				new CommandDefinition("렉", "", "공격 등 채팅외에 아무것도 안될때 사용하세요.", 0), new CommandDefinition("명령어", "", "유저 명령어를 출력합니다.", 0), new CommandDefinition("저장", "", "캐릭터를 강제로 저장합니다.", 0), new CommandDefinition("원정대재입장", "", "보스 레이드 중이던 원정대의 마지막 맵으로 이동합니다.", 0), new CommandDefinition("광장", "", "해당 서버의 광장인 헤네시스로 이동합니다.", 0), new CommandDefinition("헤네시스", "", "해당 서버의 광장인 헤네시스로 이동합니다.", 0),
				new CommandDefinition("자유시장", "", "자유시장으로 이동합니다.", 0), new CommandDefinition("인벤초기화", "모두/장착/장비/소비/설치/기타/캐시", "해당 탭의 인벤토리를 모두 비워버립니다.", 0), new CommandDefinition("도움말", "", "유저 명령어를 출력합니다.", 0), new CommandDefinition("마을", "", "마을이동", 0), new CommandDefinition("추천인", "", "추천인", 0), new CommandDefinition("경매장", "", "경매장", 0), new CommandDefinition("스텟리로드", "", "스텟리로드", 0),
				new CommandDefinition("보조무기해제", "", "보조무기해제", 0), new CommandDefinition("후원포인트선물", "", "후원포인트를 선물합니다.", 0), };
	}
}
