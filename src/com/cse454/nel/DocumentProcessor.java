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
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.scoring.Scorer;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.BasicSearcher;

public class DocumentProcessor {

	private final String docName;
	private final Scorer scorer;
	private final DocumentConnect sentenceDb;
	private final Map<String, Double> featureWeights;
	private final NERClassifier nerClassifier;

	public DocumentProcessor(String docName, DocumentConnect sentenceDb, Scorer scorer, Map<String, Double> featureWeights) throws SQLException {
		this.docName = docName;
		this.scorer = scorer;
		this.sentenceDb = sentenceDb;
		this.featureWeights = featureWeights;
		this.nerClassifier = nerClassifier;
	}

	public void run() throws Exception {
		// Setup feature generators
		// TODO:
		
		Map<String, FeatureGenerator> featureGenerators;

		// Retrieve document
		List<Sentence> sentences = sentenceDb.getDocumentByName(docName);

		// Extract entity mentions
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);

		// Generate candidate entities
		AbstractSearcher searcher = new BasicSearcher(new WikiConnect());
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}

		// Disambiguate
		Disambiguator disambiguator = new Disambiguator();
		Map<EntityMention, Entity> entities = disambiguator.disambiguate(mentions, featureWeights);


		// update the entity column
		Map<Integer, List<Entity>> sentenceEntities = convertToIdEntityListMap(entities);
		for (Entry<Integer, List<Entity>> entry : sentenceEntities.entrySet()) {
			updateEntityColumn(entry.getKey(), entry.getValue());
		}

		// String docName = "foo"; // We need to use the docname

		// Score our results (if necessary)
		scorer.ScoreResults(disambiguator.getClass(), docName, entities);

		// TODO: output entities to file
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
