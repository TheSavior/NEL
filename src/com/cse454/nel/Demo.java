package com.cse454.nel;

import java.util.List;

import com.cse454.nel.features.AllWordsHistogramFeatureGenerator;
import com.cse454.nel.features.FeatureWeights;
import com.cse454.nel.features.InLinkFeatureGenerator;
import com.cse454.nel.search.CrossWikiSearcher;


public class Demo {

	public static void main(String[] args) throws Exception {

		DocPreProcessor preProcessor = new DocPreProcessor();
		DocumentProcessor processor = new DocumentProcessor(preProcessor);

		// Here's the text to be linked (value to come from gui)
		String text = "";

		// Here's necessary feature weight input (values will come from gui)
		int crossWikiWeight = 1;
		int inLinkWeight = 1;
		int histogramWeight = 1;

		FeatureWeights weights = new FeatureWeights();
		weights.setFeature(InLinkFeatureGenerator.FEATURE_STRING, inLinkWeight);
		weights.setFeature(CrossWikiSearcher.FEATURE_STRING, crossWikiWeight);
		weights.setFeature(AllWordsHistogramFeatureGenerator.FEATURE_NAME, histogramWeight);

		// Process the document
		List<Sentence> results = processor.ProcessDocument(weights, text);

		// now you could compare each  { token | entity } in a scroll bar on the gui or something
	}
}
