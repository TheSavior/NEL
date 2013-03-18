package com.cse454.nel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.DocumentFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;

/**
 * Used to process multiple documents with a given {@link DocumentFactory}.
 * Multi-threaded.
 *
 * See {@link Main} for an example usage.
 *
 */
public class MultiDocumentProcessor {

	public static interface ProcessedDocumentCallback {
		public void onDocumentFinished(AbstractDocument document);
		public void onProcessError(Exception e);
	}

	private static final FeatureWeights DEFAULT_WEIGHTS = new FeatureWeights();
	static {
		DEFAULT_WEIGHTS.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 9);
		DEFAULT_WEIGHTS.setFeature(CrossWikiSearcher.FEATURE_STRING, 13);
		DEFAULT_WEIGHTS.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, 78);
	}
	private final int numThreads;
    private final ThreadPoolExecutor executor;
    private final Object docLock;

    private ProcessedDocumentCallback mCallback;

    public MultiDocumentProcessor(int numThreads) {
    	this.numThreads = numThreads;
    	this.docLock = new Object();
    	this.executor = new ThreadPoolExecutor(numThreads, numThreads, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
    }

    public void ProcessDocuments(DocumentFactory docs) {
    	ProcessDocuments(docs, DEFAULT_WEIGHTS);
    }

	public void ProcessDocuments(DocumentFactory docs, FeatureWeights weights) {
		// Setup Threads
		System.out.println("Starting Threads");
		for (int i = 0; i < numThreads; ++i) {
			DocumentProcessThread thread = new DocumentProcessThread(docs, weights);
			executor.execute(thread);
		}

		// Wait for them to finish
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("Executor interrupted before shutting down completely");
			e.printStackTrace();
		}
		System.out.println("Processing Complete");
	}

	public void setProcessDocumentListener(ProcessedDocumentCallback callback) {
		mCallback = callback;
	}

	private class DocumentProcessThread implements Runnable {

		private DocumentFactory docs;
		private FeatureWeights weights;

		public DocumentProcessThread(DocumentFactory docs, FeatureWeights featureWeights) {
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

				try {
					processor.processDocument(document);
				} catch (Exception e) {
					mCallback.onProcessError(e);
					continue;
				}

				// Now do some useful work with the entities!
				if (mCallback != null) {
					mCallback.onDocumentFinished(document);
				}

			}

		}

	}
}
