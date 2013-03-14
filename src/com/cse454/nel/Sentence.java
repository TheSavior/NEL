package com.cse454.nel;

public class Sentence implements Comparable<Sentence> {

	private int sentenceID;
	private String[] tokens;
	private String[] ner;
	private String[] gold;
	private String[] entities;

	public Sentence(int sentenceID, String tokens, String ner, String gold) {
		this.sentenceID = sentenceID;
		this.tokens = tokens == null ? null : tokens.split("\\s");
		this.ner = ner == null ? null : ner.split("\\s");
		this.gold = gold == null ? null : gold.split("\\s");
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

	public String[] getEntities() {
		return entities;
	}
	
	public String[] getGold() {
		return gold;
	}
	
	public void setEntities(String[] entities) {
		this.entities = entities;
	}
	
	public int compareTo(Sentence sentence) {
		return this.sentenceID - sentence.sentenceID;
	}
}
