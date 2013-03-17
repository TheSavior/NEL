package com.cse454.nel.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.dataobjects.Entity;
import com.cse454.nel.dataobjects.EntityMention;
import com.cse454.nel.features.Features;
import com.cse454.nel.mysql.WikiConnect;

public class BasicSearcher extends AbstractSearcher {

	private WikiConnect wiki;
	private Map<String, Set<Entity>> candidatesCache;

	public BasicSearcher(WikiConnect wiki) {
		this.wiki = wiki;
		candidatesCache = new HashMap<String, Set<Entity>>();
	}
	
	private Map<Entity, Features> ConvertToEmptyFeatures(Set<Entity> ents) {
		Map<Entity, Features> ret = new HashMap<>();
		for (Entity ent : ents) {
			ret.put(ent, new Features());
		}
		return ret;
	}
	
	public void GetCandidateEntities(EntityMention mention) throws Exception {
		if (candidatesCache.containsKey(mention.mentionString)) {
			mention.candidateFeatures = ConvertToEmptyFeatures(candidatesCache.get(mention.mentionString));
			return;
		}

		// Sanitize the query, and convert to wikipedia format (i.e. spaces become underscores)
		String query = mention.mentionString.replace(' ', '_');

		// Get a list of matching pages
		Map<String, String> pages = new HashMap<String, String>();
		Map<String, String> redirects = new HashMap<String, String>();
		wiki.GetPages(query, pages, redirects);
		// Now find all (if any) disambiguation links.
		// 1. pull up disambiguation page
		wiki.GetPages(query + "_(disambiguation)", redirects, redirects);


		// 2. Make sure matching regular pages aren't actually disambiguation pages (e.x. 'Chilean')
		for (Entry<String, String> page : new HashSet<Entry<String, String>>(pages.entrySet())) {
			//System.out.println("Page<" + page + ">");
			String text = wiki.GetWikiText(page.getValue());
			if (text.contains("{{disambig")) {
				redirects.put(page.getKey(), page.getValue());
				pages.remove(page.getKey());
				//System.out.println("-->Disambig");
			}
		}

		Set<String> candidates = new HashSet<String>(pages.values());

		// 3. Extract links from disambig/redirect pages
		for (String pageID : redirects.keySet()) {
			// System.out.println("Disam/Redir<" + redirects.get(pageID) +">");
			wiki.GetPageLinks(candidates, pageID);
		}

		Set<Entity> entities = new HashSet<Entity>();
		for (String page : candidates) {
			entities.add( new Entity(page) );
		}

		candidatesCache.put(mention.mentionString, entities);
		mention.candidateFeatures = ConvertToEmptyFeatures(entities);
	}
}
