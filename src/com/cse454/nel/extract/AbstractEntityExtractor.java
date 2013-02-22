package com.cse454.nel.extract;

import java.util.List;
import java.util.Map;

import com.cse454.nel.Entity;

/**
 * Abstract base class for entity extractors.
 *
 * @author andrewrogers
 *
 */
// TODO: ask the other groups their approach to extracting entities from sentences to
// make this class a bit more general for them
public abstract class AbstractEntityExtractor {

	public abstract List<Entity> extract(Map<String, String> annotations);
}
