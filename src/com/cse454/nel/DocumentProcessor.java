package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.disambiguate.Disambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.BasicSearcher;

public class DocumentProcessor {

	private final String docName;
	private final DocumentConnect sentenceDb;
	private final Map<String, Double> featureWeights;
	private final NERClassifier nerClassifier;
	private final WikiConnect wikiDb;

	public DocumentProcessor(String docName, DocumentConnect sentenceDb, Map<String, Double> featureWeights, NERClassifier nerClassifier) throws SQLException {
		this.docName = docName;
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
		disambiguator.disambiguate(mentions, featureWeights);

		// Evaluate each sentence, ratio of entities found to entities in list
		Map<Integer, List<EntityMention>> mentionSentenceList =
				listEntityMentionBySentenceID(mentions);

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
