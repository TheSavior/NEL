package com.cse454.nel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Histogram {

	private final Map<String, Integer> mMap;

	public Histogram(Map<String, Integer> map) {
		mMap = map;
	}

	public Map<String, Integer> getMap() {
		return mMap;
	}

	/**
	 * Normalizes the counts for use in dot product
	 */
	public Map<String, Double> getNormalizedMap() {
		Map<String, Double> map = new HashMap<String, Double>();
		double total = 0;
		for (Integer value : mMap.values()) {
			total += value;
		}
		for (Entry<String, Integer> entry : mMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue() / total);
		}
		return map;
	}

	public static Histogram extractFromSentenceArray(List<Sentence> sentences) {
		return extractFromSentenceArray(sentences, null);
	}

	public static Histogram extractFromSentenceArray(List<Sentence> sentences, Set<String> mentions) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if (mentions != null) {
			for (String mention : mentions) {
				map.put(mention, 0);
			}
		}
		for (Sentence sentence : sentences) {
			for (String token : sentence.getTokens()) {
				if (mentions != null && !mentions.contains(token)) {
					continue;
				}
				insertIntoHistogramMap(map, token);
			}
		}
		return new Histogram(map);
	}

	public static Histogram extractFromTokenizedString(String text) {
		return extractFromTokenizedString(text, null);
	}

	/**
	 * Extracts only the given mentions from the text to populate the histogram
	 */
	public static Histogram extractFromTokenizedString(String text, Set<String> mentions) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if (mentions != null) {
			for (String mention : mentions) {
				map.put(mention, 0);
			}
		}
		Scanner scanner = new Scanner(text);
		while (scanner.hasNext()) {
			String token = scanner.next();
			if (mentions != null && !mentions.contains(token)) {
				continue;
			}
			insertIntoHistogramMap(map, token);
		}
		return new Histogram(map);
	}

	private static void insertIntoHistogramMap(Map<String, Integer> map, String token) {
		if (!map.containsKey(token)) {
			map.put(token, 1);
		} else {
			map.put(token, map.get(token));
		}
	}
}
