/*
 * ArcStory Project
 * ���ֿ� sch2307@naver.com
 * ���� junny_adm@naver.com
 * ������ raccoonfox69@gmail.com
 * ������ ku3135@nate.com
 * ����ȫ designer@inerve.kr
 */

package launch;

import constants.ServerConstants;
import constants.programs.DatabaseBackup;
import constants.programs.DatabaseGarbageCollector;
import constants.programs.HighRanking;
import constants.programs.RewardScroll;
import constants.programs.AutoReboot;
import constants.programs.ControlUnit;
import constants.subclasses.QuickMove;
import constants.subclasses.setScriptableNPC;
import database.MYSQL;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import launch.helpers.MapleCacheData;
import launch.world.WorldAuction;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import server.items.CashItemFactory;
import server.life.MapleMonsterProvider;

public final class Start {
   
public static long START = System.currentTimeMillis();
            
    public static void main(String args[]) throws IOException {     
        
            System.out.println("[�˸�] ��ũ���丮 ���ķ����� :: V1.2." + ServerConstants.MAPLE_VERSION + " ������ ����Ǿ����ϴ�.\n");
            
            /* Ÿ�̸� ���� */
            tools.Timer.WorldTimer.getInstance().start();
            tools.Timer.EtcTimer.getInstance().start();
            tools.Timer.MapTimer.getInstance().start();
            tools.Timer.CloneTimer.getInstance().start();
            tools.Timer.EventTimer.getInstance().start();
            tools.Timer.BuffTimer.getInstance().start();
            tools.Timer.PingTimer.getInstance().start();
            tools.Timer.ShowTimer.getInstance().start();
            
            /* �����ͺ��̽� ���� */ 
            try {
                Connection con = MYSQL.getConnection();
                PreparedStatement del = con.prepareStatement("DELETE FROM acceptip");
                del.executeUpdate();
                del.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            /* ���� ���� �� ���� ���� */
	    LoginServer.getInstance().run_startup_configurations();
	    ChannelServer.startServer();
	    CashShopServer.getInstance().run_startup_configurations();
            BuddyChatServer.getInstance().run_startup_configurations();
            
            /* ���ڵ� ���� */
            SendPacketOpcode.loadOpcode();
            RecvPacketOpcode.loadOpcode();
            
            /* �޸� ���� �� ĳ�̾����� ���� */
            CashItemFactory.getInstance();
            Start.clean();
            MapleCacheData mc = new MapleCacheData();
            mc.startCacheData();
            HighRanking.getInstance().startTasking();
            WorldAuction.load();
            QuickMove.doMain();
            setScriptableNPC.doMain();
            RewardScroll.getInstance();
            MapleMonsterProvider.getInstance().retrieveGlobal();
            
            /* ���� ������ ���� */
            DatabaseGarbageCollector.main(args);
            DatabaseBackup.getInstance().startTasking();
            AutoReboot.main(args);
            ControlUnit.main(args);
            System.gc();
        
            /* ���� ���� �Ϸ� �޼��� */  
            long END = System.currentTimeMillis();
            System.out.println("[�˸�] ���� ������ ���������� �Ϸ� �Ǿ�����, �ҿ�� �ð��� : " + (END - START) / 1000.0 + "�� �Դϴ�."); 
    }
        
    public static void clean() {
        try {
            int nu = 0;
            PreparedStatement ps;
            Calendar ocal = Calendar.getInstance();
            ps = MYSQL.getConnection().prepareStatement("SELECT * FROM acheck WHERE day = 1");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key = rs.getString("keya");
                String day = ocal.get(ocal.YEAR) + "" + (ocal.get(ocal.MONTH) + 1) + "" + ocal.get(ocal.DAY_OF_MONTH);
                String da[]= key.split("_");
                if (!da[0].equals(day)) {
                    ps = MYSQL.getConnection().prepareStatement("DELETE FROM acheck WHERE keya = ?");
                    ps.setString(1, key);
                    ps.executeUpdate();
                    nu++;
                }
            }
            System.out.println("[�˸�] " + nu + "���� 1�� ���� ����� �������ϴ�.");
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }       
    }
}