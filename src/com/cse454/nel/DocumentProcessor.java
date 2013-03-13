package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.disambiguate.Disambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.scoring.Scorer;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.BasicSearcher;

public class DocumentProcessor {

	private final Map<String, Double> featureWeights;
	private final DocPreProcessor preprocessor;
	private final WikiConnect wikiDb;

	public DocumentProcessor(Map<String, Double> featureWeights, DocPreProcessor preprocessor) throws SQLException {
		this.featureWeights = featureWeights;
		this.preprocessor = preprocessor;
		this.wikiDb = new WikiConnect();
	}
	
	public List<Sentence> ProcessDocument(String text) throws Exception {
		List<Sentence> sentences = preprocessor.ProccessArticle(text);
		return ProcessDocument(sentences);
	}
	
	public List<Sentence> ProcessDocument(List<Sentence> sentences) throws Exception {
		// Extract entity mentions
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);

		// Generate candidate entities
		AbstractSearcher searcher = new BasicSearcher(wikiDb);
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}

		// Setup feature generators
		Map<String, FeatureGenerator> featureGenerators = new HashMap<String, FeatureGenerator>();
		
		AllWordsHistogramFeatureGenerator feature1 = new AllWordsHistogramFeatureGenerator(wikiDb, sentences);
		featureGenerators.put(feature1.GetFeatureName(), feature1);
		
		EntityMentionHistogramFeatureGenerator feature2 = new EntityMentionHistogramFeatureGenerator(wikiDb, sentences, mentions);
		featureGenerators.put(feature2.GetFeatureName(), feature2);
		
		// TODO: command line arg instead of 'true'?
		EntityWikiMentionHistogramFeatureGenerator feature3 = new EntityWikiMentionHistogramFeatureGenerator(wikiDb, sentences, mentions, preprocessor, true);
		featureGenerators.put(feature3.GetFeatureName(), feature3);
		
		InLinkFeatureGenerator feature4 = new InLinkFeatureGenerator(wikiDb);
		featureGenerators.put(feature4.GetFeatureName(), feature4);
		
		// Generate features
		for (String feature : featureWeights.keySet()) {
			FeatureGenerator generator = featureGenerators.get(feature);
			if (generator == null) {
				throw new Exception("No Feature Generator For Feature: '" + feature + "'");
			}
			
			for (EntityMention mention : mentions) {
				generator.GenerateFeatures(mention);
			}
		}

		// Disambiguate
		Disambiguator disambiguator = new Disambiguator();
		Map<EntityMention, Entity> entities = disambiguator.disambiguate(mentions, featureWeights);

		// Populate sentence entity data
		// Update the entity column
		Map<Integer, List<EntityMention>> sentenceEntities = convertToIdEntityListMap(entities);
		for (Entry<Integer, List<Entity>> entry : sentenceEntities.entrySet()) {
			updateEntityColumn(entry.getKey(), entry.getValue());
		}
		
		return sentences;
	}

	private void updateEntityColumn(int sentenceID, List<Entity> entities) {
		StringBuffer entityString = new StringBuffer();
		int count = 0;
		for (Entity entity : entities) {
			entityString.append(entity.wikiTitle);
			if (count != entities.size() - 1) {
				entityString.append("\t");
			}
			count++;
		}
		sentenceDb.EntityUpdate(sentenceID, entityString.toString());
	}

	private Map<Integer, List<EntityMention>> convertToIdEntityListMap(Map<EntityMention, Entity> entities) {
		Map<Integer, List<EntityMention>> sentenceEntities = new HashMap<>();

		for (Entry<EntityMention, Entity> entry : entities.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}

			Integer id = Integer.valueOf(entry.getKey().sentenceID);
			List<EntityMention> sentenceMentions = null;
			
			if (!sentenceEntities.containsKey(id)) {
				sentenceMentions = new ArrayList<>();
				sentenceEntities.put(id, sentenceMentions);
			} else {
				sentenceMentions = sentenceEntities.get(id);
			}
			
			sentenceMentions.add(entry.getKey());
		}
		return sentenceEntities;
	}
}
