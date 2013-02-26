package com.cse454.nel;

import java.util.List;

import com.cse454.nel.extract.EntityExtractor;

public class DocumentProcessor {

	public DocumentProcessor(int docID) {
		WikiConnect wiki = new WikiConnect();
		List<Sentence> sentences = wiki.getFile(docID);
		
		EntityExtractor extractor = new EntityExtractor();
		List<EntityMention> mentions = extractor.extractMentions();
		
		Searcher searcher;
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}
	}

}
