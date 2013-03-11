package com.cse454.nel;

/**
 * Simple entity data structure.
 *
 * @author andrewrogers
 */
public class Entity {

	public String wikiTitle;
	public Integer inlinks;

	public Entity(String wikiID) {
		this.wikiTitle = wikiID;
	}

	public String toString() {
		return this.wikiTitle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((wikiTitle == null) ? 0 : wikiTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (wikiTitle == null) {
			if (other.wikiTitle != null)
				return false;
		} else if (!wikiTitle.equals(other.wikiTitle))
			return false;
		return true;
	}
	
	
}
