package com.cse454.nel.document;

import java.util.List;

import com.cse454.nel.Sentence;

public abstract class AbstractDocument {
	protected String name;
	protected List<Sentence> sentences;

	protected AbstractDocument(String name) {
		this.name = name;
		this.sentences = null;
	}

	public String GetName() {
		return name;
	}

	public List<Sentence> GetSentences() throws Exception {
		if (sentences == null) {
			sentences = GenerateSentences();
		}

		return sentences;
	}

	protected abstract List<Sentence> GenerateSentences() throws Exception;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
