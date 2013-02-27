package com.cse454.nel.disambiguate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;


public class SimpleDisambiguator extends AbstractDisambiguator {

	public SimpleDisambiguator() {
		super(null, null);
	}

	@Override
	public Map<EntityMention, Entity> disambiguate(List<EntityMention> mentions) {
		Map<EntityMention, Entity> ret = new HashMap<EntityMention, Entity>();
		for (EntityMention mention : mentions) {
			Entity ent = null;
			if (mention.candidates.size() > 0) {
				ent = mention.candidates.get(0);
			}
			ret.put(mention, ent);
		}
		return ret;
	}


}
