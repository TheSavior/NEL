package com.cse454.nel;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.cse454.nel.disambiguate.AbstractDisambiguator;
import com.cse454.nel.disambiguate.SimpleDisambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.scoring.Scorer;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.BasicSearcher;

public class DocumentProcessor {

	private final int docID;
	private final Scorer scorer;

	public DocumentProcessor(int docID, Scorer scorer) throws SQLException {
		this.docID = docID;
		this.scorer = scorer;
	}

	public void run() throws Exception {
		WikiConnect wiki = new WikiConnect();
		SentenceConnect docs = new SentenceConnect();
		
		// Retrive document
		List<Sentence> sentences = docs.getDocument(this.docID);

		// Extract entity mentions
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);

		// Generate candidate entities
		AbstractSearcher searcher = new BasicSearcher(wiki);
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}

		// Disambiguate
		AbstractDisambiguator disambiguator = new SimpleDisambiguator();
		Map<EntityMention, Entity> entities = disambiguator.disambiguate(mentions);

		String docName = "foo"; // We need to use the docname
		
		// Score our results (if necessary)
		scorer.ScoreResults(docName, entities);
		
		// TODO: output entities to file
	}

}
