package com.tom_roush.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class GlyphArraySplitterRegexImpl implements GlyphArraySplitter {
    private static final String GLYPH_ID_SEPARATOR = "_";
    private final CompoundCharacterTokenizer compoundCharacterTokenizer;

    public GlyphArraySplitterRegexImpl(Set<List<Integer>> matchers) {
        this.compoundCharacterTokenizer = new CompoundCharacterTokenizer(this.getMatchersAsStrings(matchers));
    }

    public List<List<Integer>> split(List<Integer> glyphIds) {
        String originalGlyphsAsText = this.convertGlyphIdsToString(glyphIds);
        List<String> tokens = this.compoundCharacterTokenizer.tokenize(originalGlyphsAsText);
        List<List<Integer>> modifiedGlyphs = new ArrayList<List<Integer>>(tokens.size());

        for (String token: tokens) {
            modifiedGlyphs.add((this.convertGlyphIdsToList(token)));
        }

        return modifiedGlyphs;
    }

    private Set<String> getMatchersAsStrings(Set<List<Integer>> matchers) {
        Set<String> stringMatchers = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.length() == s2.length()) {
                    return s2.compareTo(s1);
                }
                return s2.length() - s1.length();
            }
        });

        for (List<Integer> glyphIds : matchers) {
            stringMatchers.add(this.convertGlyphIdsToString(glyphIds));
        }

        return stringMatchers;
    }

    private String convertGlyphIdsToString(List<Integer> glyphIds) {
        StringBuilder sb = new StringBuilder(20);
        sb.append("_");
        for (Integer glyphId : glyphIds) {
            sb.append(glyphId).append("_");
        }
        return sb.toString();
    }

    private List<Integer> convertGlyphIdsToList(String glyphIdsAsString) {
        List<Integer> gsubProcessedGlyphsIds = new ArrayList<Integer>();

        for(String glyphId : glyphIdsAsString.split("_")) {
            glyphId = glyphId.trim();
            if (!glyphId.isEmpty()) {
                gsubProcessedGlyphsIds.add(Integer.valueOf(glyphId));
            }
        }

        return gsubProcessedGlyphsIds;
    }
}
