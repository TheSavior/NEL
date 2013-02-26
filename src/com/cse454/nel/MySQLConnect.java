package com.cse454.nel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {
	protected Connection connection;

	protected static String defaultUser = "god";
	protected static String defaultPassword = "jesus";
	
	public MySQLConnect(String url, String database) throws SQLException {
		this(url, database, defaultUser, defaultPassword);
	}

	public MySQLConnect(String url, String database, String user, String password) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + url + "/" + database, user, password);
	}
}
