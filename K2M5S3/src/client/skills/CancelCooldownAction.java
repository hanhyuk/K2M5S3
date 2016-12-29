package client.skills;

import java.lang.ref.WeakReference;

import client.MapleCharacter;
import constants.GameConstants;
import packet.creators.MainPacketCreator;

public class CancelCooldownAction implements Runnable {

    private int skillId;
    private WeakReference<MapleCharacter> target;

    public CancelCooldownAction(MapleCharacter target, int skillId) {
	this.target = new WeakReference<MapleCharacter>(target);
	this.skillId = skillId;
    }

    @Override
    public void run() {
	final MapleCharacter realTarget = target.get();
	if (realTarget != null) {
	    realTarget.removeCooldown(skillId);
	    realTarget.getClient().getSession().write(MainPacketCreator.skillCooldown(skillId, 0));
            if (GameConstants.isSoulSkill(skillId)) {
                realTarget.checkSoulState(false);
            }
        }
    }
}
