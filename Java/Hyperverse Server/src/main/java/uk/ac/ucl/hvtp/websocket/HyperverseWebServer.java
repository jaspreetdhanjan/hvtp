package uk.ac.ucl.hvtp.websocket;

import spark.ModelAndView;
import spark.Spark;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * The Hyperverse web server.
 * <p>
 * Runs on https://localhost:4567
 *
 * @author Jaspreet Dhanjan, University College London
 */

public class HyperverseWebServer {
	private static boolean running = false;

	private HyperverseWebServer() {
	}

	public static synchronized void start() {
		if (running) {
			return;
		}
		running = true;

		init();
	}

	private static void init() {
		Spark.webSocket("/websocket/hyperverse", HyperverseWebSocket.class);

//		// A demo page at /hyperverse to check if we're running
//		Spark.get("/hyperverse", (request, response) ->
//		{
//			Map<String, Object> model = new HashMap<>();
//
//			return new ThymeleafTemplateEngine().render(new ModelAndView(model, "echoview"));
//		});

		Spark.init();
	}

	public static synchronized void stop() {
		if (!running) {
			return;
		}
		running = false;

		stop();
	}
}