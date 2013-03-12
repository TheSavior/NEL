package com.cse454.nel.disambiguate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Histogram;
import com.cse454.nel.NERClassifier;
import com.cse454.nel.Sentence;
import com.cse454.nel.Util;
import com.cse454.nel.WikiConnect;

public class EntityMentionHistogramDisambiguator extends AbstractDisambiguator {
	public EntityMentionHistogramDisambiguator(WikiConnect wiki, List<Sentence> sentences) {
		super(wiki);
	}

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions, List<Sentence> sentences) throws Exception {
		Map<EntityMention, Entity> entityMap = new HashMap<EntityMention, Entity>();

		Set<String> mentionWords = new HashSet<String>();
		for (EntityMention mention : mentions) {
			mentionWords.add(mention.mentionString);
		}
		// get a histogram of entity mentions in sentences
		Histogram sentenceHist = Histogram.extractFromSentenceArray(sentences, mentionWords);

		// for each wiki candidate, generate a histogram of entity mentions in their text
		for (EntityMention mention : mentions) {
			List<Entity> entities = mention.candidates;
			Entity chosenOne = null;
			double max = 0;
			for (Entity entity : entities) {
				String[] tokens = Util.tokenizeText(wiki.GetCleanedWikiText(entity.wikiTitle));
				Histogram hist = Histogram.extractFromTokenizedString(tokens, mentionWords);
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
