package com.cse454.nel.disambiguate;

import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Sentence;
import com.cse454.nel.WikiConnect;

public abstract class AbstractDisambiguator {

	protected WikiConnect wiki;
	protected List<Sentence> sentences;

	public AbstractDisambiguator(WikiConnect wiki, List<Sentence> sentences) {
		this.wiki = wiki;
		this.sentences = sentences;
	}

	public abstract Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions) throws Exception;
}
