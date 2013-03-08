package com.cse454.nel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnect {
	protected static String defaultUrl = "54.244.241.108:3306";
	protected static String defaultUser = "god";
	protected static String defaultPassword = "jesus";

	protected Connection connection;

	protected static abstract class QueryResponder<T> {
		public abstract T Result(ResultSet result) throws SQLException;
	}

	public MySQLConnect(String url, String database) throws SQLException {
		this(url, database, defaultUser, defaultPassword);
	}

	public MySQLConnect(String url, String database, String user, String password) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + database, user, password);
	}

	protected <T> T ExecuteQuery(String query, QueryResponder<T> response) throws SQLException {
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery(query);

			return response.Result(rs);

		} catch (SQLException e) {
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
