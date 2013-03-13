package com.cse454.nel;

public class Sentence {

	private int sentenceID;
	private String[] tokens;
	private String[] ner;
	private String[] entities;

	public Sentence(int sentenceID, String tokens, String ner) {
		this.sentenceID = sentenceID;
		this.tokens = tokens.split("\\s");
		this.ner = ner.split("\\s");
	}

	public Sentence(int sentenceID, String[] tokens, String[] ner) {
		this.sentenceID = sentenceID;
		this.tokens = tokens;
		this.ner = ner;
	}

	public int getSentenceId() {
		return sentenceID;
	}

	public String[] getTokens() {
		return tokens;
	}

	public String[] getNer() {
		return ner;
	}
}
