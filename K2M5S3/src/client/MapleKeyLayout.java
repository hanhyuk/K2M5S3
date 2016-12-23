/*
 * ArcStory Project
 * √÷¡÷ø¯ sch2307@naver.com
 * ¿Ã¡ÿ junny_adm@naver.com
 * øÏ¡ˆ»∆ raccoonfox69@gmail.com
 * ∞≠¡§±‘ ku3135@nate.com
 * ±Ë¡¯»´ designer@inerve.kr
 */

package client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import packet.transfer.write.WritingPacket;
import database.MYSQL;

public class MapleKeyLayout {

    private boolean changed = false;
    private final Map<Integer, MapleKeyBinding> keymap = new HashMap<>();

    public final Map<Integer, MapleKeyBinding> Layout() {
	changed = true;
	return keymap;
    }

    public final void writeData(final WritingPacket mplew) {
        MapleKeyBinding binding;
        
        mplew.write(keymap.isEmpty() ? 1 : 0);
        
        if (!keymap.isEmpty()) {
            for (int i = 0; i < 89; i++) {
                binding = keymap.get(i);
                if (binding != null) {
                    mplew.write(binding.getType());
                    mplew.writeInt(binding.getAction());
                } else {
                    mplew.write(0);
                    mplew.writeInt(0);
                }
            }
	}
    }

    public final void saveKeys(final int charid) throws SQLException {
	if (!changed || keymap.size() == 0) {
	    return;
	}
	Connection con = MYSQL.getConnection();

	PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ?");
	ps.setInt(1, charid);
	ps.execute();
	ps.close();

	boolean first = true;
	StringBuilder query = new StringBuilder();

	for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
	    if (first) {
		first = false;
		query.append("INSERT INTO keymap VALUES (");
	    } else {
		query.append(",(");
	    }
	    query.append("DEFAULT,");
	    query.append(charid).append(",");
	    query.append(keybinding.getKey().intValue()).append(",");
	    query.append(keybinding.getValue().getType()).append(",");
	    query.append(keybinding.getValue().getAction()).append(")");
	}
	ps = con.prepareStatement(query.toString());
	ps.execute();
	ps.close();
    }
}
