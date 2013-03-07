package com.cse454.nel.scoring;

import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;

public class Scorer {

	private Object lock = new Object();
	
	public void ScoreResults(int docID, Map<EntityMention, Entity> entities) {
		// Process Results
		
		synchronized (lock) {
			// Aggregate score
		}
	}
}
