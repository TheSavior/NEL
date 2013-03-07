package com.cse454.nel.search;

import java.util.ArrayList;
import java.util.HashSet;
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
		Set<String> pages = new HashSet<String>();
		Set<String> redirects = new HashSet<String>();
		wiki.GetPages(query, pages, redirects);
		
		// Now find all (if any) disambiguation links.
		// 1. pull up disambiguation page
		wiki.GetPages(query + "_(disambiguation)", redirects, redirects);

		
		// 2. Make sure matching regular pages aren't actually disambiguation pages (e.x. 'Chilean')
		for (String page : new HashSet<String>(pages)) {
			System.out.println("Page<" + page + ">");
			String text = wiki.GetWikiText(page);
			if (text.contains("{{disambig")) {
				redirects.add(page);
				pages.remove(page);
				System.out.println("-->Disambig");
			}
		}
		
		// 3. Extract links from disambig/redirect pages
		for (String redirect : redirects) {
			System.out.println("Disam/Redir<" + redirect +">");
			wiki.GetPageLinks(pages, redirect);
		}
		
		mention.candidates = new ArrayList<Entity>();
		for (String page : pages) {
			mention.candidates.add( new Entity(page) );
		}
	}
}
