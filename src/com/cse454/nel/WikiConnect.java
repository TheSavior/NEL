package com.cse454.nel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WikiConnect extends MySQLConnect {

	private static String defaultDB = "wikidb";
	
	private Map<String, String> page_textCache; // page_latest -> text

	public WikiConnect() throws SQLException {
        super(defaultUrl, defaultDB);
        
        page_textCache = new HashMap<String, String>();
	}

	/**
	 * 
	 * @param query
	 * @param pages a set of page id's
	 * @param redirects
	 * @throws Exception
	 */
	public void GetPages(String query, Set<String> pages, Set<String> redirects) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_id, page_is_redirect FROM page WHERE page_title LIKE '"+query.replaceAll("'", "''")+"' AND page_namespace = 0;");

			while (rs.next()) {
				// If this is a redirect
				if (!rs.getBoolean(2)) {
					pages.add(rs.getString(1));
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
	 * @param pages a set of page id's
	 * @param pageID
	 * @throws Exception
	 */
	public void GetPageLinks(Set<String> pages, String pageID) throws Exception {
		Statement st = null;
		ResultSet rs = null;
		// TODO:
		/*
		select page.page_id
		from pagelinks
			inner join page
				on page.page_title = pagelinks.pl_title
		where pagelinks.pl_from = 8531
		and page.page_namespace = 0
		and page.page_is_redirect = 0;
		
		select page.page_id from pagelinks inner join page on page.page_title = pagelinks.pl_title where pagelinks.pl_from = 8531 and page.page_namespace = 0 and page.page_is_redirect = 0;
		*/
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
		return ExecuteQuery(
					"SELECT page_title FROM page WHERE page_id = " + pageID,
					new QueryResponder<String>() {
						public String Result(ResultSet result) throws SQLException {
							if (result.next())
								return result.getString(1);
							else
								return null;
						}
					});
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
		System.out.println(text);
		
		text = text.replaceAll("#REDIRECT", "");			// Redirects
		text = text.replaceAll("(?s:\\{\\|.*?\\|\\})", ""); // Tables {| ... |}
	//	text = replaceWhileEffective(text, "(?s:\\{\\{(?:(?:[^\\{])|(?:\\{(?!\\{)))+?\\}\\})", ""); // {{ ... }} Directives TODO: Stack overflowing!

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
	 * Returns the wiki article text for the given id. Does not sanitize pageID
	 * @param pageID
	 * @return
	 * @throws Exception
	 */
	public String GetWikiText(String pageID) throws Exception {
		if (page_textCache.containsKey(pageID)) {
			return page_textCache.get(pageID);
		}

		String query = "SELECT text.old_text " +
					   "FROM page " +
					   "	LEFT JOIN revision " +
					   "		ON page.page_latest = revision.rev_id " +
					   "	LEFT JOIN text " +
					   "		ON text.old_id = revision.rev_text_id " +
					   "WHERE page.page_namespace = 0 and page.page_id = " + pageID;
		return ExecuteQuery(query,
					new QueryResponder<String>() {
						public String Result(ResultSet rs) throws SQLException {
							if (rs.next())
								return rs.getString(1);
							else return null;
						}
					});
	}

}
