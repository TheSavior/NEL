package com.cse454.nel.features;

import java.util.Arrays;
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

public class AllWordsHistogramFeatureGenerator implements FeatureGenerator {

	private static String FEATURE_NAME = "all_words_histogram";

	private final WikiConnect wiki;
	private final List<Sentence> document;

	public AllWordsHistogramFeatureGenerator(WikiConnect wiki, List<Sentence> sentences) {
		this.wiki = wiki;
		document = sentences;
	}

	@Override
	public String GetFeatureName() {
		return FEATURE_NAME;
	}

	@Override
	public void GenerateFeatures(EntityMention mention) throws Exception {
		Set<String> words = new HashSet<String>();
		for (Sentence sentence : document) {
			words.addAll(Arrays.asList(sentence.getTokens()));
		}
		// get a histogram of all words in sentences
		Histogram documentHistogram = Histogram.extractFromSentenceArray(document, words);

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
