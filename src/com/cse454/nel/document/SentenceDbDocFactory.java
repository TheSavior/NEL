package com.cse454.nel.document;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.cse454.nel.DocumentConnect;
import com.cse454.nel.Sentence;

public class SentenceDbDocFactory implements DocumentFactory {
	
	private Queue<String> docNames = new LinkedList<>();
	private Queue<Integer> docIDs = new LinkedList<>();
	
	public void AddDocNames(List<String> docs) {
		docNames.addAll(docs);
	}
	
	public void AddDocIDs(List<Integer> docs) {
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
			System.out.println("Generate Sentences");
			DocumentConnect docs = new DocumentConnect();
			if (dbName != null) {
				System.out.println("Generate Sentences By Name");
				return docs.getDocumentByName(dbName);
			} else {
				System.out.println("Generate Sentences By Id");
				return docs.getDocumentById(docID);
			}
		}
		
	}

}
