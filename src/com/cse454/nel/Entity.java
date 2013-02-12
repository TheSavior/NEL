package com.cse454.nel;

/**
 * Simple entity data structure.
 *
 * @author andrewrogers
 */
public class Entity {

	private String mEntityText; 	// raw text
	private String mSpan; 		// entity index span in the senetence

	public Entity(String entityText, String span) {
		mEntityText = entityText;
		mSpan = span;
	}

	public String getEntityText() {
		return mEntityText;
	}

	public String getEntitySpan() {
		return mSpan;
	}

	@Override
	public String toString() {
		return mEntityText + " " + mSpan;
	}
}
