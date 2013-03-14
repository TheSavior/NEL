package com.cse454.nel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cse454.nel.document.SentenceDbDocFactory;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.scoring.EvaluationScorer;
import com.cse454.nel.search.CrossWikiSearcher;


public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Must Specify max file to process");
			return;
		}

		// Prevent errors from standford ner
		final PrintStream err = System.err;

		System.setErr(new PrintStream(new OutputStream() {
			private StringBuffer buf = new StringBuffer();

			@Override
			public void write(int b) throws IOException {
				if (b == '\n') {
					String line = buf.toString();
					if (!(line.contains("edu.stanford.nlp.process.PTBLexer") || line.contains("WARNING: Untokenizable"))) {
						err.println(buf.toString());
					}
					buf.delete(0, buf.length());
				} else {
					buf.append((char)b);
				}
			}
		}));

		// Setup Documents
		// TODO: these could be loaded from a file
		int maxFile = Integer.parseInt(args[0]);
		List<Integer> docIDs = new ArrayList<>();
		for (int i = 0; i <= maxFile; ++i) {
			docIDs.add(i);
		}

		SentenceDbDocFactory docs = new SentenceDbDocFactory();
		docs.AddDocIDs(docIDs);

		// Setup Feature Weights
		Set<FeatureWeights> weightTrials = new HashSet<FeatureWeights>();

		// Scorer
		//FeatureWeightScorer scorer = new FeatureWeightScorer();
		EvaluationScorer scorer = new EvaluationScorer();
		MultiDocumentProcessor docProcessor = new MultiDocumentProcessor(Math.min(16, maxFile + 1));
		docProcessor.ProcessDocuments(docs, weightTrials, scorer);

		// Show scores
		System.out.println("Score: " + scorer.getTotalCorrect() + " / " + scorer.getTotalGold());
		/* for (Entry<FeatureWeights, Double> score : scorer.getScores().entrySet()) {
			System.out.println(score.getValue() + " => " + score.getKey());
		} */
	}

	public static Set<FeatureWeights> allPossibleWeights() {
		Set<FeatureWeights> featureWeights = new HashSet<>();
	    for (int i = 0; i <= 5; i++) { // all words weight
	    	for (int j = 0; j <= 5; j++) { // entity mention weight
	    		System.out.println("In feature: " + i * j * 10000);
	    		for (int k = 0; k <= 5; k++) { // entity wiki mention weight
	    			for (int l = 0; l <= 5; l++) { // entity wiki mention weight split
	    				for (int m = 0; m <= 5; m++) { // inlinks weight
	    					for (int n = 0; n <= 5; n++) {
	    						FeatureWeights weights = new FeatureWeights();
			    				weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_NAME, i);
			    				weights.setFeature(EntityMentionHistogramFeatureGenerator.FEATURE_STRING, j);
			    				weights.setFeature(EntityWikiMentionHistogramFeatureGenerator.FEATURE_STRING, k);
			    				weights.setFeature(EntityWikiMentionHistogramFeatureGenerator.FEATURE_STRING_SPLIT, l);
			    				weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, m);
			    				weights.setFeature(CrossWikiSearcher.FEATURE_STRING, n);
			    				featureWeights.add(weights);
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
	    return featureWeights;
	}

	public void getArgs(String[] args) {
		Map<String, String> options = new HashMap<String, String>();
		Map<String, String> doubleOptions = new HashMap<String, String>();
		List<String> argsList = new ArrayList<String>();
	    for (int i = 0; i < args.length; i++) {
	        switch (args[i].charAt(0)) {

	        case '-':
	            if (args[i].length() < 2)
	                throw new IllegalArgumentException("Not a valid argument: " + args[i]);
	            if (args[i].charAt(1) == '-') {
	                if (args[i].length() < 3)
	                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	                // --opt
	                String opt = args[i].substring(2, args[i].length());
	                // arg
	                if (args.length - 1 == i)
	                	throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                doubleOptions.put(opt, args[i+1]);
	                i++;
	            } else {
	                if (args.length-1 == i)
	                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                // -opt
	                options.put(args[i], args[i+1]);
	                i++;
	            }
	            break;
	        default:
	            // arg
	            argsList.add(args[i]);
	            break;
	        }
	    }
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
