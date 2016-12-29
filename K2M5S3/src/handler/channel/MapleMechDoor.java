package handler.channel;

import java.awt.Point;

import client.MapleCharacter;
import client.MapleClient;
import packet.skills.MechanicSkill;
import server.maps.AbstractHinaMapObject;
import server.maps.MapleMapObjectType;

public class MapleMechDoor extends AbstractHinaMapObject {
    
    private int owner, partyid, id;
    
    public MapleMechDoor(MapleCharacter owner, Point pos, int id) {
        super();
        this.owner = owner.getId();
        this.partyid = owner.getParty() == null ? 0 : owner.getParty().getId();
        setPosition(pos);
        this.id = id;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MechanicSkill.mechDoorSpawn(this, false));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MechanicSkill.mechDoorRemove(this, false));
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }

    public int getOwnerId() {
        return this.owner;
    }

    public int getPartyId() {
        return this.partyid;
    }

    public int getId() {
        return this.id;
    }
}