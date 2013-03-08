package com.cse454.nel.scoring;

import java.util.*;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;

public class Scorer {

	private Map<String, Set<Entity>> results;
	private Object lock = new Object();
	
	public void ScoreResults(String docID, Map<EntityMention, Entity> entities) {
		// Process Results

		/* store 
		 * 
		 */
		Set<Entity> values = new HashSet<Entity>(entities.values());
		
		synchronized (lock) {	
			results.put(docID, values);
			// Aggregate score
		}
	}

	public void ScoreOverall() {
		
	}
}
