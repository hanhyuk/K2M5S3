package a.my.made;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger {

	private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);
	
	public static void main(String[] args) {
		logger.debug("debug ��ȫ��");
		atest.Test.main(args);
		atest.p1.s1.Test.main(args);
	}
}
