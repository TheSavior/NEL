package com.cse454.nel.features;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FeatureWeights {
	private Map<String, Double> weights;
	
	public FeatureWeights() {
		weights = new HashMap<String, Double>();
	}
	
	public boolean hasFeature(String name) {
		return weights.containsKey(name);
	}
	
	public double getWeight(String name) {
		return weights.get(name);
	}
	
	public void setFeature(String name, double value) {
		weights.put(name, value);
	}
	
	public Set<Entry<String, Double>> entrySet() {
		return weights.entrySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((weights == null) ? 0 : weights.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeatureWeights other = (FeatureWeights) obj;
		if (weights == null) {
			if (other.weights != null)
				return false;
		} else if (!weights.equals(other.weights))
			return false;
		return true;
	}
	
	
}
