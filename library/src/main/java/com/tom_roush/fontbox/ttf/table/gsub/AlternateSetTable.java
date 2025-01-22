package com.tom_roush.fontbox.ttf.table.gsub;

import java.util.Arrays;

public class AlternateSetTable {
    private final int glyphCount;
    private final int[] alternateGlyphIDs;

    public AlternateSetTable(int glyphCount, int[] alternateGlyphIDs) {
        this.glyphCount = glyphCount;
        this.alternateGlyphIDs = alternateGlyphIDs;
    }

    public int getGlyphCount() {
        return this.glyphCount;
    }

    public int[] getAlternateGlyphIDs() {
        return this.alternateGlyphIDs;
    }

    public String toString() {
        return "AlternateSetTable{glyphCount=" + this.glyphCount + ", alternateGlyphIDs=" + Arrays.toString(this.alternateGlyphIDs) + '}';
    }
}