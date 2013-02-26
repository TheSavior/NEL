package com.cse454.nel.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;
import com.cse454.warmup.sf.SFConstants;

/**
 * Extracts entities using the information given in the annotation data (the sentence.*
 * files).
 *
 * @author andrewrogers
 *
 */
public class EntityExtractor extends AbstractEntityExtractor {

	@Override
	public List<Entity> extract(Map<String, String> annotations) {
		List<Entity> entities = new ArrayList<Entity>();

		String[] split;
		split = annotations.get(SFConstants.TOKENS).split("\t");
		String id = split[0];
		String[] tokens = split[1].split(" ");
		split = annotations.get(SFConstants.STANFORDNER).split("\t");
		String[] stanfordNer = split[1].split(" ");

		int length = tokens.length;
		for (int i = 0; i < length; i++) {
			if (stanfordNer[i].length() == 1
					|| stanfordNer[i].equals("DATE")
					|| stanfordNer[i].equals("PERCENT")
					|| stanfordNer[i].equals("NUMBER")) {
				continue;
			}
			int startIndex = i;
			StringBuffer buffer = new StringBuffer(tokens[i]);
			while (i < length && stanfordNer[i].equals(stanfordNer[i + 1])) {
				i++;
				buffer.append(" " + tokens[i]);
			}
			String entityText = buffer.toString();
			String span = startIndex + ":" + i;
			entities.add(new EntityMention(entityText, span));
		}
		return entities;
	}

}
