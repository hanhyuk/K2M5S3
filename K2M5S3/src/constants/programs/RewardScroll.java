package constants.programs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ServerConstants;
import tools.Randomizer;

public class RewardScroll {
	private static final Logger logger = LoggerFactory.getLogger(RewardScroll.class);

	private final static RewardScroll instance = new RewardScroll();

	HashMap<Integer, Integer> RewardScroll = new HashMap<Integer, Integer>();

	public static RewardScroll getInstance() {
		return instance;
	}

	public RewardScroll() {
		try {
			FileReader fl = new FileReader(ServerConstants.getRootPath() + ServerConstants.CONFIG_REWARD_SCROLL_PROP_PATH);
			BufferedReader br = new BufferedReader(fl);
			String[] readSplit = new String[2];
			String readLine = null;
			int i = 0;
			while ((readLine = br.readLine()) != null) {
				readSplit = readLine.split(" - ");
				RewardScroll.put(i, Integer.parseInt(readSplit[0]));
				i++;
			}
			fl.close();
			br.close();
		} catch (Exception e) {
			logger.debug("{}", e);
		}
	}

	public int getRewardScroll() {
		return RewardScroll.get(Randomizer.rand(0, RewardScroll.size() - 1));
	}

}
