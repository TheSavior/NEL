package com.cse454.nel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.SentenceDbDocFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.scoring.AbstractScorer;
import com.cse454.nel.scoring.FeatureWeightScorer;
import com.cse454.nel.search.CrossWikiSearcher;

public class FullySupervisedHumanLearner {
	private static final Scanner scanner = new Scanner(System.in);

	private static class FeatureTrackingScorer implements AbstractScorer {
		private Map<AbstractDocument, List<EntityMention>> docMentions = new HashMap<>();

		@Override public void ScoreMentions(AbstractDocument doc, List<EntityMention> mentions) {
			docMentions.put(doc, mentions);
		}

		@Override public void Score(AbstractDocument doc, FeatureWeights weights, Sentence sentence, String[] entities) {}
	}

	private static class FSHLFeatureWeights extends FeatureWeights {
		private Map<String, Integer> featureIndices = new HashMap<>();

		public void SetFeature(String feature, double weight, Integer index) {
			setFeature(feature, weight);
			featureIndices.put(feature, index);
		}

		public int getIndex(String feature) {
			return featureIndices.get(feature);
		}
	}

	private static class Range {
		public double min;
		public double max;

		public Range(double min, double max) {
			this.min = min;
			this.max = max;
		}

		public double getMid() {
			return min + (max - min) / 2;
		}
	}

	private static Double ReadDouble(String msg) {
		while (true) {
			try {
				System.out.print(msg);
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

	private static class WeightScorePair implements Comparable<WeightScorePair> {
		public FeatureWeights weights;
		public double score;

		public WeightScorePair(FeatureWeights weights, double score) {
			this.weights = weights;
			this.score = score;
		}

		public boolean equals(WeightScorePair other) {
			return score == other.score;
		}

		@Override
		public int compareTo(WeightScorePair o) {
			return (score > o.score) ? -1 : (score < o.score) ? 1 : 0;
		}


	}

	public static void main(String[] args) throws Exception {
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
		FeatureWeights dummyWeights = new FeatureWeights();
		dummyWeights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 1);
		dummyWeights.setFeature(CrossWikiSearcher.FEATURE_STRING, 1);
		dummyWeights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, 1);

		// Finally setup the scorer: also mostly just a dummy, to track the learned features for use later
		FeatureTrackingScorer mentionTracker = new FeatureTrackingScorer();

		System.out.println("Processing Documents");
		MultiDocumentProcessor processor = new MultiDocumentProcessor(Math.min(docIDs.size(), 16));
		// processor.ProcessDocuments(docs, dummyWeights, mentionTracker);

		// Now begin supervised learning
		System.out.println("Documents Processed\nBegin Supervised Learning");

		DocPreProcessor preProcessor = new DocPreProcessor();
		DocumentProcessor docProcessor = new DocumentProcessor(preProcessor);
		//while (true) {
			// Get desired trial range
		/*	Range allWordsRange = ReadRange("AllWords Feature Range: ");
			Range crossWikiRange = ReadRange("Crosswiki Feature Range: ");
			Range inLinkRange = ReadRange("Inlink Feature Range: ");

			// Setup trials
			Set<FeatureWeights> weightTrials = new HashSet<>();
			for (int i = 0; i <= 2; ++i) {
				for (int t = 0; t <= 2; ++t) {
					for (int v = 0; v <= 2; ++v) {
						double allWords = allWordsRange.min + i*allWordsRange.getMid();
						double crossWiki = crossWikiRange.min + t*crossWikiRange.getMid();
						double inlinks = inLinkRange.min + v*inLinkRange.getMid();

						FSHLFeatureWeights weights = new FSHLFeatureWeights();
						weights.SetFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, allWords, i);
						weights.SetFeature(CrossWikiSearcher.FEATURE_STRING, crossWiki, t);
						weights.SetFeature(InLinkFeatureGenerator.FEATURE_STRING, inlinks, v);

						weightTrials.add(weights);
					}
				}
			}*/

			Set<FeatureWeights> weightTrials = new HashSet<>();
			for (int i = 0; i <= 100; ++i) {
				int tmax = 100 - i;
				for (int t = 0; t <= tmax; ++t) {
					int v = 100 - t - i;
					FeatureWeights weights = new FeatureWeights();
					weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, i);
					weights.setFeature(CrossWikiSearcher.FEATURE_STRING, t);
					weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, v);
					weightTrials.add(weights);
				}
			}

			// Run trials
			FeatureWeightScorer scorer = new FeatureWeightScorer();
			for (Entry<AbstractDocument, List<EntityMention>> docMentions : mentionTracker.docMentions.entrySet()) {
				AbstractDocument doc = docMentions.getKey();
				List<EntityMention> mentions = docMentions.getValue();

				Map<Sentence, Map<FeatureWeights, String[]>> results = null;
						// docProcessor.ScoreWeightTrials(docProcessor.EnabledPrintStream(null), doc.GetSentences(), mentions, weightTrials);

				for (Entry<Sentence, Map<FeatureWeights, String[]>> entry : results.entrySet()) {
					Sentence sentence = entry.getKey();
					Map<FeatureWeights, String[]> entityTrials = entry.getValue();

					for (Entry<FeatureWeights, String[]> entities : entityTrials.entrySet()) {
						scorer.Score(doc, entities.getKey(), sentence, entities.getValue());
					}
				}
			}

			List<WeightScorePair> scores = new ArrayList<>();
			for (Entry<FeatureWeights, Double> featureScores : scorer.getScores().entrySet()) {
				scores.add(new WeightScorePair(featureScores.getKey(), featureScores.getValue()));
			}

			System.out.println("Sorting");
			Collections.sort(scores);
			for (int i = 0; i < 15; ++i) {
				WeightScorePair pair = scores.get(i);
				System.out.println(pair.score + " => " + pair.weights);
			}
			System.out.println("done");


			// Print results
		/*	double[][][] scores = new double[3][3][3];
			for (Entry<FeatureWeights, Double> featureScores : scorer.getScores().entrySet()) {
				FSHLFeatureWeights weights = (FSHLFeatureWeights) featureScores.getKey();
				double score = featureScores.getValue();
				scores[weights.getIndex(AllWordsHistogramFeatureGenerator.FEATURE_STRING)]
					  [weights.getIndex(CrossWikiSearcher.FEATURE_STRING)]
					  [weights.getIndex(InLinkFeatureGenerator.FEATURE_STRING)] = score;
			}

			System.out.println("y-axis: " + CrossWikiSearcher.FEATURE_STRING + " (" + crossWikiRange.min + ", " + crossWikiRange.max + ")");
			System.out.println("x-axis: " + InLinkFeatureGenerator.FEATURE_STRING + " (" + inLinkRange.min + ", " + inLinkRange.max + ")");
			for (int i = 0; i < 3; ++i) {
				System.out.println("\n");
				System.out.println(AllWordsHistogramFeatureGenerator.FEATURE_STRING + ": " + (allWordsRange.min + i*allWordsRange.getMid()));
				System.out.println();

				for (int t = 0; t < 3; ++t) {
					for (int v = 0; v < 3; ++v) {
						System.out.print(scores[i][t][v] + "\t\t");
					}
					System.out.println();
				}
			}*/
		//}
	}
}
