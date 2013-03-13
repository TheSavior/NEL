package com.cse454.nel.features;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.DocPreProcessor;
import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Histogram;
import com.cse454.nel.Sentence;
import com.cse454.nel.Util;
import com.cse454.nel.WikiConnect;
import com.cse454.nel.extract.NerExtractor;

public class EntityWikiMentionHistogramFeatureGenerator implements FeatureGenerator {

	public static String FEATURE_STRING = "entity-wiki-mention-histo";
	public static String FEATURE_STRING_SPLIT = "entity-wiki-mention-histo-split";

	private final Map<Entity, Map<String, Double>> wikiCache = new HashMap<>();

	private WikiConnect wiki;
	private NerExtractor extractor;
	private DocPreProcessor classifier;
	private Map<String, Double> docHist;
	private boolean splitMentions;

	public EntityWikiMentionHistogramFeatureGenerator(WikiConnect wiki, List<Sentence> sentences, List<EntityMention> mentions, DocPreProcessor classifier, boolean splitMentions) {
		this.wiki = wiki;
		this.classifier = classifier;
		this.splitMentions = splitMentions;
		this.docHist = HistogramFromMentions(mentions, sentences);
		this.extractor = new NerExtractor();
	}

	@Override
	public String GetFeatureName() {
		if (splitMentions) {
			return FEATURE_STRING_SPLIT;
		} else {
			return FEATURE_STRING;
		}
	}

	@Override
	public void GenerateFeatures(EntityMention mention) throws Exception {
		if (mention.candidateFeatures == null) {
			return;
		}

		for (Entry<Entity, Features> candidate : mention.candidateFeatures.entrySet()) {
			Map<String, Double> entHist = GetWikiHist(candidate.getKey());
			double score = Util.computeDotProduct(docHist, entHist);
			if (score == Double.NaN) {
				System.out.println("nan");
			}
			candidate.getValue().setFeature(GetFeatureName(), score);
		}
	}

	private Map<String, Double> HistogramFromMentions(List<EntityMention> mentions, List<Sentence> sentences) {
		Set<String> mentionWords = new HashSet<String>();
		for (EntityMention mention : mentions) {
			if (splitMentions) {
				String[] words = mention.mentionString.split("\\s");
				for (String w : words) {
					mentionWords.add(Util.cleanString(w).toLowerCase());
				}
			} else {
				mentionWords.add(mention.mentionString);
			}
		}

		List<Sentence> cleanedSentences = new ArrayList<Sentence>();
		for (Sentence sentence : sentences) {
			String[] tokens = sentence.getTokens();
			String[] newTokens = new String[tokens.length];
			for (int i = 0; i < tokens.length; ++i) {
				newTokens[i] = tokens[i].toLowerCase();
			}
			cleanedSentences.add(new Sentence(sentence.getSentenceId(), newTokens, sentence.getNer()));
		}

		Histogram hist = Histogram.extractFromSentenceArray(cleanedSentences, mentionWords);
		return hist.getNormalizedMap();
	}

	private Map<String, Double> GetWikiHist(Entity ent) throws SQLException {
		if (wikiCache.containsKey(ent)) {
			return wikiCache.get(ent);
		}

		String text = wiki.GetCleanedWikiText(ent.wikiTitle);
		List<Sentence> sentences = classifier.ProccessArticle(text);
		List<EntityMention> mentions = extractor.extract(sentences);
		Map<String, Double> hist = HistogramFromMentions(mentions, sentences);
		wikiCache.put(ent, hist);
		return hist;
	}
}
