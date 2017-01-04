package launch;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.AccountStatusType;
import a.my.made.dao.AccountDAO;
import a.my.made.dao.ParamMap;
import constants.ServerConstants;
import constants.programs.ControlUnit;
import constants.programs.HighRanking;
import constants.programs.RewardScroll;
import constants.subclasses.QuickMove;
import constants.subclasses.setScriptableNPC;
import database.MYSQL;
import launch.helpers.MapleCacheData;
import launch.helpers.MapleLoginHelper;
import launch.world.WorldAuction;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import server.items.CashItemFactory;
import server.life.MapleMonsterProvider;
import tools.Timer;

public final class Start {
	private static final Logger logger = LoggerFactory.getLogger(Start.class);
	
	public static void main(String args[]) throws IOException {
		logger.info("[알림] 에뮬레이터 :: V1.2." + ServerConstants.MAPLE_VERSION + " 버전이 실행되었습니다.\n");

		ServerConstants.init();
		
		SendPacketOpcode.loadOpcode();
		RecvPacketOpcode.loadOpcode();
		
		MapleLoginHelper.getInstance().loadForbiddenNames();

		Timer.startAllTimer();

		
		LoginServer.getInstance().start();
		ChannelServer.start(ServerConstants.openChannelCount);
		CashShopServer.getInstance().start();
		BuddyChatServer.getInstance().start();

		

		/* 메모리 정리 및 캐싱쓰레드 시작 */
		CashItemFactory.getInstance();
		MapleCacheData mc = new MapleCacheData();
		mc.startCacheData();
		HighRanking.getInstance().startTasking();
		WorldAuction.load();
		QuickMove.doMain();
		setScriptableNPC.doMain();
		RewardScroll.getInstance();
		MapleMonsterProvider.getInstance().retrieveGlobal();

		clearDb();
		ControlUnit.main(args);
		System.gc();

		logger.info("[알림] 서버 오픈이 정상적으로 완료 되습니다.");
	}
	
	private static void clearDb() {

        int deletedrows = 0;
        List <Integer> items = new ArrayList<Integer>();
        
        try {
    		//서버를 구동 할때 계정 상태를 모두 로그인 하지 않은 상태로 업데이트 한다.
    		final ParamMap params = new ParamMap();
    		params.put("loggedin", AccountStatusType.NOT_LOGIN.getValue());
    		AccountDAO.setAccountInfo(AccountDAO.DEFAULT_ACCOUNT_ID, params);
        	
        	
        	
            Connection con = MYSQL.getConnection();
                        
            PreparedStatement ps = con.prepareStatement("SELECT * FROM android");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int uniqueid = rs.getInt("uniqueid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `inventoryitems` WHERE uniqueid = ?");
                check.setInt(1, uniqueid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM android WHERE uniqueid = ?");
                    del.setInt(1, uniqueid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM extendedslots");
            rs = ps.executeQuery();
            while (rs.next()) {
                int uniqueid = rs.getInt("uniqueid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `inventoryitems` WHERE uniqueid = ?");
                check.setInt(1, uniqueid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM extendedslots WHERE uniqueid = ?");
                    del.setInt(1, uniqueid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("SELECT * FROM extendedslots");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `inventoryitems` WHERE characterid = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM extendedslots WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM hiredmerch");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM hiredmerch WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("SELECT * FROM hiredmerchantsaveitems");
            rs = ps.executeQuery();
            while (rs.next()) {
                int mid = rs.getInt("merchid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `hiredmerchantsaves` WHERE id = ?");
                check.setInt(1, mid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM hiredmerchantsaveitems WHERE merchid = ?");
                    del.setInt(1, mid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `inner_ability_skills`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("player_id");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `inner_ability_skills` WHERE player_id = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `inventoryitems` WHERE type = 1");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `inventoryitems` WHERE characterid = ? AND type = 1");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `inventoryslot`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `inventoryslot` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `keymap`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `keymap` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `keyvalue`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("cid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `keyvalue` WHERE cid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("SELECT * FROM `keyvalue2`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("cid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `keyvalue2` WHERE cid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
            
            
            deletedrows+=con.prepareStatement("DELETE FROM `keyvalue` WHERE `key` = '1stJobTrialCompleteTime'").executeUpdate();
            deletedrows+=con.prepareStatement("DELETE FROM `keyvalue` WHERE `key` = '2ndJobTrialCompleteTime'").executeUpdate();
            deletedrows+=con.prepareStatement("DELETE FROM `keyvalue` WHERE `key` = '3rdJobTrialCompleteTime'").executeUpdate();
            deletedrows+=con.prepareStatement("DELETE FROM `keyvalue` WHERE `key` = '4thJobTrialCompleteTime'").executeUpdate();
                        
            ps = con.prepareStatement("SELECT * FROM pets");
            rs = ps.executeQuery();
            while (rs.next()) {
                int uniqueid = rs.getInt("uniqueid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `inventoryitems` WHERE uniqueid = ?");
                check.setInt(1, uniqueid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM pets WHERE uniqueid = ?");
                    del.setInt(1, uniqueid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `questinfo`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `questinfo` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
            
            ps = con.prepareStatement("SELECT * FROM `queststatus`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `queststatus` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `quickslot`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("cid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `quickslot` WHERE cid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `rewardsaves`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("cid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `rewardsaves` WHERE cid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `skillmacros`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `skillmacros` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `skills`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `skills` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
            }
                        
            ps = con.prepareStatement("SELECT * FROM `skills_cooldowns`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("charid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `skills_cooldowns` WHERE charid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
                        
            ps = con.prepareStatement("SELECT * FROM `trocklocations`");
            rs = ps.executeQuery();
            while (rs.next()) {
                int cid = rs.getInt("characterid");
                PreparedStatement check = con.prepareStatement("SELECT * FROM `characters` WHERE id = ?");
                check.setInt(1, cid);
                ResultSet checkrs = check.executeQuery();
                if (!checkrs.next()) {
                    PreparedStatement del = con.prepareStatement("DELETE FROM `trocklocations` WHERE characterid = ?");
                    del.setInt(1, cid);
                    del.executeUpdate();
                    deletedrows++;
                    del.close();
                }
                check.close();
                checkrs.close();
            }
            ps.close();
            rs.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        logger.info("[알림] 데이터베이스 정리 프로그램에서 {}개의 행을 제거하였습니다.", deletedrows);
    
	}
}