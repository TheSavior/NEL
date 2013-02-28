package com.cse454.nel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class WikiConnect extends MySQLConnect {

	protected static String defaultDB = "wikidb";

	public WikiConnect() throws SQLException {
        super(defaultUrl, defaultDB);
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

	public void Search(String query) throws Exception {
		// Sanitize the query, and convert to wikipedia format (i.e. spaces become underscores)
		query = query.replace(' ', '_');

		// Get a list of matching pages
		Map<String, String> pages = new HashMap<String, String>();
		Set<String> redirects = new HashSet<String>();
		GetPages(query, pages, redirects);
		
		// Now find all (if any) disambiguation links.
		// 1. pull up disambiguation page
		Map<String, String> disam_pages = new HashMap<String, String>();
		GetPages(query + "_(disambiguation)", disam_pages, redirects);
		
		// 2. Extract links from the disam page
		for (Entry<String, String> entry : disam_pages.entrySet()) {
			//System.out.println("Disam: " + entry.getKey());
			GetPageLinks(pages, entry.getKey());
		}
		
		// Go through redirects and pull out all links
		while (!redirects.isEmpty()) {
			String redirect = redirects.iterator().next();
			GetPageLinks(pages, redirect);
			redirects.remove(redirect);
		}
		
		for (Entry<String, String> page : pages.entrySet()) {
			Statement st = null;
			ResultSet rs = null;
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_title FROM page WHERE page_id = "+page.getKey()+";");
			if (rs.next()) {
				System.out.println(page.getKey() + ": " + rs.getString(1));
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

	/**
	 * Returns the wiki article text for the given id. Does not sanitize pageID
	 * @param pageID
	 * @return
	 * @throws Exception
	 */
	public String GetWikiText(String pageID) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_latest FROM page WHERE page_id = "+pageID+";");

			if (rs.next()) {
				String page_latest = rs.getString(1);

				rs.close();
				rs = st.executeQuery("SELECT rev_text_id FROM revision WHERE rev_id = " + page_latest + ";");
				
				if (rs.next()) {
					String rev_text_id = rs.getString(1);
					
					rs.close();
					rs = st.executeQuery("SELECT old_text FROM text WHERE old_id = " + rev_text_id + ";");
					if (rs.next()) {
						return rs.getString(1);
					} else {
						throw new Exception("No Text For Page With Old ID = " + rev_text_id);
					}
				} else {
					throw new Exception("No Page With Rev ID: " + page_latest);
				}
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
