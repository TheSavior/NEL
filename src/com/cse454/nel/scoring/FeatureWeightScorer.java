package com.cse454.nel.scoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.features.FeatureWeights;

public class FeatureWeightScorer implements AbstractScorer {

	private final Map<FeatureWeights, Double> scores = new HashMap<>();

	public Map<FeatureWeights, Double> getScores() {
		return scores;
	}

	public void addFeatureWeightScore(FeatureWeights weights, double score) {
		if (scores.containsKey(weights)) {
			scores.put(weights, scores.get(weights) + score);
		} else {
			scores.put(weights, score);
		}
	}

	@Override
	public void Score(AbstractDocument doc, FeatureWeights weights, Sentence sentence, String[] entities) {
		List<String> goldEnts = new ArrayList<>();
		List<String> ents = new ArrayList<>();
		for (String g : sentence.getGold()) {
			if (!g.equals("0")) {
				goldEnts.add(g);
			}
		}
		if (goldEnts.size() == 0) { // no gold just return
			return;
		}
		for (String ent : entities) {
			if (!ent.equals("0") && goldEnts.contains(ent)) {
				ents.add(ent);
			}
		}
		double numerator = ents.size(); // the entities we get right
		double denominator = goldEnts.size(); // the total amount of entities
		addFeatureWeightScore(weights, numerator / denominator);
	}
}
