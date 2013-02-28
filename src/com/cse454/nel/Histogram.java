package com.cse454.nel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Histogram {

	public static Map<String, Integer> extractFromSentenceArray(List<Sentence> sentences) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Sentence sentence : sentences) {
			for (String token : sentence.getTokens()) {
				insertIntoHistogramMap(map, token);
			}
		}
		return map;
	}

	public static Map<String, Integer> extractFromTokenizedString(String text) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Scanner scanner = new Scanner(text);
		while (scanner.hasNext()) {
			String token = scanner.next();
			insertIntoHistogramMap(map, token);
		}
		return map;
	}

	public static void insertIntoHistogramMap(Map<String, Integer> map, String token) {
		if (!map.containsKey(token)) {
			map.put(token, 1);
		} else {
			map.put(token, map.get(token));
		}
	}
}
