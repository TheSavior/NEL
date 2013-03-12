package com.cse454.nel;

import java.sql.SQLException;
import java.util.Scanner;

import com.cse454.nel.search.BasicSearcher;

public class Test {

	private static Scanner scanner;

	/**
	 * @param args
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SQLException {
		scanner = new Scanner(System.in);
		WikiConnect wiki = new WikiConnect();
		DocumentConnect docs = new DocumentConnect();
		BasicSearcher searcher = new BasicSearcher(wiki);
		
		NERClassifier classifier = new NERClassifier();

		while (true) {
			System.out.print("Enter a query: ");
			String query = scanner.nextLine();

			try {
				// 1. Get wiki text
				//System.out.println(wiki.GetCleanedWikiText(query));
				
				// 2. Candidate lists
				/*EntityMention mention = new EntityMention(0, query, 0, 0);
				searcher.GetCandidateEntities(mention);
				for (Entity ent : mention.candidates) {
					System.out.println(ent.wikiTitle);
				}*/
				
				// 3. Get document
				//docs.getDocument(Integer.valueOf(query));

				// 4. Inlinks
				//System.out.println(wiki.GetInlinks(query) + " inlinks to '" + query + "'");
				
				// 5. NER
				classifier.ProccessArticle(query);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
    }

}
