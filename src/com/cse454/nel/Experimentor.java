package com.cse454.nel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.MultiDocumentProcessor.ProcessedDocumentCallback;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.SentenceDbDocFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.mysql.WikiConnect;
import com.cse454.nel.scoring.EvaluationScorer;
import com.cse454.nel.search.CrossWikiSearcher;

public class Experimentor {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, IOException {
		// Stanford NER commandeers System.err with annoying messages
		Util.PreventStanfordNERErrors();

		// Scorer
		final EvaluationScorer scorer = new EvaluationScorer();

		// Multi-doc processor processes many documents at once using our factory
		MultiDocumentProcessor multiDocProcessor = new MultiDocumentProcessor(4);
		
		// Add our callback to retrieve the results for each document
		multiDocProcessor.setProcessDocumentListener(new ProcessedDocumentCallback() {

			@Override
			public void onDocumentFinished(AbstractDocument document) {
				try {
					// write the entities to a file?
					for (Sentence sentence : document.GetSentences()) {
						scorer.Score(document, null, sentence, sentence.getEntities());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onProcessError(Exception e) {
				// error processing file
			}
		});

		// now process the docs!
		Map<Integer, CWResult> scores = new HashMap<>();
		RunCrosswikiTrial(5, scorer, multiDocProcessor, scores);
		RunCrosswikiTrial(10, scorer, multiDocProcessor, scores);
		RunCrosswikiTrial(30, scorer, multiDocProcessor, scores);
		RunCrosswikiTrial(100, scorer, multiDocProcessor, scores);
		for (Entry<Integer, CWResult> score : scores.entrySet()) {
			System.out.println(score.getKey() + ": " + score.getValue());
		}
	}
	
	private static class CWResult {
		double precision;
		long time;
		
		public CWResult(double prec, long time) {
			precision = prec;
			this.time = time;
		}
		
		@Override
		public String toString() {
			return "{" + precision + ", " + time + "}";
		}
	}
	
	private static void RunCrosswikiTrial(int maxCrosswiki, EvaluationScorer scorer, MultiDocumentProcessor processor, Map<Integer, CWResult> scores) {
		System.out.println("Processing Crosswiki: " + maxCrosswiki);
		WikiConnect.SetMaxCrosswikiEntries(maxCrosswiki);
		scorer.clearScores();

		// Setup docs
		SentenceDbDocFactory docs = new SentenceDbDocFactory();
		List<Integer> ids = new ArrayList<Integer>(15);
		for (int i = 0; i < 15; ++i) {
			ids.add(200 + i);
		}
		docs.AddDocIDs(ids);
		
		// Setup weights
		FeatureWeights weights = new FeatureWeights();
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, 1);
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 1);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING, 1);
		
		long start = System.currentTimeMillis();
		processor.ProcessDocuments(docs, weights);
		long duration = System.currentTimeMillis() - start;
		scores.put(maxCrosswiki, new CWResult(scorer.getPrecisionScore(), duration));
	}
}
