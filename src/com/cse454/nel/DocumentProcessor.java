package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.disambiguate.AbstractDisambiguator;
import com.cse454.nel.disambiguate.InLinkDisambiguator;
import com.cse454.nel.disambiguate.SimpleDisambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.scoring.Scorer;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.BasicSearcher;

public class DocumentProcessor {

	private final int docID;
	private final String docName;
	private final Scorer scorer;
	private final DocumentConnect sentenceDb;
	private final AbstractDisambiguator disambiguator;
	//private final NERClassifier nerClassifier;

	public DocumentProcessor(int docID, String docName, DocumentConnect sentenceDb, Scorer scorer, AbstractDisambiguator disambiguator) throws SQLException {
		this.docID = docID;
		this.docName = docName;
		this.scorer = scorer;
		this.sentenceDb = sentenceDb;
		this.disambiguator = disambiguator;
		//this.nerClassifier = nerClassifier;
	}

	public void run() throws Exception {

		// Retrieve document
		// List<Sentence> sentences = sentenceDb.getDocument(this.docID);
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
		Map<EntityMention, Entity> entities = disambiguator.disambiguate(mentions, sentences);


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
