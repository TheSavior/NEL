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
		ProcessedCorpus corpus = new ProcessedCorpus(docPath);
		Map<String, String> annotations = null;
		PopulateConnect connect = new PopulateConnect();
		while (corpus.hasNext()) {
			annotations = corpus.next();
			String id = annotations.get(SFConstants.TOKENS).split("\t")[0];
			String[] split = annotations.get(SFConstants.META).split("\t");
			String docId = split[2];
			String tokens = annotations.get(SFConstants.TOKENS).split("\t")[1];
			String ner = annotations.get(SFConstants.STANFORDNER).split("\t")[1];

			connect.populateSentenceRow(Integer.valueOf(id).intValue(), docId, tokens, ner);
		}
	}

	static class PopulateConnect extends WikiConnect {

		private static String sentencesDb = "jdbc:mysql://54.244.244.3:3306/sentences";

		public PopulateConnect() throws SQLException {
			super(sentencesDb, defaultUser, defaultPassword);
		}

		public void populateSentenceRow(int id, String docId, String tokens, String ner) throws Exception {
			PreparedStatement st = null;

			try {
				String sql = "INSERT INTO sentences (sentenceID, docID, tokens, ner) VALUES (?,?,?,?);";
				st = connection.prepareStatement(sql);
				st.setInt(1, id);
				st.setString(2, docId);
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
