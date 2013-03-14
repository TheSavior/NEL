package com.cse454.nel.scoring;

import javax.swing.text.AbstractDocument;

import com.cse454.nel.Sentence;
import com.cse454.nel.features.FeatureWeights;

public interface AbstractScorer {
	public void Score(AbstractDocument document, FeatureWeights weights, Sentence sentence, String[] entities);
}
