package com.cse454.nel.scripts;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.mysql.DocumentConnect;

public class ExportGold {
	public static void main(String[] args) {
		try {
			// Create file
			FileWriter fstream = new FileWriter("sentences.gold");
			BufferedWriter out = new BufferedWriter(fstream);
			DocumentConnect doc = new DocumentConnect();
			
			System.out.println("Getting gold sentences");
			Map<Integer, String> map = doc.getGoldSentences();
			
			for(Entry<Integer, String> entry : map.entrySet()) {
				out.write(entry.getKey() + "\t"+entry.getValue()+"\n");
			}
			
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		System.out.println("Done!");
	}
}
