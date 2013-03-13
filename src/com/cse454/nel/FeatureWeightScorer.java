package com.cse454.nel;

import java.util.Map;

import com.cse454.nel.features.FeatureWeights;

public class FeatureWeightScorer {

	private Map<FeatureWeights, Double> scores;

	public void addFeatureWeightScore(FeatureWeights weights, double score) {
		scores.put(weights, score);
	}

	public double score(String[] gold, String[] entities) {
		if (gold.length != entities.length) {
			throw new IllegalArgumentException("Gold data and entity data not same length");
		}
		double numerator = 0; // the entities we get right
		double denominator = 0; // the total amount of entities
		for (int i = 0; i < gold.length; i++) {
			if (!gold[i].equals("0")) {
				denominator++;
				if (gold[i].equals(entities[i])) {
					numerator++;
				}
			}
		}
		return numerator / denominator;
	}
}
