package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.dataobjects.Sentence;
import com.cse454.nel.disambiguate.Disambiguator;
import com.cse454.nel.document.AbstractDocument;
import com.cse454.nel.document.SimpleDocument;
import com.cse454.nel.extract.AbstractEntityExtractor;
import com.cse454.nel.extract.NerExtractor;
import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.mysql.WikiConnect;
import com.cse454.nel.search.AbstractSearcher;
import com.cse454.nel.search.CrossWikiSearcher;

/**
 * This class is responsible for the core functionality of the NEL system.
 *
 * TODO: change passing of List<Sentence> to document
 * TODO: create RawTextDocument for {@link #processDocument(String)}
 *
 */
public class DocumentProcessor {

	private static FeatureWeights WEIGHTS = new FeatureWeights();
	static {
		WEIGHTS.setFeature(InLinkFeatureGenerator.FEATURE_STRING, 1);
		WEIGHTS.setFeature(CrossWikiSearcher.FEATURE_STRING, 1);
		WEIGHTS.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_STRING,
				1);
	}

	private final DocPreProcessor mPreProcessor;
	private final WikiConnect mWikiDb;

	/**
	 * Constructs a new {@link DocumentProcessor}
	 * @param preprocessor Used to when linking raw text in {@link #processDocument(String)}
	 */
	public DocumentProcessor(DocPreProcessor preprocessor) {
		this.mPreProcessor = preprocessor;
		try {
			this.mWikiDb = new WikiConnect();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set the {@link FeatureWeights}s used during disambiguation.
	 * @param weights The {@link FeatureWeights} to use during this document processing.
	 */
	public void setFeatureWeights(FeatureWeights weights) {
		WEIGHTS = weights;
	}

	/**
	 * Parses text into sentences, extracts entities from them and returns a list
	 * of {@link Sentence}s with their entities field filled.
	 * @throws Exception
	 */
	public AbstractDocument processDocument(String text) throws Exception {
		List<Sentence> sentences = mPreProcessor.ProccessArticle(text);
		AbstractDocument doc = new SimpleDocument("simple", sentences);
		processDocument(doc);
		return doc;
	}

	/**
	 * Extracts entities from the given {@link AbstractDocument} and
	 * mutates its inner list of {@link Sentence}s
	 * @param document Document to extract entities from.
	 * @return The same document object given, with it's inner list of sentences changed.
	 * @throws Exception
	 */
	public void processDocument(AbstractDocument document) throws Exception {
		processDocumentHelper(document);
	}

	/**
	 * Changes out param sentences by adding their LinkedEntities field.
	 * @throws Exception
	 * @throws IllegalArgumentException
	 */
	private void processDocumentHelper(AbstractDocument document) throws Exception {
		// Extract entity mentions from sentences
		List<EntityMention> mentions = extractEntityMentions(document.GetSentences());
		document.setMentions(mentions);
		// Generate candidate entities for each mention
		generateCandidateEntities(mentions);

		// Generate feature scores for each mention
		Set<String> features = new HashSet<>();
		for (Entry<String, Double> weight : WEIGHTS.entrySet()) {
			features.add(weight.getKey());
		}
		generateFeatureScores(mentions, document.GetSentences(), features);

		// Disambiguate
		disambiguate(mentions, document.GetSentences());
	}

	/**
	 * Extracts {@link EntityMention}s from the list of {@link Sentence}s.
	 */
	private List<EntityMention> extractEntityMentions(List<Sentence> sentences) {
		AbstractEntityExtractor extractor = new NerExtractor();
		return extractor.extract(sentences);
	}

	private void generateCandidateEntities(Collection<EntityMention> mentions) {
		AbstractSearcher searcher = new CrossWikiSearcher(mWikiDb);
		for (EntityMention mention : mentions) {
			try {
				searcher.GetCandidateEntities(mention);
			}	catch (SQLException e) {
				throw new RuntimeException(e);
			}	catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generates features for each candidate in each entity mention.
	 * @throws SQLException If there's a problem connecting to the DB
	 * @throws IllegalArgumentException if there's a feature in features that doesn't exist
	 */
	private void generateFeatureScores(List<EntityMention> mentions, List<Sentence> sentences, Set<String> features)
			throws IllegalArgumentException {
		Map<String, FeatureGenerator> featureGenerators = new HashMap<String, FeatureGenerator>();

		AllWordsHistogramFeatureGenerator allwords = new AllWordsHistogramFeatureGenerator(mWikiDb, sentences);
		featureGenerators.put(allwords.GetFeatureName(), allwords);

		InLinkFeatureGenerator inlinks = new InLinkFeatureGenerator(mWikiDb);
		featureGenerators.put(inlinks.GetFeatureName(), inlinks);

		// Generate features
		for (String feature : features) {
			if (feature == CrossWikiSearcher.FEATURE_STRING) {
				continue;
			}

			FeatureGenerator generator = featureGenerators.get(feature);
			if (generator == null) {
				System.err.println("No Feature Named '" + feature + "'");
			}

			for (EntityMention mention : mentions) {
				try {
					generator.GenerateFeatures(mention);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void disambiguate(List<EntityMention> mentions, List<Sentence> sentences) {
		Disambiguator disambiguator = new Disambiguator();
		Map<Integer, List<EntityMention>> sentenceEntities = listEntityMentionBySentenceID(mentions);
		// Disambiguate
		disambiguator.disambiguate(mentions, WEIGHTS);

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
		}
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
