package com.xavier.basicPathfinderServer.json.mappers;

import java.util.LinkedList;
import java.util.List;

import com.xavier.basicPathfinderServer.characterOwned.ClassFeature;
import com.xavier.basicPathfinderServer.json.ClassTrackedFeatureJson;

public class ClassTrackedFeatureMapper {
	public static List<ClassTrackedFeatureJson> map(List<ClassFeature> features) {
		List<ClassTrackedFeatureJson> featureJsons = new LinkedList<>();
		
		for (ClassFeature feature : features) {
			if (feature.trackedResource != null) {
				ClassTrackedFeatureJson json = new ClassTrackedFeatureJson(feature.getTrackedResourceId(), feature.name, feature.description, feature.trackedResource.getRemaining());
				featureJsons.add(json);
			}
		}
		
		return featureJsons;
	}
}
