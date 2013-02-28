package com.cse454.nel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WikiConnect extends MySQLConnect {

	private static String defaultDB = "wikidb";
	
	private Map<String, String> page_latestCache; // page_id -> page_latest
	private Map<String, String> page_textCache; // page_latest -> text

	public WikiConnect() throws SQLException {
        super(defaultUrl, defaultDB);
        
        page_latestCache = new HashMap<String, String>();
        page_textCache = new HashMap<String, String>();
	}

	/**
	 * 
	 * @param query
	 * @param pages a map of <page_id, page_latest>
	 * @param redirects
	 * @throws Exception
	 */
	public void GetPages(String query, Map<String, String> pages, Set<String> redirects) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_id, page_is_redirect, page_latest FROM page WHERE page_title LIKE '"+query.replaceAll("'", "''")+"' AND page_namespace = 0;");

			while (rs.next()) {
				// If this is a redirect
				if (!rs.getBoolean(2)) {
					pages.put(rs.getString(1), rs.getString(3));
				} else if (redirects != null) {
					redirects.add(rs.getString(1));
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}
		}
	}
	
	/**
	 * Does not sanitize pageID
	 * @param pages a map of <page_id, page_latest>
	 * @param pageID
	 * @throws Exception
	 */
	public void GetPageLinks(Map<String, String> pages, String pageID) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT pl_title FROM pagelinks WHERE pl_from = "+pageID+" AND pl_namespace = 0;");

			while (rs.next()) {
				GetPages(rs.getString(1), pages, null);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}
		}
	}
	
	public String GetArticleName(String pageID) throws Exception {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_title FROM page WHERE page_id = "+pageID+";");
			
			if (rs.next()) {
				return rs.getString(1);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
	}
	
	private String replaceWhileEffective(String str, String rgx, String replace) {
		String oldStr;
		do {
			oldStr = str;
			str = str.replaceAll(rgx, replace);
		} while (str != oldStr);
		return str;
	}
	
	public String GetCleanedWikiText(String pageID) throws Exception {
		String text = GetWikiText(pageID);
		
		text = text.replaceAll("#REDIRECT", "");			// Redirects
		text = text.replaceAll("(?s:\\{\\|.*?\\|\\})", ""); // Tables {| ... |}
		text = replaceWhileEffective(text, "(?s:\\{\\{(?:(?:[^\\{])|(?:\\{(?!\\{)))+?\\}\\})", ""); // {{ ... }} Directives

		String noDoubleBracketRgx = "(?:(?:[^\\[\\|])|(?:\\[(?!\\[)))+?";	
		String innerDoubleBracketRgx = 
				"(?s:\\[\\[" +				// opening brackets
					"(?:" + noDoubleBracketRgx + "\\|)*?" + // stuff before visible text
					"(" + noDoubleBracketRgx + ")\\|?" +	// visible text
				"\\]\\])";
		text = replaceWhileEffective(text, innerDoubleBracketRgx, "$1"); // [[ ... ]] links
		text = text.replaceAll("<[^>]+>", ""); // html
		
		return text;
	}
	
	/**
	 * Returns the wiki article text for the page with the revision 'page_latest'
	 * @param page_latest
	 * @return
	 * @throws Exception 
	 */
	public String GetWikiTextFromPageLatest(String page_latest) throws Exception {
		if (page_textCache.containsKey(page_latest)) {
			return page_textCache.get(page_latest);
		}
		
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT rev_text_id FROM revision WHERE rev_id = " + page_latest + ";");
		
			if (rs.next()) {
				String rev_text_id = rs.getString(1);
			
				rs.close();
				rs = st.executeQuery("SELECT old_text FROM text WHERE old_id = " + rev_text_id + ";");
				if (rs.next()) {
					String text = rs.getString(1);
					page_textCache.put(page_latest, text);
					return text;
				} else {
					throw new Exception("No Text For Page With Old ID = " + rev_text_id);
				}
			} else {
				throw new Exception("No Page With Rev ID: " + page_latest);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
	}

	/**
	 * Returns the wiki article text for the given id. Does not sanitize pageID
	 * @param pageID
	 * @return
	 * @throws Exception
	 */
	public String GetWikiText(String pageID) throws Exception {
		if (page_latestCache.containsKey(pageID)) {
			return GetWikiTextFromPageLatest(page_latestCache.get(pageID));
		}
		
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_latest FROM page WHERE page_id = "+pageID+";");

			if (rs.next()) {
				String page_latest = rs.getString(1);
				page_latestCache.put(pageID, page_latest);
				return GetWikiTextFromPageLatest(page_latest);
			} else {
				throw new Exception("No Page With ID: " + pageID);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}

			if (rs != null) {
				rs.close();
			}
		}
	}

}
