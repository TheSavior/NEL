package com.cse454.nel;

/**
 * Simple entity data structure.
 *
 * @author andrewrogers
 */
public class Entity {

	private String mEntityText; // raw text
	private String mSpan; 		// entity index span in the sentence

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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Entity)) {
			return false;
		}
		Entity ent = (Entity) obj;
		return this.mEntityText.equals(ent.mEntityText);
	}

	@Override
	public int hashCode() {
		int hash = 5;
        hash = 89 * hash + this.mEntityText.hashCode();
        return hash;
	}
}
