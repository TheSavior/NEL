package com.cse454.dump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cse454.nel.MySQLConnect;

public class CrosswikiDumper {

	@SuppressWarnings("resource")
	public static void main(final String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("Need to provide processed doc path");
		}

		// Setup thread pool
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

		final BlockingQueue<String> lines = new ArrayBlockingQueue<>(100);
		for (int i = 0; i < 99; i++) {
			DatabaseUpdateWorker worker = new DatabaseUpdateWorker(lines);
			executor.execute(worker);
		}

/* DEBUG
		lines.add("\"All_Four_One\"	1 All_Four_One KB W09 W08 WDB W:1/1'");
		// Wait for threads to finish
		while (!lines.isEmpty()) {
			continue;
		}
		executor.shutdownNow();
		*/

		// Process file
		String docPath = args[0];
		try {
			// Open file
			BufferedReader reader = null;
			if (new File(docPath).exists()) {
				reader = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(docPath), "UTF-8"));
			} else {
				throw new Exception("Could not open file: " + docPath);
			}

			// read lines
			String line;
			while ((line = reader.readLine()) != null) {
				lines.put(line);
			}

			// Wait for threads to finish
			while (!lines.isEmpty()) {
				continue;
			}
			executor.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class DatabaseUpdateWorker implements Runnable {

		private static class CrosswikiUpdater extends MySQLConnect {
			public CrosswikiUpdater() throws SQLException {
				super(defaultUrl, "wikidb");
			}

			public void Update(String mention, double likelihood, String entity) throws SQLException {
			//System.out.println(mention + ", " + likelihood + ", " + entity);
				PreparedStatement st = null;
				try {
					st = connection.prepareStatement("INSERT INTO crosswiki VALUES (?, ?, ?)");
					st.setString(1, mention);
					st.setDouble(2, likelihood);
					st.setString(3, entity);
					st.executeUpdate();
				} catch (SQLException e) {
					throw e;
				} finally {
					st.close();
				}
			}
		}

		private final BlockingQueue<String> lines;
		private final CrosswikiUpdater connection;

		public DatabaseUpdateWorker(BlockingQueue<String> lines) throws SQLException {
			this.lines = lines;
			connection = new CrosswikiUpdater();
		}

		@Override
		public void run() {
			try {
				Pattern pattern = Pattern.compile("([^\t]*)\t([^\\s]*)\\s([^\\s]*)(\\s.*)?");
				while(true) {
					String line = lines.take();
					//System.out.println(line);
					try {
						Matcher matcher = pattern.matcher(line);

						if (matcher.matches()) {
							String mention = matcher.group(1);
							String entity = matcher.group(3);
							double likelihood = Double.parseDouble(matcher.group(2));
							connection.Update(mention, likelihood, entity);
						} else {
							throw new Exception("Matcher Didn't Match");
						}
					} catch (Exception e) {
						System.out.println("Failed to Parse Line: '" + line + "'");
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				System.err.println("Unable to take from blocking queue");
				e.printStackTrace();
			}
		}
	}
}
