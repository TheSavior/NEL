package com.cse454.nel.kbp;


public class SFConstants {

	// data types/extensions
	public static final String STANFORDNER = "stanfordner";

	public static final String DEPS_STANFORD_CC_PROCESSED = "depsStanfordCCProcessed.nodup";

	public static final String CJ = "cj";

	public static final String STANFORDPOS = "stanfordpos";

	public static final String ARTICLE_IDS = "articleIDs";

	public static final String TOKEN_SPANS = "tokenSpans";

	public static final String TOKENS = "tokens.fixed";

	public static final String TEXT = "text";

	public static final String META = "meta";

	// file prefix of the data files
	public static final String prefix = "sentences";

	// a default list of data types
	public static final String[] dataTypes = {META, TOKENS, TOKEN_SPANS, STANFORDPOS, CJ, DEPS_STANFORD_CC_PROCESSED, STANFORDNER};

	// slot filling queries
	public static final String queryFile = KbpConstants.rootPath + "sf/tac_2010_kbp_evaluation_slot_filling_queries.xml";

	// slot filling output file
	public static final String outFile = KbpConstants.rootPath + "sf/sf.pred";

	public static final String labelFile = KbpConstants.rootPath + "sf/sf.gold";


}
