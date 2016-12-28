package constants.programs;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.items.ItemInformation;
import tools.Triple;

/**
 * TODO 어떤 용도로 사용하는건지 확인필요.
 */
public class CheckItemExist {
	private static final Logger logger = LoggerFactory.getLogger(CheckItemExist.class);
	
	private static HashMap<Integer, Integer> itemList = new HashMap<Integer, Integer>();

	public static void main(String[] args) {
		Console c = System.console();
		RewardIncubator();
		boolean isLast;
		try {
			PrintWriter sql = getCreateFile("incubatordata.sql");
			sql.println("INSERT INTO `incubatordata` (`itemid`, `amount`) VALUES ");
			Iterator<Integer> items = itemList.keySet().iterator();
			while (items.hasNext()) {
				int itemEntry = items.next();
				StringBuilder shops = new StringBuilder();
				isLast = !items.hasNext();
				shops.append("(").append(itemEntry).append(", ").append("1").append(")").append(isLast ? ";" : ",");
				sql.println(shops.toString());

			}
			sql.close();
		} catch (Exception e) {
			logger.debug("{}", e);
		}

		logger.debug("완료되었습니다.");
		System.exit(0);
		return;
	}

	private static PrintWriter getCreateFile(String filename) throws IOException {
		File cf = new File(filename);
		if (!cf.exists())
			cf.createNewFile();
		FileWriter of = new FileWriter(cf.getAbsolutePath());
		PrintWriter sql = new PrintWriter(of);
		return sql;
	}

	private static void checkItem(int itemid, int type) { // 아이템, 상점, 가격
		boolean isExist = false;
		if (type == 1) {
			File source = new File("wz/Character.wz");
			List<Triple<Integer, Integer, Integer>> list = new ArrayList<Triple<Integer, Integer, Integer>>();
			String[] category = { "Accessory", "Cap", "Cape", "Coat", "Glove", "Longcoat", "Pants", "Shield", "Shoes",
					"Weapon" };
			for (String cat : category) {
				File path = new File(source.getAbsolutePath() + "/" + cat + "/" + "0" + itemid + ".img.xml");
				if (path.exists())
					isExist = true;
			}
		} else if (type == 2 || type == 3 || type == 4) {
			try {
				String[] category = { "", "", "Consume", "Install", "Etc", "Cash", "Pet", "Special" };
				String cat = category[type];
				MapleDataProvider sourceData = MapleDataProviderFactory.getDataProvider("Item.wz" + "/" + cat);
				MapleData dd = sourceData.getData("0" + itemid / 10000 + ".img");
				if (dd.getChildByPath("0" + itemid) != null) {
					isExist = true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (!isExist) {
			logger.debug("{}", itemid);
		}
	}

	public static void RewardIncubator() {
		// Gold Box
		try {
			FileReader fl = new FileReader("rewardincubator.celphis");
			BufferedReader br = new BufferedReader(fl);
			String readLine = null;
			final ItemInformation ii = ItemInformation.getInstance();
			int i = 0;
			while ((readLine = br.readLine()) != null) {

				checkItem(Integer.parseInt(readLine), Integer.parseInt(readLine) / 1000000);
				i++;
			}
			fl.close();
			br.close();
		} catch (Exception e) {
			System.out.print(e);
		}
	}
}
