package com.cse454.nel;

public class Util {

	public static String tokenizeText(String text) {
		return text.replaceAll("[^a-zA-Z0-9\\s]", "");
	}
}
