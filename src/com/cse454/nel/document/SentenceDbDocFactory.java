package com.cse454.nel.document;

import java.util.List;
import java.util.Queue;

import com.cse454.nel.DocumentConnect;
import com.cse454.nel.Sentence;

public class SentenceDbDocFactory implements DocumentFactory {
	
	private Queue<String> docNames;
	private Queue<Integer> docIDs;
	
	public void AddDocNames(List<String> docs) {
		docNames.addAll(docs);
	}
	
	public void SetDocIDs(List<Integer> docs) {
		docIDs.addAll(docs);
	}
	
	@Override
	public AbstractDocument NextDocument() {
		if (!docNames.isEmpty()) {
			String name = docNames.poll();
			return new SentenceDbDocument(name);
		} else if (!docIDs.isEmpty()) {
			int id = docIDs.poll();
			return new SentenceDbDocument(id);
		}
		
		return null;
	}
	
	private static class SentenceDbDocument extends AbstractDocument {
		
		private String dbName;
		private int docID;
		
		public SentenceDbDocument(String name) {
			super(name);
			dbName = name;
		}
		
		public SentenceDbDocument(int docID) {
			super(""+docID);
			dbName = null;
			this.docID = docID;
		}

		@Override
		protected List<Sentence> GenerateSentences() throws Exception {
			DocumentConnect docs = new DocumentConnect();
			if (dbName == null) {
				return docs.getDocumentByName(dbName);
			} else {
				return docs.getDocumentById(docID);
			}
		}
		
	}

}
