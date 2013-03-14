package com.cse454.nel.scoring;

import com.cse454.nel.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.features.FeatureWeights;

public interface AbstractScorer {
	public void Score(AbstractDocument doc, FeatureWeights weights, Sentence sentence, String[] entities);
}
