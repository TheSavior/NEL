package com.cse454.nel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.disambiguate.Disambiguator;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.EntityMentionHistogramFeatureGenerator;
import com.cse454.nel.features.EntityWikiMentionHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.mysql.WikiConnect;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.CrossWikiSearcher;

public class EvaluationDocumentProcessor {

	private final DocPreProcessor preprocessor;
	private final WikiConnect mWikiDb;

	public EvaluationDocumentProcessor(DocPreProcessor preprocessor) {
		this.preprocessor = preprocessor;
		try {
			this.mWikiDb = new WikiConnect();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<Sentence, Map<FeatureWeights, String[]>> evaluateFeatureWeights(PrintStream timeLog, List<Sentence> sentences, Set<FeatureWeights> weights) {
		try {
			List<EntityMention> mentions = ProcessDocumentFeatures(timeLog, weights, sentences);
			return ScoreWeightTrials(timeLog, sentences, mentions, weights);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public PrintStream EnabledPrintStream(final PrintStream dst) {
		return new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (dst != null) {
					System.out.write(b);
				}
			}
		});
	}

	public List<EntityMention> ProcessDocumentFeatures(PrintStream timeLog, Set<FeatureWeights> weightTrials, List<Sentence> sentences) throws Exception {

		// Pick which features we need to generate
		Set<String> features = new HashSet<String>();
		for (FeatureWeights weights : weightTrials) {
			for (Entry<String, Double> weight : weights.entrySet()) {
				features.add(weight.getKey());
			}
		}

		// Generate Features
		return extractMentionsAndGenerateFeatures(timeLog, sentences, features);
	}

	public List<EntityMention> extractMentionsAndGenerateFeatures(PrintStream timeLog, List<Sentence> sentences, Set<String> features) throws Exception {
		// Extract entity mentions
		timeLog.println("Extraact entity mentions");
		long start = System.currentTimeMillis();
		AbstractEntityExtractor extractor = new NerExtractor();
		List<EntityMention> mentions = extractor.extract(sentences);
		long end = System.currentTimeMillis();
		long duration = end - start;
		timeLog.println("Extraact entity mentions: " + duration);

		// Generate candidate entities
		timeLog.println("Generate candidate entities");
		start = System.currentTimeMillis();
		AbstractSearcher searcher = new CrossWikiSearcher(mWikiDb);//new BasicSearcher(wikiDb);
		for (EntityMention mention : mentions) {
			searcher.GetCandidateEntities(mention);
		}
		end = System.currentTimeMillis();
		duration = end - start;
		timeLog.println("Generate candidate entities: " + duration);

		timeLog.println("Generating Features");
		start = System.currentTimeMillis();

		// Setup feature generators
		Map<String, FeatureGenerator> featureGenerators = new HashMap<String, FeatureGenerator>();
		AllWordsHistogramFeatureGenerator feature1 = new AllWordsHistogramFeatureGenerator(mWikiDb, sentences);
		featureGenerators.put(feature1.GetFeatureName(), feature1);

		EntityMentionHistogramFeatureGenerator feature2 = new EntityMentionHistogramFeatureGenerator(mWikiDb, sentences, mentions);
		featureGenerators.put(feature2.GetFeatureName(), feature2);

		EntityWikiMentionHistogramFeatureGenerator feature3 = new EntityWikiMentionHistogramFeatureGenerator(mWikiDb, sentences, mentions, preprocessor, false);
		featureGenerators.put(feature3.GetFeatureName(), feature3);

		EntityWikiMentionHistogramFeatureGenerator feature4 = new EntityWikiMentionHistogramFeatureGenerator(mWikiDb, sentences, mentions, preprocessor, true);
		featureGenerators.put(feature4.GetFeatureName(), feature4);

		InLinkFeatureGenerator feature5 = new InLinkFeatureGenerator(mWikiDb);
		featureGenerators.put(feature5.GetFeatureName(), feature5);

		// Generate features
		for (String feature : features) {
			if (feature == CrossWikiSearcher.FEATURE_STRING) {
				continue;
			}

			timeLog.println("\t" + feature);
			FeatureGenerator generator = featureGenerators.get(feature);
			if (generator == null) {
				throw new Exception("No Feature Named '" + feature + "'");
			}

			long substart = System.currentTimeMillis();
			for (EntityMention mention : mentions) {
				generator.GenerateFeatures(mention);
			}
			timeLog.println("\t" + feature + ": " + (System.currentTimeMillis() - substart));
		}
		end = System.currentTimeMillis();
		duration = end - start;
		timeLog.println("Generating Features: " + duration);

		return mentions;
	}

	private Map<Sentence, Map<FeatureWeights, String[]>> ScoreWeightTrials(PrintStream timeLog, List<Sentence> sentences, List<EntityMention> mentions, Set<FeatureWeights> weightTrials) {
		// Go through all weight trials
		Disambiguator disambiguator = new Disambiguator();
		Map<Integer, List<EntityMention>> sentenceEntities = listEntityMentionBySentenceID(mentions);

		timeLog.println("Generate entity sentences");
		long start = System.currentTimeMillis();
		SortedMap<Sentence, Map<FeatureWeights, String[]>> results = new TreeMap<>();
		for (FeatureWeights weights : weightTrials) {
			// Disambiguate
			disambiguator.disambiguate(mentions, weights);

			// Collate data per sentence
			for (Sentence sentence : sentences) {
				List<EntityMention> sentMentions = sentenceEntities.get(sentence.getSentenceId());

				// Initialize entities string
				String[] ents = new String[sentence.getTokens().length];
				for (int i = 0; i < ents.length; ++i) {
					ents[i] = "0";
				}

				// Process mentions
				if (sentMentions != null) {
					for (EntityMention mention : sentMentions) {
						if (mention.chosenEntity != null) {
							for (int i = 0; i < mention.numToks; ++i) {
								ents[i + mention.tokStart] = mention.chosenEntity.wikiTitle;
							}
						}
					}
				}

				// Add to results
				Map<FeatureWeights, String[]> sentResults = results.get(sentence);
				if (sentResults == null) {
					sentResults = new HashMap<>();
					results.put(sentence, sentResults);
				}

				sentResults.put(weights, ents);
			}
		}
		long end = System.currentTimeMillis();
		long duration = end - start;
		timeLog.println("Generating entity sentences: " + duration);

		return results;
	}

	private Map<Integer, List<EntityMention>> listEntityMentionBySentenceID(List<EntityMention> mentions) {
		Map<Integer, List<EntityMention>> sentenceEntities = new HashMap<>();

		for (EntityMention entMent : mentions) {
			Integer id = Integer.valueOf(entMent.sentenceID);
			if (sentenceEntities.containsKey(id)) {
				sentenceEntities.get(id).add(entMent);
			} else {
				List<EntityMention> entityList = new ArrayList<EntityMention>();
				entityList.add(entMent);
				sentenceEntities.put(id, entityList);
			}
		}
		return sentenceEntities;
	}
}
