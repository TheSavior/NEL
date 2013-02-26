package com.cse454.nel;

/**
 * Simple entity data structure.
 *
 * @author andrewrogers
 */
public class Entity {

	private String wikiID;
	private EntityMention mention;

	public Entity(String wikiID, EntityMention mention) {
		this.wikiID = wikiID;
		this.mention = mention;
	}
}
