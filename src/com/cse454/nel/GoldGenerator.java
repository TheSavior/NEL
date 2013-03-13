package com.cse454.nel;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

// TODO:
// print sentence id and (7/32)
// start at sentence Id

public class GoldGenerator {

	private static Scanner scanner;

	private static void PrintSentence(String[] words, int highlight) {
		for (int i = 0; i < words.length; ++i) {
			if (i == highlight)
				System.out.print("[");
			System.out.print(words[i]);
			if (i == highlight)
				System.out.print("]");
			System.out.print(" ");
		}
		System.out.println();
	}
	
	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		scanner = new Scanner(System.in);
		DocumentConnect docs = new DocumentConnect();

		while (true) {
			System.out.print("Enter a docID: ");
			String query = scanner.nextLine();

			try {
				List<Sentence> sentences = docs.getDocumentById(Integer.parseInt(query));
				int sentenceCount = 0;
				
				for (Sentence sentence : sentences) {
					try {
						
						boolean success = false;
						while (!success) {
							String gold = "";
							for (int i = 0; i < sentence.getTokens().length; ++i) {
								System.out.println();
								System.out.print("\nSentence (" + query + ":" + sentence.getSentenceId() + ") (" + (sentenceCount+1) + "/" + sentences.size() + "): ");
								PrintSentence(sentence.getTokens(), i);
								while (true) {
									System.out.print(sentence.getTokens()[i] + " => ");
									String gold_tok = scanner.nextLine();
									if (gold_tok.isEmpty()) {
										gold_tok = "0";
									}
									if (gold_tok.contains(" ")) {
										System.out.println("Must not contain spaces");
									} else {
										gold += gold_tok + " ";
										break;
									}
								}
							}
						
							System.out.println("Is this correct? '" + gold + "'");
							char choice = 0;
							while (choice != 'y' && choice != 'n') {
								System.out.print("(y/n):");
								String resp = scanner.nextLine().toLowerCase();
								if (!resp.isEmpty()) {
									choice = resp.charAt(0);
								}
							}
							if (choice == 'y') {
								docs.SetGoldData(sentence.getSentenceId(), gold);
								success = true;
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					++sentenceCount;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
    }
}
