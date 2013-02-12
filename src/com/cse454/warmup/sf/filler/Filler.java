package com.cse454.warmup.sf.filler;

import java.util.Map;

import com.cse454.warmup.sf.SFEntity;


/**
 *
 * @author Xiao Ling
 */

public abstract class Filler {
	public String slotName = null;
	public abstract void predict(SFEntity mention, Map<String, String> annotations);
}
