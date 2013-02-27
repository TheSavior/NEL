package com.cse454.nel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.cse454.warmup.sf.SFConstants;
import com.cse454.warmup.sf.retriever.ProcessedCorpus;

public class PopulateSentences {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("Need to provide processed doc path");
		}
		String docPath = args[0];
		String[] dataTypes = {SFConstants.ARTICLE_IDS, SFConstants.STANFORDNER, SFConstants.TOKENS};
		ProcessedCorpus corpus = new ProcessedCorpus(docPath, dataTypes);
		new ProcessedCorpus(docPath, dataTypes);
		Map<String, String> annotations = null;
		PopulateConnect connect = new PopulateConnect();
		int count = 0;
		while (corpus.hasNext()) {
			annotations = corpus.next();
			if (count < 9315225) {
				count++;
				continue;
			}
			String[] split;
			int id = -1;
			int docId = -1;
			String tokens = null;
			String ner = null;
			split = annotations.get(SFConstants.TOKENS).split("\t");
			if (split.length == 2) {
				try {
					id = Integer.valueOf(split[0]).intValue();
				} catch (NumberFormatException e) {
					System.err.println("Error parsing id: " + split[0]);
					continue;
				}
			} else {
				System.err.println("tried to split tokens to get id, malformed format: " +
						annotations.get(SFConstants.TOKENS));
				System.err.println("skipping this sentence...");
				continue;
			}
			split = annotations.get(SFConstants.ARTICLE_IDS).split("\t");
			if (split.length >= 2) {
				docId = Integer.valueOf(split[1]).intValue();
			} else {
				System.err.println("Malformed articleIDs format: " +
						annotations.get(SFConstants.ARTICLE_IDS) +
						"\nunable to get docId");
			}
			split = annotations.get(SFConstants.TOKENS).split("\t");
			if (split.length == 2) {
				tokens = split[1];
			}
			split = annotations.get(SFConstants.STANFORDNER).split("\t");
			if (split.length == 2) {
				ner = split[1];
			}

			connect.populateSentenceRow(Integer.valueOf(id).intValue(), docId, tokens, ner);
		}
	}

	static class PopulateConnect extends MySQLConnect {

		public PopulateConnect() throws SQLException {
			super(defaultUrl, "sentences");
		}

		public void populateSentenceRow(int id, int docId, String tokens, String ner) throws Exception {
			PreparedStatement st = null;

			try {
				String sql = "INSERT INTO sentences (sentenceID, docID, tokens, ner) VALUES (?,?,?,?);";
				st = connection.prepareStatement(sql);
				st.setInt(1, id);
				st.setInt(2, docId);
				st.setString(3, tokens);
				st.setString(4, ner);
				st.executeUpdate();
			} catch (Exception e) {
				throw e;
			} finally {
				if (st != null) {
					st.close();
				}
			}
		}
	}
}
