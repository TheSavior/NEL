package com.cse454.nel.scoring;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.cse454.nel.DocumentConnect;
import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;

public class Scorer {
	private static final String goldDataFile = "doc_gold.txt";
	private static final String entityLookup = "entityLookup.txt";

	// Maps from entity id to wiki title
	private Map<String, String> lookup;

	private Map<String, Set<String>> gold;
	private Map<String, Set<String>> results;
	
	// These map the disambiguator type to the sum
	private Map<Class, Integer> matched;
	private Map<Class, Integer> total;
	
	private Map<Class, Integer> processedDocs;
	private Map<Class, Long> timing;
	
	private int scoredDocuments = 0;

	DocumentConnect sentences;
	private Object lock = new Object();

	public Scorer() throws IOException, SQLException {
		this.sentences = new DocumentConnect();

		lookup = new HashMap<String, String>();

		gold = new HashMap<String, Set<String>>();
		results = new HashMap<String, Set<String>>();
		
		matched = new HashMap<Class, Integer>();
		total = new HashMap<Class, Integer>();
		
		timing = new HashMap<Class, Long>();
		processedDocs = new HashMap<Class, Integer>();

		LoadLookup();
		LoadGoldData();
	}

	private void LoadLookup() throws IOException {
		FileInputStream fstream = new FileInputStream(entityLookup);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			// Print the content on the console
			String[] parts = strLine.split("\t");
			lookup.put(parts[0], parts[1]);
		}
		in.close();

		System.out.println("Imported "+lookup.size()+" entities");
	}


	private void LoadGoldData() throws IOException {
		FileInputStream fstream = new FileInputStream(goldDataFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		//Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			// Print the content on the console
			String[] parts = strLine.split("\t");
			HashSet<String> pieces = new HashSet<String>();
			for(int i = 1; i < parts.length; i++) {
				pieces.add(parts[i]);
			}

			gold.put(parts[0], pieces);
		}
		in.close();

		System.out.println("Imported "+gold.size()+" documents with gold data");
	}

	public void ScoreResults(Class disambiguator, String docName, Map<EntityMention, Entity> entities) throws SQLException {
		// Process Results
		//System.out.println("Scoring: "+docName+":");
		if (!gold.containsKey(docName)){
			System.out.println("\tDoc not in gold data");
			return;
		}

		Set<String> values = new HashSet<String>();
		for(Entity ent : entities.values())
		{
			if (ent != null) {
				values.add(ent.wikiTitle);
			}
		}

		Set<String> names = new HashSet<String>();
		
		synchronized(lock) {
			if (!timing.containsKey(disambiguator)) {
				timing.put(disambiguator, (long) 0);
			}
			if (!processedDocs.containsKey(disambiguator)) {
				processedDocs.put(disambiguator, 0);
			}
			
			for(String entityId : gold.get(docName)) {
				if (!matched.containsKey(disambiguator)) {
					matched.put(disambiguator, 0);
				}
				
				if (!total.containsKey(disambiguator)) {
					total.put(disambiguator, 0);
				}
								
				total.put(disambiguator, total.get(disambiguator)+1);
				
				String entity = lookup.get(entityId);
				if (values.contains(entity)){
					
					matched.put(disambiguator, matched.get(disambiguator)+1);
				}
				
				names.add(lookup.get(entityId));
			}
		}
		if (scoredDocuments % 5 == 0) {
			for(Entry<Class, Integer> entry : total.entrySet()) {
				int match = matched.get(entry.getKey());
				int totals = entry.getValue();
				float percent = (((float)match)/ totals) * 100;
				
				long runtime = timing.get(entry.getKey());
				double avgRuntime = (runtime / Math.max(1, processedDocs.get(entry.getKey()))) / 1000.0;
				System.out.println("Disambiguator: "+entry.getKey().getName()+" Scored: "+scoredDocuments+" -- Matched "+match+" out of "+totals+" total entities: "+String.format("%s",percent)+"% -- Avg Runtime: "+avgRuntime+"s");
			}
			System.out.println();
		}
	}
	
	public void AddTiming(Class disambiguator, long time) {
		synchronized(lock) {
			timing.put(disambiguator, timing.get(disambiguator)+time);
			processedDocs.put(disambiguator, processedDocs.get(disambiguator)+1);
			
			scoredDocuments++;
		}
	}

	public String Join(String seperator, Set<String> items) {
		if (items.size() == 0)
			return "";

		StringBuilder str = new StringBuilder();
		Iterator<String> iter = items.iterator();

		str.append(iter.next());
		while(iter.hasNext()){
			str.append(", "+iter.next());
		}

		return str.toString();
	}

	public void ScoreOverall() {
		System.out.println();
		System.out.println("Class\tMatched\tTotal\tPercent\tAvg. Runtime");
		for(Entry<Class, Integer> entry : total.entrySet()) {
			int match = matched.get(entry.getKey());
			int totals = entry.getValue();
			float percent = (((float)match)/ totals) * 100;
			
			long runtime = timing.get(entry.getKey());
			double avgRuntime = (runtime / Math.max(1, processedDocs.get(entry.getKey()))) / 1000.0;
			System.out.println(entry.getKey().getName()+"\t"+match+"\t"+totals+"\t"+String.format("%s",percent)+"%\t"+avgRuntime+"s");
		}
	}
}
