package com.cse454.nel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class WikiConnect extends MySQLConnect {

	protected static String defaultDB = "wikidb";

	public WikiConnect() throws SQLException {
        super(defaultUrl, defaultDB);
	}

	private void GetPages(String query, Map<String, String> pages, Set<String> redirects) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_id, page_is_redirect, page_latest FROM page WHERE page_title LIKE '"+query+"' AND page_namespace = 0;");

			while (rs.next()) {
				// If this is a redirect
				if (!rs.getBoolean(2)) {
					System.out.println("Page: " + rs.getString(1));
					pages.put(rs.getString(1), rs.getString(3));
				} else if (redirects != null) {
					System.out.println("Redirect: " + rs.getString(1));
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

	public void Search(String query) throws Exception {
		query = query.toLowerCase().replace(' ', '_').replaceAll("'", "\\'");

		Map<String, String> pages = new HashMap<String, String>();
		Set<String> redirects = new HashSet<String>();

		// Get a list of pages
		GetPages(query, pages, redirects);
		GetPages(query + "_(disambiguation)", pages, redirects);
		// Go through redirects and pull out all links
		while (!redirects.isEmpty()) {
			String redirect = redirects.iterator().next();

			Statement st = null;
			ResultSet rs = null;
			System.out.println(redirect);
			try {
				st = connection.createStatement();
				rs = st.executeQuery("SELECT pl_title FROM pagelinks WHERE pl_from = "+redirect+" AND pl_namespace = 0;");

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
				System.out.println("page_latest = " + page_latest);

				rs.close();
		//		rs = st.executeQuery("SELECT ")
				// TODO: here
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

		return null;
	}

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		Scanner scanner = new Scanner(System.in);
		WikiConnect connect = new WikiConnect();
		//SentenceConnect connect = new SentenceConnect();
		
		while (true) {
			System.out.print("Enter a query: ");
			String query = scanner.nextLine();

			try {
				connect.Search(query);
				//connect.getDocument(Integer.valueOf(query));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}
