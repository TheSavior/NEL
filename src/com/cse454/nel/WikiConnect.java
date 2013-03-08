package com.cse454.nel;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	 * @param pages <page id, page title>
	 * @param redirects <page id, page title>
	 * @throws Exception
	 */
	public void GetPages(String query, final Map<String, String> pages, final Map<String, String> redirects) throws SQLException {
		ExecuteQuery(
				"SELECT page_id, page_title, page_is_redirect FROM page WHERE page_title LIKE '"+query.replaceAll("'", "''")+"' AND page_namespace = 0;",
				new QueryResponder<Void>() {
					public Void Result(ResultSet result) throws SQLException {
						while (result.next()) {
							// If this is a redirect
							String id = result.getString(1);
							String title = result.getString(2);
							if (!result.getBoolean(3)) {
								pages.put(id, title);
							} else if (redirects != null) {
								redirects.put(id, title);
							}
						}
						return null;
					}
				}
		);
	}

	/**
	 * Does not sanitize pageID
	 * @param pages a set of page id's
	 * @param pageID
	 * @throws Exception
	 */
	public void GetPageLinks(final Set<String> pages, String pageID) throws SQLException {
		ExecuteQuery(
				"SELECT pl_title FROM pagelinks WHERE pl_from = "+pageID+" AND pl_namespace = 0;",
				new QueryResponder<Void>() {
					public Void Result(ResultSet result) throws SQLException {
						while (result.next()) {
							pages.add(result.getString(1));
						}
						return null;
					}
				}
		);
	}

	public String GetArticleName(String pageID) throws SQLException {
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

	public String GetCleanedWikiText(String pageID) throws SQLException {
		String text = GetWikiText(pageID);
		// System.out.println(text);

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
	 * Returns the number of in-links to an article with the given title
	 * @param title the article title (exact)
	 */
	public int GetInlinks(String title) throws SQLException {
		return ExecuteQuery(
				"SELECT COUNT(pl_from) FROM pagelinks WHERE pl_title = '" + title.replaceAll("'", "''") + "'",
				new QueryResponder<Integer>() {
					public Integer Result(ResultSet result) throws SQLException {
						if (result.next()) {
							return result.getInt(1);
						} else {
							throw new SQLException("No count returned.");
						}
					}
				}
		);
	}

	/**
	 * Returns the wiki article text for the given id. Does not sanitize pageID
	 * @param pageID
	 * @return
	 * @throws Exception
	 */
	public String GetWikiText(final String pageTitle) throws SQLException {
		if (page_textCache.containsKey(pageTitle)) {
			return page_textCache.get(pageTitle);
		}

		String query = "SELECT text.old_text " +
					   "FROM page " +
					   "	LEFT JOIN revision " +
					   "		ON page.page_latest = revision.rev_id " +
					   "	LEFT JOIN text " +
					   "		ON text.old_id = revision.rev_text_id " +
					   "WHERE page.page_namespace = 0 and page.page_title = '" + pageTitle + "'";
		return ExecuteQuery(query,
					new QueryResponder<String>() {
						public String Result(ResultSet rs) throws SQLException {
							if (rs.next()) {
								String text = rs.getString(1);
								page_textCache.put(pageTitle, text);
								return text;
							} else return null;
						}
					});
	}

}
