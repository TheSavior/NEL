package com.cse454.nel.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.CrossWikiData;
import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.WikiConnect;
import com.cse454.nel.features.Features;

public class CrossWikiSearcher extends AbstractSearcher {

	private WikiConnect wiki;

	public CrossWikiSearcher(WikiConnect wiki) {
		this.wiki = wiki;
	}

	@Override
	public void GetCandidateEntities(EntityMention mention) throws Exception {
		String query = mention.mentionString;

		List<CrossWikiData> crossWikiData = wiki.GetCrossWikiDocs(query, true);

		Map<Entity, Features> candidates = new HashMap<>();

		for (CrossWikiData data : crossWikiData) {
			Features features = new Features();
			features.setFeature("crosswiki-prob", data.probability);
			candidates.put(new Entity(data.URL), features);
		}

		mention.candidateFeatures = candidates;
	}

}
