package com.cse454.nel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cse454.nel.kbp.KB;
import com.cse454.nel.scoring.Scorer;


public class Main {

	//private static final String sentencesFile = "sentences.entities";
    private static Object lock = new Object();
    private static int counter = 0;
    private static int NUM_DOCUMENTS = 100;

	public static void main(String[] args) throws InterruptedException, SQLException {
		System.out.println("Start");
		
		final SentenceConnect docs = new SentenceConnect();
		
		final Scorer scorer;
		try {
			scorer = new Scorer(docs);
		} catch (IOException e1) {
			System.out.println("Cannot load gold data file");
			e1.printStackTrace();
			return;
		}
		
		List<Thread> threadPool = new ArrayList<Thread>();
		for (int i = 0; i < 16; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							DocumentProcessor process;
							synchronized (lock) {
								if (counter > NUM_DOCUMENTS) {
									break;
								}
								process = new DocumentProcessor(counter, docs, scorer);
								counter++;
							}

							process.run();
						} catch (Exception e) {
							System.err.println("Error processing document: " + counter);
							e.printStackTrace();
						}
					}
				}
			};
			thread.start();
			threadPool.add(thread);
		}

		for (Thread t : threadPool) {
			t.join();
		}

		// Now that we have results from the docs, evaluate the scorer
		scorer.ScoreOverall();
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
