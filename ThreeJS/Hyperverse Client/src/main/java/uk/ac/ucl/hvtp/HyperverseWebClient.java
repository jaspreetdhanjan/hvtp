package uk.ac.ucl.hvtp;

import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * The Hyperverse web client.
 * <p>
 * Runs on https://localhost:8080
 *
 * @author Jaspreet Dhanjan, University College London
 */

public class HyperverseWebClient {
	public static void main(String[] args) {
		port(8080);

		staticFileLocation("/public");

		get("/", (request, response) ->
		{
			Map<String, Object> model = new HashMap<>();

			return new ThymeleafTemplateEngine().render(new ModelAndView(model, "index"));
		});

		init();
	}
}
