package com.cse454.nel.features;

import com.cse454.nel.dataobjects.EntityMention;


public abstract class FeatureGenerator {
	public abstract String GetFeatureName();
	public abstract void GenerateFeatures(EntityMention mention) throws Exception;
}
