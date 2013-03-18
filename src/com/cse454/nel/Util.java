package com.cse454.nel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cse454.nel.features.Histogram;


public class Util {


	public void getArgs(String[] args) {
		Map<String, String> options = new HashMap<String, String>();
		Map<String, String> doubleOptions = new HashMap<String, String>();
		List<String> argsList = new ArrayList<String>();
	    for (int i = 0; i < args.length; i++) {
	        switch (args[i].charAt(0)) {

	        case '-':
	            if (args[i].length() < 2)
	                throw new IllegalArgumentException("Not a valid argument: " + args[i]);
	            if (args[i].charAt(1) == '-') {
	                if (args[i].length() < 3)
	                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	                // --opt
	                String opt = args[i].substring(2, args[i].length());
	                // arg
	                if (args.length - 1 == i)
	                	throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                doubleOptions.put(opt, args[i+1]);
	                i++;
	            } else {
	                if (args.length-1 == i)
	                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                // -opt
	                options.put(args[i], args[i+1]);
	                i++;
	            }
	            break;
	        default:
	            // arg
	            argsList.add(args[i]);
	            break;
	        }
	    }
	}

	public static String cleanString(String str) {
		return str.replaceAll("[^a-zA-Z0-9_\\s]", "");
	}

	public static String toksToStr(String[] toks) {
		String str = "";
		for (String tok : toks) {
			str += tok + " ";
		}
		return str;
	}


	public static String[] tokenizeText(String text) {
		return cleanString(text).split(" ");
	}

	public static double computeDotProduct(Map<String, Double> h1, Map<String, Double> h2) {
		double dotProduct = 0;
		for (Entry<String, Double> entry : h1.entrySet()) {
			String key = entry.getKey();
			if (!h2.containsKey(key)) {
				continue;
			}
			dotProduct += entry.getValue() * h2.get(key);
		}
		return dotProduct;
	}

	public static double computeDotProduct(Histogram hist1, Histogram hist2) {
		Map<String, Double> hist1Norm = hist1.getNormalizedMap();
		Map<String, Double> hist2Norm = hist2.getNormalizedMap();
		return computeDotProduct(hist1Norm, hist2Norm);
	}

	public static void PreventStanfordNERErrors() {
		final PrintStream err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			private StringBuffer buf = new StringBuffer();

			@Override
			public void write(int b) throws IOException {
				if (b == '\n') {
					String line = buf.toString();
					if (!(line.contains("edu.stanford.nlp.process.PTBLexer") || line.contains("WARNING: Untokenizable"))) {
						err.println(buf.toString());
					}
					buf.delete(0, buf.length());
				} else {
					buf.append((char)b);
				}
			}
		}));
	}
}
