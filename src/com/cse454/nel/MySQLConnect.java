package com.cse454.nel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {
	protected static String defaultUrl = "54.244.244.3:3306";
	protected static String defaultUser = "god";
	protected static String defaultPassword = "jesus";

	protected Connection connection;

	public MySQLConnect(String url, String database) throws SQLException {
		this(url, database, defaultUser, defaultPassword);
	}

	public MySQLConnect(String url, String database, String user, String password) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + database, user, password);
	}
}
