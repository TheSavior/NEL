package com.cse454.nel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.cse454.warmup.sf.SFConstants;
import com.cse454.warmup.sf.retriever.ProcessedCorpus;

public class Main {

	public static final String sentencesFile = "sentences.entities";

	public static void main(String[] args) {

		BufferedWriter fos = null;

		try {
			fos = new BufferedWriter(new FileWriter(sentencesFile));
			ProcessedCorpus corpus = new ProcessedCorpus();
			Map<String, String> annotations = null;
			int c = 0;
			while (corpus.hasNext()) {
				annotations = corpus.next();
				if (++c % 100 == 0) {
					System.err.print("finished reading " + c + " lines\r");
					break;
				}

				String[] split;
				split = annotations.get(SFConstants.TOKENS).split("\t");
				String id = split[0];
				String[] tokens = split[1].split(" ");
				split = annotations.get(SFConstants.STANFORDNER).split("\t");
				String[] stanfordNer = split[1].split(" ");

				StringBuffer outSentence = new StringBuffer(id + "\t");

				int length = tokens.length;
				for (int i = 0; i < length; i++) {
					if (stanfordNer[i].length() == 1
							|| stanfordNer[i].equals("DATE")
							|| stanfordNer[i].equals("PERCENT")
							|| stanfordNer[i].equals("NUMBER")) {
						continue;
					}
					int startIndex = i;
					StringBuffer buffer = new StringBuffer(tokens[i]);
					while (i < length && stanfordNer[i].equals(stanfordNer[i + 1])) {
						i++;
						buffer.append(" " + tokens[i]);
					}
					String entity = buffer.toString();
					String span = startIndex + ":" + i;
					outSentence.append(entity + " " + span + "\t");
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
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
