package com.cse454.nel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Main {

	public static final String sentencesFile = "sentences.entities";
    public static Object lock = new Object();

    public static int counter = 0;
	public static void main(String[] args) {
		List<Thread> threadPool = new ArrayList<Thread>();
		for (int i = 0; i < 16; i++) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					synchronized (lock) {
						try {
							DocumentProcessor process = new DocumentProcessor(counter);
							process.run();
							counter++;
						} catch (SQLException e) {
							System.err.println("Error processing document: " + counter);
							e.printStackTrace();
						}
					}
				}
			};
			threadPool.add(thread);
		}
	}

	public static void usage() {
		System.err.println("java com.cse454.nel.Main " +
				"<keyfile>");
		System.exit(-1);
	}
}
