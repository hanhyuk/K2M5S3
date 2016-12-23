/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import constants.ServerConstants;
import java.awt.Point;
import java.util.List;

import client.MapleClient;
import client.items.IItem;
import client.items.MapleInventoryType;
import database.MYSQL;
import server.life.MapleLifeProvider;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import packet.creators.MainPacketCreator;
import server.life.*;
import server.maps.MapleMapObjectType;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;

public class NPCSpawningCommands implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {

	if (splitted[0].equals("!엔피시")) {
	    int npcId = Integer.parseInt(splitted[1]);
	    MapleNPC npc = MapleLifeProvider.getNPC(npcId);
	    if (npc != null && !npc.getName().equals("MISSINGNO")) {
		npc.setPosition(c.getPlayer().getPosition());
		npc.setCy(c.getPlayer().getPosition().y);
		npc.setRx0(c.getPlayer().getPosition().x + 50);
		npc.setRx1(c.getPlayer().getPosition().x - 50);
		npc.setFh(c.getPlayer().getMap().getFootholds().findMaple(c.getPlayer().getPosition()).getId());
		npc.setCustom(true);
		c.getPlayer().getMap().addMapObject(npc);
		c.getPlayer().getMap().broadcastMessage(MainPacketCreator.spawnNPC(npc, true));
	    } else {
		c.getPlayer().dropMessage(6, "WZ에 존재하지 않는 NPC를 입력했습니다.");
	    }
	} else if (splitted[0].equals("!엔피시삭제")) {
	    for (MapleMapObject npcss : c.getPlayer().getMap().getAllNPC()) {
                MapleNPC npc = (MapleNPC) npcss;
                if (splitted[1] != null) {
                  if (npc.getId() == Integer.parseInt(splitted[1])) {
                    c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeNPC(npc.getObjectId()));
                    c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeNPCController(npc.getObjectId()));
                    c.getPlayer().getMap().removeMapObject(npc.getObjectId());
                  }
                } else {
                    c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeNPC(npc.getObjectId()));
                    c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeNPCController(npc.getObjectId()));
                    c.getPlayer().getMap().removeMapObject(npc.getObjectId());
                }
	    }

	} else if (splitted[0].equals("!현재위치")) {
	    Point pos = c.getPlayer().getPosition();
	    c.getPlayer().dropMessage(6, "CY: " + (pos.y +2) + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getMap().getFootholds().findMaple(pos).getId());
	} else if (splitted[0].equals("!플레이어엔피시")) {
	    int npcId = Integer.parseInt(splitted[1]);
	    MaplePlayerNPC npc = new MaplePlayerNPC(npcId, new MapleNPCStats(""));
	    if (npc != null) {
		npc.setPosition(c.getPlayer().getPosition());
                npc.setName(c.getPlayer().getName());
		npc.setCy(c.getPlayer().getPosition().y);
		npc.setRx0(c.getPlayer().getPosition().x + 50);
		npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setFace(c.getPlayer().getFace());
                npc.setHair(c.getPlayer().getHair());
                npc.setSkin((byte) c.getPlayer().getSkinColor());
                npc.setDirection((byte) (c.getPlayer().isFacingLeft() ? 0 : 1));
		npc.setFh(c.getPlayer().getMap().getFootholds().findMaple(c.getPlayer().getPosition()).getId());
                Map <Byte, Integer> equips = new LinkedHashMap<Byte, Integer>();
                for (IItem equip : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).list()) {
                    equips.put((byte) equip.getPosition(), equip.getItemId());
                }
                npc.setEquips(equips);
		c.getPlayer().getMap().addMapObject(npc);
		npc.broadcastPacket(c.getPlayer().getMap());
                try {
                    String sql = "INSERT INTO `playernpcs`(`id`, `name`, `hair`, `face`, `skin`, `dir`, `x`, `y`, `map`) VALUES (? ,? ,? ,? ,? ,? ,? ,? ,?)";
                    PreparedStatement ps = MYSQL.getConnection().prepareStatement(sql);
                    ps.setInt(1, npcId);
                    ps.setString(2, c.getPlayer().getName());
                    ps.setInt(3, c.getPlayer().getHair());
                    ps.setInt(4, c.getPlayer().getFace());
                    ps.setInt(5, c.getPlayer().getSkinColor());
                    ps.setInt(6, c.getPlayer().isFacingLeft() ? 0 : 1);
                    ps.setInt(7, c.getPlayer().getPosition().x);
                    ps.setInt(8, c.getPlayer().getPosition().y);
                    ps.setInt(9, c.getPlayer().getMapId());
                    ps.executeUpdate();
                    ps.close();
                    ps = MYSQL.getConnection().prepareStatement("INSERT INTO `playernpcs_equip`(`npcid`, `equipid`, `equippos`) VALUES (? ,? ,?)");
                    ps.setInt(1, npcId);
                    for (IItem equip : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).list()) {
                        ps.setInt(2, equip.getItemId());
                        ps.setByte(3, (byte) equip.getPosition());
                        ps.executeUpdate();
                    }
                    ps.close();
                    c.getPlayer().dropMessage(6, "플레이어 엔피시가 성공적으로 DB에 등록되었습니다.");
                } catch (Exception e) {
                    c.getPlayer().dropMessage(1, "플레이어 엔피시가 DB에 등록되지 못했습니다.");
                    System.err.println("[오류] 플레이어 엔피시를 제작하는 도중 오류가 발생했습니다.");
                    if (!ServerConstants.realese) e.printStackTrace();
                }
	    } else {
		c.getPlayer().dropMessage(6, "WZ에 존재하지 않는 NPC를 입력했습니다.");
	    }
        } else if (splitted[0].equals("!고정엔피시")) {
	    int npcId = Integer.parseInt(splitted[1]);
	    MapleNPC npc = MapleLifeProvider.getNPC(npcId);
	    if (npc != null && !npc.getName().equals("MISSINGNO")) {
		npc.setPosition(c.getPlayer().getPosition());
		npc.setCy(c.getPlayer().getPosition().y + 2);
		npc.setRx0(c.getPlayer().getPosition().x + 50);
		npc.setRx1(c.getPlayer().getPosition().x - 50);
		npc.setFh(c.getPlayer().getMap().getFootholds().findMaple(c.getPlayer().getPosition()).getId());
		c.getPlayer().getMap().addMapObject(npc);
		c.getPlayer().getMap().broadcastMessage(MainPacketCreator.spawnNPC(npc, true));
	    } else {
		c.getPlayer().dropMessage(6, "WZ에 존재하지 않는 NPC를 입력했습니다.");
                return;
	    }
            try {
                String sql = "INSERT INTO `spawn`(`lifeid`, `rx0`, `rx1`, `cy`, `fh`, `type`, `dir`, `mapid`, `mobTime`) VALUES (? ,? ,? ,? ,? ,? ,? ,? ,?)";
                PreparedStatement ps = MYSQL.getConnection().prepareStatement(sql);
                ps.setInt(1, npcId);
                ps.setInt(2, c.getPlayer().getPosition().x - 50);
                ps.setInt(3, c.getPlayer().getPosition().x + 50);
                ps.setInt(4, c.getPlayer().getPosition().y);
                ps.setInt(5, c.getPlayer().getMap().getFootholds().findMaple(c.getPlayer().getPosition()).getId());
                ps.setString(6, "n");
                ps.setInt(7, c.getPlayer().getFacingDirection() == 1 ? 0 : 1);
                ps.setInt(8, c.getPlayer().getMapId());
                ps.setInt(9, 0);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                System.err.println("[오류] 엔피시를 고정 등록하는데 실패했습니다.");
                if (!ServerConstants.realese) e.printStackTrace();
            }
        } else if (splitted[0].equals("!고정몹")) {
	    int mobId = Integer.parseInt(splitted[1]);
	    MapleMonster mob = MapleLifeProvider.getMonster(mobId);
	    c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            try {
                String sql = "INSERT INTO `spawn`(`lifeid`, `rx0`, `rx1`, `cy`, `fh`, `type`, `dir`, `mapid`, `mobTime`) VALUES (? ,? ,? ,? ,? ,? ,? ,? ,?)";
                PreparedStatement ps = MYSQL.getConnection().prepareStatement(sql);
                ps.setInt(1, mobId);
                ps.setInt(2, c.getPlayer().getPosition().x - 50);
                ps.setInt(3, c.getPlayer().getPosition().x + 50);
                ps.setInt(4, c.getPlayer().getPosition().y);
                ps.setInt(5, c.getPlayer().getMap().getFootholds().findMaple(c.getPlayer().getPosition()).getId());
                ps.setString(6, "m");
                ps.setInt(7, c.getPlayer().getFacingDirection() == 1 ? 0 : 1);
                ps.setInt(8, c.getPlayer().getMapId());
                ps.setInt(9, 0);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
                System.err.println("[오류] 엔피시를 고정 등록하는데 실패했습니다.");
                if (!ServerConstants.realese) e.printStackTrace();
            }
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
		    new CommandDefinition("엔피시", "<엔피시ID>", "현재 위치에 해당 ID의 엔피시를 소환합니다.", 5),
		    new CommandDefinition("엔피시삭제", "", "현재 맵에서 명령어로 소환된 모든 NPC를 제거합니다.", 5),
		    new CommandDefinition("플레이어엔피시", "<스크립트ID>", "현재 맵에 현재 플레이어를 엔피시로 등록합니다.", 5),
		    new CommandDefinition("고정엔피시", "<엔피시ID>", "현재 맵의 현재 위치에 해당 엔피시를 고정으로 등록합니다.", 5),
		    new CommandDefinition("고정몹", "<몹ID>", "현재 맵의 현재 위치에 해당 몬스터를 고정으로 등록합니다.", 5),
		    new CommandDefinition("현재위치", "", "현재 X,Y 값 등 좌표를 출력합니다.", 2)
	};
    }
}
