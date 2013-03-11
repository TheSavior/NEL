package com.cse454.nel.disambiguate;

import java.sql.SQLException;
import java.util.ArrayList;
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
import com.cse454.nel.extract.NerExtractor;

public class EntityWikiMentionHistogramDisambiguator extends AbstractDisambiguator {

	private Map<Entity, Map<String, Double>> wikiCache;
	private NerExtractor extractor;
	private NERClassifier classifier;
	private boolean splitMentions;
	
	public EntityWikiMentionHistogramDisambiguator(WikiConnect wiki, NERClassifier classifier, List<Sentence> sentences, boolean splitMentions) {
		super(wiki, sentences);
		this.classifier = classifier;
		this.splitMentions = splitMentions;
		this.extractor = new NerExtractor();
	}
	
	// TODO: do this case insensitively
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

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions) throws Exception {
		Map<String, Double> docHist = HistogramFromMentions(mentions, sentences);
		
		Map<EntityMention, Entity> ret = new HashMap<EntityMention, Entity>();
		for (EntityMention mention : mentions) {
			if (mention.candidates == null || mention.candidates.isEmpty()) {
				ret.put(mention, null);
			} else {
				double bestDP = -1;
				Entity best = null;
				
				for (Entity ent : mention.candidates) {
					Map<String, Double> entHist = GetWikiHist(ent);
					double score = Util.computeDotProduct(docHist, entHist);
					if (score > bestDP) {
						best = ent;
						bestDP = score;
					}
				}
				
				ret.put(mention, best);
			}
		}
		
		return ret;
	}

}
