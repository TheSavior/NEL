package com.cse454.nel.search;

import com.cse454.nel.dataobjects.EntityMention;

public abstract class AbstractSearcher {
	public abstract void GetCandidateEntities(EntityMention mention) throws Exception;
}
