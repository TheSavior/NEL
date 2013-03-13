package com.cse454.nel;

import java.util.ArrayList;
import java.util.List;

import com.cse454.nel.features.FeatureWeights;

public class MultiDocumentProcessor {

	// TODO: TMP location
	/*public static class AbstractScorer {
		void ScoreLine(int docId, FeatureWeights weights, Sentence, String entities);
	}*/
	
	public static void ProcessDocuments(List<Integer> docs, FeatureWeights weights, AbstractScorer scorer) {
		List<FeatureWeights> weightsList = new ArrayList<>(1);
		weightsList.add(weights);
		ProcessDocuments(docs, weightsList, scorer);
	}
	
	public static void ProcessDocuments(List<Integer> docs, List<FeatureWeights> weights, AbstractScorer scorer) {
		// TODO: do stuff
	}
}
