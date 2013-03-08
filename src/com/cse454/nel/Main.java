package com.cse454.nel;

import java.util.ArrayList;
import java.util.List;

import com.cse454.nel.scoring.Scorer;


public class Main {

	private static final String sentencesFile = "sentences.entities";
    private static Object lock = new Object();
    private static int counter = 0;
    private static int NUM_DOCUMENTS = 1286425;

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Start");
		final Scorer scorer = new Scorer();
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
								process = new DocumentProcessor(counter, scorer);
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
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
