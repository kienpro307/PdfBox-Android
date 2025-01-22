package com.tom_roush.fontbox.ttf.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapBackedGsubData implements GsubData {
    private final Language language;
    private final String activeScriptName;
    private final Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap;

    public MapBackedGsubData(Language language, String activeScriptName, Map<String, Map<List<Integer>, Integer>> glyphSubstitutionMap) {
        this.language = language;
        this.activeScriptName = activeScriptName;
        this.glyphSubstitutionMap = glyphSubstitutionMap;
    }

    public Language getLanguage() {
        return this.language;
    }

    public String getActiveScriptName() {
        return this.activeScriptName;
    }

    public boolean isFeatureSupported(String featureName) {
        return this.glyphSubstitutionMap.containsKey(featureName);
    }

    public ScriptFeature getFeature(String featureName) {
        if (!this.isFeatureSupported(featureName)) {
            throw new UnsupportedOperationException("The feature " + featureName + " is not supported!");
        } else {
            return new MapBackedScriptFeature(featureName, this.glyphSubstitutionMap.get(featureName));
        }
    }

    public Set<String> getSupportedFeatures() {
        return this.glyphSubstitutionMap.keySet();
    }
}
