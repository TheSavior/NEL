package com.cse454.nel.document;

public interface DocumentFactory {
	/**
	 * Get the next document to be processed, or null when done.  NOT Thread Safe
	 * @return
	 */
	public AbstractDocument NextDocument();
}
