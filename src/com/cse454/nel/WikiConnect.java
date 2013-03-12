package com.cse454.nel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WikiConnect extends MySQLConnect {

	private static WikiConnect instance = null;
	
	private static String defaultDB = "wikidb";

	private Map<String, String> page_textCache; // page_latest -> text

	private WikiConnect() throws SQLException {
        super(defaultUrl, defaultDB);

        page_textCache = new HashMap<String, String>();
	}
	
	public static WikiConnect getInstance() throws SQLException {
      if(instance == null) {
    	  instance = new WikiConnect();
      }
      return instance;
   }

	public List<CrossWikiData> GetCrossWikiDocs(String entityMention) throws Exception {

		String query = "Select * from crosswiki where mention = ?";
		PreparedStatement st = null;
		ResultSet rs = null;

		List<CrossWikiData> crossWikiData = new ArrayList<CrossWikiData>();
		try {
			st = connection.prepareStatement(query);
			st.setString(1, entityMention);
			rs = st.executeQuery();
			while (rs.next()) {
				crossWikiData.add(new CrossWikiData(rs.getString(1), rs.getDouble(2), rs.getString(3)));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (st != null)
				st.close();
			if (rs != null)
				rs.close();
		}
		return crossWikiData;
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


	private String ReplaceWhileEffective(String str, String rgx, String replace) {
		String oldStr;
		do {
			oldStr = str;
			str = str.replaceAll(rgx, replace);
		} while (str != oldStr);
		return str;
	}

	/**
	 *
	 * @param str
	 * @param startStr
	 * @param endStr must be same length as startStr (or exceptions will ensue)
	 * @return
	 */
	private String RemoveRecursiveStruct(String str, String startStr, String endStr) {
		int tokLen = startStr.length();
		String ret = "";

		while (true) {
			int start = str.indexOf(startStr);
			if (start >= 0) {
				ret += str.substring(0, start);
				str = str.substring(start);

				// Now find the end of this struct
				int depth = 1;
				int i = startStr.length();
				int len = str.length() - tokLen;
				while (i <= len) {
					String substr = str.substring(i, i+tokLen);
					if (substr.equals(startStr)) {
						++depth;
					} else if (substr.equals(endStr)) {
						--depth;
						if (depth == 0) {
							i += tokLen;
							break;
						}
					}
					++i;
				}

				// We have found the whole struct, remove it.
				str = str.substring(i);

			} else {
				ret += str;
				break;
			}
		}


		return ret;
	}

	public String GetCleanedWikiText(String pageID) throws SQLException {
		String text = GetWikiText(pageID);

		text = text.replaceAll("#REDIRECT", "");			// Redirects
		text = text.replaceAll("(?s:\\{\\|.*?\\|\\})", ""); // Tables {| ... |}
		text = RemoveRecursiveStruct(text, "{{", "}}");

		String noDoubleBracketRgx = "(?:(?:[^\\[\\|])|(?:\\[(?!\\[)))+?";
		String innerDoubleBracketRgx =
				"(?s:\\[\\[" +				// opening brackets
					"(?:" + noDoubleBracketRgx + "\\|)*?" + // stuff before visible text
					"(" + noDoubleBracketRgx + ")\\|?" +	// visible text
				"\\]\\])";
		text = ReplaceWhileEffective(text, innerDoubleBracketRgx, "$1"); // [[ ... ]] links
		text = text.replaceAll("<[^>]+>", ""); // html

		return text;
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
					   "WHERE page.page_namespace = 0 and page.page_title = ?";
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = connection.prepareStatement(query);
			st.setString(1, pageTitle);
			rs = st.executeQuery();
			if (rs.next()) {
				String text = rs.getString(1);
				page_textCache.put(pageTitle, text);
				return text;
			}
			return null;
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
