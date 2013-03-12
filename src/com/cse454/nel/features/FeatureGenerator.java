package com.cse454.nel.features;

import com.cse454.nel.EntityMention;


public interface FeatureGenerator {
	public String GetFeatureName();
	public void GenerateFeatures(EntityMention mention) throws Exception;
}
