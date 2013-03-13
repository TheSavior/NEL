package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.disambiguate.Disambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.CrossWikiSearcher;

public class DocumentProcessor {

	private final DocPreProcessor preprocessor;
	private final WikiConnect wikiDb;

	public DocumentProcessor(DocPreProcessor preprocessor) throws SQLException {
		this.preprocessor = preprocessor;
		this.wikiDb = new WikiConnect();
	}

	public List<Sentence> ProcessDocument(FeatureWeights weights, String text) throws Exception {
		List<Sentence> sentences = preprocessor.ProccessArticle(text);
		Set<FeatureWeights> weightTrials = new HashSet<>();
		weightTrials.add(weights);

		Map<Sentence, Map<FeatureWeights, String[]>> evaluations = ProcessDocument(weightTrials, sentences);

		for (Sentence sentence : sentences) {
			Map<FeatureWeights, String[]> nels = evaluations.get(sentence);
			sentence.setEntities(nels.get(weightTrials));
		}

		return sentences;
	}

	public Map<Sentence, Map<FeatureWeights, String[]>> ProcessDocument(Set<FeatureWeights> weightTrials, List<Sentence> sentences) throws Exception {
		// Extract entity mentions
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);

		// Generate candidate entities
		AbstractSearcher searcher = new CrossWikiSearcher(wikiDb);//new BasicSearcher(wikiDb);
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}

		// Setup feature generators
		Map<String, FeatureGenerator> featureGenerators = new HashMap<String, FeatureGenerator>();

		AllWordsHistogramFeatureGenerator feature1 = new AllWordsHistogramFeatureGenerator(wikiDb, sentences);
		featureGenerators.put(feature1.GetFeatureName(), feature1);

		EntityMentionHistogramFeatureGenerator feature2 = new EntityMentionHistogramFeatureGenerator(wikiDb, sentences, mentions);
		featureGenerators.put(feature2.GetFeatureName(), feature2);

		EntityWikiMentionHistogramFeatureGenerator feature3 = new EntityWikiMentionHistogramFeatureGenerator(wikiDb, sentences, mentions, preprocessor, true);
		featureGenerators.put(feature3.GetFeatureName(), feature3);
		
		EntityWikiMentionHistogramFeatureGenerator feature4 = new EntityWikiMentionHistogramFeatureGenerator(wikiDb, sentences, mentions, preprocessor, false);
		featureGenerators.put(feature4.GetFeatureName(), feature4);

		InLinkFeatureGenerator feature5 = new InLinkFeatureGenerator(wikiDb);
		featureGenerators.put(feature4.GetFeatureName(), feature5);

		// Generate features
		for (Entry<String, FeatureGenerator> generator : featureGenerators.entrySet()) {
			for (EntityMention mention : mentions) {
				generator.getValue().GenerateFeatures(mention);
			}
		}

		// Go through all weight trials
		Disambiguator disambiguator = new Disambiguator();
		Map<Integer, List<EntityMention>> sentenceEntities = listEntityMentionBySentenceID(mentions);

		Map<Sentence, Map<FeatureWeights, String[]>> results = new HashMap<>();
		for (FeatureWeights weights : weightTrials) {
			// Disambiguate
			disambiguator.disambiguate(mentions, weights);

			// Collate data per sentence
			for (Sentence sentence : sentences) {
				List<EntityMention> sentMentions = sentenceEntities.get(sentence.getSentenceId());

				// Initialize entities string
				String[] ents = new String[sentence.getTokens().length];
				for (int i = 0; i < ents.length; ++i) {
					ents[i] = "0";
				}

				// Process mentions
				for (EntityMention mention : sentMentions) {
					if (mention.chosenEntity != null) {
						for (int i = 0; i < mention.numToks; ++i) {
							ents[i + mention.tokStart] = mention.chosenEntity.wikiTitle;
						}
					}
				}

				// Add to results
				Map<FeatureWeights, String[]> sentResults = results.get(sentence);
				if (sentResults == null) {
					sentResults = new HashMap<>();
					results.put(sentence, sentResults);
				}

				sentResults.put(weights, ents);
			}
		}

		return results;
	}

	private Map<Integer, List<EntityMention>> listEntityMentionBySentenceID(List<EntityMention> mentions) {
		Map<Integer, List<EntityMention>> sentenceEntities = new HashMap<>();

		for (EntityMention entMent : mentions) {
			Integer id = Integer.valueOf(entMent.sentenceID);
			if (sentenceEntities.containsKey(id)) {
				sentenceEntities.get(id).add(entMent);
			} else {
				List<EntityMention> entityList = new ArrayList<EntityMention>();
				entityList.add(entMent);
				sentenceEntities.put(id, entityList);
			}
		}
		return sentenceEntities;
	}
}
