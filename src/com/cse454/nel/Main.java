package com.cse454.nel;

import com.cse454.nel.extract.HistogramExtractor;
import com.cse454.warmup.sf.retriever.ProcessedCorpus;

public class Main {

	public static final String sentencesFile = "sentences.entities";

	public static void main(String[] args) {

		try {
			ProcessedCorpus corpus = new ProcessedCorpus();
			HistogramExtractor extractor = new HistogramExtractor(corpus);
			extractor.extract(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		BufferedWriter fos = null;
		AbstractEntityExtractor extractor;
		try {
			fos = new BufferedWriter(new FileWriter(sentencesFile));
			extractor = new EntityExtractor();
			ProcessedCorpus corpus = new ProcessedCorpus();
			Map<String, String> annotations = null;
			int c = 0;
			while (corpus.hasNext()) {
				annotations = corpus.next();
				if (++c % 100 == 0) {
					System.err.print("finished reading " + c + " lines\r");
					break;
				}
				String id = annotations.get(SFConstants.TOKENS).split("\t")[0];
				StringBuffer outSentence = new StringBuffer(id + "\t");
				List<Entity> entities = extractor.extract(annotations);
				for (int i = 0; i < entities.size(); i++) {
					outSentence.append(entities.get(i).toString());
					if (i < entities.size() - 1) {
						outSentence.append("\t");
					}
				}
				outSentence.append("\n");
				fos.write(outSentence.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		*/
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
