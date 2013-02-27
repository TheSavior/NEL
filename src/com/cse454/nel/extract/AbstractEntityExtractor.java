package com.cse454.nel.extract;

import java.util.List;

import com.cse454.nel.EntityMention;
import com.cse454.nel.Sentence;

/**
 * Abstract base class for entity extractors.
 *
 * @author andrewrogers
 *
 */
// TODO: ask the other groups their approach to extracting entities from sentences to
// make this class a bit more general for them
public abstract class AbstractEntityExtractor {
	public abstract List<EntityMention> extract(List<Sentence> annotations);
}
