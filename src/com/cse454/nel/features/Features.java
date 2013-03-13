package com.cse454.nel.features;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Features {
	private Map<String, Double> features;
	
	public Features() {
		features = new HashMap<String, Double>();
	}
	
	public double getFeature(String name) {
		return features.get(name);
	}
	
	public void setFeature(String name, double value) {
		features.put(name, value);
	}
	
	public Set<Entry<String, Double>> entrySet() {
		return features.entrySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
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
		Features other = (Features) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		return true;
	}
	
	
}
