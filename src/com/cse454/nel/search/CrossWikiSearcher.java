package com.cse454.nel.search;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cse454.nel.dataobjects.Entity;
import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.features.Features;
import com.cse454.nel.mysql.WikiConnect;
import com.cse454.nel.scripts.CrossWikiDataScript;

public class CrossWikiSearcher extends AbstractSearcher {

	public static final String FEATURE_STRING = "crosswiki-prob";

	private WikiConnect wiki;

	public CrossWikiSearcher(WikiConnect wiki) {
		this.wiki = wiki;
	}

	@Override
	public void GetCandidateEntities(EntityMention mention) throws SQLException {
		String query = mention.mentionString;
		
		List<CrossWikiDataScript> crossWikiData = wiki.GetCrossWikiDocs(query, true);

		Map<Entity, Features> candidates = new HashMap<>();

		for (CrossWikiDataScript data : crossWikiData) {
			Features features = new Features();
			features.setFeature(FEATURE_STRING, data.probability);
			candidates.put(new Entity(data.URL), features);
		}

		mention.candidateFeatures = candidates;
	}

}
