package com.cse454.nel.disambiguate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.Sentence;
import com.cse454.nel.WikiConnect;

public class InLinkDisambiguator extends AbstractDisambiguator {

	public InLinkDisambiguator(WikiConnect wiki, List<Sentence> sentences) {
		super(wiki, sentences);
	}

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions)
			throws Exception {
		Map<EntityMention, Entity> ret = new HashMap<EntityMention, Entity>();
		Map<String, Integer> inLinkCache = new HashMap<String, Integer>();
		for (EntityMention entityMention : mentions) {
			if (entityMention.sentenceID == 9) {
				System.out.println("break");
			}
			List<Entity> entities = entityMention.candidates;
			Integer max = -1;
			Entity chosenOne = null;
			for (Entity entity : entities) {
				if (inLinkCache.containsKey(entity.wikiTitle)) {
					entity.inlinks = inLinkCache.get(entity.wikiTitle);
				} else {
					entity.inlinks = wiki.GetInlinks(entity.wikiTitle);
					inLinkCache.put(entity.wikiTitle, entity.inlinks);
				}
				if (entity.inlinks > max) {
					chosenOne = entity;
					max = entity.inlinks;
				}
			}
			ret.put(entityMention, chosenOne);
		}
		return ret;
	}

}
