package com.cse454.nel;

import java.util.List;

public class EntityMention {
	int sentenceID;
	int tokStart, tokEnd;
	String mentionString;
	public List<Entity> candidates;
	
	public EntityMention(int sentenceID, int tokStart, int tokEnd) {
		this.sentenceID = sentenceID;
		this.tokStart = tokStart;
		this.tokEnd = tokEnd;
		candidates = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sentenceID;
		result = prime * result + tokEnd;
		result = prime * result + tokStart;
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
		EntityMention other = (EntityMention) obj;
		if (sentenceID != other.sentenceID)
			return false;
		if (tokEnd != other.tokEnd)
			return false;
		if (tokStart != other.tokStart)
			return false;
		return true;
	}

/*
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
	}*/
}
