/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleCharacter;
import client.MapleClient;
import client.items.Equip;
import client.items.IEquip;
import client.items.IItem;
import client.items.Item;
import client.items.MapleInventoryType;
import client.skills.ISkill;
import client.skills.SkillFactory;
import client.skills.SkillStatEffect;
import client.stats.BuffStats;
import client.stats.DiseaseStats;
import client.stats.MonsterStatus;
import client.stats.MonsterStatusEffect;
import client.stats.PlayerStat;
import community.MaplePartyOperation;
import constants.GameConstants;
import constants.ServerConstants;
import constants.subclasses.QuickMove;
import database.MYSQL;
import handler.channel.InventoryHandler;
import handler.channel.MapleMechDoor;
import launch.ChannelServer;
import launch.holder.MapleCoolDownValueHolder;
import launch.holder.MapleDiseaseValueHolder;
import launch.world.WorldBroadcasting;
import packet.creators.AndroidPacket;
import packet.creators.MainPacketCreator;
import packet.creators.MobPacket;
import packet.creators.PetPacket;
import packet.creators.RunePacket;
import packet.creators.SoulWeaponPacket;
import packet.creators.UIPacket;
import packet.skills.MechanicSkill;
import packet.transfer.write.Packet;
import server.items.InventoryManipulator;
import server.items.ItemInformation;
import server.life.MapleLifeProvider;
import server.life.MapleMonster;
import server.life.MapleMonsterProvider;
import server.life.MapleMonsterStats;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.OverrideMonsterStats;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.Spawns;
import tools.Pair;
import tools.Randomizer;
import tools.Timer.MapTimer;
import tools.Triple;

public class MapleMap {

    private final Map<Integer, MapleMapObject> mapobjects = new HashMap<Integer, MapleMapObject>();
    private final Collection<Spawns> monsterSpawn = new LinkedList<Spawns>();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final List<MapleCharacter> characters = new ArrayList<MapleCharacter>();
    private final Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
    private final List<Rectangle> areas = new ArrayList<Rectangle>();
    private MapleFootholdTree footholds = null;
    private float monsterRate, recoveryRate;
    private MapleMapEffect mapEffect;
    private byte channel;
    private short decHP = 0, createMobInterval = 9000;
    private String fieldType = "";
    private int protectItem = 0, barrier = 0, mapid, returnMapId, timeLimit, fieldLimit, maxRegularSpawn = 0;
    private int runningOid = 100000, forcedReturnMap = 999999999;
    private boolean town, clock, personalShop, everlast = false, dropsDisabled = false;
    private String mapName, streetName, onUserEnter, onFirstUserEnter, fieldScript;
    private Map<Integer, MapleNPC> tempnpcs3 = new HashMap<Integer, MapleNPC>();
    private Map<Integer, MapleMonster> tempmonsters3 = new HashMap<Integer, MapleMonster>();
    private WeakReference<MapleCharacter> changeMobOrigin = null;
    private List<Integer> droppedItems = new LinkedList<Integer>();
    private long maptimer = 0;
    private long damage = 0;
    private long mobcount = 0;
    public short soulamount;
    private final Lock mutex = new ReentrantLock();
    private final Map<Integer, List<MapleMapObject>> magnetics = new HashMap<Integer, List<MapleMapObject>>();
    private ScheduledFuture<?> catchstart = null;
    private long lastPlayerLeft = System.currentTimeMillis();
    private int EliteMobCount;
    private int EliteMobCommonCount;
    private boolean elitebossmap;
    private boolean elitebossrewardmap;
    
