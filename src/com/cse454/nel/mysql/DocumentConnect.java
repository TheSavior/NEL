package com.cse454.nel.mysql;

import java.sql.*;
import java.util.*;

import com.cse454.nel.dataobjects.Sentence;

public class DocumentConnect extends MySQLConnect {

	public DocumentConnect() throws SQLException {
		super(defaultUrl, "sentences");
	}

	public void EntityUpdate(int sentenceId, String entityString) {
		String sql = "UPDATE sentences SET entities = ? where sentenceID = ?";
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement(sql);
			st.setString(1, entityString);
			st.setInt(2, sentenceId);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void SetGoldData(int sentenceId, String gold) {
		String sql = "UPDATE sentences SET gold = ? where sentenceID = ?";
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement(sql);
			st.setString(1, gold);
			st.setInt(2, sentenceId);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public String getArticleId(int docID) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = connection.prepareStatement("SELECT docName FROM sentences WHERE docID = ? LIMIT 1");
			st.setInt(1, docID);
			rs = st.executeQuery();

			rs.first();
			String articleID = rs.getString("docName");

			return articleID;
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
	}

	public List<Sentence> getDocumentById(int docID) throws SQLException {
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement("SELECT sentenceID, tokens, ner, gold FROM sentences WHERE docID = ? ORDER BY sentenceID");
			st.setInt(1,docID);
		} catch (Exception e) {
			throw e;
		}
		return getDocument(st);
	}

	public List<Sentence> getDocumentByName(String name) throws SQLException {
		PreparedStatement st = null;
		try {
			st = connection.prepareStatement("SELECT sentenceID, tokens, ner, gold FROM sentences WHERE docName = ? ORDER BY sentenceID");
			st.setString(1, name);
		} catch (Exception e) {
			throw e;
		}
		return getDocument(st);
	}

	public List<Sentence> getDocument(PreparedStatement st) throws SQLException {
		ResultSet rs = null;

		try {
			rs = st.executeQuery();

			List<Sentence> sentences = new ArrayList<Sentence>();
			while (rs.next()) {
				Sentence sentence = new Sentence(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
				sentences.add(sentence);
			}

			return sentences;
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
	}
	
	public SortedMap<Integer, String> getGoldSentences() throws SQLException {
		SortedMap<Integer, String> map = new TreeMap<Integer, String>();
		
		ResultSet rs = null;
		PreparedStatement st = null;

		try {		
			try {
				st = connection.prepareStatement("SELECT sentenceID, gold FROM sentences WHERE !isnull(gold) ORDER BY sentenceID;");
			} catch (Exception e) {
				throw e;
			}
			
			rs = st.executeQuery();
			
			while (rs.next()) {
				map.put(rs.getInt("sentenceID"), rs.getString("gold"));
			}

			return map;
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
	}
}
