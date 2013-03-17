package com.cse454.nel.features;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.Util;
import com.cse454.nel.dataobjects.Entity;
import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.mysql.WikiConnect;

public class AllWordsHistogramFeatureGenerator extends FeatureGenerator {

	public static String FEATURE_STRING = "all_words_histogram";

	private final WikiConnect wiki;
	private final List<Sentence> document;

	public AllWordsHistogramFeatureGenerator(WikiConnect wiki, List<Sentence> sentences) {
		this.wiki = wiki;
		document = sentences;
	}

	@Override
	public String GetFeatureName() {
		return FEATURE_STRING;
	}

	@Override
	public void GenerateFeatures(EntityMention mention) throws SQLException {
		Set<String> words = new HashSet<String>();
		for (Sentence sentence : document) {
			words.addAll(Arrays.asList(sentence.getTokens()));
		}
		// get a histogram of all words in sentences
		Histogram documentHistogram = Histogram.extractFromSentenceArray(document, words);

		Map<Entity, Features> candidateFeatures = mention.candidateFeatures;
		for (Entry<Entity, Features> entry : candidateFeatures.entrySet()) {
			Entity candidate = entry.getKey();
			String[] tokens = Util.tokenizeText(wiki.GetCleanedWikiText(candidate.wikiTitle));
			Histogram hist = Histogram.fromTokens(tokens);
			double dotProduct = Util.computeDotProduct(documentHistogram, hist);
			entry.getValue().setFeature(GetFeatureName(), dotProduct);
		}
	}

}
