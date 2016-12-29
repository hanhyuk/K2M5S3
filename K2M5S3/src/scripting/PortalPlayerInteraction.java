package scripting;

import client.MapleClient;
import server.maps.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(final MapleClient c, final MaplePortal portal) {
	super(c);
	this.portal = portal;
    }

    public final MaplePortal getPortal() {
	return portal;
    }

    public final void inFreeMarket() {
	if (getMapId() != 910000000) {
	    if (getPlayer().getLevel() > 10) {
		saveLocation("FREE_MARKET");
		playPortalSE();
		warp(910000000, "st00");
	    } else {
		playerMessage(5, "레벨 10 이상만 이곳에 들어갈 수 있습니다.");
	    }
	}
    }
}
