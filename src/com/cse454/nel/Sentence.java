package com.cse454.nel;

public class Sentence {
	
	private int sentenceID;
	private String[] tokens;
	private String[] ner;
	
	public Sentence(int sentenceID, String tokens, String ner) {
		this.tokens = tokens.split("\\s");
		this.ner = tokens.split("\\s");
	}
	
	public String[] getTokens() {
		return tokens;
	}
	
	public String[] getNer() {
		return ner;
	}
}
