package uk.ac.ucl.hvtp;

import java.io.IOException;
import java.util.logging.*;

public class HyperverseLogger {
	private HyperverseLogger() {
	}

	public static Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);
		logger.setLevel(Level.ALL);

		logger.addHandler(new ConsoleHandler());

		try {
			logger.addHandler(new FileHandler(name + ".log"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Handler handler : logger.getHandlers()) {
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}

		return logger;
	}
}