package scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.MapleClient;
import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class NPCAutoWriterHelper {
	private static final Logger logger = LoggerFactory.getLogger(NPCAutoWriterHelper.class);
	
	private int npcID;
	private MapleClient ha;
	private FileOutputStream out = null;

	public NPCAutoWriterHelper(int id, MapleClient ha) {
		this.npcID = id;
		this.ha = ha;
	}

	public final boolean checkFileExist() {
		try {
			if (new File("Scripts/npc/" + npcID + ".js").exists()) {
				return true;
			}
		} catch (Exception e) {
			logger.debug("{}", e);
		}
		return false;
	}

	public static final String getNPCName(int id) {
		return MapleDataTool.getString(id + "/name",
				MapleDataProviderFactory.getDataProvider("String.wz").getData("Npc.img"), "MISSINGNO");
	}

	public static final String getNPCFunc(int id) {
		return MapleDataTool.getString(id + "/func",
				MapleDataProviderFactory.getDataProvider("String.wz").getData("Npc.img"), "MISSINGNO");
	}

	public final String getNpcName() {
		return MapleDataTool.getString(npcID + "/name",
				MapleDataProviderFactory.getDataProvider("String.wz").getData("Npc.img"), "MISSINGNO");
	}

	public final String getNpcFunc() {
		return MapleDataTool.getString(npcID + "/func",
				MapleDataProviderFactory.getDataProvider("String.wz").getData("Npc.img"), "MISSINGNO");
	}

	public final String addInfo(int id) {
		String a = "#d";
		a += "���ǽ� �̸� : " + getNPCName(id);
		a += "\r\n#r";
		if (!"MISSINGNO".equals(getNPCFunc(id))) {
			a += "���ǽ� ���� : " + getNPCFunc(id) + "\r\n";
		}
		a += "\r\n#k";
		for (MapleData d : MapleDataProviderFactory.getDataProvider("String.wz").getData("Npc.img")
				.getChildByPath(id + "").getChildren()) {
			if (!d.getName().equals("name") && !d.getName().equals("func"))
				a += d.getName() + " : " + (String) d.getData() + "\r\n";
		}
		return a;
	}

	public final void doMain() {
		try {
			if (checkFileExist()) { // �̹� ��ũ��Ʈ�� �����ϴ� ���
				return;
			}
			out = new FileOutputStream("Scripts/npc/" + npcID + ".js");
		} catch (FileNotFoundException e) {
			dropMessage("������ �ۼ��ϴµ� �����߽��ϴ�. �������α׷��� ���� ���� ������ �ִ��� Ȯ���� �ּ���.");
			logger.debug("{}", e);
		} catch (NullPointerException e) {
			dropMessage("������ �ۼ��ϴµ� �����߽��ϴ�. �� ������ ������ �߻��߽��ϴ�.");
			logger.debug("{}", e);
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

	public final void dropMessage(String text) {
		ha.getPlayer().dropMessage(1, text);
	}

	public final void writeLine(String text) {
		if (out != null) {
			try {
				out.write(text.getBytes(Charset.forName("euc-kr")));
			} catch (Exception e) {
				logger.debug("{}", e);
			}
		}
	}

	public final void newLine() {
		if (out != null) {
			try {
				out.write(System.getProperty("line.separator").getBytes());
			} catch (Exception e) {
				logger.debug("{}", e);
			}
		}
	}

	public final void closeFile() {
		try {
			out.close();
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

}
