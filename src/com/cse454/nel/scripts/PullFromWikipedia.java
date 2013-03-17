package com.cse454.nel.scripts;

import java.io.*;
import java.net.*;


public class PullFromWikipedia {

	public static final String UTF8_BOM = "\uFEFF";
	
	public PullFromWikipedia() {
		
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public String GetWikipediaText(String pageTitle) throws IOException {
		String response = post("http://en.wikipedia.org/w/api.php?action=parse&format=xml&redirects=&prop=wikitext&page="+pageTitle);
		//String response = post(apiUrl + "action=parse", "prop=text&text=" + URLEncoder.encode(markup, "UTF-8"), "parse");
        int y = response.indexOf('>', response.indexOf("<wikitext")) + 1;
        int z = response.indexOf("</wikitext>");
        //System.out.println(decode(response));
        try {
        	return decode(response.substring(y, z));
        }
        catch(Exception e)
        {
        	return null;
        }
	}
	
	protected String decode(String in)
    {
        // Remove entity references. Oddly enough, URLDecoder doesn't nuke these.
        in = in.replace("&lt;", "<").replace("&gt;", ">"); // html tags
        in = in.replace("&amp;", "&");
        in = in.replace("&quot;", "\"");
        in = in.replace("&#039;", "'");
        return in;
    }
	
	protected String post(String url) throws IOException
    {
        URLConnection connection = new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        StringBuilder temp = new StringBuilder(100000);
        while ((line = in.readLine()) != null)
        {
            temp.append(line);
            temp.append("\n");
        }
        in.close();
        return temp.toString();
    }
}
