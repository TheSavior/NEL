package com.cse454.nel.disambiguate;

import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Sentence;
import com.cse454.nel.WikiConnect;

public abstract class AbstractDisambiguator {

	protected WikiConnect wiki;

	public AbstractDisambiguator(WikiConnect wiki) {
		this.wiki = wiki;
	}

	public abstract Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions, List<Sentence> sentences) throws Exception;
}
