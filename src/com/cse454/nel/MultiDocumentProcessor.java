package com.cse454.nel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.AbstractDocumentFactory;
import com.cse454.nel.features.FeatureWeights;

/**
 * Used to process multiple documents with a given {@link AbstractDocumentFactory}.
 * Multi-threaded.
 *
 * See {@link Main} for an example usage.
 *
 */
public class MultiDocumentProcessor {
	private final int numThreads;
    private final ThreadPoolExecutor executor;
    private final Object docLock;

    public MultiDocumentProcessor(int numThreads) {
    	this.numThreads = numThreads;
    	this.docLock = new Object();
    	this.executor = new ThreadPoolExecutor(numThreads, numThreads, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
    }

	public void ProcessDocuments(AbstractDocumentFactory docs, FeatureWeights weights) throws InterruptedException {
		// Setup Threads
		System.out.println("Starting Threads");
		for (int i = 0; i < numThreads; ++i) {
			DocumentProcessThread thread = new DocumentProcessThread(docs, weights);
			executor.execute(thread);
		}

		// Wait for them to finish
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		System.out.println("Processing Complete");
	}

	private class DocumentProcessThread implements Runnable {

		private AbstractDocumentFactory docs;
		private FeatureWeights weights;

		public DocumentProcessThread(AbstractDocumentFactory docs, FeatureWeights featureWeights) {
			this.docs = docs;
			this.weights = featureWeights;
		}

		@Override
		public void run() {
			DocPreProcessor preProcessor;
			try {
				preProcessor = new DocPreProcessor();
			} catch (Exception e) {
				// we need this class so just throw runtime exception
				throw new RuntimeException(e);
			}

			DocumentProcessor processor = new DocumentProcessor(preProcessor);
			processor.setFeatureWeights(weights);

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

				processor.processDocument(document);

				// Now do some useful work with the entities!

			}

		}

	}
}
