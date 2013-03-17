package com.cse454.nel.scripts;

public class CrossWikiDataScript {

	public String mention;
	public double probability;
	public String URL;

	public CrossWikiDataScript(String mention, double probability, String URL) {
		this.mention = mention;
		this.probability = probability;
		this.URL = URL;
	}
}
