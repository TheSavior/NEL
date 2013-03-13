package com.cse454.nel;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class GoldGenerator {

	private static Scanner scanner;

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
				
				for (Sentence sentence : sentences) {
					try {
						System.out.print("\nSentence: ");
						for (String token : sentence.getTokens()) {
							System.out.print(token + " ");
						}
						System.out.println();
						
						boolean success = false;
						while (!success) {
							String gold = "";
							for (String token : sentence.getTokens()) {
								System.out.print(token + " => ");
								String gold_tok = scanner.nextLine();
								gold += gold_tok + " ";
							}
						
							System.out.println("Is this correct? '" + gold + "'");
							System.out.print("(y/n):");
							String resp = scanner.nextLine().toLowerCase();
							if (resp.charAt(0) == 'y') {
								docs.SetGoldData(sentence.getSentenceId(), gold);
								success = true;
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
    }
}
