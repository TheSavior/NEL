package com.cse454.nel.disambiguate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.features.Features;

public class Disambiguator {

	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions, Map<String, Double> featureWeights) {
		Map<EntityMention, Entity> ret = new HashMap<EntityMention, Entity>();
		for (EntityMention mention : mentions) {
			if (mention.candidateFeatures == null || mention.candidateFeatures.isEmpty()) {
				ret.put(mention, null);
			} else {
				double bestScore = -1;
				Entity best = null;
				
				for (Entry<Entity, Features> entity : mention.candidateFeatures.entrySet()) {
					double score = 0;
					for (Entry<String, Double> feature : entity.getValue().entrySet()) {
						double weight = 0.0;
						if (featureWeights.containsKey(feature.getKey())) {
							weight = featureWeights.get(feature.getKey());
						}
						
						score += weight * feature.getValue();
					}
					
					if (score > bestScore) {
						best = entity.getKey();
						bestScore = score;
					}
				}
				
				ret.put(mention, best);
			}
		}
		
		return ret;
	}
}
