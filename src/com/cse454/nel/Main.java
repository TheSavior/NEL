package com.cse454.nel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.cse454.nel.scoring.Scorer;


public class Main {

	//private static final String sentencesFile = "sentences.entities";
    private static Object lock = new Object();
    private static int count = 0;
    private static int counter = 0;
    private static int NUM_DOCUMENTS = 100;

	public static void main(String[] args) throws InterruptedException, SQLException {
		System.out.println("Start");

		final DocumentConnect documentConnect = null;//new DocumentConnect();
		final Scorer scorer;
		try {
			scorer = new Scorer(documentConnect);
		} catch (IOException e1) {
			System.out.println("Cannot load gold data file");
			e1.printStackTrace();
			return;
		}

		// Setup thread pool
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 100, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

		final BlockingQueue<String> docNames = new ArrayBlockingQueue<>(100);
		for (int i = 0; i < 16; i++) {
			DocumentProcessWorker worker = new DocumentProcessWorker(docNames, documentConnect, scorer);
			executor.execute(worker);
		}

		// open the doc_gold, and feed each doc name to docNames
		String path = "./doc_gold.txt";
		try {
			// Open file
			BufferedReader reader = null;
			if (new File(path).exists()) {
				reader = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(path), "UTF-8"));
			} else {
				throw new Exception("Could not open file: " + path);
			}

			// read lines
			String line;
			while ((line = reader.readLine()) != null) {
				synchronized (lock) {
					count++;
					if (count % 100 == 0) {
						System.out.println("Count: " + count);
					}
				}
				String docName = line.split("\t")[0];
				docNames.put(docName);
			}

			// Wait for threads to finish
			while (!docNames.isEmpty()) {
				continue;
			}
			executor.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
		}


		// Now that we have results from the docs, evaluate the scorer
		scorer.ScoreOverall();
	}

	static class DocumentProcessWorker implements Runnable {

		private final BlockingQueue<String> docs;
		private final DocumentConnect documentConnect;
		private final Scorer scorer;

		public DocumentProcessWorker(BlockingQueue<String> docs, DocumentConnect documentConnect, Scorer scorer) {
			this.docs = docs;
			this.documentConnect = documentConnect;
			this.scorer = scorer;
		}

		@Override
		public void run() {
			while (true) {
				try {
					DocumentProcessor process;
					int count = 0;
//					synchronized (lock) {
//						if (counter > NUM_DOCUMENTS) {
//							break;
//						}
//						count = counter;
//						counter++;
//					}
					String docName = docs.take();
					process = new DocumentProcessor(count, docName, documentConnect, scorer);
					System.out.println(docName);
					process.run();
				} catch (Exception e) {
					System.err.println("Error processing document: " + counter);
					e.printStackTrace();
				}
			}
		}
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
