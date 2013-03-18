package com.cse454.nel.document;

import com.sun.istack.internal.Nullable;

/**
 * Provides {@link AbstractDocument}s dynamically to avoid creating as many documents in memory
 * as want to be processed.
 *
 */
public interface DocumentFactory {
	/**
	 * Get the next document to be processed, or null when done.  NOT Thread Safe
	 */
	@Nullable
	public AbstractDocument NextDocument();
}
