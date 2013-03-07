package com.cse454.nel;

import java.util.ArrayList;
import java.util.List;

import com.cse454.nel.scoring.Scorer;


public class Main {

	public static final String sentencesFile = "sentences.entities";
    public static Object lock = new Object();

    public static int counter = 0;
	public static void main(String[] args) {
		System.out.println("Start");
		final Scorer scorer = new Scorer();
		List<Thread> threadPool = new ArrayList<Thread>();
		for (int i = 0; i < 16; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						DocumentProcessor process;
						synchronized (lock) {
							process = new DocumentProcessor(counter, scorer);
							counter++;
						}
						
						process.run();
					} catch (Exception e) {
						System.err.println("Error processing document: " + counter);
						e.printStackTrace();
					}
				}
			};
			thread.start();
			threadPool.add(thread);
		}
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
