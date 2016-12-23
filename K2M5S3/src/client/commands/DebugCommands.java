/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import java.util.Arrays;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import launch.ChannelServer;
import server.maps.MaplePortal;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.quest.MapleQuest;
import packet.creators.MainPacketCreator;
import server.life.MapleNPC;
import server.life.MaplePlayerNPC;

public class DebugCommands implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
	MapleCharacter player = c.getPlayer();
        
	if (splitted[0].equals("!퀘스트초기화")) {
	    MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
	} else if (splitted[0].equals("!가까운포탈")) {
	    final MaplePortal portal = player.getMap().findClosestSpawnpoint(player.getPosition());
	    c.getPlayer().dropMessage(6, portal.getName() + " 포탈아이디: " + portal.getId() + " 스크립트: " + portal.getScriptName());
	} else if (splitted[0].equals("!소환개체")) {
	    c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
	} else if (splitted[0].equals("!쓰레드")) {
	    Thread[] threads = new Thread[Thread.activeCount()];
	    Thread.enumerate(threads);
	    String filter = "";
	    if (splitted.length > 1) {
		filter = splitted[1];
	    }
	    for (int i = 0; i < threads.length; i++) {
		String tstring = threads[i].toString();
		if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
		    c.getPlayer().dropMessage(6, i + ": " + tstring);
		}
	    }
	} else if (splitted[0].equals("!경로추적")) {
	    if (splitted.length < 1) {
		throw new IllegalCommandSyntaxException(1);
	    }
	    Thread[] threads = new Thread[Thread.activeCount()];
	    Thread.enumerate(threads);
	    Thread t = threads[Integer.parseInt(splitted[1])];
	    c.getPlayer().dropMessage(6, t.toString() + ":");
	    for (StackTraceElement elem : t.getStackTrace()) {
		c.getPlayer().dropMessage(6, elem.toString());
	    }
	} else if (splitted[0].equals("!겟찰")) {
	    c.getSession().write(MainPacketCreator.getPlayerInfo(player));
	    player.getMap().removePlayer(player);
	    player.getMap().addPlayer(player);
	} else if (splitted[0].equals("!드롭토글")) {
	    player.getMap().toggleDrops();
	} else if (splitted[0].equals("!확성기토글")) {
            ChannelServer.allToggleMegaphoneMuteState();
	    c.getPlayer().dropMessage(6, "확성기 사용가능 상태 : " + (c.getChannelServer().getMegaphoneMuteState() ? "불가능" : "가능"));
	} else if (splitted[0].equalsIgnoreCase("!리액터소환")) {
	    MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
	    MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
	    reactor.setDelay(-1);
	    reactor.setPosition(c.getPlayer().getPosition());
	    c.getPlayer().getMap().spawnReactor(reactor);
	} else if (splitted[0].equals("!리액터치기")) {
	    c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
	} else if (splitted[0].equals("!리액터목록") || splitted[0].equals("!현재맵리액터")) {
	    MapleMap map = c.getPlayer().getMap();
	    List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
	    for (MapleMapObject reactorL : reactors) {
		MapleReactor reactor2l = (MapleReactor) reactorL;
		c.getPlayer().dropMessage(6, "리액터: 오브젝트id: " + reactor2l.getObjectId() + " 리액터ID: " + reactor2l.getReactorId() + " 위치: " + reactor2l.getPosition().toString() + " 상태: " + reactor2l.getState());
	    }
	} else if (splitted[0].equals("!리액터파괴")) {
	    MapleMap map = c.getPlayer().getMap();
	    List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
	    if (splitted[1].equals("모두")) {
		for (MapleMapObject reactorL : reactors) {
		    MapleReactor reactor2l = (MapleReactor) reactorL;
		    c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
		}
	    } else {
		c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
	    }
	} else if (splitted[0].equals("!리액터초기화")) {
	    c.getPlayer().getMap().resetReactors(c);
	} else if (splitted[0].equals("!리액터설정")) {
	    c.getPlayer().getMap().setReactorState(c);
	} else if (splitted[0].equals("!드롭삭제")) {
	    List<MapleMapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
	    for (MapleMapObject i : items) {
		c.getPlayer().getMap().removeMapObject(i);
		c.getPlayer().getMap().broadcastMessage(MainPacketCreator.removeItemFromMap(i.getObjectId(), 0, 0), i.getPosition());
	    }
	} else if (splitted[0].equals("!경험치배율")) {
	    if (splitted.length > 1) {
		final int rate = Integer.parseInt(splitted[1]);
		c.getChannelServer().setExpRate(rate);
		c.getPlayer().dropMessage(6, "경험치 배율이 " + rate + "x 배로 수정되었습니다.");
	    } else {
		c.getPlayer().dropMessage(6, "사용법: !경험치배율 <숫자>");
	    }
	} else if (splitted[0].equals("!드롭배율")) {
	    if (splitted.length > 1) {
		final int rate = Integer.parseInt(splitted[1]);
		c.getChannelServer().setDropRate(rate);
		c.getPlayer().dropMessage(6, "드롭 배율이 " + rate + "x 배로 수정되었습니다.");
	    } else {
		c.getPlayer().dropMessage(6, "사용법: !드롭배율 <숫자>");
	    }
	} else if (splitted[0].equals("!모두튕")) {
	    c.getChannelServer().getPlayerStorage().disconnectAll();
	} else if (splitted[0].equals("!현재맵엔피시")) {
            MapleMap map = c.getPlayer().getMap();
            c.getPlayer().Message("현재 맵 : "+map.getId()+" - "+map.getStreetName() + " : "+map.getMapName());
            for (MapleMapObject mo : map.getAllNPC()) {
                c.getPlayer().Message(7, ((MapleNPC) mo).getId() + " : " + ((MapleNPC) mo).getName());
            }
        } else if (splitted[0].equals("!현재맵포탈")) {
            MapleMap map = c.getPlayer().getMap();
            c.getPlayer().Message("현재 맵 : "+map.getId()+" - "+map.getStreetName() + " : "+map.getMapName());
            for (MaplePortal mp : map.getPortals()) {
                c.getPlayer().Message(7, mp.getId() + " : " + mp.getName() + " 스크립트 : "+mp.getScriptName() +" 목적지 : "+mp.getTarget());
            }
        } else if (splitted[0].equals("!현재맵플레이어")) {
            MapleMap map = c.getPlayer().getMap();
            c.getPlayer().Message("현재 맵 : "+map.getId()+" - "+map.getStreetName() + " : "+map.getMapName());
            for (MapleMapObject pr : map.getAllPlayer()) {
                c.getPlayer().Message(7, ((MapleCharacter) pr).getName());
            }
        } else if (splitted[0].equals("!명예치")) {
            int d = Integer.parseInt(splitted[1]);
            c.getPlayer().addInnerExp(d);
            c.getPlayer().message(5, "명예치가 "+d+" 상승했습니다.");
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
		    new CommandDefinition("모두튕", "", "현재 채널의 모든 플레이어를 강제로 접속종료합니다.", 6),
		    new CommandDefinition("드롭삭제", "", "현재 맵의 모든 아이템을 삭제합니다.", 4),
		    new CommandDefinition("퀘스트초기화", "", "현재 받아있는 퀘스트를 모두 지워버립니다.", 2),
		    new CommandDefinition("가까운포탈", "", "가장 가까이 있는 포탈을 출력합니다.", 2),
		    new CommandDefinition("소환개체", "", "현재 소환된 개체를 출력합니다.", 5),
		    new CommandDefinition("확성기토글", "", "모든 채널의 확성기 사용가능 상태를 변경합니다.", 5),
		    new CommandDefinition("쓰레드", "", "활성화된 쓰레드를 출력합니다.", 6),
		    new CommandDefinition("경로추적", "<쓰레드넘버>", "해당 쓰레드가 어디서 어떻게 시작되었는지를 추적합니다.", 6),
		    new CommandDefinition("겟찰", "", "캐릭터 정보 패킷을 전송합니다.", 6),
		    new CommandDefinition("드롭토글", "", "현재 맵에서 몬스터의 드롭가능상태를 변경합니다.", 2),
		    new CommandDefinition("리액터소환", "<리액터ID>", "입력한 ID의 리액터를 소환합니다.", 4),
		    new CommandDefinition("리액터치기", "<리액터오브젝트ID>", "입력한 ID의 리액터를 칩니다.", 4),
		    new CommandDefinition("리액터초기화", "", "모든 리액터의 상태를 0으로 만듭니다.", 4),
		    new CommandDefinition("리액터목록", "", "현재 맵의 모든 리액터를 출력합니다.", 4),
		    new CommandDefinition("현재맵리액터", "", "현재 맵의 모든 리액터를 출력합니다.", 4),
                    new CommandDefinition("현재맵플레이어", "", "현재 맵의 모든 플레이어를 출력합니다.", 4),
		    new CommandDefinition("리액터파괴", "<리액터오브젝트ID>", "해당 ID의 리액터를 파괴합니다.", 4),
		    new CommandDefinition("리액터설정", "<리액터오브젝트ID> <상태>", "해당 ID의 리액터의 상태를 조종합니다.", 4),
		    new CommandDefinition("경험치배율", "<배율>", "현재 채널의 경험치 배율을 조정합니다.", 6),
		    new CommandDefinition("드롭배율", "<배율>", "현재 채널의 드롭 배율을 조정합니다.", 6),
		    new CommandDefinition("현재맵엔피시", "", "현재맵에 있는 엔피시의 목록을 모두 출력합니다.", 1),
		    new CommandDefinition("현재맵포탈", "", "현재맵에 있는 포탈의 목록을 모두 출력합니다.", 1),
		    new CommandDefinition("명예치", "", "명예치 올림", 5)
		};
    }
}
