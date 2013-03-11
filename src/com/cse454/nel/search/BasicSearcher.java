package com.cse454.nel.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.WikiConnect;

public class BasicSearcher extends AbstractSearcher {
	
	private WikiConnect wiki;
	private Map<String, List<Entity>> candidatesCache;
	
	public BasicSearcher(WikiConnect wiki) {
		this.wiki = wiki;
		candidatesCache = new HashMap<String, List<Entity>>();
	}
	
	public void GetCandidateEntities(EntityMention mention) throws Exception {
		if (candidatesCache.containsKey(mention.mentionString)) {
			mention.candidates = candidatesCache.get(mention.mentionString);
		}
		
		// Sanitize the query, and convert to wikipedia format (i.e. spaces become underscores)
		String query = mention.mentionString.replace(' ', '_');

		// Get a list of matching pages
		Map<String, String> pages = new HashMap<String, String>();
		Map<String, String> redirects = new HashMap<String, String>();
		wiki.GetPages(query, pages, redirects);
		System.out.println("pages:");
		for (Entry<String, String> entry : pages.entrySet()) {
			System.out.println(entry);
		}
		System.out.println("redirs:");
		for (Entry<String, String> entry : redirects.entrySet()) {
			System.out.println(entry);
		}
		
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
			System.out.println("Disam/Redir<" + redirects.get(pageID) +">");
			wiki.GetPageLinks(candidates, pageID);
		}
		
		mention.candidates = new ArrayList<Entity>();
		for (String page : candidates) {
			mention.candidates.add( new Entity(page) );
		}
		
		candidatesCache.put(mention.mentionString, mention.candidates);
	}
}
