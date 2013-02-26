package com.cse454.nel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class WikiConnect {

	protected static String defaultUrl = "jdbc:mysql://54.244.244.3:3306/wikidb";
	protected static String defaultUser = "god";
	protected static String defaultPassword = "jesus";

	protected Connection connection;

	public WikiConnect() throws SQLException {
        this(defaultUrl, defaultUser, defaultPassword);
	}

	public WikiConnect(String url, String user, String password) throws SQLException {
		connection = DriverManager.getConnection(url, user, password);
	}

	private void GetPages(String query, Map<String, String> pages, Set<String> redirects) throws Exception {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT page_id, page_title, page_is_redirect, page_latest FROM page WHERE page_title LIKE '"+query+"' AND page_namespace = 0;");

			int numCols = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				System.out.print("[");
				for (int i = 1; i <= numCols; ++i) {
					if (i != 1) {
						System.out.print(" | ");
					}
					System.out.print("'" + rs.getString(i) + "'");
				}

				System.out.println("]");

				// If this is a redirect
				if (rs.getBoolean(3)) {
					redirects.add(rs.getString(1));
				} else {
					pages.put(rs.getString(1), rs.getString(2)); // TODO: use page_latest to get real data, use this for page_title for show now
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
					System.out.println("\t\t" + rs.getString(1));
					GetPages(rs.getString(1), pages, redirects);
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

		while (true) {
			System.out.print("Enter a query: ");
			String query = scanner.nextLine();

			try {
				connect.Search(query);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
    }
}
