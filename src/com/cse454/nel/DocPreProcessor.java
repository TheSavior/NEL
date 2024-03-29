package com.cse454.nel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cse454.nel.dataobjects.Sentence;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * This class is used to generate {@link Sentence} objects from raw text.
 *
 */
public class DocPreProcessor {

	private final Object lock = new Object();

	private AbstractSequenceClassifier<CoreLabel> classifier;

	public DocPreProcessor() throws ClassCastException, ClassNotFoundException, IOException {
	      String serializedClassifier = "english.conll.4class.distsim.crf.ser.gz";
	      classifier = CRFClassifier.getClassifier(serializedClassifier);
	}

	public List<Sentence> ProccessArticle(String text) {
		List<Sentence> sentences = new ArrayList<Sentence>();

		List<List<CoreLabel>> out = null;
		synchronized (lock) {
			out = classifier.classify(text);
		}
		int sentenceCounter = 0;
		for (List<CoreLabel> sentence : out) {
			int numWords = sentence.size();
			String[] tokens = new String[numWords];
			String[] ner = new String[numWords];

			for (int i = 0; i < numWords; ++i) {
				CoreLabel word = sentence.get(i);
				tokens[i] = word.word();
				ner[i] = word.get(AnswerAnnotation.class);
			}
			sentences.add(new Sentence(sentenceCounter, tokens, ner));
			++sentenceCounter;
		}

		return sentences;
	}

}
