package com.cse454.nel.document;

import java.util.List;

import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.mysql.DocumentConnect;

/**
 * {@link SentenceDbDocument} represents a document retreived from the Sentences database.
 *
 */
public class SentenceDbDocument extends AbstractDocument {
	private String dbName;
	private int docID;

	public SentenceDbDocument(String dbName) {
		super(dbName);
		this.dbName = dbName;
	}

	public SentenceDbDocument(int docID) {
		super(""+docID);
		dbName = null;
		this.docID = docID;
	}

	@Override
	protected List<Sentence> GenerateSentences() throws Exception {
		DocumentConnect docs = new DocumentConnect();
		if (dbName != null) {
			return docs.getDocumentByName(dbName);
		} else {
			return docs.getDocumentById(docID);
		}
	}
}
