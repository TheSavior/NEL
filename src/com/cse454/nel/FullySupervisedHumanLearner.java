package com.cse454.nel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.cse454.nel.document.SentenceDbDocFactory;

public class FullySupervisedHumanLearner {
	private static final Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) {
		Util.PreventStanfordNERErrors();
	
		scanner.nextLine();
		

	
		List<Integer> docIDs = new ArrayList<>();
		while (true) {
			try {
				System.out.print("Enter A Doc Range (or 'done' to finish)\nStart: ");
				String startStr = scanner.nextLine();
				if (startStr.equals("done")) {
					break;
				}
				int start = Integer.parseInt(startStr);
				System.out.print("End (inclusive): ");
				int end = Integer.parseInt(scanner.nextLine());
			}
		}
		
		SentenceDbDocFactory docs = new SentenceDbDocFactory();
		docs.AddDocIDs(docIDs);
	}
}
