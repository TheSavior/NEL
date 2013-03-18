package com.cse454.nel.document;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.sun.istack.internal.Nullable;

/**
 * Factory for creating empty {@link SentenceDbDocument}s.
 */
public class SentenceDbDocFactory implements DocumentFactory {

	private Queue<String> docNames = new LinkedList<>();
	private Queue<Integer> docIDs = new LinkedList<>();

	/**
	 * Add documents by name.
	 */
	public void AddDocNames(List<String> docs) {
		docNames.addAll(docs);
	}

	/**
	 * Add documents by ID.
	 */
	public void AddDocIDs(List<Integer> docs) {
		docIDs.addAll(docs);
	}

	/**
	 * First poll from docNames, then docIDs, finaly returning null if empty.
	 */
	@Nullable
	@Override
	public AbstractDocument NextDocument() {
		if (!docNames.isEmpty()) {
			String name = docNames.poll();
			return new SentenceDbDocument(name);
		} else if (!docIDs.isEmpty()) {
			int id = docIDs.poll();
			return new SentenceDbDocument(id);
		}

		return null;
	}
}
