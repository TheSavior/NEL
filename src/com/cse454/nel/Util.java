package com.cse454.nel;

import java.util.Map;
import java.util.Map.Entry;


public class Util {

	public static String[] tokenizeText(String text) {
		text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
		return text.split(" ");
	}

	public static double computeDotProduct(Map<String, Double> h1, Map<String, Double> h2) {
		double dotProduct = 0;
		for (Entry<String, Double> entry : h1.entrySet()) {
			String key = entry.getKey();
			if (!h2.containsKey(key)) {
				continue;
			}
			dotProduct += entry.getValue() * h2.get(key);
		}
		return dotProduct;
	}

	public static double computeDotProduct(Histogram hist1, Histogram hist2) {
		Map<String, Double> hist1Norm = hist1.getNormalizedMap();
		Map<String, Double> hist2Norm = hist2.getNormalizedMap();
		return computeDotProduct(hist1Norm, hist2Norm);
	}
}
