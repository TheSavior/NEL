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
	
	private int matched = 0;
	private int total = 0;
	
	private int scoredDocuments = 0;

	DocumentConnect sentences;
	private Object lock = new Object();

	public Scorer() throws IOException, SQLException {
		this.sentences = new DocumentConnect();

		lookup = new HashMap<String, String>();

		gold = new HashMap<String, Set<String>>();
		results = new HashMap<String, Set<String>>();

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

	public void ScoreResults(String docName, Map<EntityMention, Entity> entities) throws SQLException {
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
		for(String entityId : gold.get(docName)) {
			total++;
			
			String entity = lookup.get(entityId);
			if (values.contains(entity)){
				matched++;
			}
			
			names.add(lookup.get(entityId));
		}
		
		scoredDocuments++;
		if (scoredDocuments % 10 == 0) {
			float percent = (((float)matched)/ total) * 100;
			System.out.println("Scored: "+scoredDocuments+" -- Matched "+matched+" out of "+total+" total entities: "+String.format("%s",percent)+" %");
		}

		//System.out.println("\tGold: "+ Join(", ", names));
		//System.out.println("\tGiven: "+ Join(", ", values));

/*
		synchronized (lock) {
			results.put(docName, values);
			// Aggregate score
		}
		*/
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
		for(Entry<String, Set<String>> entry : results.entrySet()) {
		    String key = entry.getKey();
		    Set<String> values = entry.getValue();

		    System.out.println(key+"\t"+StringUtils.join(values, "\t"));
		}
	}
}
