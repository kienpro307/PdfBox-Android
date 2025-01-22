package com.tom_roush.fontbox.ttf.model;

import java.util.Set;

public interface GsubData {
    GsubData NO_DATA_FOUND = new GsubData() {
        public boolean isFeatureSupported(String featureName) {
            throw new UnsupportedOperationException();
        }

        public Language getLanguage() {
            throw new UnsupportedOperationException();
        }

        public ScriptFeature getFeature(String featureName) {
            throw new UnsupportedOperationException();
        }

        public String getActiveScriptName() {
            throw new UnsupportedOperationException();
        }

        public Set<String> getSupportedFeatures() {
            throw new UnsupportedOperationException();
        }
    };

    Language getLanguage();

    String getActiveScriptName();

    boolean isFeatureSupported(String var1);

    ScriptFeature getFeature(String var1);

    Set<String> getSupportedFeatures();
}
