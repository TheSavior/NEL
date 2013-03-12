package com.cse454.nel;

public class CrossWikiData {

	public String mention;
	public double probability;
	public String URL;

	public CrossWikiData(String mention, double probability, String URL) {
		this.mention = mention;
		this.probability = probability;
		this.URL = URL;
	}
}
