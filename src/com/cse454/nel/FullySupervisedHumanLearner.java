package com.cse454.nel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.SentenceDbDocFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.scoring.AbstractScorer;
import com.cse454.nel.search.CrossWikiSearcher;

public class FullySupervisedHumanLearner {
	private static final Scanner scanner = new Scanner(System.in);
	
	private static class FeatureTrackingScorer implements AbstractScorer {
		private Map<String, List<EntityMention>> docMentions;
		
		@Override public void ScoreMentions(AbstractDocument doc, List<EntityMention> mentions) {
			docMentions.put(doc.GetName(), mentions);
		}

		@Override public void Score(AbstractDocument doc, FeatureWeights weights, Sentence sentence, String[] entities) {}
	}
	
	private static class Range {
		public double min;
		public double max;
		
		public Range(double min, double max) {
			this.min = min;
			this.max = max;
		}
	}
	
	private static Double ReadDouble(String msg) {
		while (true) {
			try {
				System.out.println(msg);
				return Double.parseDouble(scanner.nextLine());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private static Range ReadRange(String msg) {
		System.out.println(msg);
		double min = ReadDouble("\tmin: ");
		double max = ReadDouble("\tmax: ");
		return new Range(min, max);
	}

	public static void main(String[] args) throws InterruptedException {
		Util.PreventStanfordNERErrors();
	
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
				
				for (int i = start; i <= end; ++i) {
					docIDs.add(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (Integer i : docIDs) {
			System.out.println("doc: " + i);
		}
		
		// Doc Factory
		SentenceDbDocFactory docs = new SentenceDbDocFactory();
		docs.AddDocIDs(docIDs);
		
		// Features: these are dummy weights so the processor creates the right features
		FeatureWeights weights = new FeatureWeights();
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 1);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, 1);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_NAME, 1);
		
		// Finally setup the scorer: also mostly just a dummy, to track the learned features for use later
		FeatureTrackingScorer scorer = new FeatureTrackingScorer();
		
		System.out.println("Processing Documents");
		MultiDocumentProcessor processor = new MultiDocumentProcessor(Math.min(docIDs.size(), 16));
		processor.ProcessDocuments(docs, weights, scorer);
		
		// Now begin supervised learning
		System.out.println("Documents Processed\nBegin Supervised Learning");
		
		while (true) {
			Range allWordsRange = ReadRange("AllWords Feature Range: ");
			Range crossWikiRange = ReadRange("Crosswiki Feature Range: ");
			Range inlinkRange = ReadRange("Inlink Feature Range: ");
		}
	}
}
