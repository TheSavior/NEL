package com.cse454.nel.features;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.mysql.WikiConnect;

public class InLinkFeatureGenerator implements FeatureGenerator {

	public static final String FEATURE_STRING = "inlink_feature";

	private final WikiConnect wiki;
	private final Map<String, Integer> inLinkCache = new HashMap<String, Integer>();

	public InLinkFeatureGenerator(WikiConnect wiki) throws SQLException {
		this.wiki = wiki;
	}

	@Override
	public String GetFeatureName() {
		return FEATURE_STRING;
	}

	@Override
	public void GenerateFeatures(EntityMention mention) throws Exception {
		Map<Entity, Features> candidateFeatures = mention.candidateFeatures;
		double total = 0;
		for (Entry<Entity, Features> entry : candidateFeatures.entrySet()) {
			Entity candidate = entry.getKey();
			if (inLinkCache.containsKey(candidate.wikiTitle)) {
				candidate.inlinks = inLinkCache.get(candidate.wikiTitle);
			} else {
				candidate.inlinks = wiki.GetInlinks(candidate.wikiTitle);
				inLinkCache.put(candidate.wikiTitle, candidate.inlinks);
			}
			total += candidate.inlinks;
		}
		for (Entry<Entity, Features> entry : candidateFeatures.entrySet()) {
			Entity candidate = entry.getKey();
			int inlinks = candidate.inlinks;
			double normalized = inlinks / total;
			entry.getValue().setFeature(GetFeatureName(), normalized);
		}
	}
}
