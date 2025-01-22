package com.tom_roush.fontbox.ttf.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MapBackedScriptFeature implements ScriptFeature {
    private final String name;
    private final Map<List<Integer>, Integer> featureMap;

    public MapBackedScriptFeature(String name, Map<List<Integer>, Integer> featureMap) {
        this.name = name;
        this.featureMap = featureMap;
    }

    public String getName() {
        return this.name;
    }

    public Set<List<Integer>> getAllGlyphIdsForSubstitution() {
        return this.featureMap.keySet();
    }

    public boolean canReplaceGlyphs(List<Integer> glyphIds) {
        return this.featureMap.containsKey(glyphIds);
    }

    public Integer getReplacementForGlyphs(List<Integer> glyphIds) {
        if (!this.canReplaceGlyphs(glyphIds)) {
            throw new UnsupportedOperationException("The glyphs " + glyphIds + " cannot be replaced");
        } else {
            return this.featureMap.get(glyphIds);
        }
    }

    public int hashCode() {
        return Objects.hash(this.featureMap, this.name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            MapBackedScriptFeature other = (MapBackedScriptFeature)obj;
            return Objects.equals(other.name, this.name) && Objects.equals(other.featureMap, this.featureMap);
        } else {
            return false;
        }
    }
}
