package com.cse454.nel.features;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Histogram;
import com.cse454.nel.Sentence;
import com.cse454.nel.Util;
import com.cse454.nel.WikiConnect;

public class EntityMentionHistogramFeatureGenerator implements FeatureGenerator {

	public static String FEATURE_STRING = "entity_mention_histogram_feature";

	private WikiConnect wiki;
	private List<Sentence> document;
	private List<EntityMention> mentions;

	public EntityMentionHistogramFeatureGenerator(WikiConnect wiki, List<Sentence> sentences, List<EntityMention> mentions) {
		this.wiki = wiki;
		this.document = sentences;
		this.mentions = mentions;
	}

	@Override
	public String GetFeatureName() {
		return FEATURE_STRING;
	}

	@Override
	public void GenerateFeatures(EntityMention mention) throws Exception {
		//Set<String> words = new HashSet<String>();
		Set<String> mentionWords = new HashSet<String>();
		for (EntityMention em : mentions) {
			mentionWords.add(em.mentionString);
		}
		// get a histogram of entity mentions in sentences
		Histogram documentHistogram = Histogram.extractFromSentenceArray(document, mentionWords);

		Map<Entity, Map<String, Double>> candidateFeatures = mention.candidateFeatures;
		for (Entry<Entity, Map<String,Double>> entry : candidateFeatures.entrySet()) {
			Entity candidate = entry.getKey();
			String[] tokens = Util.tokenizeText(wiki.GetCleanedWikiText(candidate.wikiTitle));
			Histogram hist = Histogram.fromTokens(tokens);
			double dotProduct = Util.computeDotProduct(documentHistogram, hist);
			entry.getValue().put(GetFeatureName(), dotProduct);
		}
	}
}
