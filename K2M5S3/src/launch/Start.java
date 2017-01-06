package launch;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import a.my.made.dao.CommonDAO;
import client.skills.SkillFactory;
import constants.ServerConstants;
import constants.programs.ControlUnit;
import constants.programs.RewardScroll;
import constants.subclasses.QuickMove;
import handler.login.CharLoginHandler;
import launch.world.WorldAuction;
import packet.opcode.RecvPacketOpcode;
import packet.opcode.SendPacketOpcode;
import server.items.CashItemFactory;
import server.items.ItemInformation;
import server.life.MapleMonsterProvider;
import tools.Timer;

public final class Start {
	private static final Logger logger = LoggerFactory.getLogger(Start.class);
	
	public static void main(String args[]) throws IOException {
		logger.info("[�˸�] ���ķ����� :: V1.2." + ServerConstants.MAPLE_VERSION + " ������ ����Ǿ����ϴ�.\n");

		//���� ������ �ʿ��� ����� �ε�
		ServerConstants.init();
		
		//���ʿ��� DB ���� ����
		CommonDAO.deleteUnnecessaryDbInfoAtStartUp();
		//��� ����� �α׾ƿ�
		CommonDAO.updateAllUserLogout();
		
		//��Ŷ ���� �ε�
		SendPacketOpcode.loadOpcode();
		RecvPacketOpcode.loadOpcode();
		
		//ĳ���� ���� �ε�
		CashItemFactory.getInstance().cashingCashItemInfo();
		//���� ������ ������ �ε�.
		RewardScroll.getInstance().cashingRewardScrollInfo();
		
		//TODO ����� ���� �ε�. �м� �ʿ�.
		WorldAuction.getInstance().cachingAuctionInfo();
		
		//����Ҽ� ���� ĳ���� �ε�
		CharLoginHandler.cachingForbiddenNames();
		//TODO ���� �̵� ���� �ε�. ��� ���Ǵ��� Ȯ�� �ʿ�.
		QuickMove.cachingQuickMoveInfo();
		//�۷ι� ��� ���� �ε�
		MapleMonsterProvider.getInstance().cachingGlobalDropInfo();
		
		//�����췯 ���
		Timer.startAllTimer();
		
		//�α���, ä��, ĳ�ü�, ����ê ���� Ȱ��ȭ
		LoginServer.getInstance().start();
		ChannelServer.start(ServerConstants.openChannelCount);
		CashShopServer.getInstance().start();
		BuddyChatServer.getInstance().start();

		//hh
		//MapleCacheData mc = new MapleCacheData(); mc.startCacheData();
		
		SkillFactory.cachingSkillInfo();
		ItemInformation.getInstance().cachingItemInfo();
		
		
		
		
		
		ControlUnit.main(args);
		
		System.gc();

		logger.info("[�˸�] ���� ������ ���������� �Ϸ� �ǽ��ϴ�.");
	}
}