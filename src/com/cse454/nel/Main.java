package com.cse454.nel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;


public class Main {

	//private static final String sentencesFile = "sentences.entities";
    private static Object lock = new Object();
    private static int count = 0;
    private static int counter = 0;
    private static int NUM_DOCUMENTS = 100;
    private static boolean FINISHED_READING_DOCNAMES = false;
    private static int THREADS_WORKING = 0;
    private final static ThreadPoolExecutor executor =new ThreadPoolExecutor(16, 16, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

	public static void main(String[] args) throws Exception {

		DocPreProcessor preProcessor = new DocPreProcessor();
		Set<FeatureWeights> featureWeights = allPossibleWeights();

		DocumentProcessor processor = new DocumentProcessor(preProcessor);
		DocumentConnect docConnect = new DocumentConnect();
		List<Sentence> sentences = docConnect.getDocumentById(0);
		Map<Sentence, Map<FeatureWeights, String[]>> results =
				processor.ProcessDocument(featureWeights, sentences);

		Map<FeatureWeights, Double> scores = new HashMap<>();
		for (Sentence sentence : results.keySet()) {
			Map<FeatureWeights, String[]> entities = results.get(sentence);
			for (Entry<FeatureWeights, String[]> entry : entities.entrySet()) {
				double score = FeatureWeightScorer.score(sentence.getGold(), entry.getValue());
				if (scores.containsKey(entry.getKey())) {
					scores.put(entry.getKey(), scores.get(entry.getKey()) + score);
				} else {
					scores.put(entry.getKey(), score);
				}
			}
		}

		double max = 0;
		FeatureWeights chosenWeights = null;
		for (FeatureWeights weights : scores.keySet()) {
			double score = scores.get(weights);
			if (score > max) {
				max = score;
				chosenWeights = weights;
			}
		}
		System.out.println(chosenWeights);
		System.out.println("Score: " + max);
		System.exit(0);
	}

	public static Set<FeatureWeights> allPossibleWeights() {
		Set<FeatureWeights> featureWeights = new HashSet<>();
	    for (int i = 0; i <= 10; i++) { // all words weight
	    	for (int j = 0; j <= 10; j++) { // entity mention weight
	    		for (int k = 0; k <= 10; k++) { // entity wiki mention weight
	    			for (int l = 0; l <= 10; l++) { // entity wiki mention weight split
	    				for (int m = 0; m <= 10; m++) { // inlinks weight
	    					for (int n = 0; n <= 10; n++) {
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
