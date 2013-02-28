package com.cse454.nel.disambiguate;

import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Histogram;
import com.cse454.nel.Sentence;
import com.cse454.nel.WikiConnect;

public class HistogramDisambiguator extends AbstractDisambiguator {

	public HistogramDisambiguator(WikiConnect wiki, List<Sentence> sentences) {
		super(wiki, sentences);
	}

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions) {
		// get a histogram of text in sentences
		Map<String, Integer> sentenceHist = Histogram.extractFromSentenceArray(sentences);

		// TODO Auto-generated method stub
		return null;
	}

}
