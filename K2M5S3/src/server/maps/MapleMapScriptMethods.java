package server.maps;

import java.awt.Point;

import client.MapleClient;
import packet.creators.MainPacketCreator;
import packet.creators.UIPacket;
import server.life.MapleLifeProvider;
import server.life.MapleMonster;

public class MapleMapScriptMethods {

    private static final Point witchTowerPos = new Point(-60, 184);

    private static enum onFirstUserEnter {

        PinkBeen_before, onRewordMap,mpark_mobRegen, mPark_summonBoss, banban_Summon, NULL;

        private static onFirstUserEnter fromString(String Str) {
            try {
                return valueOf(Str);
            } catch (IllegalArgumentException ex) {
                return NULL;
            }
        }
    };

    private static enum onUserEnter {
        
        startEreb,
        mPark_stageEff,
        starforce_enter,
        NULL;

        private static onUserEnter fromString(String Str) {
            try {
                return valueOf(Str);
            } catch (IllegalArgumentException ex) {
                return NULL;
            }
        }
    };

    public static void startScript_FirstUser(MapleClient c, String scriptName) {
        switch (onFirstUserEnter.fromString(scriptName)) {
            case PinkBeen_before: {
                handlePinkBeanStart(c);
                break;
            }
            case onRewordMap: {
                reloadWitchTower(c);
                break;
            }
            case mPark_summonBoss:
            case mpark_mobRegen: {
                for (MapleMapObject mob : c.getPlayer().getMap().getAllMonster()) {
                    c.getPlayer().getEventInstance().registerMonster((MapleMonster) mob);
                }
                break;
            }
            case banban_Summon: {
                c.getPlayer().getMap().startMapEffect("차원의 틈에서 반반을 소환하자.", 5120025, 5000);
                break;
            }
            default: {
                //System.out.println("Unhandled script : " + scriptName + ", type : onFirstUserEnter - MAPID " + c.getPlayer().getMapId());
                break;
            }
        }
    }

    public static void startScript_User(final MapleClient c, String scriptName) {
        String data = "";
        switch (onUserEnter.fromString(scriptName)) {
            case mPark_stageEff: {
                c.getPlayer().send(UIPacket.showInfo("몬스터를 모두 잡아야 다음 스테이지로 이동할 수 있습니다."));
                switch ((c.getPlayer().getMapId() % 1000) / 100) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        c.getSession().write(MainPacketCreator.showEffect("monsterPark/stageEff/stage"));
                        c.getSession().write(MainPacketCreator.showEffect("monsterPark/stageEff/number/" + (((c.getPlayer().getMapId() % 1000) / 100) + 1)));
                        break;
                    case 5:
                        c.getSession().write(MainPacketCreator.showEffect("monsterPark/stageEff/final"));
                        break;
                }
                break;
            }
            case starforce_enter: {
                c.getPlayer().getMap().startMapEffect(c.getPlayer().getMap().getBarrier() + " 이상의 스타포스가 필요한 지역입니다.", 5120024);
                break;
            }
            default: {
                //System.out.println("Unhandled script : " + scriptName + ", type : onUserEnter - MAPID " + c.getPlayer().getMapId());
                break;
            }
        }
    }

    private static final int getTiming(int ids) {
        if (ids <= 5) {
            return 5;
        } else if (ids >= 7 && ids <= 11) {
            return 6;
        } else if (ids >= 13 && ids <= 17) {
            return 7;
        } else if (ids >= 19 && ids <= 23) {
            return 8;
        } else if (ids >= 25 && ids <= 29) {
            return 9;
        } else if (ids >= 31 && ids <= 35) {
            return 10;
        } else if (ids >= 37 && ids <= 38) {
            return 15;
        }
        return 0;
    }

    private static void showIntro(final MapleClient c, final String data) {
        c.getSession().write(UIPacket.IntroDisableUI(true));
        c.getSession().write(UIPacket.showWZEffect(data, 0));
    }

    private static void handlePinkBeanStart(MapleClient c) {
        final MapleMap map = c.getPlayer().getMap();
        map.killAllMonsters(true);
        map.respawn(true);

        if (map.containsNPC(2141000) == -1) {
            map.spawnNpc(2141000, new Point(-190, -42));
        }
    }

    private static void reloadWitchTower(MapleClient c) {
        final MapleMap map = c.getPlayer().getMap();
        map.killAllMonsters(false);

        final int level = c.getPlayer().getLevel();
        int mob;
        if (level <= 10) {
            mob = 9300367;
        } else if (level <= 20) {
            mob = 9300368;
        } else if (level <= 30) {
            mob = 9300369;
        } else if (level <= 40) {
            mob = 9300370;
        } else if (level <= 50) {
            mob = 9300371;
        } else if (level <= 60) {
            mob = 9300372;
        } else if (level <= 70) {
            mob = 9300373;
        } else if (level <= 80) {
            mob = 9300374;
        } else if (level <= 90) {
            mob = 9300375;
        } else if (level <= 100) {
            mob = 9300376;
        } else {
            mob = 9300377;
        }
        map.spawnMonsterOnGroundBelow(MapleLifeProvider.getMonster(mob), witchTowerPos);
    }
}
