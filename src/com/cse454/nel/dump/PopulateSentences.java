package com.cse454.nel.dump;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.MySQLConnect;
import com.cse454.nel.kbp.ProcessedCorpus;
import com.cse454.nel.kbp.SFConstants;

public class PopulateSentences {

	public static void main(final String[] args) throws Exception {
		if (args.length != 1) {
			throw new IllegalArgumentException("Need to provide processed doc path");
		}
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));
		// final ExecutorService executor = Executors.newFixedThreadPool(100);
		final BlockingQueue<String> lines = new ArrayBlockingQueue<>(100);
		for (int i = 0; i < 99; i++) {
			DatabaseUpdateWorker worker = new DatabaseUpdateWorker(lines);
			executor.execute(worker);
		}
		String docPath = args[0];
		String[] dataTypes = {SFConstants.META};
		ProcessedCorpus corpus = null;
		try {
			corpus = new ProcessedCorpus(docPath, dataTypes);
			Map<String, String> annotations = null;
			while (corpus.hasNext()) {
				annotations = corpus.next();
				lines.put(annotations.get(SFConstants.META));
			}
			while (!lines.isEmpty()) {
				continue;
			}
			executor.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class DatabaseUpdateWorker implements Runnable {

		private final BlockingQueue<String> lines;
		private final MySQLConnect connection;

		public DatabaseUpdateWorker(BlockingQueue<String> lines) throws SQLException {
			this.lines = lines;
			connection = new MySQLConnect(MySQLConnect.defaultUrl, "sentences");
		}

		@Override
		public void run() {
			try {
				while(true) {
					String line = lines.take();
					String[] split = line.split("\t");
					if (split.length != 5) {
						return;
					}
					int id = Integer.valueOf(split[0]).intValue();
					String docName = split[2];
					String sql = "UPDATE sentences SET docName = ? where sentenceID = ?";
					PreparedStatement st = null;
					try {
						st = connection.connection.prepareStatement(sql);
						st.setString(1, docName);
						st.setInt(2, id);
						st.executeUpdate();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (st != null) {
							try {
								st.close();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				System.err.println("Unable to take from blocking queue");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
