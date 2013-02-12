package com.cse454.nel;

import java.util.Map;

import com.cse454.warmup.sf.SFConstants;
import com.cse454.warmup.sf.retriever.ProcessedCorpus;

public class Main {
	public static void main(String[] args) {
		try {
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
					System.out.print(entity + " " + span +"\n");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
