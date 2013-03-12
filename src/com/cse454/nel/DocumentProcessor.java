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

	private final String docName;
	private final Scorer scorer;
	private final DocumentConnect sentenceDb;
	private final Map<String, Double> featureWeights;
	private final NERClassifier nerClassifier;
	private final WikiConnect wikiDb;

	public DocumentProcessor(String docName, DocumentConnect sentenceDb, Scorer scorer, Map<String, Double> featureWeights, NERClassifier nerClassifier) throws SQLException {
		this.docName = docName;
		this.scorer = scorer;
		this.sentenceDb = sentenceDb;
		this.featureWeights = featureWeights;
		this.nerClassifier = nerClassifier;
		this.wikiDb = new WikiConnect();
	}

	public void run() throws Exception {
		// Retrieve document
		// TODO: generisize this for mitchel
		List<Sentence> sentences = sentenceDb.getDocumentByName(docName);

		// Extract entity mentions
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);

		// Generate candidate entities
		AbstractSearcher searcher = new BasicSearcher(new WikiConnect());
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
		EntityWikiMentionHistogramFeatureGenerator feature3 = new EntityWikiMentionHistogramFeatureGenerator(wikiDb, sentences, mentions, nerClassifier, true);
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


		// Update the entity column
		// TODO: generisize this for mitchel
		Map<Integer, List<Entity>> sentenceEntities = convertToIdEntityListMap(entities);
		for (Entry<Integer, List<Entity>> entry : sentenceEntities.entrySet()) {
			updateEntityColumn(entry.getKey(), entry.getValue());
		}

		// Score our results (if necessary)
		scorer.ScoreResults(disambiguator.getClass(), docName, entities);
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

	private Map<Integer, List<Entity>> convertToIdEntityListMap(Map<EntityMention, Entity> entities) {
		Map<Integer, List<Entity>> sentenceEntities = new HashMap<>();

		for (Entry<EntityMention, Entity> entry : entities.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			Integer id = Integer.valueOf(entry.getKey().sentenceID);
			if (sentenceEntities.containsKey(id)) {
				sentenceEntities.get(id).add(entry.getValue());
			} else {
				List<Entity> entityList = new ArrayList<Entity>();
				entityList.add(entry.getValue());
				sentenceEntities.put(id, entityList);
			}
		}
		return sentenceEntities;
	}
}
