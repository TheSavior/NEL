package com.cse454.nel.scripts;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.DocPreProcessor;
import com.cse454.nel.EvaluationDocumentProcessor;
import com.cse454.nel.Util;
import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.AbstractDocumentFactory;
import com.cse454.nel.document.SentenceDbDocFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.scoring.AbstractScorer;
import com.cse454.nel.scoring.EvaluationScorer;
import com.cse454.nel.search.CrossWikiSearcher;

/**
 * This class is used to evaluate the results of processing documents.
 * @author andrewrogers
 *
 */
public class EvaluationScript {

	private static Object docLock = new Object();
	private static Object scoreLock = new Object();

	public static void main(String[] args) throws Exception {
		// Prevent errors from standford ner
		Util.PreventStanfordNERErrors();

		int numThreads = 16;
		ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
		EvaluationScorer scorer = new EvaluationScorer();
		// Setup Feature Weights
		Set<FeatureWeights> weightTrials = new HashSet<FeatureWeights>();
		FeatureWeights weights = new FeatureWeights();
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 2);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, 13);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, 185);
		weightTrials.add(weights);

		// Setup Documents
		List<Integer> docIDs = new ArrayList<>();
		for (int i = 213; i <= 213; i++) {
			docIDs.add(i);
		}
		/*
		for (int i = 225; i <= 237; i++) {
			docIDs.add(i);
		}
		*/
		SentenceDbDocFactory docs = new SentenceDbDocFactory();
		docs.AddDocIDs(docIDs);

		// Setup Threads
		System.out.println("Starting Threads");
		for (int i = 0; i < 1; i++) {
			final Set<FeatureWeights> w = weightTrials;
			EvaluationDocumentProcessThread thread = new EvaluationDocumentProcessThread(docs, w, scorer);
			executor.execute(thread);
		}

		// Wait for them to finish
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

		// Show scores
		System.out.println("--- Evaluation ---");
		System.out.printf("Precision: \t\t%f\n", scorer.getPrecisionScore());
		System.out.printf("Recall: \t\t%f\n", scorer.getRecallScore());
		System.out.printf("Overall ratio: \t\t%f / %f\t%f\n", scorer.getTotalCorrect(), scorer.getTotalGold(), scorer.getOverallRation());

	}

	/**
	 * This class was used in evaluation and testing for the project.
	 *
	 */
	private static class EvaluationDocumentProcessThread implements Runnable {

		private AbstractDocumentFactory docs;
		private Set<FeatureWeights> featureWeights;
		private AbstractScorer scorer;

		public EvaluationDocumentProcessThread(AbstractDocumentFactory docs, Set<FeatureWeights> featureWeights, AbstractScorer scorer) {
			this.docs = docs;
			this.featureWeights = featureWeights;
			this.scorer = scorer;
		}

		@Override
		public void run() {
			try {
				DocPreProcessor preProcessor = new DocPreProcessor();
				EvaluationDocumentProcessor processor = new EvaluationDocumentProcessor(preProcessor);

				System.out.println("Thread Begin Processing.");
				while (true) {
					// Get Next Document
					AbstractDocument document;
					synchronized (docLock) {
						document = docs.NextDocument();
					}

					// If no doc, we're done
					if (document == null) {
						break;
					}

					List<Sentence> sentences = document.GetSentences();

					try {
						// Process Document

						PrintStream timeLog = processor.EnabledPrintStream(null);
						List<EntityMention> mentions =
								processor.ProcessDocumentFeatures(timeLog, featureWeights, sentences);
						Map<Sentence, Map<FeatureWeights, String[]>> results =
								processor.evaluateFeatureWeights(timeLog, sentences, featureWeights);

						synchronized (scoreLock) {
							scorer.ScoreMentions(document, mentions);
						}

						// Score Document
						for (Entry<Sentence, Map<FeatureWeights, String[]>> entry : results.entrySet()) {
							Sentence sentence = entry.getKey();
							Map<FeatureWeights, String[]> entityTrials = entry.getValue();

							for (Entry<FeatureWeights, String[]> entities : entityTrials.entrySet()) {
								synchronized (scoreLock) {
									scorer.Score(document, entities.getKey(), sentence, entities.getValue());
								}
							}
						}
						System.out.println("Done Processing Doc: " + document.GetName());

					} catch (Exception e) {
						System.out.println("Exception processing document: " + document.GetName());
						e.printStackTrace(System.out);
					}

				}
			} catch (Exception e) {
				System.out.println("Exception in thread:");
				e.printStackTrace(System.out);
			}
		}
	}
}
