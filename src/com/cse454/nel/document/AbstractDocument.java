package com.cse454.nel.document;

import java.util.List;

import com.cse454.nel.dataobjects.Sentence;
import com.sun.istack.internal.Nullable;

/**
 * Abstract Document describes a document with a document name, and a list of 
 * sentences in that document.
 *
 */
public abstract class AbstractDocument {
	
	protected String docName;
	protected List<Sentence> sentences;

	protected AbstractDocument(String name) {
		this.docName = name;
		this.sentences = null;
	}

	public String GetName() {
		return docName;
	}

	/**
	 * Returns a list of {@link Sentence}s from this document, or <code>null</code> if they haven't
	 * been generated yet.
	 */
	@Nullable
	public List<Sentence> GetSentences() {
		return sentences;
	}

	protected abstract List<Sentence> GenerateSentences() throws Exception;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docName == null) ? 0 : docName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDocument other = (AbstractDocument) obj;
		if (docName == null) {
			if (other.docName != null)
				return false;
		} else if (!docName.equals(other.docName))
			return false;
		return true;
	}
}