    public MapleMap(final int mapid, final int channel, final int returnMapId, final float monsterRate) {
        this.mapid = mapid;
        this.channel = (byte) channel;
        this.returnMapId = returnMapId;
        this.monsterRate = monsterRate;
        EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapobj = new EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>>(MapleMapObjectType.class);
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            mapobj.put(type, new LinkedHashMap<Integer, MapleMapObject>());
        }
    }

    public final void toggleDrops() {
        this.dropsDisabled = !dropsDisabled;
    }
    
    public int EliteMobCount() {
        return this.EliteMobCount;
    }

    public int EliteMobCommonCount() {
        return this.EliteMobCommonCount;
    }

    public void SetEliteMobCount(int a) {
        this.EliteMobCount = a;
    }

    public void SetEliteMobCommonCount(int a) {
        this.EliteMobCommonCount = a;
    }

    public boolean isEliteBossMap() {
        return this.elitebossmap;
    }

    public void setEliteBossMap(boolean bool) {
        this.elitebossmap = bool;
    }

    public boolean isEliteBossRewardMap() {
        return this.elitebossrewardmap;
    }

    public void setEliteBossRewardMap(boolean bool) {
        this.elitebossrewardmap = bool;
    }

    public final int getId() {
        return mapid;
    }

    public boolean canDelete() {
        return (System.currentTimeMillis() - lastPlayerLeft > (30 * 60 * 1000L)) && (getCharactersSize() == 0);
    }
    
    public final MapleMap getReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
    }

    public final int getReturnMapId() {
        return returnMapId;
    }

    public final int getForcedReturnId() {
        return forcedReturnMap;
    }

    public final MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
    }

    public final void setForcedReturnMap(final int map) {
        this.forcedReturnMap = map;
    }

    public final float getRecoveryRate() {
        return recoveryRate;
    }

    public final void setRecoveryRate(final float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }
    
    public final int getBarrier() {
        return barrier;
    }

    public final void setBarrier(final int barrier) {
        this.barrier = barrier;
    }

    public final int getFieldLimit() {
        return fieldLimit;
    }

    public final void setFieldLimit(final int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public final String getFieldType() {
        return fieldType;
    }

    public final void setFieldType(final String fieldType) {
        this.fieldType = fieldType;
    }

    public final void setCreateMobInterval(final short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public final void setTimeLimit(final int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public final void setMapName(final String mapName) {
        this.mapName = mapName;
    }

    public final String getMapName() {
        return mapName;
    }

    public final String getStreetName() {
        return streetName;
    }

    public final void setChangeableMobOrigin(MapleCharacter d) {
        this.changeMobOrigin = new WeakReference<MapleCharacter>(d);
    }

    public final MapleCharacter getChangeableMobOrigin() {
        if (changeMobOrigin == null) {
            return null;
        }
        return changeMobOrigin.get();
    }

    public final void setFirstUserEnter(final String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public final void setUserEnter(final String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }
    
    public final void setFieldScript(final String fieldScript) {
        this.fieldScript = fieldScript;
    }

    public final boolean hasClock() {
        return clock;
    }

    public final void setClock(final boolean hasClock) {
        this.clock = hasClock;
    }

    public final boolean isTown() {
        return town;
    }

    public final void setTown(final boolean town) {
        this.town = town;
    }

    public final boolean allowPersonalShop() {
        return personalShop;
    }

    public final void setPersonalShop(final boolean personalShop) {
        this.personalShop = personalShop;
    }

    public final void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    public final void setEverlast(final boolean everlast) {
        this.everlast = everlast;
    }

    public final boolean getEverlast() {
        return everlast;
    }

    public final int getHPDec() {
        return decHP;
    }

    public final void setHPDec(final int delta) {
        decHP = (short) delta;
    }

    public final int getHPDecProtect() {
        return protectItem;
    }

    public final void setHPDecProtect(final int delta) {
        this.protectItem = delta;
    }

    public final int getCurrentPartyId() {
        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (chr.getPartyId() != -1) {
                    return chr.getPartyId();
                }
            }
            return -1;
        } finally {
            mutex.unlock();
        }
    }

    public final void addMapObject(final MapleMapObject mapobject) {
        mutex.lock();
        int newoid;
        try {
            newoid = ++runningOid;
            mapobject.setObjectId(newoid);
            mapobjects.put(newoid, mapobject);
        } finally {
            mutex.unlock();
        }
        if (mapobject.getType() == MapleMapObjectType.ITEM) {
            droppedItems.add(mapobject.getObjectId());
        }

    }

    private void spawnAndAddRangedMapObject(final MapleMapObject mapobject, final DelayedPacketCreation packetbakery, final SpawnCondition condition) {
        mutex.lock();
        try {
            runningOid++;
            mapobject.setObjectId(runningOid);
            mapobjects.put(runningOid, mapobject);
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (condition == null || condition.canSpawn(chr)) {
                    if (chr.getPosition().distanceSq(mapobject.getPosition()) <= GameConstants.maxViewRangeSq()) {
                        packetbakery.sendPackets(chr.getClient());
                        chr.addVisibleMapObject(mapobject);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
    }
    
    public final List<MapleMist> getMistInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        final List<MapleMist> ret2 = new ArrayList<MapleMist>();
        mutex.lock();
        try {
            final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
            MapleMapObject obj;
            while (ltr.hasNext()) {
                obj = ltr.next();
                if (MapObject_types.contains(obj.getType())) {
                    if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                        ret.add(obj);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
        return ret2;
    }
    
    public final List<MapleSummon> getSummonInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        final List<MapleSummon> ret2 = new ArrayList<MapleSummon>();
        mutex.lock();
        try {
            final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
            MapleMapObject obj;
            while (ltr.hasNext()) {
                obj = ltr.next();
                if (MapObject_types.contains(obj.getType())) {
                    if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                        ret.add(obj);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
        return ret2;
    }
    
    public final List<MapleRune> getRuneInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> mapobjects = this.getMapObjectsInRange(from, rangeSq);
        final List<MapleRune> runes = new ArrayList<MapleRune>();
        for (int i = 0; i < mapobjects.size(); i++) {
            if (mapobjects.get(i).getType() == MapleMapObjectType.RUNE) {
                runes.add((MapleRune) mapobjects.get(i));
            }
        }
        return runes;
    }

    public final MapleMapObject getMapObject(final int oid) {
        return mapobjects.get(oid);
    }
    
    public final MapleMapObject getMapObject(int oid, MapleMapObjectType type) {
        mutex.lock();
        try {
            return mapobjects.get(oid);
        } finally {
            mutex.unlock();
        }
    }
    
    public final void checkClockContact(final MapleCharacter chr, final MapleMonster monster) {
        final Point m_Pos = monster.getPosition();
        if (this.getFieldType().equals("63")) {
            for (MapleMapObject object : this.getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MIST))) {
                final MapleMist clock = (MapleMist) object;
                if (clock.isClock()) {
                    Rectangle rect = new Rectangle(clock.getBox().x, clock.getBox().y, clock.getBox().width, clock.getBox().height);
                    if (rect.intersects(monster.getRectangle())) {
                        clock.setUsed(true);
                        chr.send(UIPacket.showInfo("반반이 시간을 움직임"));
                        return;
                    }
                }
            }
        }
    }

    public final void removeMapObject(final int num) {
        mutex.lock();
        try {
            mapobjects.remove(Integer.valueOf(num));
        } finally {
            mutex.unlock();
        }
        if (droppedItems.contains(Integer.valueOf(num))) {
            droppedItems.remove(Integer.valueOf(num));
        }

    }

    public final void removeMapObject(final MapleMapObject obj) {
        mutex.lock();
        try {
            if (mapobjects.containsKey(Integer.valueOf(obj.getObjectId()))) {
                mapobjects.remove(Integer.valueOf(obj.getObjectId()));
            }
        } finally {
            mutex.unlock();
        }
        if (obj.getType() == MapleMapObjectType.ITEM) {
            if (droppedItems.contains(Integer.valueOf(obj.getObjectId()))) {
                droppedItems.remove(Integer.valueOf(obj.getObjectId()));
            }
        }
    }

    public final Point calcPointMaple(final Point initial) {
        final MapleFoothold fh = footholds.findMaple(initial);
        if (fh == null) {
//            System.err.println("[오류] 위치를 구하던 중 바닥의 풋홀드를 구하는데 실패했습니다.");
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            final double s1 = Math.abs(fh.getY2() - fh.getY1());
            final double s2 = Math.abs(fh.getX2() - fh.getX1());
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            } else {
                dropY = fh.getY1() + (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            }
        }
        return new Point(initial.x, dropY);
    }

    public final Point calcDropPos(final Point initial, final Point fallback) {
        final Point ret = calcPointMaple(new Point(initial.x, initial.y - 50));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(final MapleCharacter chr, final MapleMonster mob) {
        final ItemInformation ii = ItemInformation.getInstance();
        final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getTruePosition().x, cmServerrate = ChannelServer.getInstance(channel).getMesoRate(), chServerrate = ChannelServer.getInstance(channel).getDropRate(), caServerrate = 10;
        Item idrop;
        Item te;
        byte d = 1;
        Point pos = new Point(0, mob.getTruePosition().y);
        double showdown = 100.0;
        final MonsterStatusEffect mse = mob.getBuff(MonsterStatus.SHOWDOWN);
        final MapleMonsterProvider mi = MapleMonsterProvider.getInstance();
        final List<MonsterDropEntry> derp = mi.retrieveDrop(mob.getId());
        final MapleClient c = null;
        if (derp == null) {
            return;
        }
        final List<MonsterDropEntry> dropEntry = new ArrayList<MonsterDropEntry>(derp);
        if (chr.isEquippedSoulWeapon()) {
            dropEntry.add(new MonsterDropEntry(4001536, Integer.MAX_VALUE, 1, Randomizer.rand(1, 3), (short) 0));
        }
        Collections.shuffle(dropEntry);
        boolean mesoDropped = false;
        int ce = 0, maxdrop = -1;
        if ((mob.getStats().isBoss()) && (ServerConstants.useBossMaxDrop)) {
            maxdrop = ServerConstants.bossMaxDrop;
        } else if ((!mob.getStats().isBoss()) && (ServerConstants.useMaxDrop)) {
            maxdrop = ServerConstants.maxDrop;
        }
        for (final MonsterDropEntry de : dropEntry) {
            if (de.itemId == mob.getStolen()) {
                continue;
            }
            if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP || de.itemId == 4000244) {
                ce = (int) ((de.chance * chServerrate) / 10);
            } else {
                ce = (int) (de.chance * chServerrate);
            }
            if (de.itemId == 4000245) {
                return;
            }
            if ((Randomizer.nextInt(999999) < de.chance * chServerrate * (showdown / 100.0D)) && ((maxdrop == -1) || (d < maxdrop + 1))) {
                if (mesoDropped && droptype != 3 && de.itemId == 0) {
                    continue;
                }
                if (de.itemId / 10000 == 238 && !mob.getStats().isBoss()) {
                    continue;
                }
                if (droptype == 3) {
                    pos.x = (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) {
                    int mesos = Randomizer.nextInt(1 + Math.abs(de.Maximum - de.Minimum)) + de.Minimum;
                    if (mesos > 0) {
                        spawnMobMesoDrop((int) (mesos * cmServerrate), calcDropPos(pos, mob.getTruePosition()), mob, chr, false, droptype);
                        mesoDropped = true;
                    }
                } else {
                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                        if (GameConstants.환생의불꽃아이템(idrop.getItemId())) {
                           idrop = (Item) InventoryHandler.환생의불꽃((Equip)idrop);
                        }
                    } else {
                        final int range = Math.abs(de.Maximum - de.Minimum);
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(range <= 0 ? 1 : range) + de.Minimum : 1), (byte) 0);
                    }
                        spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
                }
                d++;
            }
        }
        final List<MonsterGlobalDropEntry> globalEntry = new ArrayList<MonsterGlobalDropEntry>(mi.getGlobalDrop());
        Collections.shuffle(globalEntry);
        final int cashz = (int) ((mob.getStats().isBoss() && mob.getStats().getHPDisplayType() == 0 ? 20 : 1) * caServerrate);
        final int cashModifier = (int) (mob.getMobExp() / 1000 + mob.getMobMaxHp() / 20000); //no rate
        for (final MonsterGlobalDropEntry de : globalEntry) {
            if (Randomizer.nextInt(999999) < de.chance) {
                if (de.itemId != 0) {
                    if (droptype == 3) {
                        pos.x = (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                    } else {
                        pos.x = (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    }
                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                        if (GameConstants.환생의불꽃아이템(idrop.getItemId())) {
                           idrop = InventoryHandler.환생의불꽃((Equip)idrop);
                        }
                    } else {
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1), (byte) 0);
                    }
                    if (de.itemId == 4000421 && !chr.haveItem(2430492)) {
                        return;
                    }
                    if (de.itemId == 2022165) {
                        return;
                    }
                    if ((de.itemId == 4001513 && !(mob.getStats().getLevel() >= 105 && mob.getStats().getLevel() <= 114)) || (de.itemId == 4001515 && !(mob.getStats().getLevel() >= 115 && mob.getStats().getLevel() <= 159)) || (de.itemId == 4001521 && !(mob.getStats().getLevel() >= 160 && mob.getStats().getLevel() <= 250))) {
                        return;
                    }
                    spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
                    d++;
                } else {
                    chr.modifyCSPoints(1, Randomizer.rand(50, 1000), true);
                }
            }
        }
    }

    private final void killMonster(final MapleMonster monster) { // For mobs with removeAfter
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        monster.spawnRevives(this);
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
        removeMapObject(monster);
    }

    public final void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean second, final byte animation) {
        if ((monster.getId() == 8810018 || monster.getId() == 8810122) && !second) {
            MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    killMonster(monster, chr, true, true, (byte) 1);
                    killAllMonsters(true);
                }
            }, 3000);
            return;
        }
        
        MapleCharacter dropOwner = monster.killBy(chr);
        
        if (GameConstants.isZero(chr.getJob())) { //WP 흡수
             int gainWP = Randomizer.rand(0, 5);
             chr.gainWP(gainWP);
             chr.send(MainPacketCreator.ZeroUpdate(chr));
             chr.send(MainPacketCreator.absorbingDF(monster.getObjectId(), chr.addWP(gainWP), gainWP, true, chr, chr.getTruePosition()));
             chr.send(MainPacketCreator.ZeroWP(gainWP));
        }
         
        if ((chr.getSkillLevel(4221013) > 0) && (chr.KillingPoint < 5)) {
          chr.KillingPoint += 1;
          chr.send(MainPacketCreator.KillingPoint(chr.KillingPoint));
        }
        
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));
        spawnedMonstersOnMap.decrementAndGet();
        removeMapObject(monster);
        
        if (monster.getBuffToGive() > -1) {
            final int buffid = monster.getBuffToGive();
            final SkillStatEffect buff = ItemInformation.getInstance().getItemEffect(buffid);
            for (final MapleMapObject mmo : characters) {
                final MapleCharacter c = (MapleCharacter) mmo;
                if (c.isAlive()) {
                    buff.applyTo(c);
                    switch (monster.getId()) {
                        case 8810018:
                        case 8810122:
                        case 8820001:
                        case 8820101:
                            c.getClient().getSession().write(MainPacketCreator.showOwnBuffEffect(buffid, 12)); // HT nine spirit
                            broadcastMessage(c, MainPacketCreator.showBuffeffect(c.getId(), c.getLevel(), buffid, 0, 12), false); // HT nine spirit
                            break;
                    }
                }
            }
        }
        final int mobid = monster.getId();

        if (mobid == 8810018) {
            chr.setExpeditionKilledBoss(true);
            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(6, "수많은 도전 끝에 혼테일을 격파한 원정대여! 그대들이 진정한 리프레의 영웅이다!").getBytes());
        } else if (mobid == 8810122) { // Horntail
            chr.setExpeditionKilledBoss(true);
            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(6, "수많은 도전 끝에 카오스 혼테일을 격파한 원정대여! 그대들이 진정한 리프레의 영웅이다!").getBytes());
        } else if (mobid == 8820001) {
            chr.setExpeditionKilledBoss(true);
            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(6, "지치지 않는 열정으로 핑크빈을 물리친 원정대여! 그대들이 진정한 시간의 승리자다!").getBytes());
        } else if (mobid == 8820101) {
            chr.setExpeditionKilledBoss(true);
            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(6, "지치지 않는 열정으로 카오스 핑크빈을 물리친 원정대여! 그대들이 진정한 시간의 승리자다!").getBytes());
        } else if (mobid == 8850011) {
            chr.setExpeditionKilledBoss(true);
            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(6, "미래에서 여제 시그너스를 물리친 원정대여! 그대들이 진정한 시간의 승리자다!").getBytes());
        } else if (mobid >= 8800003 && mobid <= 8800010) {
            boolean makeZakReal = true;
            for (final MapleMapObject mons : getAllMonster()) {
                MapleMonster mob = (MapleMonster) mons;
                if (mob.getId() >= 8800003 && mob.getId() <= 8800010) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (final MapleMapObject mons : getAllMonster()) {
                    MapleMonster mob = (MapleMonster) mons;
                    if (mob.getId() == 8800000) {
                        final Point pos = mons.getPosition();
                        this.killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8800000), pos);
                        break;
                    }
                }
            }
        } else if (mobid >= 8800103 && mobid <= 8800110) {
            boolean makeZakReal = true;
            for (final MapleMapObject mons : getAllMonster()) {
                MapleMonster mob = (MapleMonster) mons;
                if (mob.getId() >= 8800103 && mob.getId() <= 8800110) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (final MapleMapObject mons : getAllMonster()) {
                    MapleMonster mob = (MapleMonster) mons;
                    if (mob.getId() == 8800100) {
                        final Point pos = mons.getPosition();
                        this.killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8800100), pos);
                        break;
                    }
                }
            }
        } else if (mobid == 8820008 || mobid == 8820108) { //wipe out statues and respawn
            for (final MapleMapObject mmo : getAllMonster()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getLinkOid() != monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if (mobid >= 8820010 && mobid <= 8820014) {
            for (final MapleMapObject mmo : getAllMonster()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getId() != 8820000 && mons.getId() != 8820001 && mons.getObjectId() != monster.getObjectId() && mons.isAlive() && mons.getLinkOid() == monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if (mobid >= 8820110 && mobid <= 8820114) {
            for (final MapleMapObject mmo : getAllMonster()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getId() != 8820100 && mons.getId() != 8820101 && mons.getObjectId() != monster.getObjectId() && mons.isAlive() && mons.getLinkOid() == monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if (mobid >= 8810102 && mobid <= 8810109) {
            boolean notyetdead = false;
            for (int i = 8810102; i < 8810109; i++) {
                if (getMonsterById(i) != null) {
                    notyetdead = true;
                    break;
                }
            }
            if (!notyetdead) {
                killMonster(getMonsterById(8810122), chr, false, false, (byte) 0);
            }
        } else if (mobid >= 8850000 && mobid <= 8850003) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(mobid + 1), new Point(-363, 100));
        } else if (mobid == 8850004) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8850012), new Point(-363, 100));
        }
        if (mobid == 8800002) { //자쿰
            chr.setExpeditionKilledBoss(true);
        }
        if (mobid == 8800102) { //카오스자쿰
            chr.setExpeditionKilledBoss(true);
        }
        if (mobid == 8840000) { //반레온
            chr.setExpeditionKilledBoss(true);
        } 
        switch(mapid) {
            case 105200130:
            case 105200200:
            case 105200300:
            case 105200400:
            case 105200140:
            case 105200210:
            case 105200313:
            case 105200411:
                if(chr.getClient().getChannelServer().getMapFactory().getMap(mapid).getAllMonster().size() == 0) {
                    broadcastMessage(MainPacketCreator.showEffect("Gstar/ClearS"));
                }
        }
        if(monster.getId() == 8920100) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8920101), monster.getPosition());
        } else if (monster.getId() == 8920101) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8920102), monster.getPosition());
        } else if (monster.getId() == 8920102) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8920103), monster.getPosition());
        }
        if (monster.getId() == 8900100) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8900101), monster.getPosition());
        } else if (monster.getId() == 8900101) {
            spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(8900102), monster.getPosition());
        }
        
        if (monster.getId() == 8910001 || monster.getId() == 8900102 || monster.getId() == 8920103 || monster.getId() == 8930100) {
            broadcastMessage(MainPacketCreator.showEffect("killing/clear"));
            broadcastMessage(MainPacketCreator.playSound("Party1/Clear"));
            int i = monster.getId() == 8930100 ? 7 + Randomizer.nextInt(13) : monster.getId() == 8920103 ? 5 + Randomizer.nextInt(4) : monster.getId() == 8900102 ? 3 + Randomizer.nextInt(4) : 6 + Randomizer.nextInt(9);
            for (final MapleCharacter partymem : chr.getClient().getChannelServer().getPartyMembers(chr.getParty())) {
               partymem.gainItem(4310064, i);
            }        
            broadcastMessage(UIPacket.getItemTopMsg(4310064, "이그드라실 룬의 돌을 " + i + "개 획득 하셨습니다."));
        }
       
        if (withDrops) {
                if (dropOwner == null) {
                    dropOwner = chr;
                }
                dropFromMonster(dropOwner, monster);
        }
        
        if (!this.isEliteBossMap() && !monster.isEliteMonster() && !monster.isEliteBoss() && !monster.getStats().isBoss() && monster.getStats().getLevel() - 20 <= chr.getLevel() && chr.getLevel() <= monster.getStats().getLevel() + 20) {
            SetEliteMobCommonCount(this.EliteMobCommonCount + 1);
        } else if (!this.isEliteBossMap() && monster.isEliteMonster()) {
            SetEliteMobCount(this.EliteMobCount + 1);
            if (1 <= EliteMobCount() && EliteMobCount() <= 14) {
                broadcastMessage(MainPacketCreator.startMapEffect("어두운 기운이 사라지지 않아 이곳을 음산하게 만들고 있습니다.", 5120124, true));
            } else if (15 <= EliteMobCount() && EliteMobCount() <= 20) {
                broadcastMessage(MainPacketCreator.startMapEffect("이곳이 어두운 기운으로 가득차 곧 무슨 일이 일어날 듯 합니다.", 5120124, true));
            }
            monster.setEliteMonster(false);
            timeAllPlayer(this);
        } else if (monster.isEliteBoss()) { //보스 퇴치시
            setEliteBossMap(false);
            CancelEliteBossAlarm(this, chr);
            setEliteBossRewardMap(true);
            broadcastMessage(UIPacket.showSpecialMapEffect(3, 0, "Bgm36.img/HappyTimeShort", "Map/Map/Map9/924050000.img/back"));
            final String msg1 = "착한 모험가들에게 선물을 주지! 내가 던지는 아이템을 잘 받아 봐!";
            tools.Timer.ShowTimer.getInstance().schedule(new Runnable() {
                @Override
                public final void run() {
                    broadcastMessage(MainPacketCreator.startMapEffectTime(msg1, 0x92, 4000));
                    final MapleMonster mob1 = MapleLifeProvider.getMonster(8220028);
                    final MapleMonster mob2 = MapleLifeProvider.getMonster(8220028);
                    final MapleMonster mob3 = MapleLifeProvider.getMonster(8220028);
                    final MapleMonster mob4 = MapleLifeProvider.getMonster(8220028);
                    final MapleMonster[] mobs = {mob1, mob2, mob3, mob4};
                    final OverrideMonsterStats ostats1 = new OverrideMonsterStats();
                    ostats1.setOFirstAttack(false);
                    final OverrideMonsterStats ostats2 = new OverrideMonsterStats();
                    ostats2.setOFirstAttack(false);
                    final OverrideMonsterStats ostats3 = new OverrideMonsterStats();
                    ostats3.setOFirstAttack(false);
                    final OverrideMonsterStats ostats4 = new OverrideMonsterStats();
                    ostats4.setOFirstAttack(false);
                    mob1.setOverrideStats(ostats1);
                    mob2.setOverrideStats(ostats2);
                    mob3.setOverrideStats(ostats3);
                    mob4.setOverrideStats(ostats4);
                    for (int i = 0; i < 4; i++) {
                        spawnMonster(mobs[i], -2);
                    }
                    final List<Integer> specialCount = new ArrayList<>();
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            for (int i = 0; i < mobs.length; i++) {
                                int Random = Randomizer.rand(0, 62);
                                int itemid = 0;
                                if (Random >= 0 && Random <= 10) {
                                    itemid = 2432391;
                                } else if (Random >= 11 && Random <= 20) {
                                    itemid = 2432392;
                                } else if (Random >= 21 && Random <= 30) {
                                    itemid = 2432393;
                                } else if (Random >= 31 && Random <= 40) {
                                    itemid = 2432394;
                                } else if (Random >= 41 && Random <= 50) {
                                    itemid = 2432395;
                                } else if (Random >= 51 && Random <= 55) {
                                    itemid = 2432396;
                                } else if (Random >= 56 && Random <= 60) {
                                    itemid = 2432397;
                                } else if (Random >= 61 && Random <= 62) {
                                    if (specialCount.size() < 3) {
                                        itemid = 2432398;
                                        specialCount.add(itemid);
                                    }
                                }
                                MapleMonster dropper = getMonsterByOid(mobs[i].getObjectId());
                                IItem drop = new Item(itemid, (byte) 0, (short) 1, (byte) 0);

                                spawnItemDrop(dropper, chr, drop, dropper.getPosition(), true, true, true, true, 0x96, Randomizer.rand(0x37, 0xC7));
                            }
                        }

                    }, 500, 500);
                    broadcastMessage(MainPacketCreator.getMilliClock(22000));
                    MapTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public final void run() {
                            setEliteBossRewardMap(false);
                            broadcastMessage(UIPacket.cancelSpecialMapEffect());
                            broadcastMessage(MainPacketCreator.playSound("eliteMonster/gameOver"));
                            List<MapleMapObject> items = getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
                            for (MapleMapObject i : items) {
                                removeMapObject(i);
                                broadcastMessage(MainPacketCreator.removeItemFromMap(i.getObjectId(), 0, 0), i.getPosition());
                            }

                        }
                    }, 23000);
                    /*
                     2432391 - 경험치(소)
                     2432392 - 경험치(대)
                     2432393 - 메소(소)
                     2432394 - 메소(대)
                     2432395 - 포션
                     2432396 - 방어구
                     2432397 - 무기
                     2432398 - 희귀 아이템
                     */
                }
            }, 7000L);
        }
        if (this.EliteMobCommonCount == 200 && this.EliteMobCount < 5) {
            SetEliteMobCommonCount(0);
            MapleMonster elite = makeEliteMonster(monster);
            spawnMonsterOnGroundBelow(elite, elite.getPosition());
            broadcastMessage(UIPacket.playSpecialMapSound("Field.img/eliteMonster/Regen"), chr.getPosition());
            broadcastMessage(MainPacketCreator.startMapEffect("어두운 기운과 함께 강력한 몬스터가 출현합니다.", 5120124, true));
            timeAllPlayer(this);
        }
        if (this.EliteMobCommonCount == 200 && this.EliteMobCount >= 8) { //보스 소환
            SetEliteMobCommonCount(0);
            SetEliteMobCount(0);
            setEliteBossMap(true);
            broadcastMessage(UIPacket.showSpecialMapEffect(2, 0, "Bgm36.img/RoyalGuard", null));
            chr.getMap().setClock(true);
            chr.getMap().setTimeLimit(timeLimit);

            final int RandomI = Randomizer.rand(0, 4);
            final int BossID = 8220020 + RandomI + 2;
            final int EffectID = 5120120 + RandomI + 5;
            final String EffectMsgs[] = {"검은 기사 모카딘 : 위대한 분을 위하여 너를 처단하겠다.",
                "미친 마법사 카리아인 : 미천한 것들이 날뛰고 있구나. 크크크크...",
                "돌격형 CQ57 : 목표발견. 제거 행동을 시작한다.",
                "인간사냥꾼 줄라이 : 사냥감이 나타났군.",
                "싸움꾼 플레드 : 재미 있겠군. 어디 한 번 놀아볼까.",};
            final MapleMonster boss = makeEliteBoss(monster, MapleLifeProvider.getMonster(BossID));
            final MapleMonster elite1 = makeEliteMonster(monster);
            final MapleMonster elite2 = makeEliteMonster(monster);
            MapleMonster killmob;
            for (MapleMapObject monstermo : getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
                killmob = (MapleMonster) monstermo;
                killMonster(killmob);
            }
            tools.Timer.ShowTimer.getInstance().schedule(new Runnable() {
                public final void run() {
                    spawnMonsterOnGroundBelow(elite1, elite1.getPosition());
                    spawnMonsterOnGroundBelow(elite2, elite2.getPosition());
                    spawnMonsterOnGroundBelow(boss, boss.getPosition());
                    AlarmEliteBoss(chr.getMap(), chr, BossID);
                    broadcastMessage(MainPacketCreator.startMapEffect(EffectMsgs[RandomI], EffectID, true));
                    timeAllPlayer(chr.getMap());
                }
            }, 6000L);
        }
    }
    
    public MapleMonster makeEliteMonster(final MapleMonster monster) {
        final MapleMonster elite = MapleLifeProvider.getMonster(monster.getId());
        final OverrideMonsterStats ostats = new OverrideMonsterStats();
        final MapleMonsterStats stats = elite.getStats();
        elite.setEliteMonster(true);
        elite.setEliteType(Randomizer.rand(0x70, 0x88));
        ostats.setOHp(elite.getMobMaxHp() * 30);
        ostats.setOMp(elite.getMobMaxMp());
        ostats.setOPad(stats.getPad() * 8);
        ostats.setOPhysicalDefense(stats.getPhysicalDefense());
        ostats.setOMad(stats.getMad() + 60);
        ostats.setOMagicDefense(stats.getMagicDefense());
        ostats.setOSpeed(stats.getSpeed() + 30);
        ostats.setOAcc(stats.getAcc());
        ostats.setOEva(stats.getEva());
        ostats.setOPushed(stats.getPushed() * 2);
        ostats.setOLevel(stats.getLevel());
        elite.setOverrideStats(ostats);
        elite.setPosition(monster.getTruePosition());
        elite.setFh(monster.getFh());
        return elite;
    }

    public MapleMonster makeEliteBoss(final MapleMonster sourcemob, final MapleMonster sourceboss) {
        final MapleMonster eliteboss = MapleLifeProvider.getMonster(sourceboss.getId());
        final OverrideMonsterStats ostats = new OverrideMonsterStats();
        eliteboss.setEliteBoss(true);
        eliteboss.setEliteType(Randomizer.rand(0x64, 0x88));
        ostats.setOHp(sourcemob.getMobMaxHp() * 75); //필드몹의 75배
        ostats.setOMp(sourcemob.getMobMaxMp());
        ostats.setOPad(0);
        ostats.setOPhysicalDefense(sourcemob.getStats().getPhysicalDefense());
        ostats.setOMad((int) (sourcemob.getStats().getMad() * 2.5));
        ostats.setOMagicDefense(sourcemob.getStats().getMagicDefense());
        ostats.setOSpeed(sourcemob.getStats().getSpeed() + 35);
        ostats.setOAcc(sourcemob.getStats().getAcc());
        ostats.setOEva(sourcemob.getStats().getEva());
        ostats.setOPushed(0);
        ostats.setOLevel(sourcemob.getStats().getLevel());
        ostats.setOExp(0);
        eliteboss.setOverrideStats(ostats);
        eliteboss.setFh(sourcemob.getFh());
        return eliteboss;
    }
    
   private void time(final MapleCharacter chr) {
        MapTimer.getInstance().schedule(new Runnable() {
            public final void run() {
                chr.send(MainPacketCreator.removeMapEffect());
            }
        }, 5000L);
    }

    private void timeAllPlayer(final MapleMap map) {
        MapTimer.getInstance().schedule(new Runnable() {
            public final void run() {
                broadcastMessage(MainPacketCreator.removeMapEffect());
            }
        }, 5000L);
    }

    private void AlarmEliteBoss(final MapleMap currentmap, final MapleCharacter player, final int mobid) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public final void run() {
                if (currentmap.isEliteBossMap()) {
                    MapleWorldMapProvider mwmp = ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory();
                    Iterator itr = mwmp.getMaps().values().iterator();
                    while (itr.hasNext()) {
                        MapleMap target = ((MapleMap) itr.next());
                        if (target.getReturnMapId() == currentmap.getReturnMapId()) {
                            target.broadcastMessage(UIPacket.eliteBossNotice(2, currentmap.getId(), mobid));
                        }
                    }
                    mwmp = null;
                    itr = null;
                } else {
                    this.cancel();
                }
            }
        }, 0, 30000);
    }

    private void CancelEliteBossAlarm(final MapleMap currentmap, final MapleCharacter player) {
        MapleWorldMapProvider mwmp = ChannelServer.getInstance(player.getClient().getChannel()).getMapFactory();
        Iterator itr = mwmp.getMaps().values().iterator();
        while (itr.hasNext()) {
            MapleMap target = ((MapleMap) itr.next());
            if (target.getReturnMapId() == currentmap.getReturnMapId()) {
                target.broadcastMessage(UIPacket.eliteBossNotice(1, currentmap.getId(), 0));
            }
        }
        mwmp = null;
        itr = null;
    }

    public final void killAllMonsters(final boolean animate) {
        for (final MapleMapObject m : getAllMonster()) {
            MapleMonster monster = (MapleMonster) m;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
            removeMapObject(monster);
        }
    }

    public final void killMonster(final int monsId) {
        for (final MapleMapObject mmo : getAllMonster()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
                spawnedMonstersOnMap.decrementAndGet();
                removeMapObject(mmo);
                break;
            }
        }
    }

    public final void destroyReactor(final int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        if (reactor == null) {
            return;
        }
        broadcastMessage(MainPacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);

        if (reactor.getDelay() > 0) {
            MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public final void run() {
                    respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }
    

    /*
     * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
     * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
     */
    public final void resetReactors(MapleClient c) {
        setReactorState(c, (byte) 0);
    }

    public final void setReactorState(MapleClient c) {
        setReactorState(c, (byte) 1);
    }

    public final void setReactorState(MapleClient c, byte state) {
        for (final MapleMapObject o : getAllReactor()) {
            ((MapleReactor) o).setState(state);
            ((MapleReactor) o).setTimerActive(false);
            broadcastMessage(MainPacketCreator.triggerReactor((MapleReactor) o, 1, c.getPlayer().getId()));
        }
    }

    /*
     * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
     */
    public final void shuffleReactors() {
        List<Point> points = new ArrayList<Point>();
        for (final MapleMapObject o : getAllReactor()) {
            points.add(((MapleReactor) o).getPosition());
        }
        Collections.shuffle(points);
        for (final MapleMapObject o : getAllReactor()) {
            ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
        }
    }

    /**
     * Automagically finds a new controller for the given monster from the chars
     * on the map...
     *
     * @param monster
     */
    public final void updateMonsterController(final MapleMonster monster, int Type1, int Type2) {
        if (!monster.isAlive()) {
            return;
        }
        if (monster.getController() != null) {
            if (monster.getController().getMap() != this) {
                monster.getController().stopControllingMonster(monster);
            } else {
                return;
            }
        }
        int mincontrolled = -1;
        MapleCharacter newController = null;

        this.mutex.lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) ltr.next();
                if ((!chr.isHidden()) && ((chr.getControlledMonsters().size() < mincontrolled) || (mincontrolled == -1))) {
                    mincontrolled = chr.getControlledMonsters().size();
                    newController = chr;
                }
            }
        } finally {
            this.mutex.unlock();
        }
        if (newController != null) {
            if (monster.isFirstAttack() && monster.getId() != 8220028) {
                if (Type1 == -1 || Type1 == 1) {
                    newController.controlMonster(monster, true);
                }
                if (monster.getId() != 8220028) {
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                }
            } else if (Type1 == -1 || Type1 == 1) {
                newController.controlMonster(monster, false);
            }
        }
    }

    public final int containsNPC(final int npcid) {
        for (MapleMapObject obj : getAllNPC()) {
            if (((MapleNPC) obj).getId() == npcid) {
                return obj.getObjectId();
            }
        }
        return -1;
    }

    public MapleMonster getMonsterById(int id) {
        mutex.lock();
        try {
            MapleMonster ret = null;
            Iterator<MapleMapObject> itr = getAllMonster().iterator();
            while (itr.hasNext()) {
                MapleMonster n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            mutex.unlock();
        }
    }

    public int countMonsterById(int id) {
        mutex.lock();
        try {
            int ret = 0;
            Iterator<MapleMapObject> itr = getAllMonster().iterator();
            while (itr.hasNext()) {
                MapleMonster n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret++;
                }
            }
            return ret;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * returns a monster with the given oid, if no such monster exists returns
     * null
     *
     * @param oid
     * @return
     */
    public final MapleMonster getMonsterByOid(final int oid) {
        final MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.MONSTER) {
            return (MapleMonster) mmo;
        }
        return null;
    }

    public final MapleNPC getNPCByOid(final int oid) {
        final MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.NPC || mmo.getType() == MapleMapObjectType.PLAYERNPC) {
            
            return (MapleNPC) mmo;
        }
        return null;
    }

    public final MapleNPC getNPCById(final int id) {
        for (MapleMapObject hmo : getAllNPC()) {
            MapleNPC d = (MapleNPC) hmo;
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    public final MapleReactor getReactorByOid(final int oid) {
        final MapleMapObject mmo = getMapObject(oid);
        if (mmo == null) {
            return null;
        }
        if (mmo.getType() == MapleMapObjectType.REACTOR) {
            return (MapleReactor) mmo;
        }
        return null;
    }

    public final MapleReactor getReactorByName(final String name) {
        for (final MapleMapObject obj : getAllReactor()) {
            if (((MapleReactor) obj).getName().equals(name)) {
                return (MapleReactor) obj;
            }
        }
        return null;
    }

    public final MapleReactor getReactor(final int rid) {
        for (final MapleMapObject obj : getAllReactor()) {
            if (((MapleReactor) obj).getReactorId() == rid) {
                return (MapleReactor) obj;
            }
        }
        return null;
    }

    public final void spawnTempNpc(final int id, final int x, final int y, final int owner) {
        final MapleNPC npc = MapleLifeProvider.getNPC(id);
        final Point pos = new Point(x, y);
        npc.setPosition(pos);
        npc.setCy(y);
        npc.setRx0(x + 50);
        npc.setRx1(x - 50);
        npc.setFh(getFootholds().findMaple(pos).getId());
        npc.setTemp(true);
        addMapObject(npc);
        tempnpcs3.put(owner, npc);
        for (MapleMapObject mo : characters) {
            MapleCharacter hp = ((MapleCharacter) mo);
            if (hp.getId() == owner) {
                hp.send(MainPacketCreator.spawnNPC(npc, true));
            }
        }
    }

    public final void removeTempNpc(final int id, final int owner) {

        for (final MapleMapObject npcmo : getAllNPC()) {
            final MapleNPC npc = (MapleNPC) npcmo;
            if (npc.isTemp() && npc.getId() == id && tempnpcs3.get(owner).getId() == id) {
                broadcastMessage(MainPacketCreator.removeNPC(npc.getObjectId()));
                removeMapObject(npc);
            }
        }
    }

    public final void spawnNpc(final int id, final Point pos) {
        final MapleNPC npc = MapleLifeProvider.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findMaple(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(MainPacketCreator.spawnNPC(npc, true));
    }

    public final void removeNpc(final int id) {
        Iterator<MapleMapObject> itr = getAllNPC().iterator();
        while (itr.hasNext()) {
            MapleNPC npc = (MapleNPC) itr.next();
            if (npc.isCustom() && (id == -1 || npc.getId() == id)) {
                broadcastMessage(MainPacketCreator.removeNPCController(npc.getObjectId()));
                broadcastMessage(MainPacketCreator.removeNPC(npc.getObjectId()));
                removeMapObject(npc);
                break;
            }
        }
    }

    public final void spawnMonster_sSack(final MapleMonster mob, final Point pos, final int spawnType) {
        spawnMonster_sSack(mob, pos, spawnType, 0);
    }

    public final void spawnMonster_sSack(final MapleMonster mob, final Point pos, final int spawnType, int effect) {
        final Point spos = calcPointMaple(new Point(pos.x, pos.y - 100));
        mob.setPosition(spos);
        spawnMonster(mob, spawnType, effect);
    }

    public final int getMapId() {
        return mapid;
    }

    public final void spawnMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
        spawnMonster_sSack(mob, pos, -2);
    }

    public final void spawnTempMonster(final int key, final int id, final Point pos) {
        if (tempmonsters3.containsKey(key) && getAllPlayer().size() == 1) {
            killMonster(tempmonsters3.get(key));
        }
        final MapleMonster mob = MapleLifeProvider.getMonster(id);
        tempmonsters3.put(key, mob);
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public final void spawnZakum(final Point pos) {
        final int[] zakpart = {8800000, 8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010};
        final int[] effectId = {0x77, 0x0B, 0, 0, 0, 0, 0, 0, 0, 0xEC};
        StringBuilder zakstr = new StringBuilder();
        for (final int i : zakpart) {
            final MapleMonster part = MapleLifeProvider.getMonster(i);
            final Point spos = calcPointMaple(new Point(pos.x, pos.y - 100));
            part.setPosition(spos);
            part.setFake(true);
            if (part.getId() >= 8800003 && part.getId() <= 8800010) {
                zakstr.append("R_Arm_00").append(part.getId() % 1000).append("_bound");
            } else {
                zakstr.append("box_body");
            }
            spawnFakeMonster(part, true, effectId[i], zakstr.toString());
            zakstr.delete(0, zakstr.length());
        }
    }

    public final void spawnChaosZakum(final Point pos) {
        final int[] zakpart = {8800100, 8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};
        final int[] effectId = {0x77, 0x0B, 0, 0, 0, 0, 0, 0, 0, 0xEC};
        StringBuilder zakstr = new StringBuilder();
        for (final int i : zakpart) {
            final MapleMonster part = MapleLifeProvider.getMonster(i);
            final Point spos = calcPointMaple(new Point(pos.x, pos.y - 100));
            part.setPosition(spos);
            part.setFake(true);
            if (part.getId() >= 8800103 && part.getId() <= 8800110) {
                zakstr.append("R_Arm_00").append(part.getId() % 1000).append("_bound");
            } else {
                zakstr.append("box_body");
            }
            spawnFakeMonster(part, true, effectId[i], zakstr.toString());
            zakstr.delete(0, zakstr.length());
        }
    }

    public final void spawnFakeMonsterOnGroundMaple(final MapleMonster mob, final Point pos) {
        Point spos = new Point(pos.x, pos.y - 100);
        spos = calcPointMaple(spos);
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob, false, 0, null);
    }

    private final void checkRemoveAfter(final MapleMonster monster) {
        final int ra = monster.getStats().getRemoveAfter();

        if (ra > 0) {
            MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public final void run() {
                    if (monster != null) {
                        killMonster(monster);
                    }
                }
            }, ra * 1000);
        }
    }

    public final void spawnRevives(final MapleMonster monster, final int oid) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        monster.setLinkOid(oid);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, -2, 0, oid, monster.isEliteMonster())); // TODO effect
            }
        }, null);
        updateMonsterController(monster, -1, 0);
        spawnedMonstersOnMap.incrementAndGet();
    }
    
    public final void spawnMonster(final MapleMonster monster, final int spawnType) {
        spawnMonster(monster, spawnType, 0);
    }

    public final void spawnMonster(final MapleMonster monster, final int spawnType, final int effect) {
        monster.setMap(this);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            public final void sendPackets(MapleClient c) {
                if (c != null) {
                    c.getSession().write(MobPacket.spawnMonster(monster, spawnType, effect, 0, monster.isEliteMonster()));
                }
            }
        }, null);
        updateMonsterController(monster, -1, 0);
        checkRemoveAfter(monster);
        spawnedMonstersOnMap.incrementAndGet();
    }
   
    public final void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        monster.setMap(this);
        monster.setPosition(pos);

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, -2, effect, 0, monster.isEliteMonster()));
            }
        }, null);
        updateMonsterController(monster, -1, 0);
        spawnedMonstersOnMap.incrementAndGet();
    }

    public final void spawnFakeMonster(final MapleMonster monster, final boolean isZakum, final int effetId, final String effectString) {
        monster.setMap(this);
        monster.setFake(true);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
		c.getSession().write(MobPacket.spawnFakeMonster(monster, 0, isZakum, effetId, effectString));
            }
        }, null);
        updateMonsterController(monster, -1, 0);
        spawnedMonstersOnMap.incrementAndGet();
    }
    
    public final void spawnRune(final MapleRune rune) {
        rune.setMap(this);
        spawnAndAddRangedMapObject(rune, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                /* Respawn Effect 발동 */
                c.getSession().write(RunePacket.spawnRune(rune, true));
                c.getSession().write(RunePacket.spawnRune(rune, false));
            }
        }, null);
    }

    public final void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);
        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MainPacketCreator.spawnReactor(reactor));
            }
        }, null);
    }

    private final void respawnReactor(final MapleReactor reactor) {
        if (reactor.getReactorId() >= 100000 && reactor.getReactorId() <= 200011 && reactor.getRank() > 0) {
            int reactid = GameConstants.getRandomProfessionReactorByRank(reactor.getRank());
            final MapleReactorStats stats = MapleReactorFactory.getReactor(reactid);
            final MapleReactor myReactor = new MapleReactor(stats, reactid);
            myReactor.setPosition(reactor.getPosition());
            myReactor.setDelay(900000);
            myReactor.setState((byte) 0);
            myReactor.setName("광맥");
            myReactor.setRank(reactor.getRank());
            spawnReactor(myReactor);
        } else {
            reactor.setState((byte) 0);
            reactor.setAlive(true);
            spawnReactor(reactor);
        }
    }

    public final void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            public final void sendPackets(MapleClient c) {
                door.sendSpawnData(c, true);
                c.getSession().write(MainPacketCreator.resetActions());
            }
        }, null);
    }
    
    public final void spawnMechDoor(final MapleMechDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MechanicSkill.mechDoorSpawn(door, true));
                c.getSession().write(MainPacketCreator.resetActions());
            }
        }, new SpawnCondition() {
            public final boolean canSpawn(final MapleCharacter chr) {
                return chr.getParty() == null;
            }
        });
    }

    public final void spawnDragon(final MapleDragon summon) {
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MainPacketCreator.spawnDragon(summon));
            }
        }, null);
    }

    public final void spawnSummon(final MapleSummon summon, final boolean animated, final int duration) {
        try {
            spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
                @Override
                public void sendPackets(MapleClient c) {
                    c.getSession().write(MainPacketCreator.spawnSummon(summon, summon.getSkillLevel(), duration, animated));
                }
            }, null);
            if (duration > 0) {
                MapTimer.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        broadcastMessage(MainPacketCreator.removeSummon(summon, true));
                        removeMapObject(summon);
                        summon.getOwner().removeVisibleMapObject(summon);
                        if (summon.getOwner().getSummons().get(summon.getSkill()) != null) {
                            summon.getOwner().getSummons().remove(summon.getSkill());
                        }
                    }
                }, duration);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final void spawnExtractor(final MapleExtractor ex) {
        spawnAndAddRangedMapObject(ex, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                ex.sendSpawnData(c);
            }
        }, null);
    }

    public final void spawnClockMist(final MapleMist clock) {
        spawnAndAddRangedMapObject(clock, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                broadcastMessage(MainPacketCreator.spawnClockMist(clock));
            }
        }, null);
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                broadcastMessage(MainPacketCreator.removeMist(clock.getObjectId(), false));
                removeMapObject(clock);
            }
        }, 22000);
    }
    
    public final void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake, boolean rv, boolean burningregion, boolean timecapsule) {
        spawnAndAddRangedMapObject(mist, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
               broadcastMessage(MainPacketCreator.spawnMist(mist));
            }
        }, null);

        final MapTimer tMan = MapTimer.getInstance();
        final ScheduledFuture<?> poisonSchedule;

        if (poison) {
            poisonSchedule = tMan.register(new Runnable() {
                @Override
                public void run() {
                    for (final MapleMapObject mo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                        if (mist.makeChanceResult()) {
                            ((MapleMonster) mo).applyStatus(mist.getOwner(), new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), null, false), true, duration, false);
                        }
                    }
                }
            }, 2000, 2500);
        } else if (rv) {
            poisonSchedule = tMan.register(new Runnable() {
                @Override
                public void run() {
                    for (final MapleMapObject mo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                        if (mist.makeChanceResult()) {
                            final MapleCharacter chr = ((MapleCharacter) mo);
                            chr.addMP((int) (mist.getSource().getX() * (chr.getStat().getMaxMp() / 100.0)));
                        }
                    }
                }
            }, 2000, 2500);
        } else if (burningregion) {
            poisonSchedule = tMan.register(new Runnable() {
                @Override
                public void run() {
                    for (final MapleMapObject mo : getAllPlayer()) {
                        final MapleCharacter chr = ((MapleCharacter) mo);
                        final ISkill skill = SkillFactory.getSkill(GameConstants.getLinkedAttackSkill(12121005));
                        final SkillStatEffect effect = skill.getEffect(chr.getSkillLevel(mist.getOwner().getSkillLevel(skill)));
                        boolean contain = getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER)).contains(mo);
                        if (chr.getBuffedValue(BuffStats.BOOSTER_R, 12121005) != null) {
                            if (!contain) {
                                chr.cancelEffect(skill.getEffect(1), false, -1);
                            }
                        } else {
                            if (contain) {
                                effect.applyTo(chr);
                            }
                        }
                    }
                }
            }, 2000, 2500);
        } else if (timecapsule) {
            poisonSchedule = tMan.register(new Runnable() {
                @Override
                public void run() {
                    for (MapleMapObject mmo : getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                        MapleCharacter chr = (MapleCharacter) mmo;
                        //캡슐 존재여부 체크
                        for (final MapleMapObject mistoo : chr.getMap().getMapObjectsInRange(chr.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MIST))) {
                            final MapleMist check = (MapleMist) mistoo;
                            if (mist.getOwner() == check.getOwner() && mist.isTimeCapsule()) {
                                for (MapleCoolDownValueHolder mcdvh : chr.getAllCooldowns()) {
                                    if (mcdvh.skillId != 36121007) {
                                        chr.changeCooldown(mcdvh.skillId, -15000);
                                    }
                                }
                            } else {
                                return;
                            }
                        }
                    }
                }
            }, 5000, 5000);
        } else {
            poisonSchedule = null;
        }
        tMan.schedule(new Runnable() {
            @Override
            public void run() {
                broadcastMessage(MainPacketCreator.removeMist(mist.getObjectId(), false));
                removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
            }
        }, duration);
    }

    public final void checkMaxItemInMap() {
        if (droppedItems.size() + 1 > 400) {
            MapleWorldMapItem mapitem = (MapleWorldMapItem) getMapObject(droppedItems.get(0));
            if (mapitem == null) {
                return;
            }
            if (mapitem.isPickedUp()) {
                return;
            }
            mapitem.setPickedUp(true);
            broadcastMessage(MainPacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
            removeMapObject(mapitem);
        }
    }

    public final void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleWorldMapItem drop = new MapleWorldMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(MainPacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
    }


    public final void spawnMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {

        final Point droppos = calcDropPos(position, position);
        final MapleWorldMapItem mdrop = new MapleWorldMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
        checkMaxItemInMap();
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MainPacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        if (!everlast) {
            MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 60000L);
        }
    }

    public final void spawnMobMesoDrop(final int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final MapleWorldMapItem mdrop = new MapleWorldMapItem(meso, position, dropper, owner, droptype, playerDrop);

        checkMaxItemInMap();
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MainPacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), position, (byte) 1));
            }
        }, null);

        MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 60000L);
    }

    public final void spawnMobDrop(final IItem idrop, final Point dropPos, final MapleMonster mob, final MapleCharacter chr, final byte droptype, final int questid) {
        final MapleWorldMapItem mdrop = new MapleWorldMapItem(idrop, dropPos, mob, chr, droptype, false, questid);
        checkMaxItemInMap();
        //Start nx block from fm
        int[] nxItems = {5150030, 5151025, 5152033, 5152035, 1002186, 1082102, 1002999, 1052211, 1072175, 1003000, 1052212, 1003001, 1052213, 1072406, 1002998, 1052210, 1072404};
        if (mapid != 104040000) { //HHG1
            for (int i : nxItems) {
                if (mdrop.getItemId() == i) {
                    return;
                }
            }
        }
        if (mdrop.getItemId() != 4001536) {
            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
                @Override
                public void sendPackets(MapleClient c) {
                    if (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) {
                        c.getSession().write(MainPacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte) 1));
                    }
                }
            }, null);
        } else { 
            runningOid++;
            mdrop.setObjectId(runningOid);
            chr.send(MainPacketCreator.dropItemFromMapObject(mdrop, mdrop.getPosition(), dropPos, (byte) 1));
            chr.send(MainPacketCreator.dropItemFromMapObject(mdrop, mdrop.getPosition(), dropPos, (byte) 0));
            chr.send(MainPacketCreator.removeItemFromMap(mdrop.getObjectId(), (byte) 2, chr.getId()));
            chr.send(SoulWeaponPacket.giveSoulGauge(chr.addgetSoulCount(), chr.getEquippedSoulSkill()));
            chr.checkSoulState(false);
        }
        MapTimer.getInstance().schedule(new ExpireMapItemJob(mdrop), 60000L);
        activateItemReactors(mdrop, chr.getClient());    
        if (mdrop.getItemId() == 4001536) {
            removeMapObject(mdrop);
        }
    }
    
    public final void spawnItemDrop(MapleMapObject dropper, MapleCharacter owner, IItem item, Point pos, boolean ffaDrop, boolean playerDrop) {
        spawnItemDrop(dropper, owner, item, pos, ffaDrop, playerDrop, false, false, 0, 0);
    }
        
    public final void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean playerDrop, boolean fly, boolean touch, int gradiant, int speed) {

        checkMaxItemInMap();
        IEquip equip = null;
        if (item.getType() == 1) {
            equip = (IEquip) item;
        }
        final Point droppos = calcDropPos(pos, pos);
        final MapleWorldMapItem drop = new MapleWorldMapItem(item, droppos, dropper, owner, (byte) 0, playerDrop, equip, fly, touch, gradiant, speed);

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(MainPacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 1));
            }
        }, null);
        broadcastMessage(MainPacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte) 0));

        if (!everlast) {
            MapTimer.getInstance().schedule(new ExpireMapItemJob(drop), 60000L);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(final MapleWorldMapItem drop, final MapleClient c) {
        final IItem item = drop.getItem();

        for (final MapleMapObject o : getAllReactor()) {
            final MapleReactor react = (MapleReactor) o;
            for (int i = 0; i < react.getStats().getStateEventSize(react.getState()); i++) {
                if (react.getReactorType((byte) i) == 100) {
                    if (react.getReactItem((byte) i).getLeft() == item.getItemId() && react.getReactItem((byte) i).getRight() == item.getQuantity()) {
                        if (react.getArea().contains(drop.getPosition())) {
                            if (!react.isTimerActive()) {
                                MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                                react.setTimerActive(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public final void returnEverLastItem(final MapleCharacter chr) {
        for (final MapleMapObject o : getAllItems()) {
            final MapleWorldMapItem item = ((MapleWorldMapItem) o);
            item.setPickedUp(true);
            broadcastMessage(MainPacketCreator.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getPosition());
            if (item.getMeso() > 0) {
                chr.gainMeso(item.getMeso(), false);
            } else {
                InventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
            }
            removeMapObject(item);
        }
    }
    
    public final void startMapEffect(final String msg, final int itemId) {
        startMapEffect(msg, itemId, 30000);
    }

    public final void startMapEffect(final String msg, final int itemId, final long time) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        MapTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, time);
    }

    public final void addPlayer(final MapleCharacter chr) {
        mutex.lock();
        try {
            RespawnNPC();
            characters.add(chr);
            mapobjects.put(chr.getObjectId(), chr);
        } finally {
            mutex.unlock();
        }

        if (!chr.isHidden()) {
            broadcastMessage(chr, MainPacketCreator.spawnPlayerMapobject(chr), false);
        }
        sendObjectPlacement(chr);

        if (chr.getKeyValue2("mountid") == -1) {
            chr.setKeyValue2("mountid", 0);
        }
        
        if (chr.getKeyValue2("mountskillid") == -1) {
            chr.setKeyValue2("mountskillid", 0);
        }
        
        if (!onFirstUserEnter.equals("")) {
            if (getCharactersSize() == 1) {
                MapleMapScriptMethods.startScript_FirstUser(chr.getClient(), onFirstUserEnter);
            }
        }
        
        if (!onUserEnter.equals("")) {
            MapleMapScriptMethods.startScript_User(chr.getClient(), onUserEnter);
        }
        
        if (mapid ==  ServerConstants.startMap) {
            chr.send(MainPacketCreator.musicChange("BgmEvent2/risingStar2"));
            mapEffect = new MapleMapEffect(ServerConstants.serverWelcome, 5121035);
            chr.send(mapEffect.makeStartData());
        }
        
        if (mapid == 109050001) { //이벤트맵 나가는 곳
            MapleMap map = chr.getClient().getChannelServer().getMapFactory().getMap(ServerConstants.startMap);
            chr.changeMap(map, map.getPortal(0));
        }
        
        if (QuickMove.getQuickMoves(mapid) != null) {
            chr.send(MainPacketCreator.getQuickMove(QuickMove.getQuickMoves(mapid)));
            chr.setQuickMoved(true);
        }
        chr.dispelDebuff(DiseaseStats.TELEPORT); //팅방지
                
        if (chr.getJob() >= 1400 && chr.getJob() <= 1412) {
            chr.acaneAim = 0;
        }
        
        for (int i = 0; i < 3; ++i) {
            if (chr.getPet(i) != null) {
                chr.getPet(i).setPos(chr.getPosition()); //펫 좌표 업데이트
                chr.getClient().send(PetPacket.updatePet(chr, chr.getPet(i), false, chr.getPetLoot()));
                broadcastMessage(PetPacket.showPet(chr, chr.getPet(i), false, false));
            }
        }
        
        if (chr.getPetAutoHP() > 0) {
            chr.getClient().send(MainPacketCreator.getPetAutoHP(chr.getPetAutoHP()));
        }
        
        if (chr.getPetAutoMP() > 0) {
            chr.getClient().send(MainPacketCreator.getPetAutoMP(chr.getPetAutoMP()));
        }
        
        if (chr.getAndroid() != null) { //Set
            chr.getAndroid().setPosition(chr.getPosition()); //안드로이드 좌표 업데이트
            broadcastMessage(chr, AndroidPacket.spawnAndroid(chr, chr.getAndroid()), true);
        }
        
        if (getHPDec() > 0) {
            chr.startHurtHp();
        }
        
        if (chr.getParty() != null) {
            chr.silentPartyUpdate();
            chr.getClient().getSession().write(MainPacketCreator.updateParty(chr.getClient().getChannel(), chr.getParty(), MaplePartyOperation.SILENT_UPDATE, null));
            chr.updatePartyMemberHP();
            chr.receivePartyMemberHP();
        }

        if (!chr.getSummons().isEmpty()) {
            for (MapleSummon summon : chr.getSummons().values()) {
                if (!summon.isStaticSummon()) {
                    summon.setPosition(chr.getPosition());
                    chr.addVisibleMapObject(summon);
                    spawnSummon(summon, false, SkillFactory.getSkill(summon.getSkill()).getEffect(summon.getSkillLevel()).getDuration());
                }
            }
        }

        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }
        
        if (timeLimit > 0 && getForcedReturnMap() != null) {
            chr.startMapTimeLimitTask(timeLimit, getForcedReturnMap());
        }
        
        if (chr.getBuffedValue(BuffStats.MONSTER_RIDING) != null) {
            if (FieldLimitType.Mount.check(fieldLimit)) {
                chr.cancelBuffStats(-1, BuffStats.MONSTER_RIDING);
            }
        }
        
        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted() && !chr.getEventInstance().isCleared()) {
            chr.getClient().getSession().write(MainPacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }
        
        if (chr.getEventInstance() != null && chr.getEventInstance().isUsingAchievementRatio() && !chr.getEventInstance().isCleared()) {
            chr.getClient().getSession().write(UIPacket.AchievementRatio(chr.getEventInstance().getAchievementRatio()));
        }
        
        if (hasClock()) {
            final Calendar cal = Calendar.getInstance();
            chr.getClient().getSession().write((MainPacketCreator.getClockTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))));
        }
        
        if (chr.getEventInstance() != null) {
            chr.getEventInstance().onMapLoad(chr);
        }
        
        if (GameConstants.isEvan(chr.getJob()) && chr.getJob() >= 2200 && chr.getBuffedValue(BuffStats.MONSTER_RIDING) == null) {
            if (chr.getDragon() == null) {
                chr.makeDragon();
            }
            spawnDragon(chr.getDragon());
            updateMapObjectVisibility(chr, chr.getDragon());
        }
        
        for (BuffStats check : GameConstants.getBroadcastBuffs()) {
            if (chr.getBuffedValue(check) != null) {
                SkillStatEffect eff = chr.getBuffedSkillEffect(check);
                Integer value = chr.getBuffedValue(check);
                if (check == BuffStats.SHADOW_PARTNER) {
                    value = 1;
                } else if (check == BuffStats.WK_CHARGE) {
                    value = 1;
                } else if (check == BuffStats.DAMAGE_RESIST && eff.getSourceId() == 23111005) {
                    value = 1;
                } else if (check == BuffStats.MORPH) {
                    value = eff.getMorph(chr);
                } else if (check == BuffStats.SOARING) {
                    value = 1;
                } else if (check == BuffStats.ITEM_EFFECT) {
                    value = 0;
                } else if (check == BuffStats.WIND_WALK) {
                    value = 0;
                } else if (check == BuffStats.DARK_SPECULATION) {
                    value = 0;
                }
                broadcastMessage(chr, MainPacketCreator.giveForeignBuff(chr.getId(), Collections.singletonList(new Triple<BuffStats, Integer, Boolean>(check, value, false)), eff), false);
            }
        }
        
        if (chr.getBuffedSkillEffect(BuffStats.ITEM_EFFECT) != null) {
            int skillid = chr.getBuffedSkillEffect(BuffStats.ITEM_EFFECT).getSourceId();
            if (GameConstants.isAngelicBlessBuffEffectItem(skillid)) {
                broadcastMessage(MainPacketCreator.showAngelicBlessBuffEffect(chr.getId(), skillid));
            }
        }

        for (MapleDiseaseValueHolder hdvh : chr.getAllDiseases()) {
            for (Pair<DiseaseStats, Integer> p : GameConstants.getBroadcastDebuffs()) {
                if (hdvh.disease == p.getLeft()) {
                    broadcastMessage(MainPacketCreator.giveForeignDebuff(chr.getId(), p.getLeft(), new MobSkill(p.getRight(), 1)));
                }
            }
        }

        if (!isExpiredMapTimer()) {
            long lefttime = maptimer - System.currentTimeMillis();
            int sec = (int) (lefttime / 1000);
            chr.send(MainPacketCreator.getClock(sec));
        }
        
        if (isEliteBossMap()) {
            chr.send(UIPacket.showSpecialMapEffect(2, 1, "Bgm36.img/RoyalGuard", null));
        } else if (isEliteBossRewardMap()) {
            chr.send(UIPacket.showSpecialMapEffect(3, 1, "Bgm36.img/HappyTimeShort", "Map/Map/Map9/924050000.img/back"));
        }
        
        /* 룬 시작 */
            mutex.lock();
            try {
                final List<MapleMapObject> monsters = this.getAllMonster();
                if (!this.isTown() && monsters.size() > 0 && Randomizer.nextInt(380) < 48) {
                    MapleMonster mob = (MapleMonster) monsters.get(Randomizer.rand(0, monsters.size() - 1));
                    MapleRune rune = new MapleRune(Randomizer.rand(0, 7), mob.getPosition().x, mob.getPosition().y, this);
                    this.spawnRune(rune);
                }
            } finally {
                mutex.unlock();
            }
        /* 룬 종료 */

        if (chr.getSkillEffect() != null) {
            if (isTown()) {
                chr.setKeyDownSkill_Time(0);
                broadcastMessage(MainPacketCreator.skillCancel(chr, chr.getSkillEffect().getSkillId()));
                chr.setSkillEffect(null);
            } else {
                broadcastMessage(MainPacketCreator.skillEffect(chr, chr.getSkillEffect(), chr.getPosition()));
            }
        }
    }

    public final void removePlayer(final MapleCharacter chr) {
        lastPlayerLeft = System.currentTimeMillis();
        if (everlast) {
            returnEverLastItem(chr);
        }
        mutex.lock();
        try {
            characters.remove(chr);
        } finally {
            mutex.unlock();
        }
        removeMapObject(chr.getObjectId());
        broadcastMessage(MainPacketCreator.removePlayerFromMap(chr.getId()));
        chr.checkFollow();
        for (final MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster, -1, 0);
        }
        chr.leaveMap();
        chr.cancelMapTimeLimitTask();
        for (MapleSummon summon : chr.getSummons().values()) {
            broadcastMessage(MainPacketCreator.removeSummon(summon, true));
            removeMapObject(summon);
            chr.removeVisibleMapObject(summon);
            if (chr.getSummons().get(summon.getSkill()) != null) {
                chr.getSummons().remove(summon.getSkill());
            }
        }
        if (chr.getExtractor() != null) {
            removeMapObject(chr.getExtractor());
            chr.setExtractor(null);
            chr.message(5, "맵을 이동하여 분해기가 해체되었습니다.");
        }
        if (chr.getDragon() != null) {
            removeMapObject(chr.getDragon());
        }
        if (tempnpcs3.containsKey(chr.getId())) {
            removeTempNpc(tempnpcs3.get(chr.getId()).getId(), chr.getId());
            tempnpcs3.remove(chr.getId());
        }
    }

    public final void broadcastMessage(final Packet packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    public final void broadcastMessage(final MapleCharacter source, final Packet packet, final boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    public final void broadcastMessage(final Packet packet, final Point rangedFrom) {
        broadcastMessage(null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public final void broadcastMessage(final MapleCharacter source, final Packet packet, final Point rangedFrom) {
        broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    private final void broadcastMessage(final MapleCharacter source, final Packet packet, final double rangeSq, final Point rangedFrom) {
        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter chr;
            while (ltr.hasNext()) {
                chr = ltr.next();
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
    }

    private final void sendObjectPlacement(final MapleCharacter c) {
        if (c == null) {
            return;
        }
        for (final MapleMapObject o : getMapObjectsInRange(c.getPosition(), GameConstants.maxViewRangeSq(), GameConstants.rangedMapobjectTypes)) {
            if (o.getType() == MapleMapObjectType.REACTOR) {
                if (!((MapleReactor) o).isAlive()) {
                    continue;
                }
            }
            o.sendSpawnData(c.getClient());
            c.addVisibleMapObject(o);
        }
        for (final MapleMapObject o : getAllMonster()) {
            updateMonsterController((MapleMonster) o, -1, 0);
        }
    }
    
    
    public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq) {
        final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

        mutex.lock();
        try {
            final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
            MapleMapObject obj;
            while (ltr.hasNext()) {
                obj = ltr.next();
                if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                    ret.add(obj);
                }
            }
        } finally {
            mutex.unlock();
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

        mutex.lock();
        try {
            final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
            MapleMapObject obj;
            while (ltr.hasNext()) {
                obj = ltr.next();
                if (MapObject_types.contains(obj.getType())) {
                    if (from.distanceSq(obj.getPosition()) <= rangeSq) {
                        ret.add(obj);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjectsInRect(final Rectangle box, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new LinkedList<MapleMapObject>();

        mutex.lock();
        try {
            final Iterator<MapleMapObject> ltr = mapobjects.values().iterator();
            MapleMapObject obj;
            while (ltr.hasNext()) {
                obj = ltr.next();
                if (MapObject_types.contains(obj.getType())) {
                    if (box.contains(obj.getPosition())) {
                        ret.add(obj);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
        return ret;
    }

    public final List<MapleCharacter> getPlayersInRect(final Rectangle box, final List<MapleCharacter> CharacterList) {
        final List<MapleCharacter> character = new LinkedList<MapleCharacter>();

        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter a;
            while (ltr.hasNext()) {
                a = ltr.next();
                if (CharacterList.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
                }
            }
        } finally {
            mutex.unlock();
        }
        return character;
    }

    public final List<MapleMapObject> getAllItems() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
    }

    public final List<MapleMapObject> getAllNPC() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC, MapleMapObjectType.PLAYERNPC));
    }

    public final List<MapleMapObject> getAllPlayerNPC() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYERNPC));
    }

    public final List<MapleMapObject> getAllReactor() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
    }

    public final List<MapleMapObject> getAllPlayer() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
    }

    public final List<MapleMapObject> getAllMonster() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
    }

    public final List<MapleSummon> getAllSummons() {
        return getSummonInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SUMMON));
    }

    public final List<MapleMapObject> getAllDoor() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.DOOR));
    }

    public final List<MapleMapObject> getAllMechDoor() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.DOOR));
    }

    public List<MapleMapObject> getAllHiredMerchant() {
        return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT));
    }

    public List<MapleMist> getAllMistsThreadsafe() {
        return getMistInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MIST));
    }

    public final List<MapleSummon> getAllSummon() {
        return getSummonInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SUMMON));
    }

    public final List<MapleRune> getAllRune() {
        return getRuneInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.RUNE));
    }

    public final void addPortal(final MaplePortal myPortal) {
        portals.put(myPortal.getId(), myPortal);
    }

    public final MaplePortal getPortal(final String portalname) {
        for (final MaplePortal port : portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public final MaplePortal getPortal(final int portalid) {
        return portals.get(portalid);
    }

    public final List<MaplePortal> getPortalSP() {
        List<MaplePortal> res = new LinkedList<MaplePortal>();
        for (final MaplePortal port : portals.values()) {
            if (port.getName().equals("sp")) {
                res.add(port);
            }
        }
        return res;
    }

    public final void addMapleArea(final Rectangle rec) {
        areas.add(rec);
    }

    public final List<Rectangle> getAreas() {
        return new ArrayList<Rectangle>(areas);
    }

    public final Rectangle getArea(final int index) {
        return areas.get(index);
    }

    public final void setFootholds(final MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public final MapleFootholdTree getFootholds() {
        return footholds;
    }

    public final void loadMonsterRate(final boolean first) {
        final int spawnSize = monsterSpawn.size();
        maxRegularSpawn = Math.round(spawnSize * monsterRate);
        if (maxRegularSpawn < 2) {
            maxRegularSpawn = 2;
        } else if (maxRegularSpawn > spawnSize) {
            maxRegularSpawn = spawnSize - (spawnSize / 15);
        }
        Collection<Spawns> newSpawn = new LinkedList<Spawns>();
        Collection<Spawns> newBossSpawn = new LinkedList<Spawns>();
        for (final Spawns s : monsterSpawn) {
            if (s.getMonster().getStats().isBoss()) {
                newBossSpawn.add(s);
            } else {
                newSpawn.add(s);
            }
        }
        monsterSpawn.clear();
        monsterSpawn.addAll(newBossSpawn);
        monsterSpawn.addAll(newSpawn);
        respawn(true);
        if (first && spawnSize > 0) {
            MapTimer.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    respawn(false);
                }
            }, createMobInterval);
        }
    }

    public final void addMonsterSpawn(final MapleMonster monster, final int mobTime, final String msg) {
        final Point newpos = calcPointMaple(monster.getPosition());
        newpos.y -= 1;

        monsterSpawn.add(new SpawnPoint(monster, newpos, mobTime, msg));
    }

    public final void addAreaMonsterSpawn(final MapleMonster monster, Point pos1, Point pos2, Point pos3, final int mobTime, final String msg) {
        pos1 = calcPointMaple(pos1);
        pos2 = calcPointMaple(pos2);
        pos3 = calcPointMaple(pos3);
        pos1.y -= 1;
        pos2.y -= 1;
        pos3.y -= 1;

        monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg));
    }

    public final Collection<MapleCharacter> getCharacters() {
        final List<MapleCharacter> chars = new ArrayList<MapleCharacter>();

        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            while (ltr.hasNext()) {
                chars.add(ltr.next());
            }
        } finally {
            mutex.unlock();
        }
        return chars;
    }

    public final MapleCharacter getCharacterById_InMap(final int id) {
        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            MapleCharacter c;
            while (ltr.hasNext()) {
                c = ltr.next();
                if (c.getId() == id) {
                    return c;
                }
            }
        } finally {
            mutex.unlock();
        }
        return null;
    }

    private final void updateMapObjectVisibility(final MapleCharacter chr, final MapleMapObject mo) {
        if (chr == null) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) { // monster entered view range
            if (mo.getType() == MapleMapObjectType.MIST || mo.getType() == MapleMapObjectType.EXTRACTOR || mo.getType() == MapleMapObjectType.ANDROID || mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= GameConstants.maxViewRangeSq()) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else { // monster left view range
            if (mo.getType() != MapleMapObjectType.MIST && mo.getType() != MapleMapObjectType.EXTRACTOR && mo.getType() != MapleMapObjectType.ANDROID && mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > GameConstants.maxViewRangeSq()) {
                chr.removeVisibleMapObject(mo);
                mo.sendDestroyData(chr.getClient());
            } else if (mo.getType() == MapleMapObjectType.MONSTER) { //monster didn't leave view range, and is visible
                if (chr.getPosition().distanceSq(mo.getPosition()) <= GameConstants.maxViewRangeSq()) {
                    updateMonsterController((MapleMonster) mo, -1, 0);
                }
            }
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);

        mutex.lock();
        try {
            final Iterator<MapleCharacter> ltr = characters.iterator();
            while (ltr.hasNext()) {
                updateMapObjectVisibility(ltr.next(), monster);
            }
        } finally {
            mutex.unlock();
        }
    }

    public void movePlayer(final MapleCharacter player, final Point newPosition) {
        player.setPosition(newPosition);

        final Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        final MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);

        for (MapleMapObject mo : visibleObjectsNow) {
            if (getMapObject(mo.getObjectId()) == mo) {
                updateMapObjectVisibility(player, mo);
            } else {
                player.removeVisibleMapObject(mo);
            }
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), GameConstants.maxViewRangeSq())) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = null;
        double distance, shortestDistance = Double.POSITIVE_INFINITY;
        for (MaplePortal portal : portals.values()) {
            distance = portal.getPosition().distanceSq(from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public void setMapTimer(long time) {
        try {
            PreparedStatement ps = MYSQL.getConnection().prepareStatement("DELETE FROM bosscooltime WHERE map = ? AND channel = ?");
            ps.setInt(1, mapid);
            ps.setInt(2, channel);
            ps.executeUpdate();
            ps = MYSQL.getConnection().prepareStatement("INSERT INTO bosscooltime VALUES (?, ?, ?)");
            ps.setInt(1, channel);
            ps.setInt(2, mapid);
            ps.setLong(3, time);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            System.err.println("[오류] DB로 보스 쿨타임을 저장하는데 실패했습니다.");
            if (!ServerConstants.realese) {
                e.printStackTrace();
            }
        }
        this.maptimer = time;
    }

    public void setMapTimerNotDB(long time) {
        this.maptimer = time;
    }

    public boolean isExpiredMapTimer() {
        return maptimer < System.currentTimeMillis();
    }

    public String spawnDebug() {
        StringBuilder sb = new StringBuilder("Mapobjects in map : ");
        sb.append(this.getMapObjectSize());
        sb.append(" spawnedMonstersOnMap: ");
        sb.append(spawnedMonstersOnMap);
        sb.append(" spawnpoints: ");
        sb.append(monsterSpawn.size());
        sb.append(" maxRegularSpawn: ");
        sb.append(maxRegularSpawn);
        sb.append(" actual monsters: ");
        sb.append(getAllMonster().size());

        return sb.toString();
    }

    public final int getMapObjectSize() {
        return mapobjects.size();
    }

    public final int getCharactersSize() {
        return characters.size();
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(portals.values());
    }

    public int getSpawnedMonstersOnMap() {
        return spawnedMonstersOnMap.get();
    }

    private class ExpireMapItemJob implements Runnable {

        private MapleWorldMapItem mapitem;

        public ExpireMapItemJob(MapleWorldMapItem mapitem) {
            this.mapitem = mapitem;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                if (droppedItems.contains(Integer.valueOf(mapitem.getObjectId()))) {
                    droppedItems.remove(Integer.valueOf(mapitem.getObjectId()));
                }
                if (mapitem.isPickedUp()) {
                    return;
                }
                mapitem.setPickedUp(true);

                broadcastMessage(MainPacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
                removeMapObject(mapitem);
            }
        }
    }

    private class ActivateItemReactor implements Runnable {

        private MapleWorldMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleWorldMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        @Override
        public void run() {
            if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
                if (mapitem.isPickedUp()) {
                    reactor.setTimerActive(false);
                    return;
                }
                mapitem.setPickedUp(true);
                broadcastMessage(MainPacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
                removeMapObject(mapitem);
                try {
                    reactor.hitReactor(c);
                } catch (Exception e) {
                    if (!ServerConstants.realese) {
                        e.printStackTrace();
                    }
                }
                reactor.setTimerActive(false);

                if (reactor.getDelay() > 0) {
                    MapTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            reactor.setState((byte) 0);
                            broadcastMessage(MainPacketCreator.triggerReactor(reactor, 0, c.getPlayer().getId()));
                        }
                    }, reactor.getDelay());
                }
            }
        }
    }
    
    public int countSummonSkill(MapleCharacter chr, int skill) {
        int count = 0;
        if (GameConstants.isEvan(chr.getJob())) {
            return 0;
        }
        List <MapleMapObject> mapobjs = chr.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SUMMON));
        for (MapleMapObject o : mapobjs) {
            if (o.getType() == MapleMapObjectType.SUMMON) {
                if (((MapleSummon) o).getOwnerChr() == chr) {
                    if (((MapleSummon) o).getSkill() == skill) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    public List <MapleMapObject> getSummonObjects(MapleCharacter chr, int skill) {
        List <MapleMapObject> ret = new ArrayList<MapleMapObject>();
        List <MapleMapObject> mapobjs = chr.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.SUMMON));
        for (MapleMapObject o : mapobjs) {
            if (o.getType() == MapleMapObjectType.SUMMON) {
                if (((MapleSummon) o).getOwnerChr() == chr) {
                    if (((MapleSummon) o).getSkill() == skill) {
                        ret.add(o);
                    }
                }
            }
        }
        return ret;
    }

    public void respawn(final boolean force) {
        if (!isEliteBossMap() && !isEliteBossRewardMap()) {
            if (force) {
                final int numShouldSpawn = monsterSpawn.size() - spawnedMonstersOnMap.get();
                if (numShouldSpawn > 0) {
                    int spawned = 0;
                    for (Spawns spawnPoint : monsterSpawn) {
                        spawnPoint.spawnMonster(this);
                        spawned++;
                        if (spawned >= numShouldSpawn) {
                            break;
                        }
                    }
                }
            } else {
                if (getCharactersSize() <= 0) {
                    return;
                }
                final int numShouldSpawn = maxRegularSpawn - spawnedMonstersOnMap.get();
                if (numShouldSpawn > 0) {
                    int spawned = 0;
                    final List<Spawns> randomSpawn = new ArrayList<Spawns>(monsterSpawn);
                    Collections.shuffle(randomSpawn);
                    for (Spawns spawnPoint : randomSpawn) {
                        if (spawnPoint.shouldSpawn()) {
                            spawnPoint.spawnMonster(this);
                            spawned++;
                        }
                        if (spawned >= numShouldSpawn) {
                            break;
                        }
                    }
                }
            }
        }
    } 

    private static interface DelayedPacketCreation {
        void sendPackets(MapleClient c);
    }

    private static interface SpawnCondition {
        boolean canSpawn(MapleCharacter chr);
    }
    
    public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, boolean isLeft, Collection<MapleCharacter> chr) {
        Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                Point attackedPlayer = a.getPosition();
                MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
                Point nearestPort = Port.getPosition();
                double safeDis = attackedPlayer.distance(nearestPort);
                double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());

                if (isLeft) {
                    if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 1
                            && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight) {
                        character.add(a);
                    }
                } else {
                    if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 1
                            && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }
    
    public void startCatch() {
        if (catchstart == null) {
            broadcastMessage(MainPacketCreator.getClock(180));
            catchstart = MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    broadcastMessage(MainPacketCreator.serverNotice(1, "제한시간 2분이 지나 양이 승리하였습니다!\r\n모든 분들은 게임 보상맵으로 이동됩니다."));
                    for (MapleCharacter chr : getCharacters()) {
                        chr.getStat().setHp(chr.getStat().getMaxHp());
                        chr.updateSingleStat(PlayerStat.HP, chr.getStat().getMaxHp());
                        if (chr.isCatching) {
                            chr.changeMap(chr.getClient().getChannelServer().getMapFactory().getMap(109090201), chr.getClient().getChannelServer().getMapFactory().getMap(109090201).getPortalSP().get(0));
                        } else {
                            chr.changeMap(chr.getClient().getChannelServer().getMapFactory().getMap(109090100), chr.getClient().getChannelServer().getMapFactory().getMap(109090100).getPortalSP().get(0));
                        }
                    }
                    stopCatch();
                }
            }, 180000);
        }
    }

    public void stopCatch() {
        if (catchstart != null) {
            catchstart.cancel(true);
            catchstart = null;
        }
    }
    
    public void RespawnNPC() {
        try {
            Connection con = MYSQL.getConnection();
            String sql = "SELECT * FROM `spawn` WHERE mapid = "+mapid+" AND type = 'n'";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (!CheckNPC(rs.getInt("lifeid"))) {
                    {
                        final MapleNPC npc = MapleLifeProvider.getNPC(rs.getInt("lifeid"));
                        npc.setRx0(rs.getInt("rx0"));
                        npc.setRx1(rs.getInt("rx1"));
                        npc.setCy(rs.getInt("cy"));
                        npc.setF(rs.getInt("dir"));
                        npc.setFh(rs.getInt("fh"));
                        npc.setPosition(new Point (npc.getRx0() + 50, npc.getCy()));
                        if (npc != null) {
                            addMapObject(npc);
                        } else {
                            System.err.println("[오류] 엔피시 데이터를 만드는중 널 포인터 오류가 발생했습니다.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[오류] 엔피시를 DB로부터 불러오는데 오류가 발생했습니다.");
            if (!ServerConstants.realese) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean CheckNPC(int i) {
        for (MapleMapObject ob : getAllNPC()) {
            MapleNPC imsi = (MapleNPC)ob;
            if (imsi.getId() == i) {
                return true;
            }
        }
        return false;
    }
    
    public final MapleCharacter getCharacterById(MapleCharacter player, final int id) {
        for (MapleCharacter chr : player.getMap().getCharacters()) {
            if (chr.getId() == id) {
                return chr;
            }
        }
        return null;
    }
}
