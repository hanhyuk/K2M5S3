/*
 * ArcStory Project
 * 최주원 sch2307@naver.com
 * 이준 junny_adm@naver.com
 * 우지훈 raccoonfox69@gmail.com
 * 강정규 ku3135@nate.com
 * 김진홍 designer@inerve.kr
 */

package constants.programs;

import client.MapleCharacter;
import constants.ServerConstants;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import launch.ChannelServer;
import launch.Start;
import launch.world.WorldBroadcasting;
import packet.creators.MainPacketCreator;
import tools.Timer.WorldTimer;

/**
 *
 * @author 김승현
 */
public class AutoReboot {
    private static String path = "";
    private static int time = 0;
    
    public static void main(final String args[]) {
            WorldTimer tMan = WorldTimer.getInstance();
            Runnable r = new Runnable() {
                public void run() {
                            if (time == 0) {
                                System.out.println("[알림] 현 시간부로 4시간 주기로 서버를 자동으로 리부팅합니다.");
                                time++;
                            } else if (time == -1) {
                                time = 0;
                            } else {
                            WorldBroadcasting.broadcastMessage(MainPacketCreator.serverNotice(1, "안정적인 서버운영을 위해, 약 1분간 서버리부팅이 진행됩니다.\r\n불편을 끼쳐드려 죄송합니다."));
                            try {
                                Thread.sleep(10000L);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }  
                            for (ChannelServer cserv : ChannelServer.getAllInstances()) { //서버 종료전, 데이터베이스 저장.
                                cserv.saveAllMerchant();
                                for (MapleCharacter hp : cserv.getPlayerStorage().getAllCharacters().values()) {
                                    if (hp != null)
                                        hp.saveToDB(false, false);
                                    }
                            }
                            MedalRanking.getInstance().save();
                            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                                cserv.getPlayerStorage().disconnectAll();
                            }
                            runFile();
                            System.exit(1);
                        }
                  }
            };
        tMan.register(r, 14400000);
    }
    
    public static void runFile() {
        Runtime rt = Runtime.getRuntime(); 
        try {
            rt.exec(ServerConstants.path + "\\AutoReboot.exe");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
