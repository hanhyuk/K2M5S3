package client.commands;

import java.util.Arrays;

import client.MapleClient;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;

public class MonsterInfoCommands implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) throws Exception, IllegalCommandSyntaxException {

	MapleMap map = c.getPlayer().getMap();
	double range = Double.POSITIVE_INFINITY;

	if (splitted.length > 1) {
	    int irange = Integer.parseInt(splitted[1]);
	    if (splitted.length <= 2) {
		range = irange * irange;
	    } else {
		map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
	    }
	}
	if (splitted[0].equals("!킬올")) {
	    MapleMonster mob;
	    for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
		mob = (MapleMonster) monstermo;
		map.killMonster(mob, c.getPlayer(), false, false, (byte) 1);
	    }
	} else if (splitted[0].equals("!킬올드롭")) {
	    MapleMonster mob;
	    for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
		mob = (MapleMonster) monstermo;
		map.killMonster(mob, c.getPlayer(), true, false, (byte) 1);
	    }
	} else if (splitted[0].equals("!킬올노스폰")) {
	    map.killAllMonsters(false);
	    
	} else if (splitted[0].equals("!몬스터소환개체")) {
	    MapleMonster mob;
	    for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
		mob = (MapleMonster) monstermo;
		c.getPlayer().dropMessage(6, "몬스터 " + mob.toString());
	    }
	}
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
		    new CommandDefinition("킬올", "(<범위>) (<맵ID>)", "모든 몬스터를 죽입니다. 맵 ID가 정의될 경우, 범위값은 무시됩니다.", 5),
		    new CommandDefinition("킬올노스폰", "(<범위>) (<맵ID>)", "모든 몬스터를 죽이되, 해당 몬스터는 다른몬스터로 부활하지 않습니다.", 5),
		    new CommandDefinition("킬올드롭", "(<범위>) (<맵ID>)", "모든 몬스터를 죽이되, 해당 몬스터는 아이템도 드롭하게 됩니다.", 5),
		    new CommandDefinition("몬스터소환개체", "", "현재 맵의 모든 몬스터를 출력합니다.", 5)
	};
    }
}
