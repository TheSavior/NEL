package com.cse454.nel.scoring;

import java.util.List;

import com.cse454.nel.EntityMention;
import com.cse454.nel.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.features.FeatureWeights;

public interface AbstractScorer {
	public void ScoreMentions(AbstractDocument doc, List<EntityMention> mentions);
	public void Score(AbstractDocument doc, FeatureWeights weights, Sentence sentence, String[] entities);
}
