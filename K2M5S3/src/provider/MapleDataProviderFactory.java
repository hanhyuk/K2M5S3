package provider;

import java.io.File;

import constants.ServerConstants;
import provider.WzXML.XMLWZFile;

public class MapleDataProviderFactory {

	private static final String MIDDLE_DIR_NAME = "wz/";
	
	public static MapleDataProvider getDataProvider(String path) {
		return getDataProvider(fileInWZPath(path));
	}
	
	public static File fileInWZPath(String filename) {
		return new File(ServerConstants.getRootPath(), MIDDLE_DIR_NAME + filename);
	}
	
	private static MapleDataProvider getDataProvider(Object in) {
		return getWZ(in, false);
	}
	
	private static MapleDataProvider getWZ(Object in, boolean provideImages) {
		if (in instanceof File) {
			File fileIn = (File) in;
			return new XMLWZFile(fileIn);
		}
		throw new IllegalArgumentException("Can't create data provider for input " + in);
	}
}