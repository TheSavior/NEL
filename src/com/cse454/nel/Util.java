package com.cse454.nel;

import java.util.Map;
import java.util.Map.Entry;


public class Util {

	public static String[] tokenizeText(String text) {
		text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
		return text.split(" ");
	}

	public static double computeDotProduct(Histogram hist1, Histogram hist2) {
		Map<String, Double> hist1Norm = hist1.getNormalizedMap();
		Map<String, Double> hist2Norm = hist2.getNormalizedMap();
		double dotProduct = 0;
		for (Entry<String, Double> entry : hist1Norm.entrySet()) {
			String key = entry.getKey();
			if (!hist2Norm.containsKey(key)) {
				continue;
			}
			dotProduct += entry.getValue() * hist2Norm.get(key);
		}
		return dotProduct;
	}
}
