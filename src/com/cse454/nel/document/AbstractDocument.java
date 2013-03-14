package com.cse454.nel.document;

import java.util.List;

import com.cse454.nel.Sentence;

public abstract class AbstractDocument {
	protected String name;
	protected List<Sentence> sentences;
	
	protected AbstractDocument(String name) {
		this.name = name;
		this.sentences = null;
	}
	
	public String GetName() {
		return name;
	}
	
	public List<Sentence> GetSentences() {
		if (sentences == null) {
			sentences = GenerateSentences();
		}	
		
		return sentences;
	}
	
	protected abstract List<Sentence> GenerateSentences();
}
