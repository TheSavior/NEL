package com.cse454.nel;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.DocumentFactory;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.scoring.AbstractScorer;

public class MultiDocumentProcessor {
	private final int numThreads;
    private final ThreadPoolExecutor executor;
    private final Object docLock;
    private final Object scoreLock;

    public MultiDocumentProcessor(int numThreads) {
    	this.numThreads = numThreads;
    	this.docLock = new Object();
    	this.scoreLock = new Object();
    	this.executor = new ThreadPoolExecutor(numThreads, numThreads, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
    }

	public void ProcessDocuments(DocumentFactory docs, FeatureWeights weights, final AbstractScorer scorer) throws InterruptedException {
		Set<FeatureWeights> weightsList = new HashSet<>(1);
		weightsList.add(weights);
		ProcessDocuments(docs, weightsList, scorer);
	}

	public void ProcessDocuments(DocumentFactory docs, Set<FeatureWeights> weights, AbstractScorer scorer) throws InterruptedException {
		// Setup Threads
		System.out.println("Starting Threads");
		for (int i = 0; i < numThreads; ++i) {
			final Set<FeatureWeights> w = weights;
			DocumentProcessThread thread = new DocumentProcessThread(docs, scorer, w);
			executor.execute(thread);
		}

		// Wait for them to finish
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		System.out.println("Processing Complete");
	}

	private class DocumentProcessThread implements Runnable {

		private DocumentFactory docs;
		private final AbstractScorer scorer;
		private Set<FeatureWeights> featureWeights;

		public DocumentProcessThread(DocumentFactory docs, AbstractScorer scorer, Set<FeatureWeights> featureWeights) {
			this.docs = docs;
			this.scorer = scorer;
			this.featureWeights = featureWeights;
		}

		@Override
		public void run() {
			try {
				DocPreProcessor preProcessor = new DocPreProcessor();
				DocumentProcessor processor = new DocumentProcessor(preProcessor);

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
								processor.ScoreWeightTrials(timeLog, sentences, mentions, featureWeights);
						
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
