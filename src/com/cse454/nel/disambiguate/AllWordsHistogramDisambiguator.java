package com.cse454.nel.disambiguate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Histogram;
import com.cse454.nel.Sentence;
import com.cse454.nel.Util;
import com.cse454.nel.WikiConnect;

public class AllWordsHistogramDisambiguator extends AbstractDisambiguator {

	public AllWordsHistogramDisambiguator(WikiConnect wiki, List<Sentence> sentences) {
		super(wiki, sentences);
	}

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions) throws Exception {
		Map<EntityMention, Entity> entityMap = new HashMap<EntityMention, Entity>();

		Set<String> words = new HashSet<String>();
		for (Sentence sentence : sentences) {
			words.addAll(Arrays.asList(sentence.getTokens()));
		}
		// get a histogram of all words in sentences
		Histogram sentenceHist = Histogram.extractFromSentenceArray(sentences, words);

		// for each wiki candidate, generate a histogram of entity mentions in their text
		for (EntityMention mention : mentions) {
			List<Entity> entities = mention.candidates;
			Entity chosenOne = null;
			double max = 0;
			for (Entity entity : entities) {
				String[] tokens = Util.tokenizeText(wiki.GetCleanedWikiText(entity.wikiTitle));
				Histogram hist = Histogram.fromTokens(tokens);
				double dotProduct = Util.computeDotProduct(sentenceHist, hist);
				if (dotProduct > max) {
					max = dotProduct;
					chosenOne = entity;
				}
			}
			entityMap.put(mention, chosenOne);
		}
		return entityMap;
	}

}
