package com.cse454.nel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.features.FeatureWeights;

public class FeatureWeightScorer {

	private Map<FeatureWeights, Double> scores = new HashMap<>();
	
	public Map<FeatureWeights, Double> getScores() {
		return scores;
	}

	public void addFeatureWeightScore(FeatureWeights weights, double score) {
		if (scores.containsValue(weights)) {
			scores.put(weights, scores.get(weights) + score);
		} else {
			scores.put(weights, score);
		}
	}
	
	public void addDocumentScores(
		Map<Sentence, Map<FeatureWeights, String[]>> results) {

		for (Entry<Sentence, Map<FeatureWeights, String[]>> entry : results.entrySet()) {
			Sentence sentence = entry.getKey();
			Map<FeatureWeights, String[]> entityTrials = entry.getValue();

			for (Entry<FeatureWeights, String[]> entities : entityTrials.entrySet()) {
				double score = FeatureWeightScorer.score(sentence.getGold(), entities.getValue());
				if (score == Double.NaN) {
					System.out.println("hdsf");
				}
				addFeatureWeightScore(entities.getKey(), score);
			}
			
		}
	}

	public static double score(String[] gold, String[] entities) {
		List<String> goldEnts = new ArrayList<>();
		List<String> ents = new ArrayList<>();
		for (String g : gold) {
			if (!g.equals("0")) {
				goldEnts.add(g);
			}
		}
		for (String ent : entities) {
			if (!ent.equals("0") && goldEnts.contains(ent)) {
				ents.add(ent);
			}
		}
		double numerator = ents.size(); // the entities we get right
		double denominator = goldEnts.size(); // the total amount of entities
		return denominator == 0 ? 0 : numerator / denominator;
	}
}
