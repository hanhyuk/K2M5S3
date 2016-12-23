/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import java.util.HashMap;

import server.maps.MaplePortal;
import server.maps.MapleMap;
import client.MapleClient;

public class MapleoCommands implements Command {

    private final HashMap<String, Integer> Mapleomaps = new HashMap<String, Integer>();
    
    public MapleoCommands() {
	Mapleomaps.put("GM맵", 180000000);
	Mapleomaps.put("사우스페리", 60000);
	Mapleomaps.put("암허스트", 1010000);
	Mapleomaps.put("헤네시스", 100000000);
	Mapleomaps.put("엘리니아", 101000000);
	Mapleomaps.put("페리온", 102000000);
	Mapleomaps.put("커닝시티", 103000000);
	Mapleomaps.put("리스항구", 104000000);
	Mapleomaps.put("슬리피우드", 105000000);
	Mapleomaps.put("플로리나비치", 110000000);
	Mapleomaps.put("오르비스", 200000000);
	Mapleomaps.put("행복의마을", 209000000);
	Mapleomaps.put("엘나스", 211000000);
	Mapleomaps.put("루디브리엄", 220000000);
	Mapleomaps.put("아쿠아리움", 230000000);
	Mapleomaps.put("리프레", 240000000);
	Mapleomaps.put("무릉", 250000000);
	Mapleomaps.put("백초마을", 251000000);
	Mapleomaps.put("지구방위본부", 221000000);
	Mapleomaps.put("아랫마을", 222000000);
	Mapleomaps.put("샤레니안", 990000000);
	Mapleomaps.put("피아누스", 230040420);
	Mapleomaps.put("혼테일", 240060200);
	Mapleomaps.put("그리핀", 240020101);
	Mapleomaps.put("마뇽", 240020401);
	Mapleomaps.put("자쿰", 280030000);
	Mapleomaps.put("파풀라투스", 220080001);
	Mapleomaps.put("아리안트", 260000100);
	Mapleomaps.put("노틸러스", 120000000);
	Mapleomaps.put("에레브", 130000000);
	Mapleomaps.put("엘린숲", 300000000);
	Mapleomaps.put("시간의신전", 270000000);
	Mapleomaps.put("핑크빈", 270050100);
	Mapleomaps.put("리엔", 140000000);
        Mapleomaps.put("에우렐", 101050000);
	Mapleomaps.put("에델슈타인", 310000000);
	Mapleomaps.put("시청", 310000001);
    }

    @Override
    public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {
	if (splitted.length < 2) {
	    c.getPlayer().dropMessage(6, "사용법: !이동 <맵이름>   [!이동 맵] 을 입력하면 사용가능한 맵 목록을 볼 수 있습니다.");
	} else {
	    if (Mapleomaps.containsKey(splitted[1])) {
		MapleMap target = c.getChannelServer().getMapFactory().getMap(Mapleomaps.get(splitted[1]));
		MaplePortal targetPortal = target.getPortal(0);
		c.getPlayer().changeMap(target, targetPortal);
	    } else {
		if (splitted[1].equals("맵")) {
		    c.getPlayer().dropMessage(6, "사용가능한 맵 목록은 다음과 같습니다.");
		    StringBuilder sb = new StringBuilder();
		    for (String s : Mapleomaps.keySet()) {
			sb.append(s + ", ");
		    }
		    c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
		} else {
		    c.getPlayer().dropMessage(6, "문법이 올바르지 않습니다. !이동 맵 을 입력하면 사용가능한 맵 목록을 볼 수 있습니다.");
		}
	    }
	}
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
            new CommandDefinition("이동", "?", "입력한 마을/맵 으로 이동합니다.", 1)
        };
    }
}
