/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 배지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package client.commands;

import java.text.DateFormat;
import java.util.Calendar;

import client.MapleCharacter;
import client.MapleClient;
import database.MYSQL;
import launch.ChannelServer;
import tools.StringUtil;
import java.sql.PreparedStatement;

public class BanningCommands implements Command {

    @Override
    public void execute(MapleClient c, String[] splitted) throws Exception {
	ChannelServer cserv = c.getChannelServer();

	if (splitted[0].equals("!밴")) {
	    if (splitted.length < 3) {
		return;
	    }
	    final StringBuilder sb = new StringBuilder(c.getPlayer().getName());
	    sb.append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));

	    final MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

	    if (target != null) {
		sb.append(" (IP: ").append(target.getClient().getSession().getRemoteAddress().toString().split(":")[0]).append(")");
		if (target.ban(sb.toString(), true, false)) {
		    c.getPlayer().dropMessage(6, "성공적으로 밴 되었습니다.");
                    if (c.getPlayer().getKeyValue("Banned_Today") == null) {
                        c.getPlayer().setKeyValue("Banned_Today", "0");
                    }
                    c.getPlayer().setKeyValue("Banned_Today", (Integer.parseInt(c.getPlayer().getKeyValue("Banned_Today"))+1)+"");
		} else {
		    c.getPlayer().dropMessage(6, "밴에 실패했습니다.");
		}
	    } else {
		if (MapleCharacter.ban(splitted[1], sb.toString(), false)) {
		    c.getPlayer().dropMessage(6, splitted[1] + " 오프라인 밴 성공.");
		} else {
		    c.getPlayer().dropMessage(6, splitted[1] + " 를 밴 하는데 실패했습니다.");
		}
	    }

	} else if (splitted[0].equals("!기간밴")) {
	    final MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
	    final int reason = Integer.parseInt(splitted[2]);
	    final int numDay = Integer.parseInt(splitted[3]);

	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, numDay);
	    final DateFormat df = DateFormat.getInstance();

	    if (victim == null) {
		c.getPlayer().dropMessage(6, "해당 캐릭터를 찾을 수 없습니다.");
		return;
	    }
	    victim.tempban("기간 밴 : " + c.getPlayer().getName() + "", cal, reason, true);
	    c.getPlayer().dropMessage(6, "" + splitted[1] + " 캐릭터가 " + df.format(cal.getTime()) + " 까지 성공적으로 밴 되었습니다.");

	} else if (splitted[0].equals("!밴풀기")) {
	    if (splitted.length < 1) {
		c.getPlayer().dropMessage(6, "!밴풀기 <캐릭터이름>");
	    } else {
		final byte result = c.unban(splitted[1]);
		if (result == -1) {
		    c.getPlayer().dropMessage(6, "해당 캐릭터를 발견하지 못했습니다.");
		} else if (result == -2) {
		    c.getPlayer().dropMessage(6, "캐릭터의 밴을 해제하는데 오류가 발생했습니다.");
		} else {
		    c.getPlayer().dropMessage(6, "캐릭터가 성공적으로 밴이 해제되었습니다.");
		}
	    }

	} else if (splitted[0].equals("!접속끊기")) {
	    int level = 0;
	    MapleCharacter victim;
	    if (splitted[1].charAt(0) == '-') {
		level = StringUtil.countCharacters(splitted[1], 'f');
		victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
	    } else {
		victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
	    }
            try {
                PreparedStatement ps = MYSQL.getConnection().prepareStatement("SELECT accountid WHERE id = ?");
                ps.setInt(1, victim.getId());
                
            } catch (Exception e) {

            }
	    if (level < 2) {
		victim.getClient().getSession().close();
		if (level >= 1) {
		    victim.getClient().disconnect(true, false);
		}
	    } else {
		c.getPlayer().dropMessage(6, "Please use dc -f instead.");
	    }
	}
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
		    new CommandDefinition("밴", "<캐릭터이름> <이유>", "해당 ip와 mac주소, 계정을 영구적으로 밴 시킵니다.", 3),
                    new CommandDefinition("밴풀기", "<캐릭터이름>", "밴 된 ip와 mac주소, 계정의 밴을 해제합니다.", 3),
		    new CommandDefinition("기간밴", "<캐릭터이름> <이유> <밴 될 일수>", "해당 계정을 해당 일 수 동안 밴 시킵니다.", 3),
		    new CommandDefinition("접속끊기", "[-f] <캐릭터이름>", "해당 캐릭터를 강제로 접속종료시킵니다. 현접에걸렸다면 -f 옵션을 사용하세요.", 3)
	};
    }
}
