package com.cse454.nel.document;

import java.util.List;

import com.cse454.nel.dataobjects.Sentence;

public class SimpleDocument extends AbstractDocument {

	public SimpleDocument(String name, List<Sentence> sentences) {
		super(name);
		this.sentences = sentences;
	}

	@Override
	protected List<Sentence> GenerateSentences() throws Exception {
		return sentences;
	}

}
