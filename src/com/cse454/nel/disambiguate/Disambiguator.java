package com.cse454.nel.disambiguate;

import java.util.List;
import java.util.Map.Entry;

import com.cse454.nel.dataobjects.Entity;
import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.Features;

public class Disambiguator {

	public void disambiguate(List<EntityMention> mentions, FeatureWeights featureWeights) {
		for (EntityMention mention : mentions) {
			if (mention.candidateFeatures == null || mention.candidateFeatures.isEmpty()) {
				mention.chosenEntity = null;
			} else {
				double bestScore = -1;
				Entity best = null;
				for (Entry<Entity, Features> entity : mention.candidateFeatures.entrySet()) {
					double score = 0;
					for (Entry<String, Double> feature : entity.getValue().entrySet()) {
						double weight = 0.0;
						if (featureWeights.hasFeature(feature.getKey())) {
							weight = featureWeights.getWeight(feature.getKey());
						}

						score += weight * feature.getValue();
					}

					if (score > bestScore) {
						best = entity.getKey();
						bestScore = score;
					}
				}

				mention.chosenEntity = best;
			}
		}
	}
}
