package com.cse454.nel;

/**
 * Simple entity data structure.
 *
 * @author andrewrogers
 */
public class Entity {

	public String wikiID;
	public Integer inlinks;

	public Entity(String wikiID) {
		this.wikiID = wikiID;
	}
}
