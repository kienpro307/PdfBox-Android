package com.tom_roush.fontbox.ttf.model;

import java.util.List;
import java.util.Set;

public interface ScriptFeature {
    String getName();

    Set<List<Integer>> getAllGlyphIdsForSubstitution();

    boolean canReplaceGlyphs(List<Integer> var1);

    Integer getReplacementForGlyphs(List<Integer> var1);
}
