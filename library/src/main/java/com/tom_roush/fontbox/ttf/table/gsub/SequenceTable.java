package com.tom_roush.fontbox.ttf.table.gsub;

import java.util.Arrays;

public class SequenceTable {
    private final int glyphCount;
    private final int[] substituteGlyphIDs;

    public SequenceTable(int glyphCount, int[] substituteGlyphIDs) {
        this.glyphCount = glyphCount;
        this.substituteGlyphIDs = substituteGlyphIDs;
    }

    public int getGlyphCount() {
        return this.glyphCount;
    }

    public int[] getSubstituteGlyphIDs() {
        return this.substituteGlyphIDs;
    }

    public String toString() {
        return "SequenceTable{glyphCount=" + this.glyphCount + ", substituteGlyphIDs=" + Arrays.toString(this.substituteGlyphIDs) + '}';
    }
}
