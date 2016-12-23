/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import client.MapleCharacter;
import java.awt.Point;
import java.lang.ref.WeakReference;

/**
 *
 * @author GOOD
 */
public class MoveSubSummon {
    
    private final WeakReference<MapleCharacter> ownerchr;
    private int cid, check, skillid;
    Point point;
    
    
    public MoveSubSummon(MapleCharacter owner, int charid, int check, int skillid, Point setPoint) {
        this.ownerchr = new WeakReference<>(owner);
        this.cid = charid;
        this.check = check;
        this.skillid = skillid;
        this.point = setPoint;
    }
    
    public int getCid() {
        return cid;
    }
    
    public int getSkillid() {
        return skillid;
    }
    
    public int getCheck() {
        return check;
    }
    
    public Point getPoint() {
        return point;
    }
    
    public MapleCharacter getOwnerChr() {
        return ownerchr.get();
    }
}
