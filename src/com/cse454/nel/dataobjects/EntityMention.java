package com.cse454.nel.dataobjects;

import java.util.Map;

import com.cse454.nel.features.Features;

public class EntityMention {
	public int sentenceID;
	public int tokStart;
	public int numToks;
	public String mentionString;
	public Map<Entity, Features> candidateFeatures;
	public Entity chosenEntity;

	public EntityMention(int sentenceID, String mention, int tokStart, int numToks) {
		this.sentenceID = sentenceID;
		this.tokStart = tokStart;
		this.numToks = numToks;
		this.mentionString = mention;
		candidateFeatures = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sentenceID;
		result = prime * result + numToks;
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
		if (numToks != other.numToks)
			return false;
		if (tokStart != other.tokStart)
			return false;
		return true;
	}
}