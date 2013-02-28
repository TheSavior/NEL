package com.cse454.nel.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cse454.nel.Entity;
import com.cse454.nel.EntityMention;
import com.cse454.nel.WikiConnect;

public class BasicSearcher extends AbstractSearcher {
	
	private WikiConnect wiki;
	
	public BasicSearcher(WikiConnect wiki) {
		this.wiki = wiki;
	}
	
	public void GetCandidateEntities(EntityMention mention) throws Exception {
		
		// Sanitize the query, and convert to wikipedia format (i.e. spaces become underscores)
		String query = mention.mentionString.replace(' ', '_');

		// Get a list of matching pages
		Map<String, String> pages = new HashMap<String, String>();
		Set<String> redirects = new HashSet<String>();
		wiki.GetPages(query, pages, redirects);
		
		// Now find all (if any) disambiguation links.
		// 1. pull up disambiguation page
		Map<String, String> disam_pages = new HashMap<String, String>();
		wiki.GetPages(query + "_(disambiguation)", disam_pages, redirects);
		
		// 2. Make sure matching regular pages aren't actually disambiguation pages (e.x. 'Chilean')
		for (Entry<String, String> entry : new HashSet<Entry<String, String>>(pages.entrySet())) {
			System.out.println("Page<" + entry.getKey() + ", " + entry.getValue() +">");
			String text = wiki.GetWikiTextFromPageLatest(entry.getValue());
			if (text.contains("{{disambig")) {
				disam_pages.put(entry.getKey(), entry.getValue());
				pages.remove(entry.getKey());
				System.out.println("-->Disambig");
			}
		}
		
		// 3. Extract links from the disam page
		for (Entry<String, String> entry : disam_pages.entrySet()) {
			System.out.println("Disambig<" + entry.getKey() + ", " + entry.getValue() +">");
			wiki.GetPageLinks(pages, entry.getKey());
		}
		
		// Go through redirects and pull out all links
		while (!redirects.isEmpty()) {
			String redirect = redirects.iterator().next();
			wiki.GetPageLinks(pages, redirect);
			redirects.remove(redirect);
		}
		
		mention.candidates = new ArrayList<Entity>();
		for (Entry<String, String> page : pages.entrySet()) {
			mention.candidates.add( new Entity(page.getKey()) );
		}
	}
}
