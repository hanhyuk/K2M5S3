/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import client.MapleClient;
import client.MapleCharacter;
import java.awt.Point;
import java.lang.ref.WeakReference;
import packet.creators.MainPacketCreator;
import tools.Timer;

/**
 *
 * @author GOOD
 */
public class MapleSubSummon extends AnimatedHinaMapObjectExtend {
    
    private final WeakReference<MapleCharacter> ownerchr;
    private int check, cid, value, skillid, time, skillLevel;
    private byte value2;
    private Point strange, strange2;
    private short secondValue, strange3;
    
    public MapleSubSummon(MapleCharacter owner, short check, int cid, int value, byte value2, Point strange, Point strange2, short secondValue, int skillid, int skillLevel, short strange3, int time) {
        this.ownerchr = new WeakReference<>(owner);
        this.check = check;
        this.cid = cid;
        this.value = value;
        this.value2 = value2;
        this.strange = strange;
        this.strange2 = strange2;
        this.secondValue = secondValue;
        this.skillid = skillid;
        this.skillLevel = skillLevel;
        this.strange3 = strange3;
        this.time = time;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUBSUMMON;
    }

    @Override
    public void sendSpawnData(final MapleClient client) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                client.getSession().write(MainPacketCreator.spawnSubSummon(getOwnerChr(), MapleSubSummon.this));
            }
        };
	Timer.ShowTimer.getInstance().schedule(r, 2000);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        
    }
    
    public MapleCharacter getOwnerChr() {
        return ownerchr.get();
    }
    
    public int getCheck() {
        return check;
    }
    
    public int getCid() {
        return cid;
    }
    
    public int getValue() {
        return value;
    }
    
    public byte getValue2() {
        return value2;
    }
    
    public Point getStrange() {
        return strange;
    }
    
    public Point getStrange2() {
        return strange2;
    }
    
    public short getStrange3() {
        return strange3;
    }
    
    public short getSecondValue() {
        return secondValue;
    }
    
    public int getTime() {
        return time;
    }
    
    public int getSkillId() {
        return skillid;
    }
    
    public int getSkillLevel() {
        return skillLevel;
    }
    
}
